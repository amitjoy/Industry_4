/*******************************************************************************
 * Copyright (C) 2015 - Amit Kumar Mondal <admin@amitinside.com>
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
 *******************************************************************************/
package de.tum.in.activity.log;

import java.util.List;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.kura.KuraException;
import org.eclipse.kura.cloud.Cloudlet;
import org.eclipse.kura.cloud.CloudletTopic;
import org.eclipse.kura.db.DbService;
import org.eclipse.kura.message.KuraRequestPayload;
import org.eclipse.kura.message.KuraResponsePayload;

/**
 * The implementation of ActivityLogService
 * 
 * @see ActivityLogService
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
@Component
@Service(value = { ActivityLogServiceImpl.class })
public class ActivityLogServiceImpl extends Cloudlet implements
		ActivityLogService {

	/**
	 * Defines Application ID for Activity Logs
	 */
	private static final String APP_ID = "BLUETOOTH-V1";

	/**
	 * Kura DB Service Reference
	 */
	@Reference(bind = "bindDBService", unbind = "bindDBService")
	private volatile DbService m_dbService;

	/**
	 * Constructor
	 */
	public ActivityLogServiceImpl() {
		super(APP_ID);
	}

	/**
	 * Kura DB Service Binding Callback
	 */
	protected synchronized void bindDBService(DbService dbService) {
		if (m_dbService == null)
			m_dbService = dbService;
	}

	/**
	 * Kura DB Service Binding Callback
	 */
	protected synchronized void unbindDBService(DbService dbService) {
		if (m_dbService == dbService)
			m_dbService = null;
	}

	/** {@inheritDoc} */
	@Override
	public void saveLog(String log) {
		// TO-DO Save Log

	}

	/** {@inheritDoc} */
	@Override
	public List<String> retrieveLogs() {
		// TO-DO Retrieve Logs
		return null;
	}

	/** {@inheritDoc} */
	@Override
	protected void doGet(CloudletTopic reqTopic, KuraRequestPayload reqPayload,
			KuraResponsePayload respPayload) throws KuraException {
		if ("logs".equals(reqTopic.getResources()[0])) {
			// TO-DO retrieve logs
			final List<String> logs = retrieveLogs();

		}
	}

}
