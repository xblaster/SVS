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

package net.lo2k.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
	public static String zip(String toCompress) {
		ZipOutputStream zos = null;
		try {

			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] ba = toCompress.getBytes();
			zos = new ZipOutputStream(out);
			zos.setLevel(5);
			zos.setMethod(ZipOutputStream.DEFLATED);
			final ZipEntry ze = new ZipEntry("result");
			ze.setSize(ba.length);
			zos.putNextEntry(ze);
			zos.write(ba);
			zos.closeEntry();
			zos.close();

			// res = out.toByteArray();
			return out.toString("ISO-8859-1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public static String unZip(final String result) {
		ByteArrayOutputStream fout = null;
		try {
			final ByteArrayInputStream in = new ByteArrayInputStream(
					result.getBytes("ISO-8859-1"));
			final ZipInputStream zin = new ZipInputStream(in);

			while ((zin.getNextEntry()) != null) {
				// System.out.println("Unzipping " + ze.getName());
				fout = new ByteArrayOutputStream();
				for (int c = zin.read(); c != -1; c = zin.read()) {
					fout.write(c);
				}
				zin.closeEntry();
				fout.close();
				return fout.toString();
			}
			zin.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}
}
