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
package de.tum.in.osgi.utility;

import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

/**
 * The <code>ServiceUtil</code> is a utility class providing some usefull
 * utility methods for service handling.
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public class ServiceUtil {

	private static final class ComparableImplementation implements Comparable<Object> {

		private final Map<String, Object> props;

		private ComparableImplementation(final Map<String, Object> props) {
			this.props = props;
		}

		@Override
		@SuppressWarnings("unchecked")
		public int compareTo(final Object reference) {
			final Long otherId;
			Object otherRankObj;
			if (reference instanceof ServiceReference) {
				final ServiceReference<?> other = (ServiceReference<?>) reference;
				otherId = (Long) other.getProperty(Constants.SERVICE_ID);
				otherRankObj = other.getProperty(Constants.SERVICE_RANKING);
			} else if (reference instanceof Map) {
				final Map<String, Object> otherProps = (Map<String, Object>) reference;
				otherId = (Long) otherProps.get(Constants.SERVICE_ID);
				otherRankObj = otherProps.get(Constants.SERVICE_RANKING);
			} else {
				final ComparableImplementation other = (ComparableImplementation) reference;
				otherId = (Long) other.props.get(Constants.SERVICE_ID);
				otherRankObj = other.props.get(Constants.SERVICE_RANKING);
			}
			final Long id = (Long) this.props.get(Constants.SERVICE_ID);
			if (id.equals(otherId)) {
				return 0; // same service
			}

			Object rankObj = this.props.get(Constants.SERVICE_RANKING);

			// If no rank, then spec says it defaults to zero.
			rankObj = (rankObj == null) ? new Integer(0) : rankObj;
			otherRankObj = (otherRankObj == null) ? new Integer(0) : otherRankObj;

			// If rank is not Integer, then spec says it defaults to zero.
			final Integer rank = (rankObj instanceof Integer) ? (Integer) rankObj : new Integer(0);
			final Integer otherRank = (otherRankObj instanceof Integer) ? (Integer) otherRankObj : new Integer(0);

			// Sort by rank in ascending order.
			if (rank.compareTo(otherRank) < 0) {
				return -1; // lower rank
			} else if (rank.compareTo(otherRank) > 0) {
				return 1; // higher rank
			}

			// If ranks are equal, then sort by service id in descending order.
			return (id.compareTo(otherId) < 0) ? 1 : -1;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof ComparableImplementation) {
				return this.props.equals(((ComparableImplementation) obj).props);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return this.props.hashCode();
		}
	}

	/**
	 * Create a comparable object out of the service properties. With the result
	 * it is possible to compare service properties based on the service ranking
	 * of a service. Therefore this object acts like
	 * {@link ServiceReference#compareTo(Object)}.
	 *
	 * @param props
	 *            The service properties.
	 * @return A comparable for the ranking of the service
	 */
	public static Comparable<Object> getComparableForServiceRanking(final Map<String, Object> props) {
		return new ComparableImplementation(props);
	}
}