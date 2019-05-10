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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.StringJoiner;

import javax.imageio.ImageIO;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.cms.rendition.DynamicRendition;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionHolder;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.restapi.server.jaxrs.cms.adapter.CMSDynamicRenditionAdapter;
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
@Deploy("org.nuxeo.ecm.platform.rendition.api")
@Deploy("org.nuxeo.ecm.platform.rendition.core")
@Deploy({ "org.nuxeo.ecm.platform.picture.api" })
@Deploy({ "org.nuxeo.ecm.platform.picture.core" })
@Deploy({ "org.nuxeo.ecm.platform.picture.convert" })
@Deploy({ "org.nuxeo.ecm.platform.restapi.server.cms" })
@Deploy({ "org.nuxeo.ecm.cms.nuxeo-cms-poc-core" })
public class DynamicRenditionsViaREST extends BaseTest {

	@Inject
	protected ConversionService cs;

	protected static final String TEST_IMG = "astro.png";
	protected static final int IMG_WIDTH = 400;
	protected static final int IMG_HEIGHT = 514;
	protected static final int NEW_SIZE = 51;
	protected static final String DYN_RENDITION_NAME = "dynamicCrop";

	protected String createDocument(File srcImgFile) {

		DocumentModel doc = session.createDocumentModel("/", "myImg", "File");
		Blob blob = new FileBlob(srcImgFile);
		blob.setMimeType("image/png");
		blob.setFilename(TEST_IMG);
		doc.setPropertyValue("file:content", (Serializable) blob);
		doc = session.createDocument(doc);

		TransactionHelper.commitOrRollbackTransaction();
		TransactionHelper.startTransaction();
		return doc.getId();
	}

	@Test
	public void shouldCreateAndStoreRendition() throws Exception {

		URL url = this.getClass().getClassLoader().getResource(TEST_IMG);
		File srcImgFile = new File(url.toURI());
		BufferedImage sourceImg = ImageIO.read(srcImgFile);

		// sanity checks
		assertNotNull(cs);
		assertTrue(cs.getRegistredConverters().contains("pictureCrop"));
		assertEquals(IMG_WIDTH, sourceImg.getWidth());
		assertEquals(IMG_HEIGHT, sourceImg.getHeight());

		// create the source document
		String docUid = createDocument(srcImgFile);
		assertEquals(200, getDoc(docUid).getStatus());

		// call (and create) the conversion
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("converter", "pictureCrop");
		params.add("width", Integer.toString(NEW_SIZE));
		params.add("height", Integer.toString(NEW_SIZE));

		try (CloseableClientResponse response = getDynamicRendition(docUid, DYN_RENDITION_NAME, params)) {
			assertEquals(200, response.getStatus());

			BufferedImage result = ImageIO.read(response.getEntityInputStream());
			assertEquals(NEW_SIZE, result.getWidth());
			assertEquals(NEW_SIZE, result.getHeight());
		}

		// check that the rendition is saved
		DocumentModel source = session.getDocument(new IdRef(docUid));
		List<DynamicRendition> registeredRenditions = source.getAdapter(DynamicRenditionHolder.class).getRenditions();
		assertEquals(1, registeredRenditions.size());
		assertTrue(registeredRenditions.get(0).getName().startsWith(DYN_RENDITION_NAME + "-"));

	}

	@Test
	public void shouldRespectAllowCreateFlag() throws Exception {

		URL url = this.getClass().getClassLoader().getResource(TEST_IMG);
		File srcImgFile = new File(url.toURI());
		BufferedImage sourceImg = ImageIO.read(srcImgFile);

		// create the source document
		String docUid = createDocument(srcImgFile);

		// call (and create) the conversion
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("converter", "pictureCrop");
		params.add("width", Integer.toString(NEW_SIZE));
		params.add("height", Integer.toString(NEW_SIZE));
		params.add("create", "false");

		try (CloseableClientResponse response = getDynamicRendition(docUid, DYN_RENDITION_NAME, params)) {
			assertEquals(400, response.getStatus());
		}
		
		params.putSingle("create", "true");
		try (CloseableClientResponse response = getDynamicRendition(docUid, DYN_RENDITION_NAME, params)) {
			assertEquals(200, response.getStatus());
		}
	}

	@Test
	public void shouldCreateTransientRendition() throws Exception {

		URL url = this.getClass().getClassLoader().getResource(TEST_IMG);
		File srcImgFile = new File(url.toURI());
		BufferedImage sourceImg = ImageIO.read(srcImgFile);

		// sanity checks
		assertNotNull(cs);
		assertTrue(cs.getRegistredConverters().contains("pictureCrop"));
		assertEquals(IMG_WIDTH, sourceImg.getWidth());
		assertEquals(IMG_HEIGHT, sourceImg.getHeight());

		// create the source document
		String docUid = createDocument(srcImgFile);
		assertEquals(200, getDoc(docUid).getStatus());

		// call (and create) the conversion
		MultivaluedMap<String, String> params = new MultivaluedMapImpl();
		params.add("converter", "pictureCrop");
		params.add("width", Integer.toString(NEW_SIZE));
		params.add("height", Integer.toString(NEW_SIZE));
		params.add("cache", "false");

		try (CloseableClientResponse response = getDynamicRendition(docUid, DYN_RENDITION_NAME, params)) {
			assertEquals(200, response.getStatus());

			BufferedImage result = ImageIO.read(response.getEntityInputStream());
			assertEquals(NEW_SIZE, result.getWidth());
			assertEquals(NEW_SIZE, result.getHeight());
		}

		// check that the rendition is saved
		DocumentModel source = session.getDocument(new IdRef(docUid));
		assertNull(source.getAdapter(DynamicRenditionHolder.class));

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
