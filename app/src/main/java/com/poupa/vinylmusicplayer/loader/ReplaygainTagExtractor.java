package com.poupa.vinylmusicplayer.loader;

import com.poupa.vinylmusicplayer.model.Song;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag;

public class ReplaygainTagExtractor {

  public static void setReplaygainValues(Song song) {
    song.replaygainTrack = 0.0f;
    song.replaygainAlbum = 0.0f;

    Map<String, Float> tags = null;

    try {
      AudioFile file = AudioFileIO.read(new File(song.data));
      Tag tag = file.getTag();

      if (tag instanceof VorbisCommentTag) {
        tags = parseVorbisTags((VorbisCommentTag) tag);
      } else if (tag instanceof FlacTag) {
        tags = parseVorbisTags(((FlacTag) tag).getVorbisCommentTag());
      } else {
        tags = parseId3Tags(tag, song.data);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (tags != null && !tags.isEmpty()) {
      if (tags.containsKey("REPLAYGAIN_TRACK_GAIN")) {
        song.replaygainTrack = tags.get("REPLAYGAIN_TRACK_GAIN");
      }
      if (tags.containsKey("REPLAYGAIN_ALBUM_GAIN")) {
        song.replaygainAlbum = tags.get("REPLAYGAIN_ALBUM_GAIN");
      }
    }

  }

  private static Map<String, Float> parseId3Tags(Tag tag, String path) throws Exception {
    String id = null;

    if (tag.hasField("TXXX")) {
      id = "TXXX";
    } else if (tag.hasField("RGAD")) {    // may support legacy metadata formats: RGAD, RVA2
      id = "RGAD";
    } else if (tag.hasField("RVA2")) {
      id = "RVA2";
    }

    if (id == null) return parseLameHeader(path);

    Map<String, Float> tags = new HashMap<>();

    for (TagField field : tag.getFields(id)) {
      String[] data = field.toString().split(";");

      data[0] = data[0].substring(13, data[0].length() - 1).toUpperCase();

      if (data[0].equals("TRACK")) data[0] = "REPLAYGAIN_TRACK_GAIN";
      else if (data[0].equals("ALBUM")) data[0] = "REPLAYGAIN_ALBUM_GAIN";

      tags.put(data[0], parseFloat(data[1]));
    }

    return tags;
  }

  private static Map<String, Float> parseVorbisTags(VorbisCommentTag tag) {
    Map<String, Float> tags = new HashMap<>();

    if (tag.hasField("REPLAYGAIN_TRACK_GAIN")) tags.put("REPLAYGAIN_TRACK_GAIN", parseFloat(tag.getFirst("REPLAYGAIN_TRACK_GAIN")));
    if (tag.hasField("REPLAYGAIN_ALBUM_GAIN")) tags.put("REPLAYGAIN_ALBUM_GAIN", parseFloat(tag.getFirst("REPLAYGAIN_ALBUM_GAIN")));

    return tags;
  }

  private static Map<String, Float> parseLameHeader(String path) throws IOException { // Method taken from adrian-bl/bastp library
    Map<String, Float> tags = new HashMap<>();
    RandomAccessFile s = new RandomAccessFile(path, "r");
    byte[] chunk = new byte[12];

    s.seek(0x24);
    s.read(chunk);

    String lameMark = new String(chunk, 0, 4, "ISO-8859-1");

    if (lameMark.equals("Info") || lameMark.equals("Xing")) {
      s.seek(0xAB);
      s.read(chunk);

      int raw = b2be32(chunk);
      int gtrk_raw = raw >> 16;     /* first 16 bits are the raw track gain value */
      int galb_raw = raw & 0xFFFF;  /* the rest is for the album gain value       */

      float gtrk_val = (float) (gtrk_raw & 0x01FF) / 10;
      float galb_val = (float) (galb_raw & 0x01FF) / 10;

      gtrk_val = ((gtrk_raw & 0x0200) != 0 ? -1 * gtrk_val : gtrk_val);
      galb_val = ((galb_raw & 0x0200) != 0 ? -1 * galb_val : galb_val);

      if ((gtrk_raw & 0xE000) == 0x2000) {
        tags.put("REPLAYGAIN_TRACK_GAIN", gtrk_val);
      }
      if ((gtrk_raw & 0xE000) == 0x4000) {
        tags.put("REPLAYGAIN_ALBUM_GAIN", galb_val);
      }

    }

    return tags;
  }

  private static int b2le32(byte[] b) {
    int r = 0;
    for(int i=0; i<4; i++) {
      r |= ( b2u(b[i]) << (8*i) );
    }
    return r;
  }

  private static int b2be32(byte[] b) {
    return swap32(b2le32(b));
  }

  private static int swap32(int i) {
    return((i&0xff)<<24)+((i&0xff00)<<8)+((i&0xff0000)>>8)+((i>>24)&0xff);
  }

  private static int b2u(byte x) {
    return (x & 0xFF);
  }

  private static float parseFloat(String s) {
    float result = 0.0f;
    try {
      s = s.replaceAll("[^0-9.-]","");
      result = Float.parseFloat(s);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return result;
  }

}
