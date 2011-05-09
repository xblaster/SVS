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
import java.util.HashMap;

public class SVSSnapshotRepository<T extends Serializable> implements
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1046148868458676870L;
	HashMap<String, SVSSnapshot<T>> history;

	public SVSSnapshotRepository() {
		history = new HashMap<String, SVSSnapshot<T>>();
	}

	public void put(SVSSnapshot<T> snap) {
		history.put(snap.getRevisionNumber(), snap);
	}

	public SVSSnapshot<T> get(String revision) {
		// System.out.println("get "+revision);
		return history.get(revision);
	}

	public int getSize() {
		int totalSize = 0;
		for (SVSSnapshot<T> t : history.values()) {
			totalSize += t.getSize();
		}
		return totalSize;
	}

	public HashMap<String, SVSSnapshot<T>> getHistory() {
		return history;
	}

	public void setHistory(HashMap<String, SVSSnapshot<T>> history) {
		this.history = history;
	}
}
