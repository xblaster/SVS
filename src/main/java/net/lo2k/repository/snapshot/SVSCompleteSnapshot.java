/*
 * Copyright 2011 J�r�me Wax <jerome.wax@lo2k.net>
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

import net.lo2k.patcher.SVSPatcher;

public class SVSCompleteSnapshot<T extends Serializable> extends SVSSnapshot<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7832332404079571500L;
	private T obj;

	public SVSCompleteSnapshot() {
		//
		super();
		obj = null;
	}

	public SVSCompleteSnapshot(T object, SVSSnapshotRepository<T> repository) {
		super();
		this.obj = object;

		SVSPatcher<T> patcher = new SVSPatcher<T>();
		setRevisionNumber(patcher.getHashFor(object));

	}

	/** only for serialization **/
	public T getObj() {
		return obj;
	}

	public void setObj(T obj) {
		this.obj = obj;
		SVSPatcher<T> patcher = new SVSPatcher<T>();
		setRevisionNumber(patcher.getHashFor(obj));
	}

	/** END only for serialization **/

	@Override
	public T getObject(SVSSnapshotRepository<T> repository) {
		return obj;
	}

	@Override
	public int getSize() {
		SVSPatcher<T> patcher = new SVSPatcher<T>();
		return patcher.getStringFor(obj).length();
	}

}
