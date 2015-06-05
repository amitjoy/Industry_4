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
package de.tum.in.data.cache;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import de.tum.in.mongodb.MongoDBService;

/**
 * Listener used to dump data to MongoDB while removing the data from the cache
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class RemoveRealtimeDataListener implements RemovalListener<String, Object> {

	/** MongoDB Collection Name to dump data */
	private static final String COLLECTION_NAME = "BLUETOOTH-MILLING";

	/** MongoDB Service */
	private final MongoDBService m_mongoDBService;

	/** Constructor */
	public RemoveRealtimeDataListener(final MongoDBService mongoDbService) {
		this.m_mongoDBService = mongoDbService;
	}

	/** {@inheritDoc} */
	@Override
	public void onRemoval(final RemovalNotification<String, Object> notification) {
		this.m_mongoDBService.getDatabase().getCollection(COLLECTION_NAME);

		// TODO Insert the data
	}

}
