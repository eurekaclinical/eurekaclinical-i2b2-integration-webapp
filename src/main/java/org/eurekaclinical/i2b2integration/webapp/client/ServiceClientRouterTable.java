package org.eurekaclinical.i2b2integration.webapp.client;

/*-
 * #%L
 * Eureka! Clinical I2b2 Integration Webapp
 * %%
 * Copyright (C) 2016 Emory University
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
import com.google.inject.Inject;
import org.eurekaclinical.common.comm.clients.Route;
import org.eurekaclinical.common.comm.clients.RouterTable;
import org.eurekaclinical.i2b2integration.client.EurekaClinicalI2b2IntegrationClient;
import org.eurekaclinical.i2b2integration.webapp.props.WebappProperties;
import org.eurekaclinical.useragreement.client.EurekaClinicalUserAgreementClient;

/**
 *
 * @author Andrew Post
 */
public class ServiceClientRouterTable implements RouterTable {

    private final EurekaClinicalI2b2IntegrationClient i2b2IntegrationClient;
    
    @Inject(optional=true)	
    private EurekaClinicalUserAgreementClient userAgreementClient;

    @Inject
    public ServiceClientRouterTable(EurekaClinicalI2b2IntegrationClient inI2b2IntegrationClient, WebappProperties inProperties) {
        this.i2b2IntegrationClient = inI2b2IntegrationClient;
    }

    @Override
    public Route[] load() {
        if (this.userAgreementClient != null) {
            return new Route[]{
                new Route("/useragreementstatuses", "/api/protected/useragreementstatuses", this.userAgreementClient),
                new Route("/", "/api/protected/", this.i2b2IntegrationClient)
            };
        } else {
            return new Route[]{
                new Route("/", "/api/protected/", this.i2b2IntegrationClient)
            };
        }
    }

}
