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
 *     Tiry
 */

package org.nuxeo.ecm.restapi.server.jaxrs.cms.adapter;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.webengine.model.WebAdapter;
import org.nuxeo.ecm.webengine.model.impl.DefaultAdapter;

@WebAdapter(name = CMSDynamicRenditionAdapter.NAME, type = "cmsAdapter")
public class CMSDynamicRenditionAdapter extends DefaultAdapter {

    public static final String NAME = "cmsRendition";
    
    @Path("{renditionName:((?:(?!/@).)*)}")
    public Object doGetCMS(@Context Request request, @PathParam("renditionName") String renditionName) {
        DocumentModel doc = getTarget().getAdapter(DocumentModel.class);
        return newObject("dynamicRendition", doc, renditionName);
    }
}
