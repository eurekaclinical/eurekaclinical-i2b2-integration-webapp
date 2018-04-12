package org.eurekaclinical.i2b2integration.webapp.utils;

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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

public class RequestService 
{
	public static String getBody(HttpServletRequest request) throws Exception
	{
/*		String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try 
	    {
	        InputStream inputStream = request.getInputStream();
	        System.out.println("inputStream:"+inputStream);
	        if (inputStream != null) {
	        	System.out.println("Reading from input stream");
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;

	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	            
	        } else {
	        	System.out.println("InputStream is null");
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    System.out.println("Bodu after reading:"+body);
	    return body;
	
		 final StringBuilder builder = new StringBuilder();
		 System.out.println("request.getReader()"+request.getReader().toString());
		    try (BufferedReader reader = request.getReader()) {
		        if (reader == null) {
		            System.out.println("Request body could not be read because it's empty.");
		            return null;
		        }
		        System.out.println("reader is not null");
		        String line;
		        while ((line = reader.readLine()) != null) {
		        	System.out.println("EachLine"+line);
		            builder.append(line);
		        }
		        System.out.println("after reading"+builder);
		        return builder.toString();
		    } catch (final Exception e) {
		    	System.out.println("Could not obtain the saml request body from the http request"+e);
		        return null;
		    }	
		    */
		    System.out.println("receive " + request.getMethod() +" notification for "+ request.getRequestURI());
		    System.out.println("+query param+"+request.getQueryString());
             Enumeration<String> headerNames = request.getHeaderNames();
            while(headerNames.hasMoreElements()) {
                String headerName = (String)headerNames.nextElement();
                System.out.println(headerName + " = " + request.getHeader(headerName));
            }

            System.out.println("\n\nParameters");
            String data = "";
           // Enumeration<String> params = request.getParameterNames();
             Map<String, String[]> map = request.getParameterMap();
             
             System.out.println("++map++"+map.size());
             Set<String> set = map.keySet();
             for (String string : set) 
             {
				System.out.println("set val"+string);
             }
             Collection<String[]> valSet = map.values();
             
             for (String[] strings : valSet) 
             {
            	 System.out.println("++Strings++"+strings);
            	 System.out.println("++Strings++"+strings.length);
            	 for (String string : strings) {
					System.out.println("++EACH STRING++"+string);
				}
            	 System.out.println("end each strings");
             }
            
            List<String> parameterNamesList =   Collections.list(request.getParameterNames());
            System.out.println("++SIZE++"+parameterNamesList.size());
            for (String string : parameterNamesList) 
            {
           	 System.out.println("++++++++names+++"+string);
            }
            
            for (String string : parameterNamesList) 
            {
            	System.out.println("each param:"+string);
            	String local= null;
            	local = request.getParameter(string);
            	System.out.println("++++local string+++"+local);
                data = string+": "+local;
                System.out.println(string + " ====== " + data);
			}

            System.out.println("\n\n Row data");
            String body = extractPostRequestBody(request);
            System.out.println("+body+:"+body);
			return data;
        }

        @SuppressWarnings("resource")
		static String extractPostRequestBody(HttpServletRequest request) {
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                Scanner s = null;
                try {
                    s = new Scanner(request.getInputStream(), "UTF-8").useDelimiter("\\A");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return s.hasNext() ? s.next() : "";
            }
            return "";
	}
	
	
}

