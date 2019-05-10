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

package org.nuxeo.ecm.cms.rendition.compute;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 * Helper code to handle conversion
 * 
 * @author tiry
 *
 */
public class DynamicRenditionExecutor {

	public static BlobHolder exec(DocumentModel source, String converterName, Map<String, String> params) {
		
		ConversionService cs = Framework.getService(ConversionService.class);
		BlobHolder bh = source.getAdapter(BlobHolder.class);
		
		Map<String,Serializable> cparams = params.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> (Serializable)e.getValue()));

		return cs.convert(converterName, bh, cparams);		
	}
	
	
}
