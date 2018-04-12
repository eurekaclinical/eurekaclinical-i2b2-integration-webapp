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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.BasicResponseHandler;
import org.eurekaclinical.common.comm.clients.ClientException;
import org.eurekaclinical.common.comm.clients.ProxyResponse;
import org.eurekaclinical.common.comm.clients.ProxyingClient;
import org.eurekaclinical.common.comm.clients.ReplacementPathAndClient;
import org.eurekaclinical.common.servlet.ProxyServlet;
import org.eurekaclinical.i2b2integration.webapp.utils.RequestWrapper;
import org.eurekaclinical.standardapis.props.CasEurekaClinicalProperties;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@Singleton
public class CustomProxyServlet extends HttpServlet  
{

	private static final long serialVersionUID = -6000798538905380354L;
	private final Injector injector;
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
    @Inject
    public CustomProxyServlet(Injector injector) 
    {
    	System.out.println("+++++++++++I AM INITIALIZED++++++++++");
        this.injector = injector;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
    		doPost(request, response);
	}
	
    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException
	{
    	try
    	{
    		RequestWrapper wrappedRequest = new RequestWrapper(request);
            request = wrappedRequest;
    		
    		
    		
    	System.out.println("completed the pre process");
        String xml = getXmlData(wrappedRequest);
    	if( xml != null)
    	{
    		
    		String pmUrl = getProxyAddress(xml);
    		URL url = new URL( pmUrl);
    		
    		//new
    		//I2b2EurekaClinicalClient client = this.injector.getInstance(I2b2EurekaClinicalClient.class);
    		I2b2EurekaClinicalClient client = new I2b2EurekaClinicalClient(url.toURI());
    		String path = request.getPathInfo();
            MultivaluedMap<String, String> requestHeaders = extractRequestHeaders(request);
            MultivaluedMap<String, String> parameterMap = toMultivaluedMap(request.getParameterMap());
            try {
                ClientResponse clientResponse = client.proxyPost(request.getInputStream(), parameterMap, requestHeaders);
                System.out.println("proxyResponse:"+clientResponse.toString());
                int clientResponseStatus = clientResponse.getStatus();
                System.out.println("clientResponseStatus"+clientResponseStatus);
                //response.setStatus(clientResponse.getStatus());
                //if(clientResponseStatus == 200)
                {
                	copyResponseHeaders(clientResponse.getHeaders(), request.getServletPath(), baseUrl(request.getContextPath(), request).toString(), response);
                    copyStream(clientResponse.getEntityInputStream(), response.getOutputStream());
                }
               /* else
                {
                	System.out.println("need to check the responds code");
                }
               */ 
            } catch (ClientException e) {
                response.setStatus(e.getResponseStatus().getStatusCode());
                response.getOutputStream().print(e.getMessage());
            }
        
    		//new
    		
    		/*HttpURLConnection  con = (HttpURLConnection)url.openConnection();
    	    // specify that we will send output and accept input
    	    con.setDoInput(true);
    	    con.setDoOutput(true);
    	    con.setConnectTimeout( 20000 );  // long timeout, but not infinite
    	    con.setReadTimeout( 20000 );
    	    con.setUseCaches (false);
    	    con.setDefaultUseCaches (false);
    	    
    	    // tell the web server what we are sending
    	    con.setRequestProperty ( "Content-Type", "text/xml" );
    	    con.setRequestProperty("Request Method","POST");
    	    Enumeration<String> headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) {
                String headerName = (String)headerNames.nextElement();
                System.out.println("header name:"+headerName);
                String headerValue = request.getHeader(headerName);
                System.out.println("headerValue:"+headerValue);
                con.setRequestProperty(headerName,headerValue);
            }
            con.setRequestMethod("POST");
            con.setRequestProperty ( "Content-Type", "text/xml" );
            OutputStreamWriter writer = new OutputStreamWriter( con.getOutputStream() );
    	    writer.write( xml );
    	    writer.flush();
    	    writer.close();
    	    
    	    
    	 // reading the response
    	    InputStreamReader reader = new InputStreamReader( con.getInputStream() );
    	    System.out.println("made the new request");
    	    StringBuilder buf = new StringBuilder();
    	    char[] cbuf = new char[ 2048 ];
    	    int num;
    	    while ( -1 != (num=reader.read( cbuf )))
    	    {
    	        buf.append( cbuf, 0, num );
    	    }
    	    response.getWriter().write(buf.toString());
    	    System.out.println("response has been processes");*/
    	}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	System.out.println("request hasbeen forwarded");
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

    private static MultivaluedMap<String, String> toMultivaluedMap(Map<String, String[]> inQueryParameters) {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        for (Map.Entry<String, String[]> parameter : inQueryParameters.entrySet()) {
            String[] values = parameter.getValue();
            for (String value : values) {
                queryParams.add(parameter.getKey(), value);
            }
        }
        return queryParams;
    }
    
    private static URI baseUrl(String contextPath, HttpServletRequest request) {
        return URI.create(request.getRequestURL().toString()).resolve(contextPath);
    }
    private static void copyResponseHeaders(
            MultivaluedMap<String, String> headers, 
            String servletPath, 
            String proxyResourceUrl,
            HttpServletResponse response) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                for (String val : entry.getValue()) {
                      /*  if ("Location".equals(key.toUpperCase())) {
                        	System.out.println("inside location condition");
                        	System.out.println("key:"+key+" value:"+replacementPathAndClient.revertPath(proxyResourceUrl));
                            response.addHeader(key, replacementPathAndClient.revertPath(proxyResourceUrl));
                        }*/
                        System.out.println("key1:"+key+" value:"+val);
                        response.addHeader(key, val);
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

	private String getXmlData(RequestWrapper request) throws IOException
	{
		List<String> parameterNamesList =   Collections.list(request.getParameterNames());
        System.out.println("++SIZE++"+parameterNamesList.size());
        for (String string : parameterNamesList) 
        {
       	 System.out.println("++++++++names+++"+string);
        }
        String xml = null;
        for (String string : parameterNamesList) 
        {
        	System.out.println("each param:"+string);
        	String local= null;
        	local = request.getParameter(string);
        	System.out.println("++++local string+++"+local);
        	xml = string+"="+local;
            System.out.println(string + " ====== " + xml);
		}
        
		return xml; 
	}
	private String getProxyAddress(String xml)
	{
		String proxyURL = null;
		proxyURL = xml.substring(xml.indexOf("<redirect_url>")+14, xml.indexOf("</redirect_url>"));
		System.out.println("proxyURL:"+proxyURL);
		
		return proxyURL;
	}
}
