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
import java.util.Date;

import net.lo2k.patcher.SVSPatch;
import net.lo2k.patcher.SVSPatcher;

/**
 * represent a state to previous version
 * 
 * @author wax
 * 
 */
public abstract class SVSSnapshot<T extends Serializable> implements
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4173789392829032729L;
	private String revisionNumber;

	private Date createdAt;

	public SVSSnapshot() {
		createdAt = new Date();
	}

	/**
	 * @param revisionNumber
	 *            the revisionNumber to set
	 */
	public void setRevisionNumber(String revisionNumber) {
		this.revisionNumber = revisionNumber;
	}

	/**
	 * @return the revisionNumber
	 */
	public String getRevisionNumber() {
		return revisionNumber;
	}

	public abstract T getObject(SVSSnapshotRepository<T> repository);

	public abstract int getSize();

	public SVSDeltaSnapshot<T> convertToSVSDeltaSnapshot(String futureRev,
			SVSSnapshotRepository<T> repository) {
		SVSPatcher<T> patcher = new SVSPatcher<T>();

		// create counter patch to return to previous version
		SVSPatch<T> patch = patcher.makeSVSPatchFor(repository.get(futureRev)
				.getObject(repository), this.getObject(repository));

		SVSDeltaSnapshot<T> deltaSnapshot = new SVSDeltaSnapshot<T>(patch,
				futureRev, repository);

		// fetch revision from Snapshot
		deltaSnapshot.setRevisionNumber(this.getRevisionNumber());
		return deltaSnapshot;
	}

	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

}
