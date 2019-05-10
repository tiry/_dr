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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.cms.rendition.DynamicRendition;
import org.nuxeo.ecm.cms.rendition.DynamicRenditionProvider;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionDocument;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionHolder;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.platform.rendition.Rendition;
import org.nuxeo.ecm.platform.rendition.service.RenditionService;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.transaction.TransactionHelper;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ AutomationFeature.class })
@Deploy("org.nuxeo.ecm.platform.rendition.api")
@Deploy("org.nuxeo.ecm.platform.rendition.core")
@Deploy({ "org.nuxeo.ecm.cms.nuxeo-cms-poc-core" })
public class DynamicRenditionsTest extends BaseTest {

	@Inject
	CoreSession session;

	@Inject
	ConversionService cs;

	@Inject
	RenditionService rs;

	@Inject
	EventService eventService;

	protected static final String DR_NAME = "dyna-text";

	protected DocumentModel createHtmlDocWithDynamicRendition(boolean preRender) {
		DocumentModel doc = session.createDocumentModel("/", "adoc", "File");
		Blob blob = Blobs.createBlob("<body>some content</body>", "text/html", "UTF-8", "dummy.html");
		doc.setPropertyValue("file:content", (Serializable) blob);

		DynamicRenditionDocument.create(doc);
		doc = session.createDocument(doc);

		DynamicRenditionHolder holder = doc.getAdapter(DynamicRenditionHolder.class);
		assertNotNull(holder);

		// add simple entry
		Map<String, String> params = new HashMap<>();
		DynamicRendition newRendition = new DynamicRendition(DR_NAME, "html2text", params);

		holder.add(newRendition, false, preRender);
		return session.saveDocument(doc);
	}

	@Test
	public void canRenderLazyRenditions() throws Exception {

		// sanity checks
		assertNotNull(cs);
		assertNotNull(rs);
		assertTrue(cs.getRegistredConverters().contains("html2text"));

		// create the doc with dynamic rendition
		DocumentModel doc = createHtmlDocWithDynamicRendition(false);

		Rendition rendition = rs.getRendition(doc, DR_NAME);
		assertNotNull(rendition);
		assertEquals(DynamicRenditionProvider.class.getSimpleName(), rendition.getProviderType());

		Blob result = rendition.getBlob();
		assertEquals("some content", result.getString());

		// verify that the rendition has been stored
		doc = session.getDocument(doc.getRef());
		Blob storedRendition = (Blob) ((List<Map<String, Serializable>>) doc
				.getPropertyValue(DynamicRenditionDocument.DR_RENDITIONS)).get(0).get(DynamicRenditionDocument.CONTENT);
		assertNotNull(storedRendition);
		assertEquals("some content", storedRendition.getString());

	}

	@Test
	public void canPreRenderRenditions() throws Exception {

		// create the doc with dynamic rendition with pre-render
		DocumentModel doc = createHtmlDocWithDynamicRendition(true);

		// because the Work is started after TX, we need to commit
		TransactionHelper.commitOrRollbackTransaction();
		TransactionHelper.startTransaction();

		eventService.waitForAsyncCompletion();

		// verify that the rendition has been stored
		doc = session.getDocument(doc.getRef());
		Blob storedRendition = (Blob) ((List<Map<String, Serializable>>) doc
				.getPropertyValue(DynamicRenditionDocument.DR_RENDITIONS)).get(0).get(DynamicRenditionDocument.CONTENT);
		assertNotNull(storedRendition);
		assertEquals("some content", storedRendition.getString());

		// access the rendition
		Rendition rendition = rs.getRendition(doc, DR_NAME);
		assertNotNull(rendition);
		assertEquals(DynamicRenditionProvider.class.getSimpleName(), rendition.getProviderType());

		Blob result = rendition.getBlob();
		assertEquals("some content", result.getString());

	}

}
