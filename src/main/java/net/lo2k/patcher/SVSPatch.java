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

package net.lo2k.patcher;

import java.io.Serializable;

import net.lo2k.zip.ZipUtil;

public class SVSPatch<T extends Serializable> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3362578737458614543L;
	private String patch;

	public SVSPatch() {
		this("");
	}

	public SVSPatch(String string) {
		setPatch(string);
	}

	public final void setPatch(String patch) {
		this.patch = ZipUtil.zip(patch);
	}

	public String getPatch() {
		return ZipUtil.unZip(patch);
		// return patch;
	}

	public int getSize() {
		return patch.length();
		// return 0;
	}

}
