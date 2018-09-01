/*
 * Copyright (C) 2013 Adrian Ulrich <adrian@blinkenlights.ch>
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
import java.util.Enumeration;

public class LameHeader extends Common {
	public HashMap getTags(RandomAccessFile s) throws IOException {
		HashMap rgain = parseLameHeader(s, 0);

		return rgain;
	}


	public HashMap parseLameHeader(RandomAccessFile s, long offset) throws IOException {
		HashMap tags = new HashMap();
		byte[] chunk = new byte[12];
		
		s.seek(offset + 0x24);
		s.read(chunk);
		
		String lameMark = new String(chunk, 0, 4, "ISO-8859-1");
		int flags = b2u(chunk[7]);

		if((flags & 0x01) !=0 ) { // header indicates that totalFrames field is present
			s.seek(offset);
			s.read(chunk);
		}

		if(lameMark.equals("Info") || lameMark.equals("Xing")) {
			s.seek(offset+0xAB);
			s.read(chunk);
			
			int raw = b2be32(chunk, 0);
			int gtrk_raw = raw >> 16;     /* first 16 bits are the raw track gain value */
			int galb_raw = raw & 0xFFFF;  /* the rest is for the album gain value       */
			
			float gtrk_val = (float)(gtrk_raw & 0x01FF)/10;
			float galb_val = (float)(galb_raw & 0x01FF)/10;
			
			gtrk_val = ((gtrk_raw&0x0200)!=0 ? -1*gtrk_val : gtrk_val);
			galb_val = ((galb_raw&0x0200)!=0 ? -1*galb_val : galb_val);
			
			if( (gtrk_raw&0xE000) == 0x2000 ) {
				addTagEntry(tags, "REPLAYGAIN_TRACK_GAIN", gtrk_val+" dB");
			}
			if( (gtrk_raw&0xE000) == 0x4000 ) {
				addTagEntry(tags, "REPLAYGAIN_ALBUM_GAIN", galb_val+" dB");
			}
			
		}
		
		return tags;
	}
	
}
