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
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import net.lo2k.patcher.SVSPatch;

/**
 * versioning utility for a serializedObject
 * 
 * @author wax
 * 
 * @param <T>
 */
public interface SVSRepository<T extends Serializable> {

	/**
	 * make a new snapshot
	 * 
	 * @param newVersion
	 * @return
	 */
	String makeSnapshot(T newVersion);

	/**
	 * restored a specified snapshot
	 * 
	 * @param snapshotHash
	 * @return
	 */
	T restoreSnapShot(String snapshotHash);

	/**
	 * get latest save snapshot
	 * 
	 * @return
	 */
	T getLatestSnapshot();

	/**
	 * get complete history hash
	 * 
	 * @return
	 */
	List<String> getHistory();

	/**
	 * get estimated history size
	 * 
	 * @return
	 */
	int getSize();

	/**
	 * apply and commit a patch into histo
	 * 
	 * @param patch
	 * @return
	 */
	T applyPatch(SVSPatch<T> patch);

	/**
	 * get latest revision number
	 * 
	 * @return
	 */
	String getLatestRevNumber();

	/**
	 * get revision number before this date
	 * 
	 * @param d
	 * @return
	 */
	String getRevisionBefore(Date d);

	/**
	 * restore object juste before this date
	 * 
	 * @param d
	 * @return
	 */
	T restoreObjectBeforeDate(Date d);

	/**
	 * create a patch beetween to revision (revision number is important)
	 * 
	 * @param rev1
	 * @param rev2
	 * @return
	 */
	SVSPatch<T> getSVSPatchBeetween(String rev1, String rev2);

	// IO SECTION
	/**
	 * load historization from a file
	 */
	SVSRepositoryImpl<T> loadFromFile(File f);

	/**
	 * save histo to a file
	 * 
	 * @param f
	 */
	void saveToFile(File f);
}
