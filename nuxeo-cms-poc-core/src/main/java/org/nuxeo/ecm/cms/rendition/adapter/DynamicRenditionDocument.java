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

package org.nuxeo.ecm.cms.rendition.adapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.cms.rendition.DynamicRendition;
import org.nuxeo.ecm.cms.rendition.compute.DynamicRenditionPreComputeWork;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

/**
 * 
 * Encapsulate the logic associated to storage of dynamic renditions in a
 * {@link DocumentModel}
 * 
 * @author tiry
 *
 */
public class DynamicRenditionDocument implements DynamicRenditionHolder {

	public static final String PARAMS = "params";
	public static final String DR_RENDITIONS = "dr:renditions";
	public static final String P_VALUE = "value";
	public static final String P_NAME = "key";
	public static final String CONTENT = "content";
	public static final String CONVERTER_NAME = "converter";
	public static final String RENDITION_NAME = "name";
	public static final String FACET = "dynamicRenditions";

	public static final String CXT_SAVE_FLAG = "saveDynamicRendition";
	
	protected DocumentModel doc;

	public DynamicRenditionDocument(DocumentModel doc) {
		this.doc = doc;
	}

	public static DynamicRenditionDocument create(DocumentModel doc) {
		if (!doc.hasFacet(FACET)) {
			doc.addFacet(FACET);
		}
		return new DynamicRenditionDocument(doc);
	}

	@Override
	public List<DynamicRendition> getRenditions() {

		List<Map<String, Serializable>> renditions = (List<Map<String, Serializable>>) doc
				.getPropertyValue(DR_RENDITIONS);
		List<DynamicRendition> result = new ArrayList<>();

		for (Map<String, Serializable> rendition : renditions) {
			result.add(asDynamicRendition(rendition));
		}
		return result;
	}

	protected DynamicRendition asDynamicRendition(Map<String, Serializable> rendition) {
		String name = (String) rendition.get(RENDITION_NAME);
		String converter = (String) rendition.get(CONVERTER_NAME);
		List<Map<String, Serializable>> raw_params = (List<Map<String, Serializable>>) rendition.get(PARAMS);

		Blob blob = (Blob) rendition.get(CONTENT);

		Map<String, String> params = new HashMap();
		for (Map<String, Serializable> entry : raw_params) {
			String key = (String) entry.get(P_NAME);
			String value = (String) entry.get(P_VALUE);
			params.put(key, value);
		}

		return new DynamicRendition(name, converter, params, blob);
	}

	public void storeRenditionResult(String name, Blob blob, boolean save) {
		DynamicRendition dr = getRendition(name);
		if (dr != null) {
			dr.setBlob(blob);
		}
		add(dr, save, false);
	}

	public void add(DynamicRendition newRendition, boolean save, boolean preRender) {

		List<Map<String, Serializable>> renditions = (List<Map<String, Serializable>>) doc
				.getPropertyValue(DR_RENDITIONS);

		boolean add = true;

		for (Map<String, Serializable> rendition : renditions) {

			String name = (String) rendition.get(RENDITION_NAME);
			if (name.equals(newRendition.getName())) {
				mkComplex(newRendition, rendition);
				add = false;
				break;
			}
		}

		if (add) {
			renditions.add(mkComplex(newRendition, null));
			if (preRender) {
				DynamicRenditionPreComputeWork work = new DynamicRenditionPreComputeWork(doc, newRendition.getName(),
						newRendition.getConverterName(), newRendition.getParams());
				WorkManager wm = Framework.getService(WorkManager.class);
				wm.schedule(work, true);
			}
		}

		doc.setPropertyValue(DR_RENDITIONS, (Serializable) renditions);

		if (save) {
			doc = doc.getCoreSession().saveDocument(doc);
		}

	}

	protected Map<String, Serializable> mkComplex(DynamicRendition rendition, Map<String, Serializable> cplx) {

		if (cplx == null) {
			cplx = new HashMap<>();
		}

		cplx.put(RENDITION_NAME, rendition.getName());
		cplx.put(CONVERTER_NAME, rendition.getConverterName());

		cplx.put(CONTENT, (Serializable) rendition.getBlob());

		List<Map<String, Serializable>> raw_params = new ArrayList<>();

		for (String key : rendition.getParams().keySet()) {
			Map<String, Serializable> entry = new HashMap<>();
			entry.put("key", key);
			entry.put("value", rendition.getParams().get(key));
			raw_params.add(entry);
		}

		cplx.put(PARAMS, (Serializable) raw_params);

		return cplx;
	}

	@Override
	public DynamicRendition getRendition(String name) {
		List<Map<String, Serializable>> renditions = (List<Map<String, Serializable>>) doc
				.getPropertyValue(DR_RENDITIONS);
		for (Map<String, Serializable> rendition : renditions) {
			if (name.equals((String) rendition.get(RENDITION_NAME))) {
				return asDynamicRendition(rendition);
			}
		}
		return null;
	}

}
