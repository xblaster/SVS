/*
 * Copyright 2011 Jérôme Wax <jerome.wax@lo2k.net>
 * http://www.lo2k.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lo2k.repository.snapshot;

import java.io.Serializable;

import net.lo2k.patcher.SVSPatch;
import net.lo2k.patcher.SVSPatcher;

public class SVSDeltaSnapshot<T extends Serializable> extends SVSSnapshot<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5177468237513223932L;
	private String futureRev;
	private SVSPatch<T> sVSPatch;

	public SVSDeltaSnapshot(SVSPatch<T> patch, String futureRev,
			SVSSnapshotRepository<T> repository) {
		super();
		this.futureRev = futureRev;
		this.sVSPatch = patch;
	}

	public SVSDeltaSnapshot() {
		this(null, "0", null);
	}

	@Override
	public T getObject(SVSSnapshotRepository<T> repository) {
		SVSSnapshot<T> futureSnap = repository.get(futureRev);

		SVSPatcher<T> patcher = new SVSPatcher<T>();
		T patchedObj = patcher.patchWith(futureSnap.getObject(repository),
				sVSPatch);
		/*System.out.println("Patch from " + futureSnap.getRevisionNumber()
				+ " => " + getRevisionNumber());*/
		return patchedObj;
	}

	@Override
	public int getSize() {
		return this.sVSPatch.getSize();
	}

	public String getFutureRev() {
		return futureRev;
	}

	public void setFutureRev(String futureRev) {
		this.futureRev = futureRev;
	}

	public SVSPatch<T> getSVSPatch() {
		return sVSPatch;
	}

	public void setPatch(SVSPatch<T> patch) {
		this.sVSPatch = patch;
	}

}
