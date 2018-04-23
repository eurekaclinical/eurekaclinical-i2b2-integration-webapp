package org.eurekaclinical.i2b2integration.webapp.servlets;

/*-
 * #%L
 * Eureka! Clinical I2b2 Integration Webapp
 * %%
 * Copyright (C) 2016 - 2018 Emory University
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eurekaclinical.common.comm.clients.ClientException;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.net.URISyntaxException;

@Singleton
public class CustomProxyServlet extends HttpServlet {

    private static final long serialVersionUID = -6000798538905380354L;
    private static final Set<String> requestHeadersToExclude;

    static {
        requestHeadersToExclude = new HashSet<>();
        for (String header : new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade", HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.COOKIE
        }) {
            requestHeadersToExclude.add(header.toUpperCase());
        }
    }
    
    private static final Set<String> responseHeadersToExclude;

    static {
        responseHeadersToExclude = new HashSet<>();
        for (String header : new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade", HttpHeaders.SET_COOKIE
        }) {
            responseHeadersToExclude.add(header.toUpperCase());
        }
    }

    public CustomProxyServlet() {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {

            String xml = extractXml(request);
            String pmUrl = extractProxyAddress(xml);
            if (pmUrl != null) {
                URL url = new URL(pmUrl);
                I2b2EurekaClinicalClient client = new I2b2EurekaClinicalClient(url.toURI());
                MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(request);
                try {
                    ClientResponse clientResponse = client.proxyPost(xml, null, requestHeaders);
                    response.setStatus(clientResponse.getStatus());
                    copyResponseHeaders(clientResponse.getHeaders(), baseUrl(request.getContextPath(), request).toString(), response);
                    copyStream(clientResponse.getEntityInputStream(), response.getOutputStream());
                } catch (ClientException e) {
                    response.setStatus(e.getResponseStatus().getStatusCode());
                    response.getOutputStream().print(e.getMessage());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getOutputStream().println("No proxy address specified");
            }
        } catch (IOException | URISyntaxException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Unfortunately, the i2b2 webclient sets the content type to
     * application/x-www-form-urlencoded, even though the POST body is actually
     * application/xml. In this situation, tomcat eagerly parses the POST body
     * into parameters, and we're stuck reconstructing the XML.
     */
    private static String extractXml(HttpServletRequest request) {
        StringBuilder xmlBuf = new StringBuilder();
        for (Map.Entry<String, String[]> me : request.getParameterMap().entrySet()) {
            xmlBuf.append(me.getKey());
            xmlBuf.append('=');
            for (String val : me.getValue()) {
                xmlBuf.append(val);
            }
        }
        return xmlBuf.toString();
    }

    private static MultivaluedMap<String, String> extractRequestHeaders(HttpServletRequest servletRequest) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        for (Enumeration<String> enm = servletRequest.getHeaderNames(); enm.hasMoreElements();) {
            String headerName = enm.nextElement();
            for (Enumeration<String> enm2 = servletRequest.getHeaders(headerName); enm2.hasMoreElements();) {
                String nextValue = enm2.nextElement();
                if (!requestHeadersToExclude.contains(headerName.toUpperCase())) {
                    headers.add(headerName, nextValue);
                }
            }
        }
        addXForwardedForHeader(servletRequest, headers);
        return headers;
    }

    private static void addXForwardedForHeader(HttpServletRequest servletRequest,
            MultivaluedMap<String, String> headers) {
        String forHeaderName = "X-Forwarded-For";
        String forHeader = servletRequest.getRemoteAddr();
        String existingForHeader = servletRequest.getHeader(forHeaderName);
        if (existingForHeader != null) {
            forHeader = existingForHeader + ", " + forHeader;
        }
        headers.add(forHeaderName, forHeader);

        String protoHeaderName = "X-Forwarded-Proto";
        String protoHeader = servletRequest.getScheme();
        headers.add(protoHeaderName, protoHeader);

    }

    private static URI baseUrl(String contextPath, HttpServletRequest request) {
        return URI.create(request.getRequestURL().toString()).resolve(contextPath);
    }

    private static void copyResponseHeaders(
            MultivaluedMap<String, String> headers,
            String proxyResourceUrl,
            HttpServletResponse response) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                    if (!responseHeadersToExclude.contains(key.toUpperCase())) {
//                        if ("Location".equals(key.toUpperCase())) {
//                            response.addHeader(key, replacementPathAndClient.revertPath(proxyResourceUrl));
//                        }
                        response.addHeader(key, val);
                    }
                }
            }
        }
    }

    private static int copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024 * 4];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    private static String extractProxyAddress(String xml) {
        String proxyURL = null;
        if (xml != null) {
            int index = xml.indexOf("<redirect_url>");
            if (index > -1) {
                proxyURL = xml.substring(index + 14, xml.indexOf("</redirect_url>"));
            }
        }
        System.out.println("proxyURL:" + proxyURL);

        return proxyURL;
    }
}
