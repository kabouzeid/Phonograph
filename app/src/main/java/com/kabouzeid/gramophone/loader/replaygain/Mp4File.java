/*
 * Copyright (C) 2016 Ian Harmon
 * Copyright (C) 2017 Google Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.kabouzeid.gramophone.loader.replaygain;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/*
* Helper class for tracking the traversal of the atom tree
*/
class Atom {
	String name;
	long start;
	int length;
	
	public Atom(String name, long start, int length) {
		this.name = name;
		this.start = start;
		this.length = length;
	}
}

/*
* MP4 tag parser
*/
public class Mp4File extends Common {

	// only these tags are returned. others may be parsed, but discarded.
	final static List<String> ALLOWED_TAGS = Arrays.asList(
		"replaygain_track_gain",
		"replaygain_album_gain",
		"title",
		"album",
		"artist",
		"albumartist",
		"composer",
		"genre",
		"year",
		"tracknumber",
		"discnumber"
	);

	// mapping between atom <-> vorbis tags
	final static HashMap<String, String> ATOM_TAGS;
	static {
		ATOM_TAGS = new HashMap<String, String>();
		ATOM_TAGS.put("�nam", "title");
		ATOM_TAGS.put("�alb", "album");
		ATOM_TAGS.put("�ART", "artist");
		ATOM_TAGS.put("aART", "albumartist");
		ATOM_TAGS.put("�wrt", "composer");
		ATOM_TAGS.put("�gen", "genre");
		ATOM_TAGS.put("�day", "year");
		ATOM_TAGS.put("trkn", "tracknumber");
		ATOM_TAGS.put("disk", "discnumber");
	}

	// These tags are 32bit integers, not strings.
	final static List<String> BINARY_TAGS = Arrays.asList(
		"tracknumber",
		"discnumber"
	);

	// maximum size for tag names or values
	final static int MAX_BUFFER_SIZE = 512;
	// only used when developing
	final static boolean PRINT_DEBUG = false;

	// When processing atoms, we first read the atom length (4 bytes),
	// and then the atom name (also 4 bytes). This value should not be changed.
	final static int ATOM_HEADER_SIZE = 8;
	
	/*
	* Traverses the atom structure of an MP4 file and returns as soon as tags
	* are parsed
	*/
	public HashMap getTags(RandomAccessFile s) throws IOException {
		HashMap tags = new HashMap();
		if (PRINT_DEBUG) { System.out.println(); }
		try {

			// maintain a trail of breadcrumbs to know what part of the file we're in,
			// so e.g. that we only parse [name] atoms that are part of a tag
			Stack<Atom> path = new Stack<Atom>();

			s.seek(0);
			int atomSize;
			byte[] atomNameRaw = new byte[4];
			String atomName;
			String tagName = null;

			// begin traversing the file
			// file structure info from http://atomicparsley.sourceforge.net/mpeg-4files.html
			while (s.getFilePointer() < s.length()) {

				// if we've read/skipped past the end of atoms, remove them from the path stack
				while (!path.empty() && s.getFilePointer() >= (path.peek().start + path.peek().length)) {
					// if we've finished the tag atom [ilst], we can stop parsing.
					// when tags are read successfully, this should be the exit point for the parser.
					if (path.peek().name.equals("ilst")) {
						if (PRINT_DEBUG) { System.out.println(); }
						return tags;
					}
					path.pop();
				}

				// read a new atom's details
				atomSize = s.readInt();

				// return if we're unable to parse an atom size
				// (e.g. previous atoms were parsed incorrectly and the
				// file pointer is misaligned)
				if (atomSize <= 0) { return tags; }

				s.read(atomNameRaw);
				atomName = new String(atomNameRaw);

				// determine if we're currently decending through the hierarchy
				// to a tag atom
				boolean approachingTagAtom = false;
				boolean onMetaAtom = false;
				boolean onTagAtom = false;
				String fourAtom = null;
				// compare everything in the current path hierarchy and the new atom as well
				// this is a bit repetitive as-is, but shouldn't be noticeable
				for (int i = 0; i <= path.size(); i++) {
					String thisAtomName = (i < path.size()) ? path.get(i).name : atomName;
					if ((i == 0 && thisAtomName.equals("moov"))         ||
						(i == 1 && thisAtomName.equals("udta"))         ||
						(i == 2 && thisAtomName.equals("meta"))         ||
						(i == 3 && thisAtomName.equals("ilst"))         ||
						(i == 4 && thisAtomName.equals("----"))         ||
						(i == 4 && ATOM_TAGS.containsKey(thisAtomName)) ||
						(i == 5 && (thisAtomName.equals("name")         || thisAtomName.equals("data")))
					) {
						approachingTagAtom = true;
						// if we're at the end of the current hierarchy, mark if it's the [meta] or a tag atom.
						if (i == path.size()) {
							onMetaAtom = thisAtomName.equals("meta");
							onTagAtom = (thisAtomName.equals("name") || thisAtomName.equals("data"));
						}
						// depth is 4 and this is a known atom: rembemer this!
						if (i == 4 && ATOM_TAGS.containsKey(thisAtomName)) {
							fourAtom = ATOM_TAGS.get(thisAtomName);
						}
					}
					// quit as soon as we know we're not on the road to a tag atom
					else {
						approachingTagAtom = false;
						break;
					}
				}

				// add the new atom to the path hierarchy
				path.push(new Atom(atomName, s.getFilePointer()-ATOM_HEADER_SIZE, atomSize));
				if (PRINT_DEBUG) { printDebugAtomPath(s, path, atomName, atomSize); }

				// skip all non-pertinent atoms
				if (!approachingTagAtom) { s.skipBytes(atomSize-ATOM_HEADER_SIZE); }
				// dive into tag-related ones
				else {
					// the meta atom has an extra 4 bytes that need to be skipped
					if (onMetaAtom) { s.skipBytes(4); }

					// read tag contents when there
					if (onTagAtom) {
						// get a tag name
						if (atomName.equals("name")) {
							// skip null bytes
							s.skipBytes(4);
							tagName = new String(readIntoBuffer(s, atomSize-(ATOM_HEADER_SIZE+4)));
						}

						// get a tag value
						else if (atomName.equals("data")) {
							// skip flags/null bytes
							s.skipBytes(8);

							// use the 'fourAtom' value if we did not have a tag name
							tagName = (tagName == null ? fourAtom : tagName);
							// read the tag
							byte[] tagBuffer = readIntoBuffer(s, atomSize-(ATOM_HEADER_SIZE+8));

							if (ALLOWED_TAGS.contains(tagName))
							{
								String tagValue = (BINARY_TAGS.contains(tagName) ? String.format("%d", b2be32(tagBuffer, 0)) : new String(tagBuffer, "UTF-8"));
								if (PRINT_DEBUG) {
									System.out.println(String.format("parsed tag '%s': '%s'\n", tagName, tagValue));
								}
								addTagEntry(tags, tagName.toUpperCase(), tagValue);
							}
							// This is the end of this tree, make sure that we don't re-use tagName in any other tree
							tagName = null;
						}
					}
				}
			}
			// End of while loop, the file has been completely read through.
			// The parser should only return here if the tags atom [ilst] was missing.
			return tags;
		}
		// if anything goes wrong, just return whatever we already have
		catch (Exception e) {
			return tags;
		}
	}

	/*
	* Reads bytes from an atom up to the buffer size limit, currently 512B
	*/
	private byte[] readIntoBuffer(RandomAccessFile s, int dataSize) throws IOException {
		// read tag up to buffer limit
		int bufferSize = Math.min(dataSize, MAX_BUFFER_SIZE);
		byte[] buffer = new byte[bufferSize];
		s.read(buffer, 0, buffer.length);
		if (dataSize > bufferSize) {
			s.skipBytes(dataSize - bufferSize);
		}
		return buffer;
	}

	/*
	* Can be used when traversing the atom hierarchy to print the tree of atoms
	*/
	private void printDebugAtomPath(RandomAccessFile s, Stack<Atom> path,
		String atomName, int atomSize) throws IOException
	{
		String treeLines = "";
		for (int i = 0; i < path.size(); i++) { treeLines += ". "; }
		long atomStart = s.getFilePointer()-ATOM_HEADER_SIZE;
		System.out.println(String.format("%-22s %8d to %8d, length %8d",
			(treeLines + "[" + atomName + "]"), atomStart, (atomStart+atomSize), atomSize));
	}
}
