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

import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;

/**
 * Utility class to hold the definition of a Dynamic Rendition based on
 * converter and associated parameters.
 * 
 * @author tiry
 *
 */
public class DynamicRendition {

	protected String name;

	protected String converterName;

	protected Map<String, String> params;

	protected Blob blob;

	public DynamicRendition(String name, String converterName, Map<String, String> params) {
		this(name, converterName, params, null);
	}

	public DynamicRendition(String name, String converterName, Map<String, String> params, Blob blob) {
		super();
		this.name = name;
		this.converterName = converterName;
		this.params = params;
		this.blob = blob;
	}

	public String getName() {
		return name;
	}

	public String getConverterName() {
		return converterName;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public Blob getBlob() {
		return blob;
	}

	public void setBlob(Blob blob) {
		this.blob = blob;
	}

}
