/*
 * (C) Copyright 2019 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * 
 *     Tiry
 *
 */

package org.nuxeo.ecm.restapi.server.jaxrs.cms.adapter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;

@WebObject(type = "dynamicRendition")
public class DynamicRenditionObject extends DefaultObject {

	@Context
	protected HttpServletRequest servletRequest;

	protected String renditionName;

	protected DocumentModel doc;

	public static final String ECHO = "echo";

	@Override
	protected void initialize(Object... args) {
		assert args != null && args.length == 2;
		doc = (DocumentModel) args[0];
		renditionName = (String) args[1];
	}

	@GET
	public Object doGet(@Context Request request, @Context HttpHeaders headers, @Context UriInfo info) {

		if (ECHO.equalsIgnoreCase(renditionName)) {
			return echo(headers, info);
		}

		// XXX

		return null;
	}

	protected String echo(HttpHeaders headers, UriInfo info) {

		StringBuilder sb = new StringBuilder();

		String UA = headers.getRequestHeader("User-Agent").get(0);
		sb.append("User-Agent: " + UA + "\n");

		for (String key : info.getQueryParameters().keySet()) {
			sb.append(key);
			sb.append(" : ");
			sb.append(info.getQueryParameters().getFirst(key));
			sb.append("\n");
		}
		return sb.toString();
	}
}
