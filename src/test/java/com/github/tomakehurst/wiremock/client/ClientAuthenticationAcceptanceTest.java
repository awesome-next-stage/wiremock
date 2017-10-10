/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.security.Authenticator;
import com.github.tomakehurst.wiremock.security.ClientAuthenticator;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.After;
import org.junit.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.testsupport.TestHttpHeader.withHeader;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


public class ClientAuthenticationAcceptanceTest {
	
	private WireMockServer server;

    @After
	public void stopServer() {
		server.stop();
	}

	@Test
	public void supportsCustomAuthenticator() {
	    server = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .adminAuthenticator(
                new Authenticator() {
                    @Override
                    public boolean authenticate(Request request) {
                        return request.containsHeader("X-Magic-Header");
                    }
                }
        ));
	    server.start();
        WireMockTestClient noAuthClient = new WireMockTestClient(server.port());
		WireMock goodClient = WireMock.create().port(server.port()).authenticator(new ClientAuthenticator() {
            @Override
            public List<HttpHeader> generateAuthHeaders() {
                return singletonList(httpHeader("X-Magic-Header", "blah"));
            }
        }).build();

		assertThat(noAuthClient.get("/__admin/mappings").statusCode(), is(401));
		assertThat(noAuthClient.get("/__admin/mappings", withHeader("X-Magic-Header", "anything")).statusCode(), is(200));

        goodClient.getServeEvents(); // Throws an exception on a non 2xx response
	}
	


}
