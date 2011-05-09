/*
 * Test harness for diff_match_patch.java
 *
 * Copyright 2006 Google Inc.
 * http://code.google.com/p/google-diff-match-patch/
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

package net.lo2k.thirdpart.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import net.lo2k.thirdpart.diff.DiffMatchPatch.DFMDiff;
import net.lo2k.thirdpart.diff.DiffMatchPatch.DFMPatch;
import net.lo2k.thirdpart.diff.DiffMatchPatch.LinesToCharsResult;

public class DiffMatchPatchTest extends TestCase {

	private DiffMatchPatch dmp;
	private DiffMatchPatch.Operation DELETE = DiffMatchPatch.Operation.DELETE;
	private DiffMatchPatch.Operation EQUAL = DiffMatchPatch.Operation.EQUAL;
	private DiffMatchPatch.Operation INSERT = DiffMatchPatch.Operation.INSERT;

	protected void setUp() {
		// Create an instance of the diff_match_patch object.
		dmp = new DiffMatchPatch();
	}

	// DIFF TEST FUNCTIONS

	public void testDiffCommonPrefix() {
		// Detect any common prefix.
		assertEquals("diff_commonPrefix: Null case.", 0,
				dmp.diff_commonPrefix("abc", "xyz"));

		assertEquals("diff_commonPrefix: Non-null case.", 4,
				dmp.diff_commonPrefix("1234abcdef", "1234xyz"));

		assertEquals("diff_commonPrefix: Whole case.", 4,
				dmp.diff_commonPrefix("1234", "1234xyz"));
	}

	public void testDiffCommonSuffix() {
		// Detect any common suffix.
		assertEquals("diff_commonSuffix: Null case.", 0,
				dmp.diff_commonSuffix("abc", "xyz"));

		assertEquals("diff_commonSuffix: Non-null case.", 4,
				dmp.diff_commonSuffix("abcdef1234", "xyz1234"));

		assertEquals("diff_commonSuffix: Whole case.", 4,
				dmp.diff_commonSuffix("1234", "xyz1234"));
	}

	public void testDiffCommonOverlap() {
		// Detect any suffix/prefix overlap.
		assertEquals("diff_commonOverlap: Null case.", 0,
				dmp.diff_commonOverlap("", "abcd"));

		assertEquals("diff_commonOverlap: Whole case.", 3,
				dmp.diff_commonOverlap("abc", "abcd"));

		assertEquals("diff_commonOverlap: No overlap.", 0,
				dmp.diff_commonOverlap("123456", "abcd"));

		assertEquals("diff_commonOverlap: Overlap.", 3,
				dmp.diff_commonOverlap("123456xxx", "xxxabcd"));
	}

	public void testDiffHalfmatch() {
		// Detect a halfmatch.
		dmp.Diff_Timeout = 1;
		assertNull("diff_halfMatch: No match #1.",
				dmp.diff_halfMatch("1234567890", "abcdef"));

		assertNull("diff_halfMatch: No match #2.",
				dmp.diff_halfMatch("12345", "23"));

		assertArrayEquals("diff_halfMatch: Single Match #1.", new String[] {
				"12", "90", "a", "z", "345678" },
				dmp.diff_halfMatch("1234567890", "a345678z"));

		assertArrayEquals("diff_halfMatch: Single Match #2.", new String[] {
				"a", "z", "12", "90", "345678" },
				dmp.diff_halfMatch("a345678z", "1234567890"));

		assertArrayEquals("diff_halfMatch: Single Match #3.", new String[] {
				"abc", "z", "1234", "0", "56789" },
				dmp.diff_halfMatch("abc56789z", "1234567890"));

		assertArrayEquals("diff_halfMatch: Single Match #4.", new String[] {
				"a", "xyz", "1", "7890", "23456" },
				dmp.diff_halfMatch("a23456xyz", "1234567890"));

		assertArrayEquals("diff_halfMatch: Multiple Matches #1.", new String[] {
				"12123", "123121", "a", "z", "1234123451234" },
				dmp.diff_halfMatch("121231234123451234123121",
						"a1234123451234z"));

		assertArrayEquals("diff_halfMatch: Multiple Matches #2.", new String[] {
				"", "-=-=-=-=-=", "x", "", "x-=-=-=-=-=-=-=" },
				dmp.diff_halfMatch("x-=-=-=-=-=-=-=-=-=-=-=-=",
						"xx-=-=-=-=-=-=-="));

		assertArrayEquals("diff_halfMatch: Multiple Matches #3.", new String[] {
				"-=-=-=-=-=", "", "", "y", "-=-=-=-=-=-=-=y" },
				dmp.diff_halfMatch("-=-=-=-=-=-=-=-=-=-=-=-=y",
						"-=-=-=-=-=-=-=yy"));

		// Optimal diff would be -q+x=H-i+e=lloHe+Hu=llo-Hew+y not
		// -qHillo+x=HelloHe-w+Hulloy
		assertArrayEquals("diff_halfMatch: Non-optimal halfmatch.",
				new String[] { "qHillo", "w", "x", "Hulloy", "HelloHe" },
				dmp.diff_halfMatch("qHilloHelloHew", "xHelloHeHulloy"));

		dmp.Diff_Timeout = 0;
		assertNull("diff_halfMatch: Optimal no halfmatch.",
				dmp.diff_halfMatch("qHilloHelloHew", "xHelloHeHulloy"));
	}

	public void testDiffLinesToChars() {
		// Convert lines down to characters.
		ArrayList<String> tmpVector = new ArrayList<String>();
		tmpVector.add("");
		tmpVector.add("alpha\n");
		tmpVector.add("beta\n");
		assertLinesToCharsResultEquals("diff_linesToChars:",
				new LinesToCharsResult("\u0001\u0002\u0001",
						"\u0002\u0001\u0002", tmpVector),
				dmp.diff_linesToChars("alpha\nbeta\nalpha\n",
						"beta\nalpha\nbeta\n"));

		tmpVector.clear();
		tmpVector.add("");
		tmpVector.add("alpha\r\n");
		tmpVector.add("beta\r\n");
		tmpVector.add("\r\n");
		assertLinesToCharsResultEquals("diff_linesToChars:",
				new LinesToCharsResult("", "\u0001\u0002\u0003\u0003",
						tmpVector), dmp.diff_linesToChars("",
						"alpha\r\nbeta\r\n\r\n\r\n"));

		tmpVector.clear();
		tmpVector.add("");
		tmpVector.add("a");
		tmpVector.add("b");
		assertLinesToCharsResultEquals("diff_linesToChars:",
				new LinesToCharsResult("\u0001", "\u0002", tmpVector),
				dmp.diff_linesToChars("a", "b"));

		// More than 256 to reveal any 8-bit limitations.
		int n = 300;
		tmpVector.clear();
		StringBuilder lineList = new StringBuilder();
		StringBuilder charList = new StringBuilder();
		for (int x = 1; x < n + 1; x++) {
			tmpVector.add(x + "\n");
			lineList.append(x + "\n");
			charList.append(String.valueOf((char) x));
		}
		assertEquals(n, tmpVector.size());
		String lines = lineList.toString();
		String chars = charList.toString();
		assertEquals(n, chars.length());
		tmpVector.add(0, "");
		assertLinesToCharsResultEquals("diff_linesToChars: More than 256.",
				new LinesToCharsResult(chars, "", tmpVector),
				dmp.diff_linesToChars(lines, ""));
	}

	public void testDiffCharsToLines() {
		// First check that Diff equality works.
		assertTrue("diff_charsToLines:",
				new DFMDiff(EQUAL, "a").equals(new DFMDiff(EQUAL, "a")));

		assertEquals("diff_charsToLines:", new DFMDiff(EQUAL, "a"),
				new DFMDiff(EQUAL, "a"));

		// Convert chars up to lines
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList(new DFMDiff(EQUAL,
				"\u0001\u0002\u0001"),
				new DFMDiff(INSERT, "\u0002\u0001\u0002"));
		ArrayList<String> tmpVector = new ArrayList<String>();
		tmpVector.add("");
		tmpVector.add("alpha\n");
		tmpVector.add("beta\n");
		dmp.dFMDiff_charsToLines(dFMDiffs, tmpVector);
		assertEquals(
				"diff_charsToLines:",
				dFMDiffList(new DFMDiff(EQUAL, "alpha\nbeta\nalpha\n"),
						new DFMDiff(INSERT, "beta\nalpha\nbeta\n")), dFMDiffs);

		// More than 256 to reveal any 8-bit limitations.
		int n = 300;
		tmpVector.clear();
		StringBuilder lineList = new StringBuilder();
		StringBuilder charList = new StringBuilder();
		for (int x = 1; x < n + 1; x++) {
			tmpVector.add(x + "\n");
			lineList.append(x + "\n");
			charList.append(String.valueOf((char) x));
		}
		assertEquals(n, tmpVector.size());
		String lines = lineList.toString();
		String chars = charList.toString();
		assertEquals(n, chars.length());
		tmpVector.add(0, "");
		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, chars));
		dmp.dFMDiff_charsToLines(dFMDiffs, tmpVector);
		assertEquals("diff_charsToLines: More than 256.",
				dFMDiffList(new DFMDiff(DELETE, lines)), dFMDiffs);
	}

	public void testDiffCleanupMerge() {
		// Cleanup a messy diff.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList();
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals("diff_cleanupMerge: Null case.", dFMDiffList(), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"),
				new DFMDiff(DELETE, "b"), new DFMDiff(INSERT, "c"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: No change case.",
				dFMDiffList(new DFMDiff(EQUAL, "a"), new DFMDiff(DELETE, "b"),
						new DFMDiff(INSERT, "c")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"),
				new DFMDiff(EQUAL, "b"), new DFMDiff(EQUAL, "c"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals("diff_cleanupMerge: Merge equalities.",
				dFMDiffList(new DFMDiff(EQUAL, "abc")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "a"), new DFMDiff(DELETE,
				"b"), new DFMDiff(DELETE, "c"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals("diff_cleanupMerge: Merge deletions.",
				dFMDiffList(new DFMDiff(DELETE, "abc")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(INSERT, "a"), new DFMDiff(INSERT,
				"b"), new DFMDiff(INSERT, "c"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals("diff_cleanupMerge: Merge insertions.",
				dFMDiffList(new DFMDiff(INSERT, "abc")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "a"), new DFMDiff(INSERT,
				"b"), new DFMDiff(DELETE, "c"), new DFMDiff(INSERT, "d"),
				new DFMDiff(EQUAL, "e"), new DFMDiff(EQUAL, "f"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: Merge interweave.",
				dFMDiffList(new DFMDiff(DELETE, "ac"),
						new DFMDiff(INSERT, "bd"), new DFMDiff(EQUAL, "ef")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "a"), new DFMDiff(INSERT,
				"abc"), new DFMDiff(DELETE, "dc"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: Prefix and suffix detection.",
				dFMDiffList(new DFMDiff(EQUAL, "a"), new DFMDiff(DELETE, "d"),
						new DFMDiff(INSERT, "b"), new DFMDiff(EQUAL, "c")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "x"),
				new DFMDiff(DELETE, "a"), new DFMDiff(INSERT, "abc"),
				new DFMDiff(DELETE, "dc"), new DFMDiff(EQUAL, "y"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: Prefix and suffix detection with equalities.",
				dFMDiffList(new DFMDiff(EQUAL, "xa"), new DFMDiff(DELETE, "d"),
						new DFMDiff(INSERT, "b"), new DFMDiff(EQUAL, "cy")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"), new DFMDiff(INSERT,
				"ba"), new DFMDiff(EQUAL, "c"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: Slide edit left.",
				dFMDiffList(new DFMDiff(INSERT, "ab"), new DFMDiff(EQUAL, "ac")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "c"), new DFMDiff(INSERT,
				"ab"), new DFMDiff(EQUAL, "a"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: Slide edit right.",
				dFMDiffList(new DFMDiff(EQUAL, "ca"), new DFMDiff(INSERT, "ba")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"),
				new DFMDiff(DELETE, "b"), new DFMDiff(EQUAL, "c"), new DFMDiff(
						DELETE, "ac"), new DFMDiff(EQUAL, "x"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: Slide edit left recursive.",
				dFMDiffList(new DFMDiff(DELETE, "abc"), new DFMDiff(EQUAL,
						"acx")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "x"), new DFMDiff(DELETE,
				"ca"), new DFMDiff(EQUAL, "c"), new DFMDiff(DELETE, "b"),
				new DFMDiff(EQUAL, "a"));
		dmp.dFMDiff_cleanupMerge(dFMDiffs);
		assertEquals(
				"diff_cleanupMerge: Slide edit right recursive.",
				dFMDiffList(new DFMDiff(EQUAL, "xca"), new DFMDiff(DELETE,
						"cba")), dFMDiffs);
	}

	public void testDiffCleanupSemanticLossless() {
		// Slide diffs to match logical boundaries.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList();
		dmp.dFMDiff_cleanupSemanticLossless(dFMDiffs);
		assertEquals("diff_cleanupSemanticLossless: Null case.", dFMDiffList(),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "AAA\r\n\r\nBBB"),
				new DFMDiff(INSERT, "\r\nDDD\r\n\r\nBBB"), new DFMDiff(EQUAL,
						"\r\nEEE"));
		dmp.dFMDiff_cleanupSemanticLossless(dFMDiffs);
		assertEquals(
				"diff_cleanupSemanticLossless: Blank lines.",
				dFMDiffList(new DFMDiff(EQUAL, "AAA\r\n\r\n"), new DFMDiff(
						INSERT, "BBB\r\nDDD\r\n\r\n"), new DFMDiff(EQUAL,
						"BBB\r\nEEE")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "AAA\r\nBBB"), new DFMDiff(
				INSERT, " DDD\r\nBBB"), new DFMDiff(EQUAL, " EEE"));
		dmp.dFMDiff_cleanupSemanticLossless(dFMDiffs);
		assertEquals(
				"diff_cleanupSemanticLossless: Line boundaries.",
				dFMDiffList(new DFMDiff(EQUAL, "AAA\r\n"), new DFMDiff(INSERT,
						"BBB DDD\r\n"), new DFMDiff(EQUAL, "BBB EEE")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "The c"), new DFMDiff(INSERT,
				"ow and the c"), new DFMDiff(EQUAL, "at."));
		dmp.dFMDiff_cleanupSemanticLossless(dFMDiffs);
		assertEquals(
				"diff_cleanupSemanticLossless: Word boundaries.",
				dFMDiffList(new DFMDiff(EQUAL, "The "), new DFMDiff(INSERT,
						"cow and the "), new DFMDiff(EQUAL, "cat.")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "The-c"), new DFMDiff(INSERT,
				"ow-and-the-c"), new DFMDiff(EQUAL, "at."));
		dmp.dFMDiff_cleanupSemanticLossless(dFMDiffs);
		assertEquals(
				"diff_cleanupSemanticLossless: Alphanumeric boundaries.",
				dFMDiffList(new DFMDiff(EQUAL, "The-"), new DFMDiff(INSERT,
						"cow-and-the-"), new DFMDiff(EQUAL, "cat.")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"),
				new DFMDiff(DELETE, "a"), new DFMDiff(EQUAL, "ax"));
		dmp.dFMDiff_cleanupSemanticLossless(dFMDiffs);
		assertEquals(
				"diff_cleanupSemanticLossless: Hitting the start.",
				dFMDiffList(new DFMDiff(DELETE, "a"), new DFMDiff(EQUAL, "aax")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "xa"), new DFMDiff(DELETE,
				"a"), new DFMDiff(EQUAL, "a"));
		dmp.dFMDiff_cleanupSemanticLossless(dFMDiffs);
		assertEquals(
				"diff_cleanupSemanticLossless: Hitting the end.",
				dFMDiffList(new DFMDiff(EQUAL, "xaa"), new DFMDiff(DELETE, "a")),
				dFMDiffs);
	}

	public void testDiffCleanupSemantic() {
		// Cleanup semantically trivial equalities.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList();
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals("diff_cleanupSemantic: Null case.", dFMDiffList(),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "ab"), new DFMDiff(INSERT,
				"cd"), new DFMDiff(EQUAL, "12"), new DFMDiff(DELETE, "e"));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: No elimination #1.",
				dFMDiffList(new DFMDiff(DELETE, "ab"),
						new DFMDiff(INSERT, "cd"), new DFMDiff(EQUAL, "12"),
						new DFMDiff(DELETE, "e")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "abc"), new DFMDiff(INSERT,
				"ABC"), new DFMDiff(EQUAL, "1234"), new DFMDiff(DELETE, "wxyz"));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: No elimination #2.",
				dFMDiffList(new DFMDiff(DELETE, "abc"), new DFMDiff(INSERT,
						"ABC"), new DFMDiff(EQUAL, "1234"), new DFMDiff(DELETE,
						"wxyz")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "a"),
				new DFMDiff(EQUAL, "b"), new DFMDiff(DELETE, "c"));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: Simple elimination.",
				dFMDiffList(new DFMDiff(DELETE, "abc"),
						new DFMDiff(INSERT, "b")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "ab"), new DFMDiff(EQUAL,
				"cd"), new DFMDiff(DELETE, "e"), new DFMDiff(EQUAL, "f"),
				new DFMDiff(INSERT, "g"));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: Backpass elimination.",
				dFMDiffList(new DFMDiff(DELETE, "abcdef"), new DFMDiff(INSERT,
						"cdfg")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(INSERT, "1"),
				new DFMDiff(EQUAL, "A"), new DFMDiff(DELETE, "B"), new DFMDiff(
						INSERT, "2"), new DFMDiff(EQUAL, "_"), new DFMDiff(
						INSERT, "1"), new DFMDiff(EQUAL, "A"), new DFMDiff(
						DELETE, "B"), new DFMDiff(INSERT, "2"));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: Multiple elimination.",
				dFMDiffList(new DFMDiff(DELETE, "AB_AB"), new DFMDiff(INSERT,
						"1A2_1A2")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "The c"), new DFMDiff(DELETE,
				"ow and the c"), new DFMDiff(EQUAL, "at."));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: Word boundaries.",
				dFMDiffList(new DFMDiff(EQUAL, "The "), new DFMDiff(DELETE,
						"cow and the "), new DFMDiff(EQUAL, "cat.")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "abcxx"), new DFMDiff(
				INSERT, "xxdef"));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: Overlap elimination #1.",
				dFMDiffList(new DFMDiff(DELETE, "abc"),
						new DFMDiff(EQUAL, "xx"), new DFMDiff(INSERT, "def")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "abcxx"), new DFMDiff(
				INSERT, "xxdef"), new DFMDiff(DELETE, "ABCXX"), new DFMDiff(
				INSERT, "XXDEF"));
		dmp.dFMDiff_cleanupSemantic(dFMDiffs);
		assertEquals(
				"diff_cleanupSemantic: Overlap elimination #2.",
				dFMDiffList(new DFMDiff(DELETE, "abc"),
						new DFMDiff(EQUAL, "xx"), new DFMDiff(INSERT, "def"),
						new DFMDiff(DELETE, "ABC"), new DFMDiff(EQUAL, "XX"),
						new DFMDiff(INSERT, "DEF")), dFMDiffs);
	}

	public void testDiffCleanupEfficiency() {
		// Cleanup operationally trivial equalities.
		dmp.Diff_EditCost = 4;
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList();
		dmp.dFMDiff_cleanupEfficiency(dFMDiffs);
		assertEquals("diff_cleanupEfficiency: Null case.", dFMDiffList(),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "ab"), new DFMDiff(INSERT,
				"12"), new DFMDiff(EQUAL, "wxyz"), new DFMDiff(DELETE, "cd"),
				new DFMDiff(INSERT, "34"));
		dmp.dFMDiff_cleanupEfficiency(dFMDiffs);
		assertEquals(
				"diff_cleanupEfficiency: No elimination.",
				dFMDiffList(new DFMDiff(DELETE, "ab"),
						new DFMDiff(INSERT, "12"), new DFMDiff(EQUAL, "wxyz"),
						new DFMDiff(DELETE, "cd"), new DFMDiff(INSERT, "34")),
				dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "ab"), new DFMDiff(INSERT,
				"12"), new DFMDiff(EQUAL, "xyz"), new DFMDiff(DELETE, "cd"),
				new DFMDiff(INSERT, "34"));
		dmp.dFMDiff_cleanupEfficiency(dFMDiffs);
		assertEquals(
				"diff_cleanupEfficiency: Four-edit elimination.",
				dFMDiffList(new DFMDiff(DELETE, "abxyzcd"), new DFMDiff(INSERT,
						"12xyz34")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(INSERT, "12"), new DFMDiff(EQUAL,
				"x"), new DFMDiff(DELETE, "cd"), new DFMDiff(INSERT, "34"));
		dmp.dFMDiff_cleanupEfficiency(dFMDiffs);
		assertEquals(
				"diff_cleanupEfficiency: Three-edit elimination.",
				dFMDiffList(new DFMDiff(DELETE, "xcd"), new DFMDiff(INSERT,
						"12x34")), dFMDiffs);

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "ab"), new DFMDiff(INSERT,
				"12"), new DFMDiff(EQUAL, "xy"), new DFMDiff(INSERT, "34"),
				new DFMDiff(EQUAL, "z"), new DFMDiff(DELETE, "cd"),
				new DFMDiff(INSERT, "56"));
		dmp.dFMDiff_cleanupEfficiency(dFMDiffs);
		assertEquals(
				"diff_cleanupEfficiency: Backpass elimination.",
				dFMDiffList(new DFMDiff(DELETE, "abxyzcd"), new DFMDiff(INSERT,
						"12xy34z56")), dFMDiffs);

		dmp.Diff_EditCost = 5;
		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "ab"), new DFMDiff(INSERT,
				"12"), new DFMDiff(EQUAL, "wxyz"), new DFMDiff(DELETE, "cd"),
				new DFMDiff(INSERT, "34"));
		dmp.dFMDiff_cleanupEfficiency(dFMDiffs);
		assertEquals(
				"diff_cleanupEfficiency: High cost elimination.",
				dFMDiffList(new DFMDiff(DELETE, "abwxyzcd"), new DFMDiff(
						INSERT, "12wxyz34")), dFMDiffs);
		dmp.Diff_EditCost = 4;
	}

	public void testDiffPrettyHtml() {
		// Pretty print.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a\n"),
				new DFMDiff(DELETE, "<B>b</B>"), new DFMDiff(INSERT, "c&d"));
		assertEquals(
				"diff_prettyHtml:",
				"<span>a&para;<br></span><del style=\"background:#ffe6e6;\">&lt;B&gt;b&lt;/B&gt;</del><ins style=\"background:#e6ffe6;\">c&amp;d</ins>",
				dmp.dFMDiff_prettyHtml(dFMDiffs));
	}

	public void testDiffText() {
		// Compute the source and destination texts.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "jump"),
				new DFMDiff(DELETE, "s"), new DFMDiff(INSERT, "ed"),
				new DFMDiff(EQUAL, " over "), new DFMDiff(DELETE, "the"),
				new DFMDiff(INSERT, "a"), new DFMDiff(EQUAL, " lazy"));
		assertEquals("diff_text1:", "jumps over the lazy",
				dmp.dFMDiff_text1(dFMDiffs));
		assertEquals("diff_text2:", "jumped over a lazy",
				dmp.dFMDiff_text2(dFMDiffs));
	}

	public void testDiffDelta() {
		// Convert a diff into delta string.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "jump"),
				new DFMDiff(DELETE, "s"), new DFMDiff(INSERT, "ed"),
				new DFMDiff(EQUAL, " over "), new DFMDiff(DELETE, "the"),
				new DFMDiff(INSERT, "a"), new DFMDiff(EQUAL, " lazy"),
				new DFMDiff(INSERT, "old dog"));
		String text1 = dmp.dFMDiff_text1(dFMDiffs);
		assertEquals("diff_text1: Base text.", "jumps over the lazy", text1);

		String delta = dmp.dFMDiff_toDelta(dFMDiffs);
		assertEquals("diff_toDelta:", "=4\t-1\t+ed\t=6\t-3\t+a\t=5\t+old dog",
				delta);

		// Convert delta string into a diff.
		assertEquals("diff_fromDelta: Normal.", dFMDiffs,
				dmp.dFMDiff_fromDelta(text1, delta));

		// Generates error (19 < 20).
		try {
			dmp.dFMDiff_fromDelta(text1 + "x", delta);
			fail("diff_fromDelta: Too long.");
		} catch (IllegalArgumentException ex) {
			// Exception expected.
		}

		// Generates error (19 > 18).
		try {
			dmp.dFMDiff_fromDelta(text1.substring(1), delta);
			fail("diff_fromDelta: Too short.");
		} catch (IllegalArgumentException ex) {
			// Exception expected.
		}

		// Generates error (%c3%xy invalid Unicode).
		try {
			dmp.dFMDiff_fromDelta("", "+%c3%xy");
			fail("diff_fromDelta: Invalid character.");
		} catch (IllegalArgumentException ex) {
			// Exception expected.
		}

		// Test deltas with special characters.
		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "\u0680 \000 \t %"),
				new DFMDiff(DELETE, "\u0681 \001 \n ^"), new DFMDiff(INSERT,
						"\u0682 \002 \\ |"));
		text1 = dmp.dFMDiff_text1(dFMDiffs);
		assertEquals("diff_text1: Unicode text.",
				"\u0680 \000 \t %\u0681 \001 \n ^", text1);

		delta = dmp.dFMDiff_toDelta(dFMDiffs);
		assertEquals("diff_toDelta: Unicode.", "=7\t-7\t+%DA%82 %02 %5C %7C",
				delta);

		assertEquals("diff_fromDelta: Unicode.", dFMDiffs,
				dmp.dFMDiff_fromDelta(text1, delta));

		// Verify pool of unchanged characters.
		dFMDiffs = dFMDiffList(new DFMDiff(INSERT,
				"A-Z a-z 0-9 - _ . ! ~ * ' ( ) ; / ? : @ & = + $ , # "));
		String text2 = dmp.dFMDiff_text2(dFMDiffs);
		assertEquals("diff_text2: Unchanged characters.",
				"A-Z a-z 0-9 - _ . ! ~ * \' ( ) ; / ? : @ & = + $ , # ", text2);

		delta = dmp.dFMDiff_toDelta(dFMDiffs);
		assertEquals("diff_toDelta: Unchanged characters.",
				"+A-Z a-z 0-9 - _ . ! ~ * \' ( ) ; / ? : @ & = + $ , # ", delta);

		// Convert delta string into a diff.
		assertEquals("diff_fromDelta: Unchanged characters.", dFMDiffs,
				dmp.dFMDiff_fromDelta("", delta));
	}

	public void testDiffXIndex() {
		// Translate a location in text1 to text2.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "a"),
				new DFMDiff(INSERT, "1234"), new DFMDiff(EQUAL, "xyz"));
		assertEquals("diff_xIndex: Translation on equality.", 5,
				dmp.dFMDiff_xIndex(dFMDiffs, 2));

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"), new DFMDiff(DELETE,
				"1234"), new DFMDiff(EQUAL, "xyz"));
		assertEquals("diff_xIndex: Translation on deletion.", 1,
				dmp.dFMDiff_xIndex(dFMDiffs, 3));
	}

	public void testDiffLevenshtein() {
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "abc"),
				new DFMDiff(INSERT, "1234"), new DFMDiff(EQUAL, "xyz"));
		assertEquals("Levenshtein with trailing equality.", 4,
				dmp.dFMDiff_levenshtein(dFMDiffs));

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "xyz"), new DFMDiff(DELETE,
				"abc"), new DFMDiff(INSERT, "1234"));
		assertEquals("Levenshtein with leading equality.", 4,
				dmp.dFMDiff_levenshtein(dFMDiffs));

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "abc"), new DFMDiff(EQUAL,
				"xyz"), new DFMDiff(INSERT, "1234"));
		assertEquals("Levenshtein with middle equality.", 7,
				dmp.dFMDiff_levenshtein(dFMDiffs));
	}

	public void testDiffBisect() {
		// Normal.
		String a = "cat";
		String b = "map";
		// Since the resulting diff hasn't been normalized, it would be ok if
		// the insertion and deletion pairs are swapped.
		// If the order changes, tweak this test as required.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "c"),
				new DFMDiff(INSERT, "m"), new DFMDiff(EQUAL, "a"), new DFMDiff(
						DELETE, "t"), new DFMDiff(INSERT, "p"));
		assertEquals("diff_bisect: Normal.", dFMDiffs,
				dmp.dFMDiff_bisect(a, b, Long.MAX_VALUE));

		// Timeout.
		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "cat"), new DFMDiff(INSERT,
				"map"));
		assertEquals("diff_bisect: Timeout.", dFMDiffs,
				dmp.dFMDiff_bisect(a, b, 0));
	}

	public void testDiffMain() {
		// Perform a trivial diff.
		LinkedList<DFMDiff> dFMDiffs = dFMDiffList();
		assertEquals("diff_main: Null case.", dFMDiffs,
				dmp.dFMDiff_main("", "", false));

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "abc"));
		assertEquals("diff_main: Equality.", dFMDiffs,
				dmp.dFMDiff_main("abc", "abc", false));

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "ab"), new DFMDiff(INSERT,
				"123"), new DFMDiff(EQUAL, "c"));
		assertEquals("diff_main: Simple insertion.", dFMDiffs,
				dmp.dFMDiff_main("abc", "ab123c", false));

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"), new DFMDiff(DELETE,
				"123"), new DFMDiff(EQUAL, "bc"));
		assertEquals("diff_main: Simple deletion.", dFMDiffs,
				dmp.dFMDiff_main("a123bc", "abc", false));

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"), new DFMDiff(INSERT,
				"123"), new DFMDiff(EQUAL, "b"), new DFMDiff(INSERT, "456"),
				new DFMDiff(EQUAL, "c"));
		assertEquals("diff_main: Two insertions.", dFMDiffs,
				dmp.dFMDiff_main("abc", "a123b456c", false));

		dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "a"), new DFMDiff(DELETE,
				"123"), new DFMDiff(EQUAL, "b"), new DFMDiff(DELETE, "456"),
				new DFMDiff(EQUAL, "c"));
		assertEquals("diff_main: Two deletions.", dFMDiffs,
				dmp.dFMDiff_main("a123b456c", "abc", false));

		// Perform a real diff.
		// Switch off the timeout.
		dmp.Diff_Timeout = 0;
		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "a"), new DFMDiff(INSERT,
				"b"));
		assertEquals("diff_main: Simple case #1.", dFMDiffs,
				dmp.dFMDiff_main("a", "b", false));

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "Apple"), new DFMDiff(
				INSERT, "Banana"), new DFMDiff(EQUAL, "s are a"), new DFMDiff(
				INSERT, "lso"), new DFMDiff(EQUAL, " fruit."));
		assertEquals("diff_main: Simple case #2.", dFMDiffs, dmp.dFMDiff_main(
				"Apples are a fruit.", "Bananas are also fruit.", false));

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "a"), new DFMDiff(INSERT,
				"\u0680"), new DFMDiff(EQUAL, "x"), new DFMDiff(DELETE, "\t"),
				new DFMDiff(INSERT, "\000"));
		assertEquals("diff_main: Simple case #3.", dFMDiffs,
				dmp.dFMDiff_main("ax\t", "\u0680x\000", false));

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "1"),
				new DFMDiff(EQUAL, "a"), new DFMDiff(DELETE, "y"), new DFMDiff(
						EQUAL, "b"), new DFMDiff(DELETE, "2"), new DFMDiff(
						INSERT, "xab"));
		assertEquals("diff_main: Overlap #1.", dFMDiffs,
				dmp.dFMDiff_main("1ayb2", "abxab", false));

		dFMDiffs = dFMDiffList(new DFMDiff(INSERT, "xaxcx"), new DFMDiff(EQUAL,
				"abc"), new DFMDiff(DELETE, "y"));
		assertEquals("diff_main: Overlap #2.", dFMDiffs,
				dmp.dFMDiff_main("abcy", "xaxcxabc", false));

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "ABCD"), new DFMDiff(EQUAL,
				"a"), new DFMDiff(DELETE, "="), new DFMDiff(INSERT, "-"),
				new DFMDiff(EQUAL, "bcd"), new DFMDiff(DELETE, "="),
				new DFMDiff(INSERT, "-"),
				new DFMDiff(EQUAL, "efghijklmnopqrs"), new DFMDiff(DELETE,
						"EFGHIJKLMNOefg"));
		assertEquals("diff_main: Overlap #3.", dFMDiffs, dmp.dFMDiff_main(
				"ABCDa=bcd=efghijklmnopqrsEFGHIJKLMNOefg",
				"a-bcd-efghijklmnopqrs", false));

		dFMDiffs = dFMDiffList(new DFMDiff(INSERT, " "),
				new DFMDiff(EQUAL, "a"), new DFMDiff(INSERT, "nd"),
				new DFMDiff(EQUAL, " [[Pennsylvania]]"), new DFMDiff(DELETE,
						" and [[New"));
		assertEquals("diff_main: Large equality.", dFMDiffs, dmp.dFMDiff_main(
				"a [[Pennsylvania]] and [[New", " and [[Pennsylvania]]", false));

		dmp.Diff_Timeout = 0.1f; // 100ms
		String a = "`Twas brillig, and the slithy toves\nDid gyre and gimble in the wabe:\nAll mimsy were the borogoves,\nAnd the mome raths outgrabe.\n";
		String b = "I am the very model of a modern major general,\nI've information vegetable, animal, and mineral,\nI know the kings of England, and I quote the fights historical,\nFrom Marathon to Waterloo, in order categorical.\n";
		// Increase the text lengths by 1024 times to ensure a timeout.
		for (int x = 0; x < 10; x++) {
			a = a + a;
			b = b + b;
		}
		long startTime = System.currentTimeMillis();
		dmp.dFMDiff_main(a, b);
		long endTime = System.currentTimeMillis();
		// Test that we took at least the timeout period.
		assertTrue("diff_main: Timeout min.",
				dmp.Diff_Timeout * 1000 <= endTime - startTime);
		// Test that we didn't take forever (be forgiving).
		// Theoretically this test could fail very occasionally if the
		// OS task swaps or locks up for a second at the wrong moment.
		assertTrue("diff_main: Timeout max.",
				dmp.Diff_Timeout * 1000 * 2 > endTime - startTime);
		dmp.Diff_Timeout = 0;

		// Test the linemode speedup.
		// Must be long to pass the 100 char cutoff.
		a = "1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n";
		b = "abcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\nabcdefghij\n";
		assertEquals("diff_main: Simple line-mode.",
				dmp.dFMDiff_main(a, b, true), dmp.dFMDiff_main(a, b, false));

		a = "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890";
		b = "abcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghijabcdefghij";
		assertEquals("diff_main: Single line-mode.",
				dmp.dFMDiff_main(a, b, true), dmp.dFMDiff_main(a, b, false));

		a = "1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n1234567890\n";
		b = "abcdefghij\n1234567890\n1234567890\n1234567890\nabcdefghij\n1234567890\n1234567890\n1234567890\nabcdefghij\n1234567890\n1234567890\n1234567890\nabcdefghij\n";
		String[] texts_linemode = dFMDiff_rebuildtexts(dmp.dFMDiff_main(a, b,
				true));
		String[] texts_textmode = dFMDiff_rebuildtexts(dmp.dFMDiff_main(a, b,
				false));
		assertArrayEquals("diff_main: Overlap line-mode.", texts_textmode,
				texts_linemode);

		// Test null inputs.
		try {
			dmp.dFMDiff_main(null, null);
			fail("diff_main: Null inputs.");
		} catch (IllegalArgumentException ex) {
			// Error expected.
		}
	}

	// MATCH TEST FUNCTIONS

	public void testMatchAlphabet() {
		// Initialise the bitmasks for Bitap.
		Map<Character, Integer> bitmask;
		bitmask = new HashMap<Character, Integer>();
		bitmask.put('a', 4);
		bitmask.put('b', 2);
		bitmask.put('c', 1);
		assertEquals("match_alphabet: Unique.", bitmask,
				dmp.match_alphabet("abc"));

		bitmask = new HashMap<Character, Integer>();
		bitmask.put('a', 37);
		bitmask.put('b', 18);
		bitmask.put('c', 8);
		assertEquals("match_alphabet: Duplicates.", bitmask,
				dmp.match_alphabet("abcaba"));
	}

	public void testMatchBitap() {
		// Bitap algorithm.
		dmp.Match_Distance = 100;
		dmp.Match_Threshold = 0.5f;
		assertEquals("match_bitap: Exact match #1.", 5,
				dmp.match_bitap("abcdefghijk", "fgh", 5));

		assertEquals("match_bitap: Exact match #2.", 5,
				dmp.match_bitap("abcdefghijk", "fgh", 0));

		assertEquals("match_bitap: Fuzzy match #1.", 4,
				dmp.match_bitap("abcdefghijk", "efxhi", 0));

		assertEquals("match_bitap: Fuzzy match #2.", 2,
				dmp.match_bitap("abcdefghijk", "cdefxyhijk", 5));

		assertEquals("match_bitap: Fuzzy match #3.", -1,
				dmp.match_bitap("abcdefghijk", "bxy", 1));

		assertEquals("match_bitap: Overflow.", 2,
				dmp.match_bitap("123456789xx0", "3456789x0", 2));

		assertEquals("match_bitap: Before start match.", 0,
				dmp.match_bitap("abcdef", "xxabc", 4));

		assertEquals("match_bitap: Beyond end match.", 3,
				dmp.match_bitap("abcdef", "defyy", 4));

		assertEquals("match_bitap: Oversized pattern.", 0,
				dmp.match_bitap("abcdef", "xabcdefy", 0));

		dmp.Match_Threshold = 0.4f;
		assertEquals("match_bitap: Threshold #1.", 4,
				dmp.match_bitap("abcdefghijk", "efxyhi", 1));

		dmp.Match_Threshold = 0.3f;
		assertEquals("match_bitap: Threshold #2.", -1,
				dmp.match_bitap("abcdefghijk", "efxyhi", 1));

		dmp.Match_Threshold = 0.0f;
		assertEquals("match_bitap: Threshold #3.", 1,
				dmp.match_bitap("abcdefghijk", "bcdef", 1));

		dmp.Match_Threshold = 0.5f;
		assertEquals("match_bitap: Multiple select #1.", 0,
				dmp.match_bitap("abcdexyzabcde", "abccde", 3));

		assertEquals("match_bitap: Multiple select #2.", 8,
				dmp.match_bitap("abcdexyzabcde", "abccde", 5));

		dmp.Match_Distance = 10; // Strict location.
		assertEquals("match_bitap: Distance test #1.", -1,
				dmp.match_bitap("abcdefghijklmnopqrstuvwxyz", "abcdefg", 24));

		assertEquals("match_bitap: Distance test #2.", 0,
				dmp.match_bitap("abcdefghijklmnopqrstuvwxyz", "abcdxxefg", 1));

		dmp.Match_Distance = 1000; // Loose location.
		assertEquals("match_bitap: Distance test #3.", 0,
				dmp.match_bitap("abcdefghijklmnopqrstuvwxyz", "abcdefg", 24));
	}

	public void testMatchMain() {
		// Full match.
		assertEquals("match_main: Equality.", 0,
				dmp.match_main("abcdef", "abcdef", 1000));

		assertEquals("match_main: Null text.", -1,
				dmp.match_main("", "abcdef", 1));

		assertEquals("match_main: Null pattern.", 3,
				dmp.match_main("abcdef", "", 3));

		assertEquals("match_main: Exact match.", 3,
				dmp.match_main("abcdef", "de", 3));

		assertEquals("match_main: Beyond end match.", 3,
				dmp.match_main("abcdef", "defy", 4));

		assertEquals("match_main: Oversized pattern.", 0,
				dmp.match_main("abcdef", "abcdefy", 0));

		dmp.Match_Threshold = 0.7f;
		assertEquals("match_main: Complex match.", 4, dmp.match_main(
				"I am the very model of a modern major general.",
				" that berry ", 5));
		dmp.Match_Threshold = 0.5f;

		// Test null inputs.
		try {
			dmp.match_main(null, null, 0);
			fail("match_main: Null inputs.");
		} catch (IllegalArgumentException ex) {
			// Error expected.
		}
	}

	// PATCH TEST FUNCTIONS

	public void testPatchObj() {
		// Patch Object.
		DFMPatch p = new DFMPatch();
		p.start1 = 20;
		p.start2 = 21;
		p.length1 = 18;
		p.length2 = 17;
		p.dFMDiffs = dFMDiffList(new DFMDiff(EQUAL, "jump"), new DFMDiff(
				DELETE, "s"), new DFMDiff(INSERT, "ed"), new DFMDiff(EQUAL,
				" over "), new DFMDiff(DELETE, "the"),
				new DFMDiff(INSERT, "a"), new DFMDiff(EQUAL, "\nlaz"));
		String strp = "@@ -21,18 +22,17 @@\n jump\n-s\n+ed\n  over \n-the\n+a\n %0Alaz\n";
		assertEquals("Patch: toString.", strp, p.toString());
	}

	public void testPatchFromText() {
		assertTrue("patch_fromText: #0.", dmp.dFMPatch_fromText("").isEmpty());

		String strp = "@@ -21,18 +22,17 @@\n jump\n-s\n+ed\n  over \n-the\n+a\n %0Alaz\n";
		assertEquals("patch_fromText: #1.", strp, dmp.dFMPatch_fromText(strp)
				.get(0).toString());

		assertEquals("patch_fromText: #2.", "@@ -1 +1 @@\n-a\n+b\n", dmp
				.dFMPatch_fromText("@@ -1 +1 @@\n-a\n+b\n").get(0).toString());

		assertEquals("patch_fromText: #3.", "@@ -1,3 +0,0 @@\n-abc\n", dmp
				.dFMPatch_fromText("@@ -1,3 +0,0 @@\n-abc\n").get(0).toString());

		assertEquals("patch_fromText: #4.", "@@ -0,0 +1,3 @@\n+abc\n", dmp
				.dFMPatch_fromText("@@ -0,0 +1,3 @@\n+abc\n").get(0).toString());

		// Generates error.
		try {
			dmp.dFMPatch_fromText("Bad\nPatch\n");
			fail("patch_fromText: #5.");
		} catch (IllegalArgumentException ex) {
			// Exception expected.
		}
	}

	public void testPatchToText() {
		String strp = "@@ -21,18 +22,17 @@\n jump\n-s\n+ed\n  over \n-the\n+a\n  laz\n";
		List<DFMPatch> patches;
		patches = dmp.dFMPatch_fromText(strp);
		assertEquals("patch_toText: Single.", strp,
				dmp.dFMPatch_toText(patches));

		strp = "@@ -1,9 +1,9 @@\n-f\n+F\n oo+fooba\n@@ -7,9 +7,9 @@\n obar\n-,\n+.\n  tes\n";
		patches = dmp.dFMPatch_fromText(strp);
		assertEquals("patch_toText: Dual.", strp, dmp.dFMPatch_toText(patches));
	}

	public void testPatchAddContext() {
		dmp.Patch_Margin = 4;
		DFMPatch p;
		p = dmp.dFMPatch_fromText("@@ -21,4 +21,10 @@\n-jump\n+somersault\n")
				.get(0);
		dmp.dFMPatch_addContext(p,
				"The quick brown fox jumps over the lazy dog.");
		assertEquals("patch_addContext: Simple case.",
				"@@ -17,12 +17,18 @@\n fox \n-jump\n+somersault\n s ov\n",
				p.toString());

		p = dmp.dFMPatch_fromText("@@ -21,4 +21,10 @@\n-jump\n+somersault\n")
				.get(0);
		dmp.dFMPatch_addContext(p, "The quick brown fox jumps.");
		assertEquals("patch_addContext: Not enough trailing context.",
				"@@ -17,10 +17,16 @@\n fox \n-jump\n+somersault\n s.\n",
				p.toString());

		p = dmp.dFMPatch_fromText("@@ -3 +3,2 @@\n-e\n+at\n").get(0);
		dmp.dFMPatch_addContext(p, "The quick brown fox jumps.");
		assertEquals("patch_addContext: Not enough leading context.",
				"@@ -1,7 +1,8 @@\n Th\n-e\n+at\n  qui\n", p.toString());

		p = dmp.dFMPatch_fromText("@@ -3 +3,2 @@\n-e\n+at\n").get(0);
		dmp.dFMPatch_addContext(p,
				"The quick brown fox jumps.  The quick brown fox crashes.");
		assertEquals("patch_addContext: Ambiguity.",
				"@@ -1,27 +1,28 @@\n Th\n-e\n+at\n  quick brown fox jumps. \n",
				p.toString());
	}

	@SuppressWarnings("deprecation")
	public void testPatchMake() {
		LinkedList<DFMPatch> patches;
		patches = dmp.dFMPatch_make("", "");
		assertEquals("patch_make: Null case.", "", dmp.dFMPatch_toText(patches));

		String text1 = "The quick brown fox jumps over the lazy dog.";
		String text2 = "That quick brown fox jumped over a lazy dog.";
		String expectedPatch = "@@ -1,8 +1,7 @@\n Th\n-at\n+e\n  qui\n@@ -21,17 +21,18 @@\n jump\n-ed\n+s\n  over \n-a\n+the\n  laz\n";
		// The second patch must be "-21,17 +21,18", not "-22,17 +21,18" due to
		// rolling context.
		patches = dmp.dFMPatch_make(text2, text1);
		assertEquals("patch_make: Text2+Text1 inputs.", expectedPatch,
				dmp.dFMPatch_toText(patches));

		expectedPatch = "@@ -1,11 +1,12 @@\n Th\n-e\n+at\n  quick b\n@@ -22,18 +22,17 @@\n jump\n-s\n+ed\n  over \n-the\n+a\n  laz\n";
		patches = dmp.dFMPatch_make(text1, text2);
		assertEquals("patch_make: Text1+Text2 inputs.", expectedPatch,
				dmp.dFMPatch_toText(patches));

		LinkedList<DFMDiff> dFMDiffs = dmp.dFMDiff_main(text1, text2, false);
		patches = dmp.dFMPatch_make(dFMDiffs);
		assertEquals("patch_make: Diff input.", expectedPatch,
				dmp.dFMPatch_toText(patches));

		patches = dmp.dFMPatch_make(text1, dFMDiffs);
		assertEquals("patch_make: Text1+Diff inputs.", expectedPatch,
				dmp.dFMPatch_toText(patches));

		patches = dmp.dFMPatch_make(text1, text2, dFMDiffs);
		assertEquals("patch_make: Text1+Text2+Diff inputs (deprecated).",
				expectedPatch, dmp.dFMPatch_toText(patches));

		patches = dmp.dFMPatch_make("`1234567890-=[]\\;',./",
				"~!@#$%^&*()_+{}|:\"<>?");
		assertEquals(
				"patch_toText: Character encoding.",
				"@@ -1,21 +1,21 @@\n-%601234567890-=%5B%5D%5C;',./\n+~!@#$%25%5E&*()_+%7B%7D%7C:%22%3C%3E?\n",
				dmp.dFMPatch_toText(patches));

		dFMDiffs = dFMDiffList(new DFMDiff(DELETE, "`1234567890-=[]\\;',./"),
				new DFMDiff(INSERT, "~!@#$%^&*()_+{}|:\"<>?"));
		assertEquals(
				"patch_fromText: Character decoding.",
				dFMDiffs,
				dmp.dFMPatch_fromText(
						"@@ -1,21 +1,21 @@\n-%601234567890-=%5B%5D%5C;',./\n+~!@#$%25%5E&*()_+%7B%7D%7C:%22%3C%3E?\n")
						.get(0).dFMDiffs);

		text1 = "";
		for (int x = 0; x < 100; x++) {
			text1 += "abcdef";
		}
		text2 = text1 + "123";
		expectedPatch = "@@ -573,28 +573,31 @@\n cdefabcdefabcdefabcdefabcdef\n+123\n";
		patches = dmp.dFMPatch_make(text1, text2);
		assertEquals("patch_make: Long string with repeats.", expectedPatch,
				dmp.dFMPatch_toText(patches));

		// Test null inputs.
		try {
			dmp.dFMPatch_make(null);
			fail("patch_make: Null inputs.");
		} catch (IllegalArgumentException ex) {
			// Error expected.
		}
	}

	public void testPatchSplitMax() {
		// Assumes that Match_MaxBits is 32.
		LinkedList<DFMPatch> patches;
		patches = dmp.dFMPatch_make("abcdefghijklmnopqrstuvwxyz01234567890",
				"XabXcdXefXghXijXklXmnXopXqrXstXuvXwxXyzX01X23X45X67X89X0");
		dmp.dFMPatch_splitMax(patches);
		assertEquals(
				"patch_splitMax: #1.",
				"@@ -1,32 +1,46 @@\n+X\n ab\n+X\n cd\n+X\n ef\n+X\n gh\n+X\n ij\n+X\n kl\n+X\n mn\n+X\n op\n+X\n qr\n+X\n st\n+X\n uv\n+X\n wx\n+X\n yz\n+X\n 012345\n@@ -25,13 +39,18 @@\n zX01\n+X\n 23\n+X\n 45\n+X\n 67\n+X\n 89\n+X\n 0\n",
				dmp.dFMPatch_toText(patches));

		patches = dmp
				.dFMPatch_make(
						"abcdef1234567890123456789012345678901234567890123456789012345678901234567890uvwxyz",
						"abcdefuvwxyz");
		String oldToText = dmp.dFMPatch_toText(patches);
		dmp.dFMPatch_splitMax(patches);
		assertEquals("patch_splitMax: #2.", oldToText,
				dmp.dFMPatch_toText(patches));

		patches = dmp
				.dFMPatch_make(
						"1234567890123456789012345678901234567890123456789012345678901234567890",
						"abc");
		dmp.dFMPatch_splitMax(patches);
		assertEquals(
				"patch_splitMax: #3.",
				"@@ -1,32 +1,4 @@\n-1234567890123456789012345678\n 9012\n@@ -29,32 +1,4 @@\n-9012345678901234567890123456\n 7890\n@@ -57,14 +1,3 @@\n-78901234567890\n+abc\n",
				dmp.dFMPatch_toText(patches));

		patches = dmp
				.dFMPatch_make(
						"abcdefghij , h : 0 , t : 1 abcdefghij , h : 0 , t : 1 abcdefghij , h : 0 , t : 1",
						"abcdefghij , h : 1 , t : 1 abcdefghij , h : 1 , t : 1 abcdefghij , h : 0 , t : 1");
		dmp.dFMPatch_splitMax(patches);
		assertEquals(
				"patch_splitMax: #4.",
				"@@ -2,32 +2,32 @@\n bcdefghij , h : \n-0\n+1\n  , t : 1 abcdef\n@@ -29,32 +29,32 @@\n bcdefghij , h : \n-0\n+1\n  , t : 1 abcdef\n",
				dmp.dFMPatch_toText(patches));
	}

	public void testPatchAddPadding() {
		LinkedList<DFMPatch> patches;
		patches = dmp.dFMPatch_make("", "test");
		assertEquals("patch_addPadding: Both edges full.",
				"@@ -0,0 +1,4 @@\n+test\n", dmp.dFMPatch_toText(patches));
		dmp.dFMPatch_addPadding(patches);
		assertEquals("patch_addPadding: Both edges full.",
				"@@ -1,8 +1,12 @@\n %01%02%03%04\n+test\n %01%02%03%04\n",
				dmp.dFMPatch_toText(patches));

		patches = dmp.dFMPatch_make("XY", "XtestY");
		assertEquals("patch_addPadding: Both edges partial.",
				"@@ -1,2 +1,6 @@\n X\n+test\n Y\n",
				dmp.dFMPatch_toText(patches));
		dmp.dFMPatch_addPadding(patches);
		assertEquals("patch_addPadding: Both edges partial.",
				"@@ -2,8 +2,12 @@\n %02%03%04X\n+test\n Y%01%02%03\n",
				dmp.dFMPatch_toText(patches));

		patches = dmp.dFMPatch_make("XXXXYYYY", "XXXXtestYYYY");
		assertEquals("patch_addPadding: Both edges none.",
				"@@ -1,8 +1,12 @@\n XXXX\n+test\n YYYY\n",
				dmp.dFMPatch_toText(patches));
		dmp.dFMPatch_addPadding(patches);
		assertEquals("patch_addPadding: Both edges none.",
				"@@ -5,8 +5,12 @@\n XXXX\n+test\n YYYY\n",
				dmp.dFMPatch_toText(patches));
	}

	public void testPatchApply() {
		dmp.Match_Distance = 1000;
		dmp.Match_Threshold = 0.5f;
		dmp.Patch_DeleteThreshold = 0.5f;
		LinkedList<DFMPatch> patches;
		patches = dmp.dFMPatch_make("", "");
		Object[] results = dmp.dFMPatch_apply(patches, "Hello world.");
		boolean[] boolArray = (boolean[]) results[1];
		String resultStr = results[0] + "\t" + boolArray.length;
		assertEquals("patch_apply: Null case.", "Hello world.\t0", resultStr);

		patches = dmp.dFMPatch_make(
				"The quick brown fox jumps over the lazy dog.",
				"That quick brown fox jumped over a lazy dog.");
		results = dmp.dFMPatch_apply(patches,
				"The quick brown fox jumps over the lazy dog.");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0] + "\t" + boolArray[1];
		assertEquals("patch_apply: Exact match.",
				"That quick brown fox jumped over a lazy dog.\ttrue\ttrue",
				resultStr);

		results = dmp.dFMPatch_apply(patches,
				"The quick red rabbit jumps over the tired tiger.");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0] + "\t" + boolArray[1];
		assertEquals("patch_apply: Partial match.",
				"That quick red rabbit jumped over a tired tiger.\ttrue\ttrue",
				resultStr);

		results = dmp.dFMPatch_apply(patches,
				"I am the very model of a modern major general.");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0] + "\t" + boolArray[1];
		assertEquals("patch_apply: Failed match.",
				"I am the very model of a modern major general.\tfalse\tfalse",
				resultStr);

		patches = dmp
				.dFMPatch_make(
						"x1234567890123456789012345678901234567890123456789012345678901234567890y",
						"xabcy");
		results = dmp
				.dFMPatch_apply(
						patches,
						"x123456789012345678901234567890-----++++++++++-----123456789012345678901234567890y");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0] + "\t" + boolArray[1];
		assertEquals("patch_apply: Big delete, small change.",
				"xabcy\ttrue\ttrue", resultStr);

		patches = dmp
				.dFMPatch_make(
						"x1234567890123456789012345678901234567890123456789012345678901234567890y",
						"xabcy");
		results = dmp
				.dFMPatch_apply(
						patches,
						"x12345678901234567890---------------++++++++++---------------12345678901234567890y");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0] + "\t" + boolArray[1];
		assertEquals(
				"patch_apply: Big delete, big change 1.",
				"xabc12345678901234567890---------------++++++++++---------------12345678901234567890y\tfalse\ttrue",
				resultStr);

		dmp.Patch_DeleteThreshold = 0.6f;
		patches = dmp
				.dFMPatch_make(
						"x1234567890123456789012345678901234567890123456789012345678901234567890y",
						"xabcy");
		results = dmp
				.dFMPatch_apply(
						patches,
						"x12345678901234567890---------------++++++++++---------------12345678901234567890y");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0] + "\t" + boolArray[1];
		assertEquals("patch_apply: Big delete, big change 2.",
				"xabcy\ttrue\ttrue", resultStr);
		dmp.Patch_DeleteThreshold = 0.5f;

		// Compensate for failed patch.
		dmp.Match_Threshold = 0.0f;
		dmp.Match_Distance = 0;
		patches = dmp
				.dFMPatch_make(
						"abcdefghijklmnopqrstuvwxyz--------------------1234567890",
						"abcXXXXXXXXXXdefghijklmnopqrstuvwxyz--------------------1234567YYYYYYYYYY890");
		results = dmp.dFMPatch_apply(patches,
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ--------------------1234567890");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0] + "\t" + boolArray[1];
		assertEquals(
				"patch_apply: Compensate for failed patch.",
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ--------------------1234567YYYYYYYYYY890\tfalse\ttrue",
				resultStr);
		dmp.Match_Threshold = 0.5f;
		dmp.Match_Distance = 1000;

		patches = dmp.dFMPatch_make("", "test");
		String patchStr = dmp.dFMPatch_toText(patches);
		dmp.dFMPatch_apply(patches, "");
		assertEquals("patch_apply: No side effects.", patchStr,
				dmp.dFMPatch_toText(patches));

		patches = dmp.dFMPatch_make(
				"The quick brown fox jumps over the lazy dog.", "Woof");
		patchStr = dmp.dFMPatch_toText(patches);
		dmp.dFMPatch_apply(patches,
				"The quick brown fox jumps over the lazy dog.");
		assertEquals("patch_apply: No side effects with major delete.",
				patchStr, dmp.dFMPatch_toText(patches));

		patches = dmp.dFMPatch_make("", "test");
		results = dmp.dFMPatch_apply(patches, "");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0];
		assertEquals("patch_apply: Edge exact match.", "test\ttrue", resultStr);

		patches = dmp.dFMPatch_make("XY", "XtestY");
		results = dmp.dFMPatch_apply(patches, "XY");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0];
		assertEquals("patch_apply: Near edge exact match.", "XtestY\ttrue",
				resultStr);

		patches = dmp.dFMPatch_make("y", "y123");
		results = dmp.dFMPatch_apply(patches, "x");
		boolArray = (boolean[]) results[1];
		resultStr = results[0] + "\t" + boolArray[0];
		assertEquals("patch_apply: Edge partial match.", "x123\ttrue",
				resultStr);
	}

	private void assertArrayEquals(String error_msg, Object[] a, Object[] b) {
		List<Object> list_a = Arrays.asList(a);
		List<Object> list_b = Arrays.asList(b);
		assertEquals(error_msg, list_a, list_b);
	}

	private void assertLinesToCharsResultEquals(String error_msg,
			LinesToCharsResult a, LinesToCharsResult b) {
		assertEquals(error_msg, a.chars1, b.chars1);
		assertEquals(error_msg, a.chars2, b.chars2);
		assertEquals(error_msg, a.lineArray, b.lineArray);
	}

	// Construct the two texts which made up the diff originally.
	private static String[] dFMDiff_rebuildtexts(LinkedList<DFMDiff> dFMDiffs) {
		String[] text = { "", "" };
		for (DFMDiff myDFMDiff : dFMDiffs) {
			if (myDFMDiff.operation != DiffMatchPatch.Operation.INSERT) {
				text[0] += myDFMDiff.text;
			}
			if (myDFMDiff.operation != DiffMatchPatch.Operation.DELETE) {
				text[1] += myDFMDiff.text;
			}
		}
		return text;
	}

	// Private function for quickly building lists of diffs.
	private static LinkedList<DFMDiff> dFMDiffList(DFMDiff... diffs) {
		LinkedList<DFMDiff> myDFMDiffList = new LinkedList<DFMDiff>();
		for (DFMDiff myDFMDiff : diffs) {
			myDFMDiffList.add(myDFMDiff);
		}
		return myDFMDiffList;
	}
}
