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

import java.util.Map;

import org.nuxeo.ecm.cms.rendition.adapter.DynamicRenditionHolder;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.Work;

/**
 * Asynchronous {@link Work} used to pre-prender the dynamic renditions
 * 
 * @author tiry
 *
 */
public class DynamicRenditionPreComputeWork extends AbstractWork {

	private static final long serialVersionUID = 1L;

	protected String renditionName;
	protected String repositoryName;
	protected DocumentRef source;	
	protected String converterName;
	protected Map<String, String> params;
	
	public DynamicRenditionPreComputeWork(DocumentModel source, String renditionName, String converterName, Map<String, String> params) {
		this.repositoryName = source.getRepositoryName();
		this.source = source.getRef();
		this.renditionName=renditionName;
		this.converterName=converterName;
		this.params = params;
	}
	
	
	@Override
	public String getTitle() {		
		return "Dynamic Rendition " + renditionName + " on " + source.toString() + " using " + converterName;
	}

	@Override
	public void work() {
		
		openSystemSession();
		
		DocumentModel doc = session.getDocument(this.source);				
		DynamicRenditionHolder holder = doc.getAdapter(DynamicRenditionHolder.class);
		
		BlobHolder bh = DynamicRenditionExecutor.exec(doc, converterName, params);
		holder.storeRenditionResult(renditionName, bh.getBlob(), true);

	}

}
