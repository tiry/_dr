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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.StringJoiner;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
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

import com.google.inject.Inject;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@RunWith(FeaturesRunner.class)
@Features({ RestServerFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD, init = RestServerInit.class)
@Deploy({ "org.nuxeo.ecm.platform.picture.convert" })
@Deploy({ "org.nuxeo.ecm.platform.restapi.server.cms" })

public class DynamicRenditionsViaREST extends BaseTest {

	@Inject
	ConversionService cs;

	protected static final String TEST_IMG = "astro.png";
	protected static final int IMG_WIDTH = 400;
	protected static final int IMG_HEIGHT = 514;

	protected String createDocument(File srcImgFile) {
		
		DocumentModel doc = session.createDocumentModel("/", "myImg", "File");
        Blob blob = new FileBlob(srcImgFile);
        doc.setPropertyValue("file:content", (Serializable) blob);
        doc = session.createDocument(doc);

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();
        return doc.getId();
	}
	
	@Test
	public void shoudCallAdapter() throws Exception {
		
		URL url = this.getClass().getClassLoader().getResource(TEST_IMG);
		File srcImgFile = new File(url.toURI());
		BufferedImage sourceImg = ImageIO.read(srcImgFile);		
					
		// sanity checks
		assertNotNull(cs);
		assertTrue(cs.getRegistredConverters().contains("pictureCrop"));
		assertEquals(IMG_WIDTH, sourceImg.getWidth());
		assertEquals(IMG_HEIGHT, sourceImg.getHeight());

		String docUid = createDocument(srcImgFile);		
		assertEquals(200, getDoc(docUid).getStatus());
				
		
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		
		params.add("converter", "pictureCrop");
		params.add("width", "50");
		params.add("height", "50");
		
		 try (CloseableClientResponse response = getDynamicRendition(docUid, "dynamicCrop", params)) {
	            assertEquals(200, response.getStatus());
	            
	            String outcome = IOUtils.toString(response.getEntityInputStream(), "UTF-8");
	            
	        }
		
	}

	protected CloseableClientResponse getDoc(String docUid) {
		StringJoiner path = new StringJoiner("/").add("id").add(docUid);
		return getResponse(RequestType.GET, path.toString());
	}

	protected CloseableClientResponse getDynamicRendition(String docUid, String name,
			MultivaluedMap<String, String> params) {
		StringJoiner path = new StringJoiner("/").add("id").add(docUid);
		path.add("@" + CMSDynamicRenditionAdapter.NAME + "/" + name);
		return getResponse(RequestType.GET, path.toString(), params);
	}
}
