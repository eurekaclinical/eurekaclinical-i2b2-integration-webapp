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


import java.io.InputStream;
import java.net.URI;

import javax.ws.rs.core.MultivaluedMap;

import org.eurekaclinical.common.comm.clients.ClientException;
import org.eurekaclinical.common.comm.clients.EurekaClinicalClient;

import com.sun.jersey.api.client.ClientResponse;

class I2b2EurekaClinicalClient extends EurekaClinicalClient {

    private URI uri;

    protected I2b2EurekaClinicalClient(URI uri) {
        super(null);
        assert uri != null : "uri cannot be null";
        this.uri = uri;
    }

    @Override
    protected URI getResourceUrl() {
        return uri;
    }

    ClientResponse proxyPost(String body, MultivaluedMap<String, String> parameterMap, MultivaluedMap<String, String> headers)
            throws ClientException {
        System.out.println("uri++++:" + uri);
        return doPostForProxy("", body, parameterMap, headers);
    }
}
