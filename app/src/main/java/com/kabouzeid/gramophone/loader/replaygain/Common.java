/*
 * Copyright (C) 2013 Adrian Ulrich <adrian@blinkenlights.ch>
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
import java.util.HashMap;
import java.util.ArrayList;

public class Common {
	private static final int MAX_COMMENT_SIZE = 512;

	/**
	 * Returns a 32bit int from given byte offset in LE
	 */
	public int b2le32(byte[] b, int off) {
		int r = 0;
		for(int i=0; i<4; i++) {
			r |= ( b2u(b[off+i]) << (8*i) );
		}
		return r;
	}

	/**
	 * Same as b2le32 but reads from a RandomAccessFile instead of a buffer
	 */
	public int raf2le32(RandomAccessFile fh, long off) throws IOException {
		byte[] scratch = new byte[4];
		fh.seek(off);
		fh.read(scratch);
		return b2le32(scratch, 0);
	}

	public int b2be32(byte[] b, int off) {
		return swap32(b2le32(b, off));
	}
	
	public int swap32(int i) {
		return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
	}

	/**
	 * Returns a 16bit int from given byte offset in LE
	 */
	public int b2le16(byte[] b, int off) {
		return ( b2u(b[off]) | b2u(b[off+1]) << 8 );
	}

	/**
	 * convert 'byte' value into unsigned int
	 */
	public int b2u(byte x) {
		return (x & 0xFF);
	}
	
	/**
	 * Printout debug message to STDOUT
	 */
	public void debug(String s) {
		System.out.println("DBUG "+s);
	}

	/**
	 * Throws an exception, killing the parser
	 */
	public void xdie(String reason) throws IOException {
		throw new IOException(reason);
	}

	public HashMap parse_vorbis_comment(RandomAccessFile fh, PageInfo.PageParser pp, long offset, long payload_len) throws IOException {
		HashMap tags = new HashMap();
		long last_byte = offset + payload_len;

		// skip vendor string in format: [LEN][VENDOR_STRING] -> 4 = LEN = 32bit int
		offset += 4 + raf2le32(fh, offset);

		// we can now read the number of comments in this file, we will also
		// adjust offset to point to the value after this 32bit int
		int comments = raf2le32(fh, offset);
		offset += 4;

		for ( ; comments > 0; comments--) {
			int comment_len = raf2le32(fh, offset);
			offset += 4;
			long can_read = last_byte - offset; // indicates the last byte of this page
			int do_read  = (int)(can_read > comment_len ? comment_len : can_read); // how much data is readable in this page

			if (do_read >= 3) {
				int bsize = (do_read > MAX_COMMENT_SIZE ? MAX_COMMENT_SIZE : do_read);
				byte[] data = new byte[bsize];
				fh.seek(offset);
				fh.read(data);
				String   tag_raw = new String(data);
				String[] tag_vec = tag_raw.split("=", 2);
				String   tag_key = tag_vec[0].toUpperCase();
				addTagEntry(tags, tag_key, tag_vec[1]);
			}

			// set offset to begin of next tag (OR the end of this page!)
			offset += do_read;

			// We hit the end of a stream
			// this is most likely due to the fact that we cropped do_read to not cross
			// the page boundary -> we must now calculate the position of the next tag
			if (offset == last_byte) {
				int partial_cruft = comment_len - do_read; // how many bytes we did not read
				while(partial_cruft > 0) {
					PageInfo pi = pp.parse_stream_page(fh, last_byte);
					if (pi.header_len <1 || pi.payload_len < 1)
						xdie("Data from callback doesnt make much sense");

					offset += pi.header_len;             // move position behind page header
					last_byte = offset + pi.payload_len; // and adjust the last byte to pos + payload_size

					if (offset+partial_cruft < last_byte) {
						offset += partial_cruft; // partial data ends in this block: just adjust the ofset
						break;
					} else {
						// this page just contains data from the partial tag -> skip to next one
						offset = last_byte;
						partial_cruft -= pi.payload_len;
					}
				}
			}
		}
		return tags;
	}

	public void addTagEntry(HashMap tags, String key, String value) {
		if(tags.containsKey(key)) {
			((ArrayList)tags.get(key)).add(value); // just add to existing vector
		}
		else {
			ArrayList l = new ArrayList<String>();
			l.add(value);
			tags.put(key, l);
		}
	}
	
}
