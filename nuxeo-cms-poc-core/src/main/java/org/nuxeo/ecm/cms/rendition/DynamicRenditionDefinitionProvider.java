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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionHolder;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeEntry;
import org.nuxeo.ecm.platform.mimetype.interfaces.MimetypeRegistry;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinition;
import org.nuxeo.ecm.platform.rendition.service.RenditionDefinitionProvider;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 * Contribute a new {@link RenditionDefinitionProvider} that will expose new
 * dynamic Renditions that are not based on an XMl contribution.
 * 
 * @author tiry
 *
 */
public class DynamicRenditionDefinitionProvider implements RenditionDefinitionProvider {

	public static final String DYN_RENDITION_KIND = "nuxeo:dynamic:conversion";

	@Override
	public List<RenditionDefinition> getRenditionDefinitions(DocumentModel doc) {

		MimetypeRegistry mimetypeRegistry = Framework.getService(MimetypeRegistry.class);

		DynamicRenditionHolder holder = doc.getAdapter(DynamicRenditionHolder.class);
		if (holder == null) {
			return Collections.emptyList();
		}

		List<DynamicRendition> renditions = holder.getRenditions();

		List<RenditionDefinition> renditionDefinitions = new ArrayList<>();

		for (DynamicRendition dr : renditions) {

			RenditionDefinition renditionDefinition = new RenditionDefinition();
			renditionDefinition.setEnabled(true);
			renditionDefinition.setName(dr.getName());
			renditionDefinition.setKind(DYN_RENDITION_KIND);
			renditionDefinition.setProvider(new DynamicRenditionProvider());
			renditionDefinition.setVisible(true);
			renditionDefinition.setLabel(dr.getName());

			Blob blob = dr.getBlob();
			if (blob != null) {
				MimetypeEntry mimeType = mimetypeRegistry.getMimetypeEntryByMimeType(blob.getMimeType());
				renditionDefinition.setIcon("/icons/" + mimeType.getIconPath());
			}
			renditionDefinitions.add(renditionDefinition);
		}
		return renditionDefinitions;
	}

}
