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

package org.nuxeo.ecm.cms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.StringJoiner;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.restapi.server.jaxrs.cms.adapter.CMSDynamicRenditionAdapter;
import org.nuxeo.ecm.restapi.server.jaxrs.cms.adapter.DynamicRenditionObject;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.ecm.restapi.test.RestServerFeature;
import org.nuxeo.jaxrs.test.CloseableClientResponse;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.sun.jersey.core.util.MultivaluedMapImpl;

@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD, init = RestServerInit.class)
@Deploy({"org.nuxeo.ecm.platform.restapi.server.cms"})
public class DynamicRenditionsAdapterTest extends BaseTest {

    @Test
    public void shoudCallAdapter() throws Exception {

    	DocumentModel doc = session.createDocumentModel("/", "adoc", "File");
        Blob blob = Blobs.createBlob("Dummy txt", "text/plain", null, "dummy.txt");
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();

        try (CloseableClientResponse response = getDoc(doc)) {
            assertEquals(200, response.getStatus());
        }

        MultivaluedMap<String, String> params = new MultivaluedMapImpl();        
        params.add("converter", "cropme");
        params.add("width", "300");
        params.add("heigh", "300");                
        
        try (CloseableClientResponse response = getDynamicRendition(doc, DynamicRenditionObject.ECHO, params)) {
            assertEquals(200, response.getStatus());
            
            String outcome = IOUtils.toString(response.getEntityInputStream(), "UTF-8");
            
            assertTrue(outcome.contains("User-Agent"));
            assertTrue(outcome.contains("cropme"));
            assertTrue(outcome.contains("width"));
            assertTrue(outcome.contains("300"));            
        }
    }


    protected CloseableClientResponse getDoc(DocumentModel doc) {
        StringJoiner path = new StringJoiner("/").add("id").add(doc.getId());
        return getResponse(RequestType.GET, path.toString());
    }

    protected CloseableClientResponse getDynamicRendition(DocumentModel doc, String name, MultivaluedMap<String, String> params) {
        StringJoiner path = new StringJoiner("/").add("id").add(doc.getId());
        path.add("@" + CMSDynamicRenditionAdapter.NAME + "/" + name);               
        return getResponse(RequestType.GET, path.toString(), params);
    }
}
