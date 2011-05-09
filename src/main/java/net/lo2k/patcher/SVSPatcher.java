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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;

import net.lo2k.thirdpart.diff.DiffMatchPatch;
import net.lo2k.thirdpart.diff.DiffMatchPatch.DFMPatch;

import org.ho.yaml.YamlDecoder;
import org.ho.yaml.YamlEncoder;

public class SVSPatcher<T extends Serializable> {

	private static final DiffMatchPatch diffMatchPatch = new DiffMatchPatch();

	public SVSPatch<T> makeSVSPatchFor(T object1, T object2) {
		String xml1 = getStringFor(object1);
		String xml2 = getStringFor(object2);

		// diffMatchPatch.Match_Distance = 1;
		diffMatchPatch.Diff_EditCost = 6;
		LinkedList<DFMPatch> l = diffMatchPatch.dFMPatch_make(xml1, xml2);

		return new SVSPatch<T>(diffMatchPatch.dFMPatch_toText(l));

		// return new Patch<T>()
	}

	public T patchWith(T object1, SVSPatch<T> patch) {
		String xml1 = getStringFor(object1);
		LinkedList<DFMPatch> patches = new LinkedList<DFMPatch>(
				diffMatchPatch.dFMPatch_fromText(patch.getPatch()));

		Object[] objects = diffMatchPatch.dFMPatch_apply(patches, xml1);
		boolean[] flags = (boolean[]) objects[1];
		for (boolean flag : flags) {
			if (flag == false) {
				// System.out.println("INFO cannot patch :\n"+patches.get(i)+"\n*********");

				// DEBUG PART
				FileWriter fdebug;
				try {
					fdebug = new FileWriter("debug.dmp");
					fdebug.write(object1.toString());
					fdebug.write("\n\nPATCH ************\n\n");
					fdebug.write(patch.getPatch().toString());
					fdebug.close();
					System.exit(-1);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		// System.out.println(objects[0].toString());
		// System.out.println(flags.length);
		return getObjectFromString(objects[0].toString());
	}

	public String getStringFor(T object) {
		ByteArrayOutputStream sw = new ByteArrayOutputStream();
		YamlEncoder xenc = new YamlEncoder(sw);
		// xenc.getConfig().setEncoding("UTF-8");
		// XMLEncoder xenc = new XMLEncoder(sw);

		xenc.writeObject(object);
		xenc.close();
		return sw.toString();
	}

	@SuppressWarnings("unchecked")
	public T getObjectFromString(String xml) {
		YamlDecoder xdec = new YamlDecoder(new ByteArrayInputStream(
				xml.getBytes()));
		// XMLDecoder xdec = new XMLDecoder(new
		// ByteArrayInputStream(xml.getBytes()));
		// xdec.getConfig().setEncoding("UTF-8");
		try {
			return (T) xdec.readObject();
		} catch (EOFException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	/**
	 * create hash for an object
	 * 
	 * @param object
	 * @return
	 */
	public String getHashFor(T object) {

		String stringToHash = getStringFor(object);

		byte[] source = stringToHash.getBytes();
		byte[] hash = null;
		try {
			hash = MessageDigest.getInstance("SHA1").digest(source);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		if (hash == null) {
			return null;
		}

		final StringBuilder hashString = new StringBuilder();
		for (byte element : hash) {
			String hex = Integer.toHexString(element);
			if (hex.length() == 1) {
				hashString.append('0');
				hashString.append(hex.charAt(hex.length() - 1));
			} else {
				hashString.append(hex.substring(hex.length() - 2));
			}
		}

		return hashString.toString();

	}

}
