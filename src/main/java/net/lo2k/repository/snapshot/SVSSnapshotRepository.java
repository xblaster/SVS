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
import java.util.LinkedList;
import java.util.List;

import net.lo2k.patcher.SVSPatch;
import net.lo2k.patcher.SVSPatcher;

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
	
	private static final int NEAREST_RANGE = 8;
	private List<String> getNearestRevisionFrom(int index, List<String> revisions) {
		List<String> result = new LinkedList<String>();
		for (int i = index +1 ; i < index + NEAREST_RANGE ; i++) {
			/*if (i < 0) {
				break;
			}*/ //useless in theory
			if (i >= revisions.size()) {
				break;
			}
			result.add(revisions.get(index));
		}
		
		return result;
	}
	
	public void optimize() {
		@SuppressWarnings("unchecked")
		LinkedList<String> revisions = new LinkedList(history.keySet());
		
		for (int i = 0; i < revisions.size(); i++) {
			List<String> nearestRev = getNearestRevisionFrom(i, revisions);
			findBettestPatchFor(revisions.get(i),nearestRev);
		}
		
		
	}

	private void findBettestPatchFor(String string, List<String> nearestRev) {
		SVSSnapshot<T> snap = history.get(string);
		
		SVSPatcher<T> localPatcher = new SVSPatcher<T>();
		
		int size = snap.getSize();
		
		T object = snap.getObject(this);
		
		
		SVSSnapshot<T> newPatch = snap;
		
		//crawl nearestRev for a better patch
		for (String rev: nearestRev) {
			SVSSnapshot<T> snapshot = history.get(rev);
			T oldObject = snapshot.getObject(this);
			
			SVSPatch<T> newPossiblePatch = localPatcher.makeSVSPatchFor(oldObject, object);
			
			SVSSnapshot<T> newPossibleSnapshot = new SVSDeltaSnapshot<T>(newPossiblePatch, rev, this);
			
				if (newPossibleSnapshot.getSize() < newPatch.getSize()) {
					System.out.println("find better candidate reduce by "+(size - newPossibleSnapshot.getSize()));
					newPatch = newPossibleSnapshot;
				}
		}
		
		if (newPatch!=snap) {
			//store new patch
			history.put(string, newPatch);
		}
	}
}
