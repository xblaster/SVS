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
package net.lo2k;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.lo2k.patcher.SVSPatch;
import net.lo2k.patcher.SVSPatcher;
import net.lo2k.repository.SVSRepository;
import net.lo2k.repository.SVSRepositoryImpl;

public class SVSSnapShotTest extends TestCase {

	public static Test suite() {
		return new TestSuite(SVSSnapShotTest.class);
	}

	public void testObject() {

		SVSPatcher<Person> patcher = new SVSPatcher<Person>();

		// first
		Person p = new Person();
		p.setName("Bob");
		p.setAge(17);
		p.setTel("1545645646");
		p.setAdress("3 rue du gymnase\n89245 Bidonville");

		// modified person
		Person p1 = new Person();
		p1.setName("Bob");
		p1.setAge(18);
		p1.setTel("33355566");
		p1.setAdress("3 rue du gymnase\n33333 Bidonville");

		// patch
		SVSPatch<Person> patch = patcher.makeSVSPatchFor(p, p1);

		// object to patch (slightly different)
		Person pToPatch = new Person();
		pToPatch.setName("Bob José");
		pToPatch.setAge(17);
		pToPatch.setTel("1545645646");
		pToPatch.setAdress("9 rue du gymnase\n89245 Bidonville");

		Person patchedPerson = patcher.patchWith(pToPatch, patch);

		assertEquals("33355566", patchedPerson.getTel());
		assertEquals(18, patchedPerson.getAge());
		assertEquals("9 rue du gymnase\n33333 Bidonville",
				patchedPerson.getAdress());
	}

	/**
	 * 
	 */
	public void testDiff() {
		SVSPatcher<String> patcher = new SVSPatcher<String>();
		SVSPatch<String> patch = patcher.makeSVSPatchFor("Ou est l'avion ?",
				"Ou est le bateau ?");

		patcher.patchWith("Ou est l'avion ? saperlipopette !", patch);
	}

	public void testListDiff() {
		SVSPatcher<LinkedList<String>> patcher = new SVSPatcher<LinkedList<String>>();

		LinkedList<String> list1 = new LinkedList<String>();
		list1.add("Tomate");
		list1.add("Robert");
		list1.add("Fish");
		list1.add("Poireaux");
		list1.add("Eyes");
		LinkedList<String> list2 = new LinkedList<String>();
		list2.add("Tomate");
		list2.add("Nicolas");
		list2.add("Fish");
		list2.add("Poireaux");
		list2.add("Gateau");

		SVSPatch<LinkedList<String>> patch = patcher.makeSVSPatchFor(list1,
				list2);

		LinkedList<String> listToPatch = new LinkedList<String>();
		listToPatch.add("Tomate");
		listToPatch.add("Samantha");
		listToPatch.add("Fish");
		listToPatch.add("Poireaux");
		listToPatch.add("Eyes");

		LinkedList<String> listPatched = patcher.patchWith(listToPatch, patch);
		assertEquals("Gateau", listPatched.get(4));
	}

	public String getStringFromUrl(String url) {
		URL webContent = null;
		try {
			webContent = new URL(url);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(
					webContent.openStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String inputLine = "";
		String totalLine = "";

		try {
			while ((inputLine = in.readLine()) != null) {
				totalLine += inputLine + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return totalLine;
	}

	public String randomModif(String string) {

		String[] randomReplace = { "cat", "dog", "horse", "funk", "lady gaga" };

		Random r = new Random();
		String res = string;
		for (int i = 0; i < r.nextInt(10) + 1; i++) {
			// get Random String
			int offset = r.nextInt(string.length() - 30);
			try {
				res = res.substring(0, offset)
						+ randomReplace[r.nextInt(randomReplace.length)]
						+ res.substring(offset, res.length() - 1);
				// String toReplace = res.substring(offset,
				// offset+r.nextInt(20)+1);
				// res = res.replace(toReplace,
				// randomReplace[r.nextInt(randomReplace.length)]);
			} catch (Exception e) {
				// nothing
			}

		}
		return res;
	}

	public void testVersionned() {
		SVSRepository<String> beacon = new SVSRepositoryImpl<String>();

		String totalString = getStringFromUrl("http://www.lo2k.net/v7/");

		String firstRev = beacon.makeSnapshot(totalString);
		String modifString = "";
		for (int i = 0; i < 10; i++) {
			System.out.print(i + " ");
			modifString = randomModif(totalString);
			beacon.makeSnapshot(modifString);
		}

		String orig = beacon.restoreSnapShot(firstRev);

		assertEquals(totalString, orig);

		// beacon.saveToFile(new File("dump.txt"));

		System.out.println("restored");
		System.out.println("Size :" + beacon.getSize());
		System.out.println("Original document size :" + orig.length());
	}

	/*
	 * public void testLoadFromFileIntensive() { String firstRev =
	 * "9d0c7a8a0dfd1d7c4f346ad43490ed54f2272324";
	 * 
	 * //load histo with 1000 modifs
	 * 
	 * SimpleVersioned<String> simpleVersioned = new SimpleVersioned<String>();
	 * Versioned<String> beacon = simpleVersioned.loadFromFile(new
	 * File("src/test/resources/dumpTest.chx")); String orig =
	 * beacon.restoreSnapShot(firstRev);
	 * 
	 * Patcher<String> patcher = new Patcher<String>();
	 * 
	 * //verify hash assertEquals(firstRev, patcher.getHashFor(orig)); }
	 */

	public void testPatch() {
		SVSRepository<String> beacon = new SVSRepositoryImpl<String>();

		String rev1 = beacon.makeSnapshot("Wow");
		String expanded = beacon.makeSnapshot("World of Warcraft");
		beacon.makeSnapshot("World of Warcraft\n2");
		String expandedWow3Hash = beacon.makeSnapshot("World of Warcraft\n3");
		beacon.makeSnapshot("Wow\n3");

		// create patch -Wow +World of Warcraft
		SVSPatch<String> patch = beacon.getSVSPatchBeetween(rev1, expanded);

		// try to apply it on "Wow 3"
		beacon.applyPatch(patch);
		assertEquals("World of Warcraft\n3", beacon.getLatestSnapshot());

		// verify that hash is identical to previous histo "World of Warcraft 3"
		assertEquals(expandedWow3Hash, beacon.getLatestRevNumber());
	}

	public void testDate() {
		SVSRepository<String> beacon = new SVSRepositoryImpl<String>();

		beacon.makeSnapshot("Wow");
		String expanded = beacon.makeSnapshot("World of Warcraft");

		Date d = new Date();

		beacon.makeSnapshot("World of Warcraft 34343");
		beacon.makeSnapshot("World of Warcraft 34343");
		beacon.makeSnapshot("World of Warcraft 3343433");

		assertEquals(expanded, beacon.getRevisionBefore(d));

		assertEquals(beacon.restoreSnapShot(expanded),
				beacon.restoreObjectBeforeDate(d));

	}

}