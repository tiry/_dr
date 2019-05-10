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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.codec.digest.DigestUtils;
import org.nuxeo.ecm.cms.rendition.DynamicRendition;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionDocument;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionHolder;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.query.api.PageProviderService;
import org.nuxeo.ecm.platform.rendition.Rendition;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

@WebObject(type = "dynamicRendition")
public class DynamicRenditionObject extends DefaultObject {

	@Context
	protected CoreSession session;

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
	public Object doGet(@Context Request request, @Context HttpHeaders headers, @Context UriInfo info,
			 @DefaultValue("true") @QueryParam("create") boolean allowCreate,
			 @DefaultValue("true") @QueryParam("cache") boolean save) {

		if (ECHO.equalsIgnoreCase(renditionName)) {
			return echo(headers, info);
		}

		doc.putContextData(DynamicRenditionDocument.CXT_SAVE_FLAG,save);
		
		DynamicRenditionHolder rh = doc.getAdapter(DynamicRenditionHolder.class);
		if (rh==null) {

			if (!allowCreate) {
				return Response.status(Status.BAD_REQUEST).build();
			} 
			
			DynamicRenditionDocument.create(doc);
			if (save) {
				doc = session.saveDocument(doc);
			}
			rh = doc.getAdapter(DynamicRenditionHolder.class);
		}
				
		String dynRendionName = computeDynamicRendionName(info);
		
		DynamicRendition dr = rh.getRendition(dynRendionName);
		if (dr==null) {
			// rendition is not already registered
			if (!allowCreate) {
				return Response.status(Status.NOT_ACCEPTABLE).build();	
			}
			
			MultivaluedMap<String, String> qp = info.getQueryParameters();
			String converterName = qp.getFirst("converter");
			if (converterName==null) {
				converterName = renditionName;	
			}
			Map<String, String> params = new HashMap<>();
			for (String k: qp.keySet()) {
				params.put(k, qp.getFirst(k));
			}
			dr = new DynamicRendition(dynRendionName, converterName, params);
			rh.add(dr, save, false);
			if (save) {
				doc = session.getDocument(doc.getRef());
			}
		}
		
		RenditionService rs = Framework.getService(RenditionService.class);		
		Rendition rendition = rs.getRendition(doc, dynRendionName);
		
		return rendition.getBlob();
	}

	protected String computeDynamicRendionName(UriInfo info) {
		
		StringBuilder sb = new StringBuilder(renditionName);
		sb.append(" - ");
		MultivaluedMap<String, String> qp = info.getQueryParameters();
		for (String key : qp.keySet()) {
			sb.append(key);
			sb.append(":");
			sb.append(qp.getFirst(key));
			sb.append(" - ");			
		}
		
		return renditionName + "-" + DigestUtils.sha1Hex(sb.toString());
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
