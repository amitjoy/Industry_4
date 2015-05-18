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

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class DataCache implements EventHandler {

	private static final String DATA_CACHE_TOPIC = "de/tum/in/device/cache/*";

	public void handleEvent(Event event) {
		if (DATA_CACHE_TOPIC.startsWith(event.getTopic())) {

		}
	}

}
