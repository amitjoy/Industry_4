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
package de.tum.in.osgi.utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Loads resources from any bundle
 * 
 * @author AMIT KUMAR MONDAL
 *
 */
public class BundleResourceLoaderImpl implements IBundleResourceLoader {

	@Override
	public <T> T loadResource(final Class<?> bundleClazz, final Class<T> resourceTypeclazz, final String pathToFile)
			throws IOException {
		final Bundle bundle = FrameworkUtil.getBundle(bundleClazz);
		final URL url = bundle.getEntry(pathToFile);
		final InputStream stream = new FileInputStream(url.getFile());

		if (resourceTypeclazz.isInstance(InputStream.class)) {
			return resourceTypeclazz.cast(stream);
		}

		return null;
	}

}
