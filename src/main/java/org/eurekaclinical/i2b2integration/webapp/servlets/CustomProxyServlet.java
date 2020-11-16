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
import java.io.BufferedReader;
import java.io.Reader;
import javax.servlet.ServletOutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public class CustomProxyServlet extends HttpServlet {

    private static final long serialVersionUID = 0L;
    private static final Logger LOGGER = Logger.getLogger(CustomProxyServlet.class.getName());
    private static final Set<String> REQUEST_HEADERS_TO_EXCLUDE;

    private static final String REDIRECT_OPEN_TAG = "<redirect_url>";
    private static final String REDIRECT_CLOSE_TAG = "</redirect_url>";
    private static final int REDIRECT_OPEN_TAG_LEN = REDIRECT_OPEN_TAG.length();

    static {
        REQUEST_HEADERS_TO_EXCLUDE = new HashSet<>();
        for (String header : new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade", HttpHeaders.CONTENT_LENGTH,
            HttpHeaders.COOKIE
        }) {
            REQUEST_HEADERS_TO_EXCLUDE.add(header.toUpperCase());
        }
    }

    private static final Set<String> RESPONSE_HEADERS_TO_EXCLUDE;

    static {
        RESPONSE_HEADERS_TO_EXCLUDE = new HashSet<>();
        for (String header : new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade", HttpHeaders.SET_COOKIE
        }) {
            RESPONSE_HEADERS_TO_EXCLUDE.add(header.toUpperCase());
        }
    }

    public CustomProxyServlet() {
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            String xml = extractBody(request.getReader());
            LOGGER.log(Level.INFO,"Request message: {}", xml);
            String pmUrl = extractProxyAddress(xml);
            ServletOutputStream out = response.getOutputStream();
            if (pmUrl != null) {
                I2b2EurekaClinicalClient client = new I2b2EurekaClinicalClient(URI.create(pmUrl));
                MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(request);
                LOGGER.log(Level.INFO,"Request headers: {}", requestHeaders);
                try {
                    ClientResponse clientResponse = client.proxyPost(xml, null, requestHeaders);
                    int responseStatus = clientResponse.getStatus();
                    LOGGER.log(Level.INFO,"Proxy response status: {}", responseStatus);
                    response.setStatus(responseStatus);
                    MultivaluedMap<String, String> responseHeaders = clientResponse.getHeaders();
                    LOGGER.log(Level.INFO,"Proxy response headers: {}", responseHeaders);
                    copyResponseHeaders(responseHeaders, 
                            baseUrl(request.getContextPath(), request).toString(), 
                            response);
                    copyStream(clientResponse.getEntityInputStream(), out);
                } catch (ClientException e) {
                    int responseStatus = e.getResponseStatus().getStatusCode();
                    LOGGER.log(Level.INFO,"Proxy error, response status: {}", responseStatus);
                    response.setStatus(responseStatus);
                    String responseMessage = e.getLocalizedMessage();
                    LOGGER.log(Level.INFO,"Proxy error, response message: {}", responseMessage);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.log(Level.INFO,"No proxy address specified");
            }
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Grab the POST body as a string.
     */
    private static String extractBody(Reader reader) throws IOException {
        StringBuilder buf = new StringBuilder();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                buf.append(line);
            }
        }
        return buf.toString();
    }

    private static MultivaluedMap<String, String> extractRequestHeaders(HttpServletRequest request) {
        MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
        for (Enumeration<String> enm = request.getHeaderNames(); enm.hasMoreElements();) {
            String headerName = enm.nextElement();
            for (Enumeration<String> enm2 = request.getHeaders(headerName); enm2.hasMoreElements();) {
                String nextValue = enm2.nextElement();
                if (!REQUEST_HEADERS_TO_EXCLUDE.contains(headerName.toUpperCase())) {
                    headers.add(headerName, nextValue);
                }
            }
        }
        addXForwardedForHeader(request, headers);
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
                    if (!RESPONSE_HEADERS_TO_EXCLUDE.contains(key.toUpperCase())) {
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
            int index = xml.indexOf(REDIRECT_OPEN_TAG);
            if (index > -1) {
                proxyURL = xml.substring(index + REDIRECT_OPEN_TAG_LEN, xml.indexOf(REDIRECT_CLOSE_TAG));
            }
        }

        return proxyURL;
    }
}
