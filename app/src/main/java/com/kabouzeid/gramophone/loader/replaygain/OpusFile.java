/*
 * Copyright (C) 2015 Adrian Ulrich <adrian@blinkenlights.ch>
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


public class OpusFile extends OggFile {
	// A list of tags we are going to ignore in the OpusTags section
	private static final String[] FORBIDDEN_TAGS = {"REPLAYGAIN_TRACK_GAIN", "REPLAYGAIN_TRACK_PEAK", "REPLAYGAIN_ALBUM_GAIN", "REPLAYGAIN_ALBUM_PEAK"};

	public HashMap getTags(RandomAccessFile s) throws IOException {

		// The opus specification is very strict: The first packet MUST
		// contain the OpusHeader while the 2nd MUST contain the
		// OggHeader payload: https://wiki.xiph.org/OggOpus
		long pos = 0;
		PageInfo pi =  parse_stream_page(s, pos);

		HashMap tags = new HashMap();
		HashMap opus_head = parse_opus_head(s, pos+pi.header_len, pi.payload_len);
		pos += pi.header_len+pi.payload_len;

		// Check if we parsed a version number and ensure it doesn't have any
		// of the upper 4 bits set (eg: <= 15)
		if(opus_head.containsKey("version") && (Integer)opus_head.get("version") <= 0xF) {
			// Get next page: The spec requires this to be an OpusTags head
			pi = parse_stream_page(s, pos);
			tags = parse_opus_vorbis_comment(s, pos+pi.header_len, pi.payload_len);
			// ...and merge replay gain intos into the tags map
			calculate_gain(opus_head, tags);
		}

		return tags;
	}

	/**
	 * Adds replay gain information to the tags hash map
	 */
	private void calculate_gain(HashMap header, HashMap tags) {
		// Remove any unacceptable tags (Opus files must not have
		// their own REPLAYGAIN_* fields)
		for(String k : FORBIDDEN_TAGS) {
			tags.remove(k);
		}
		// Include the gain value found in the opus header
		int header_gain = (Integer)header.get("header_gain");
		addTagEntry(tags, "R128_BASTP_BASE_GAIN", ""+header_gain);
	}


	/**
	 * Attempts to parse an OpusHead block at given offset.
	 * Returns an hash-map, will be empty on failure
	 */
	private HashMap parse_opus_head(RandomAccessFile s, long offset, long pl_len) throws IOException {
		/* Structure:
		 * 8 bytes of 'OpusHead'
		 * 1 byte  version
		 * 1 byte  channel count
		 * 2 bytes pre skip
		 * 4 bytes input-sample-rate
		 * 2 bytes outputGain as Q7.8
		 * 1 byte  channel map
		 * --> 19 bytes
		 */

		HashMap id_hash = new HashMap();
		byte[] buff = new byte[19];
		if(pl_len >= buff.length) {
			s.seek(offset);
			s.read(buff);
			if((new String(buff, 0, 8)).equals("OpusHead")) {
				id_hash.put("version"      , b2u(buff[8]));
				id_hash.put("channels"     , b2u(buff[9]));
				id_hash.put("pre_skip"     , b2le16(buff, 10));
				id_hash.put("sampling_rate", b2le32(buff, 12));
				id_hash.put("header_gain"  , (int)((short)b2le16(buff, 16)));
				id_hash.put("channel_map"  , b2u(buff[18]));
			}
		}

		return id_hash;
	}

	/**
	 * Parses an OpusTags section
	 * Returns a hash map of the found tags
	 */
	private HashMap parse_opus_vorbis_comment(RandomAccessFile s, long offset, long pl_len) throws IOException {
		final int magic_len = 8; // OpusTags
		byte[] magic = new byte[magic_len];

		if(pl_len < magic_len)
			xdie("opus comment field is too short!");

		// Read and check magic signature
		s.seek(offset);
		s.read(magic);

		if(!new String(magic, 0, magic_len).equals("OpusTags"))
			xdie("Damaged packet found!");

		return parse_vorbis_comment(s, this, offset+magic_len, pl_len-magic_len);
	}

}
