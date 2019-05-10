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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.cms.rendition.DynamicRendition;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionDocument;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionHolder;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.restapi.test.BaseTest;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ CoreFeature.class })
@Deploy({ "org.nuxeo.ecm.cms.nuxeo-cms-poc-core" })
public class DynamicRenditionsAdapterTest extends BaseTest {

	@Inject
	CoreSession session;

	protected DocumentModel createDummyDoc(boolean addFacet) {
		DocumentModel doc = session.createDocumentModel("/", "adoc", "File");
		Blob blob = Blobs.createBlob("Dummy txt", "text/plain", null, "dummy.txt");
		doc.setPropertyValue("file:content", (Serializable) blob);
		if (addFacet) {
			DynamicRenditionDocument.create(doc);
		}
		return session.createDocument(doc);
	}

	@Test
	public void checkFacetAndAdapter() throws Exception {

		DocumentModel doc = createDummyDoc(false);
		assertFalse(doc.hasFacet(DynamicRenditionDocument.FACET));

		DynamicRenditionHolder holder = doc.getAdapter(DynamicRenditionHolder.class);
		assertNull(holder);

		DynamicRenditionDocument.create(doc);
		doc = session.saveDocument(doc);

		assertTrue(doc.hasFacet(DynamicRenditionDocument.FACET));

		holder = doc.getAdapter(DynamicRenditionHolder.class);
		assertNotNull(holder);

		assertEquals(0, holder.getRenditions().size());

	}

	@Test
	public void checkAddAndUpdateRendition() {

		DocumentModel doc = createDummyDoc(true);
		DynamicRenditionHolder holder = doc.getAdapter(DynamicRenditionHolder.class);
		assertEquals(0, holder.getRenditions().size());

		// create a new entry
		Map<String, String> params = new HashMap<>();
		params.put("width", "300");
		params.put("height", "300");
		DynamicRendition newRendition = new DynamicRendition("dyna1", "foo", params);

		holder.add(newRendition, true, false);

		// re-fetch and check content
		doc = session.getDocument(doc.getRef());
		holder = doc.getAdapter(DynamicRenditionHolder.class);
		assertEquals(1, holder.getRenditions().size());
		assertEquals("foo", holder.getRenditions().get(0).getConverterName());
		assertEquals("300", holder.getRenditions().get(0).getParams().get("width"));
		assertEquals("300", holder.getRenditions().get(0).getParams().get("height"));

		// update
		newRendition = new DynamicRendition("dyna1", "foo2", params);
		params.put("width", "400");

		holder.add(newRendition, true, false);

		// re-fetch and check content
		doc = session.getDocument(doc.getRef());
		holder = doc.getAdapter(DynamicRenditionHolder.class);
		assertEquals(1, holder.getRenditions().size());
		assertEquals("foo2", holder.getRenditions().get(0).getConverterName());
		assertEquals("300", holder.getRenditions().get(0).getParams().get("height"));
		assertEquals("400", holder.getRenditions().get(0).getParams().get("width"));

		// add new
		newRendition = new DynamicRendition("dyna2", "bar", params);
		holder.add(newRendition, true, false);

		// re-fetch and check content
		doc = session.getDocument(doc.getRef());
		holder = doc.getAdapter(DynamicRenditionHolder.class);
		assertEquals(2, holder.getRenditions().size());

	}

}
