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

package org.nuxeo.ecm.cms.rendition;

import java.util.Collections;
import java.util.List;

import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionDocument;
import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionHolder;
import org.nuxeo.ecm.cms.rendition.compute.DynamicRenditionExecutor;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.rendition.extension.RenditionProvider;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinition;

/**
 * {@link RenditionProvider} implementation that uses a definition that is
 * stored inside the {@link DocumentModel} itself
 * 
 * @author tiry
 *
 */
public class DynamicRenditionProvider implements RenditionProvider {

	@Override
	public boolean isAvailable(DocumentModel doc, RenditionDefinition definition) {
		return true;
	}

	@Override
	public List<Blob> render(DocumentModel doc, RenditionDefinition definition) {

		DynamicRenditionHolder holder = doc.getAdapter(DynamicRenditionHolder.class);
		if (holder == null) {
			return Collections.emptyList();
		}

		DynamicRendition dr = holder.getRendition(definition.getName());
		Blob blob = dr.getBlob();
		
		if (blob == null) {
			// just in time rendering
			BlobHolder bh = DynamicRenditionExecutor.exec(doc, dr.getConverterName(), dr.getParams());
			
			// check if we need to cache or not			
			Boolean save = (Boolean) doc.getContextData(DynamicRenditionDocument.CXT_SAVE_FLAG);
			save = save==null || save;

			holder.storeRenditionResult(definition.getName(), bh.getBlob(), save);

			dr = holder.getRendition(definition.getName());
			blob = dr.getBlob();
		}

		return blob != null ? Collections.singletonList(blob) : Collections.emptyList();
	}
}
