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
package de.tum.in.data.format;

/**
 * Represents Bluetooth Realtime Data
 *
 * @author AMIT KUMAR MONDAL
 *
 */
public final class BluetoothData {

	/** */
	private String fx;

	/** */
	private String fy;

	/** */
	private String fz;

	/** */
	private String mx;

	/** */
	private String my;

	/** */
	private String mz;

	/** Constructor */
	public BluetoothData(final String fx, final String fy, final String fz, final String mx, final String my,
			final String mz) {
		super();
		this.fx = fx;
		this.fy = fy;
		this.fz = fz;
		this.mx = mx;
		this.my = my;
		this.mz = mz;
	}

	/**
	 * @return the fx
	 */
	public final String getFx() {
		return this.fx;
	}

	/**
	 * @return the fy
	 */
	public final String getFy() {
		return this.fy;
	}

	/**
	 * @return the fz
	 */
	public final String getFz() {
		return this.fz;
	}

	/**
	 * @return the mx
	 */
	public final String getMx() {
		return this.mx;
	}

	/**
	 * @return the my
	 */
	public final String getMy() {
		return this.my;
	}

	/**
	 * @return the mz
	 */
	public final String getMz() {
		return this.mz;
	}

	/**
	 * @param fx
	 *            the fx to set
	 */
	public final void setFx(final String fx) {
		this.fx = fx;
	}

	/**
	 * @param fy
	 *            the fy to set
	 */
	public final void setFy(final String fy) {
		this.fy = fy;
	}

	/**
	 * @param fz
	 *            the fz to set
	 */
	public final void setFz(final String fz) {
		this.fz = fz;
	}

	/**
	 * @param mx
	 *            the mx to set
	 */
	public final void setMx(final String mx) {
		this.mx = mx;
	}

	/**
	 * @param my
	 *            the my to set
	 */
	public final void setMy(final String my) {
		this.my = my;
	}

	/**
	 * @param mz
	 *            the mz to set
	 */
	public final void setMz(final String mz) {
		this.mz = mz;
	}

	/** {@inheritDoc}} */
	@Override
	public String toString() {
		return "Mx=" + this.mx + ", " + "My=" + this.my + ", " + "Mz=" + this.mz + ", " + "Fx=" + this.fx + ", " + "fy="
				+ this.fy + ", " + "fz=" + this.fz;
	}

}
