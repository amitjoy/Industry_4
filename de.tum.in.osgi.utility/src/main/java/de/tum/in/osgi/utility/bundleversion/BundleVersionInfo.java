/*******************************************************************************
 * Copyright 2015 Amit Kumar Mondal <admin@amitinside.com>
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
package de.tum.in.osgi.utility.bundleversion;

import org.osgi.framework.Version;

/**
 * Provides bundle version information, which can be extracted from bundle files
 * or Bundle objects.
 *
 * @author AMIT KUMAR MONDAL
 */
public abstract class BundleVersionInfo<T> implements Comparable<BundleVersionInfo<?>> {

	private static final int A_GREATER = 1;
	private static final int B_GREATER = -1;
	/**
	 * Name of the BND attribute that provides the bundle's last modified
	 * timestamp
	 */
	public static final String BND_LAST_MODIFIED = "Bnd-LastModified";

	/**
	 * Value for {@link #getBundleLastModified} if corresponding header is not
	 * present
	 */
	public static final long BND_LAST_MODIFIED_MISSING = -1L;

	private static final int EQUAL = 0;

	/** Marker used by Maven to identify snapshots */
	public static final String SNAPSHOT_MARKER = "SNAPSHOT";

	/**
	 * Compare based on bundle version info, and for snapshots based on
	 * {@link #getBundleLastModified}
	 */
	@Override
	public int compareTo(final BundleVersionInfo<?> other) {
		// Handle null values
		if (other == null) {
			throw new IllegalArgumentException("b is null, cannot compare");
		}

		// Handle non-bundles: we don't want them!
		if (!this.isBundle()) {
			throw new IllegalArgumentException("Not a bundle, cannot compare: " + this);
		}
		if (!other.isBundle()) {
			throw new IllegalArgumentException("Not a bundle, cannot compare:" + other);
		}

		// First compare symbolic names
		int result = this.getBundleSymbolicName().compareTo(other.getBundleSymbolicName());

		// Then compare versions
		if (result == EQUAL) {
			final Version va = this.getVersion();
			final Version vb = other.getVersion();
			if ((va == null) && (vb == null)) {
				// result = EQUAL
			} else if (vb == null) {
				result = A_GREATER;
			} else if (va == null) {
				result = B_GREATER;
			} else {
				result = va.compareTo(vb);
			}
		}

		// Then, if snapshots, compare modification times, more recent comes
		// first
		if ((result == EQUAL) && this.isSnapshot()) {
			final long ma = this.getBundleLastModified();
			final long mb = other.getBundleLastModified();
			if (ma > mb) {
				result = A_GREATER;
			} else if (mb > ma) {
				result = B_GREATER;
			}
		}

		return result;
	}

	/**
	 * Return the bundle last modification time, based on the BND_LAST_MODIFIED
	 * manifest header, if available. This is *not* the Bundle.getLastModified()
	 * value, which refers to actions in the OSGi framework.
	 *
	 * @return BND_LAST_MODIFIED_MISSING if header not supplied
	 */
	public abstract long getBundleLastModified();

	/** Return the bundle symbolic name, null if not available */
	public abstract String getBundleSymbolicName();

	/** Return the source of information: underlying File or Bundle */
	public abstract T getSource();

	/** Return the bundle version, null if not available */
	public abstract Version getVersion();

	/** True if the provided data is a valid bundle */
	public abstract boolean isBundle();

	/** True if the bundle version indicates a snapshot */
	public abstract boolean isSnapshot();

}