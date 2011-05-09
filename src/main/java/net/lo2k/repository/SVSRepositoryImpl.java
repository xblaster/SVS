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

package net.lo2k.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.lo2k.patcher.SVSPatch;
import net.lo2k.patcher.SVSPatcher;
import net.lo2k.repository.snapshot.SVSCompleteSnapshot;
import net.lo2k.repository.snapshot.SVSSnapshot;
import net.lo2k.repository.snapshot.SVSSnapshotRepository;

import org.ho.yaml.YamlDecoder;
import org.ho.yaml.YamlEncoder;

public class SVSRepositoryImpl<T extends Serializable> implements
		SVSRepository<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -309698173263581762L;

	// No list for serialization
	LinkedList<String> snapshots;

	SVSSnapshotRepository<T> repository;

	public SVSRepositoryImpl() {
		snapshots = new LinkedList<String>();
		repository = new SVSSnapshotRepository<T>();
	}

	public void appendToHistory(SVSSnapshot<T> snapshot) {
		snapshots.add(snapshot.getRevisionNumber());
		repository.put(snapshot);
	}

	@Override
	public String makeSnapshot(T toSnap) {
		SVSSnapshot<T> newSnapshot = new SVSCompleteSnapshot<T>(toSnap,
				repository);
		appendToHistory(newSnapshot);

		// history is never empty
		if (snapshots.size() > 1) {

			// fetch previous snap
			SVSSnapshot<T> previousSnap = repository.get(snapshots
					.get(snapshots.size() - 2));
			// convert previous entry to "delta snap"
			SVSSnapshot<T> convertedToSnap = previousSnap
					.convertToSVSDeltaSnapshot(newSnapshot.getRevisionNumber(),
							repository);

			// store only convertedToSnap is smaller than completeSnap
			if (convertedToSnap.getSize() < previousSnap.getSize()) {

				System.out.println("delta: " + convertedToSnap.getSize()
						+ " | gain: "
						+ (previousSnap.getSize() - convertedToSnap.getSize()));
				// store also only if it's not the same as previous
				if (convertedToSnap.getRevisionNumber() != newSnapshot
						.getRevisionNumber()) {
					repository.put(convertedToSnap);
				}
			} else {
				System.out.println("keep complete: " + previousSnap.getSize());
			}
		}

		return newSnapshot.getRevisionNumber();
	}

	@Override
	public T restoreSnapShot(String snapshotHash) {
		return repository.get(snapshotHash).getObject(repository);
	}

	@Override
	public List<String> getHistory() {
		return snapshots;
	}

	@Override
	public int getSize() {
		return repository.getSize();
	}

	@Override
	public void saveToFile(File f) {
		FileOutputStream sw = null;
		try {
			sw = new FileOutputStream(f);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			GZIPOutputStream gzipOutputStream = new GZIPOutputStream(sw);
			YamlEncoder xenc = new YamlEncoder(gzipOutputStream);
			xenc.writeObject(this);
			xenc.close();
			gzipOutputStream.close();
			sw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sw.toString());
	}

	// THIS IS A REALLY DIRT CODE !
	@SuppressWarnings("unchecked")
	@Override
	public SVSRepositoryImpl<T> loadFromFile(File f) {
		try {
			GZIPInputStream gzipInputStream;
			FileInputStream sw = null;
			sw = new FileInputStream(f);
			gzipInputStream = new GZIPInputStream(sw);
			YamlDecoder xenc = new YamlDecoder(gzipInputStream);
			SVSRepositoryImpl<T> res = null;
			res = (SVSRepositoryImpl<T>) xenc.readObject();
			xenc.close();
			sw.close();
			return res;
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return null;
	}

	public LinkedList<String> getSnapshots() {
		return snapshots;
	}

	public void setSnapshots(LinkedList<String> snapshots) {
		this.snapshots = snapshots;
	}

	public SVSSnapshotRepository<T> getRepository() {
		return repository;
	}

	public void setRepository(SVSSnapshotRepository<T> repository) {
		this.repository = repository;
	}

	@Override
	public T applyPatch(SVSPatch<T> patch) {
		T latestRev = getLatestSnapshot();
		SVSPatcher<T> p = new SVSPatcher<T>();
		T newObj = p.patchWith(latestRev, patch);
		makeSnapshot(newObj);
		return newObj;
	}

	@Override
	public T getLatestSnapshot() {
		return restoreSnapShot(getLatestRevNumber());
	}

	@Override
	public String getLatestRevNumber() {
		return getHistory().get(getHistory().size() - 1);
	}

	@Override
	public SVSPatch<T> getSVSPatchBeetween(String rev1, String rev2) {
		SVSPatcher<T> patcher = new SVSPatcher<T>();
		SVSPatch<T> patch = patcher.makeSVSPatchFor(restoreSnapShot(rev1),
				restoreSnapShot(rev2));
		return patch;
	}

	@Override
	public String getRevisionBefore(Date d) {

		String result = null;

		// history is ordered
		for (String str : getHistory()) {
			SVSSnapshot<T> snapshot = repository.get(str);
			if (snapshot.getCreatedAt().after(d)) { // we are to far
				return result;
			}
			result = str;
		}

		if (result == null) {
			throw new SVSRevisionNotFindException();
		}
		return result;

	}

	@Override
	public T restoreObjectBeforeDate(Date d) {
		return restoreSnapShot(getRevisionBefore(d));
	}

}
