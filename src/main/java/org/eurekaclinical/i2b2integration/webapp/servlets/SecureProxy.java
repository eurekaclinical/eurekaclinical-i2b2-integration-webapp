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
import java.io.StringWriter;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SecureProxy extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String i2b2UserName;
	private String i2b2Password;
	private String i2b2Server;

	private DocumentBuilderFactory builderFactory;

	@Inject
	private ServletContext context;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		doPost(request, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			String user = (String) request.getSession().getAttribute("user");
			
			HttpClient client = HttpClients.createDefault();
			HttpPost post = new HttpPost(this.i2b2Server);
			post.setHeader("Content-Type", "text/xml");
			StringWriter writer = parseXML(request.getInputStream(), user);
			
			post.setEntity(new StringEntity(writer.toString()));
			writer.flush();
			writer.close();
			
			HttpResponse response = client.execute(post);
			
			resp.setContentType("text/xml");
	        resp.setStatus(HttpServletResponse.SC_OK);
	        
	        IOUtils.copy(response.getEntity().getContent(), resp.getOutputStream());
	        
	        response.getEntity().getContent().close();
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private StringWriter parseXML(ServletInputStream servletInputStream, String user)
			throws SAXException, IOException, ParserConfigurationException,
			XPathExpressionException, TransformerFactoryConfigurationError, TransformerException {
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(servletInputStream);
		XPath xPath = XPathFactory.newInstance().newXPath();

		// User
		if (this.i2b2UserName != null) {
			String userNameExpression = "//message_header/security/username";
			Node userNode = (Node) xPath.evaluate(userNameExpression, document,
					XPathConstants.NODE);
			userNode.setTextContent(this.i2b2UserName);
		} else if (user != null) {
			String userNameExpression = "//message_header/security/username";
			Node userNode = (Node) xPath.evaluate(userNameExpression, document,
					XPathConstants.NODE);
			userNode.setTextContent(user);
		}

		// Password
		if (this.i2b2Password != null) {
			String passwordExpression = "//message_header/security/password";
			Node passwordNode = (Node) xPath.evaluate(passwordExpression,
					document, XPathConstants.NODE);

			passwordNode.setTextContent(this.i2b2Password);

			if (passwordNode.hasAttributes()) {
				NamedNodeMap attributes = passwordNode.getAttributes();
				attributes.removeNamedItem("token_ms_timeout");
				attributes.removeNamedItem("is_token");
			}
		}

		// Write changes to a file
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();


		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(document), new javax.xml.transform.stream.StreamResult(writer));
		
		return writer;
	}

	@Override
	public void init(ServletConfig config) {
		this.i2b2UserName = context.getInitParameter("i2b2UserName");
		this.i2b2Password = context.getInitParameter("i2b2Password");
		this.i2b2Server = context.getInitParameter("i2b2Server");

		builderFactory = DocumentBuilderFactory.newInstance();
	}
}