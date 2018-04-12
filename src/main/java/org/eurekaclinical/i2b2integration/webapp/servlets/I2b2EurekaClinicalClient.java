package org.eurekaclinical.i2b2integration.webapp.servlets;

import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;

import org.eurekaclinical.common.comm.clients.ClientException;
import org.eurekaclinical.common.comm.clients.EurekaClinicalClient;
import org.eurekaclinical.common.comm.clients.ProxyResponse;
import org.eurekaclinical.common.comm.clients.ReplacementPathAndClient;

import com.sun.jersey.api.client.ClientResponse;

class I2b2EurekaClinicalClient  extends EurekaClinicalClient
{

	private URI uri;
	protected I2b2EurekaClinicalClient(URI uri) 
	{
		super(null);
		assert uri != null: "uri cannot be null";
		this.uri =uri ;
	}

	@Override
	protected URI getResourceUrl() 
	{
		return uri;
	}
	ClientResponse proxyPost(InputStream inputStream,MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers)
            throws ClientException 
	{
		System.out.println("uri++++:"+uri);
        return doPostForProxy("", inputStream, parameterMap, headers);
    }
}
