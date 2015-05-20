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

/**
 * Listener used to dump data while removing the data from the cache
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class RemoveRealtimeDataListener implements
		RemovalListener<String, Object> {

	/** {@inheritDoc} */
	@Override
	public void onRemoval(RemovalNotification<String, Object> notification) {
		// TO-DO Add logic to dump to MongoDB

	}

}