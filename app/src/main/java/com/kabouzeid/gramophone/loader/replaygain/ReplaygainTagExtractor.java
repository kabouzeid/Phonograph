package com.kabouzeid.gramophone.loader.replaygain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

public class ReplaygainTagExtractor {
  public static float replaygainTrack = 0;
  public static float replaygainAlbum = 0;

  public static void processReplaygainValues(String path) {
    HashMap tags = new Bastp().getTags(path);
    replaygainTrack = 0;
    replaygainAlbum = 0;

    if(tags.containsKey("REPLAYGAIN_TRACK_GAIN"))
      replaygainTrack = getFloatFromString((String)((ArrayList)tags.get("REPLAYGAIN_TRACK_GAIN")).get(0));
    if(tags.containsKey("REPLAYGAIN_ALBUM_GAIN"))
      replaygainAlbum = getFloatFromString((String)((ArrayList)tags.get("REPLAYGAIN_ALBUM_GAIN")).get(0));

    // likely OPUS
    if(tags.containsKey("R128_TRACK_GAIN"))
      replaygainTrack = 5.0f + getFloatFromString((String)((ArrayList)tags.get("R128_TRACK_GAIN")).get(0)) / 256.0f;
    else if(tags.containsKey("R128_BASTP_BASE_GAIN"))
      replaygainTrack = 0.0f + getFloatFromString((String)((ArrayList)tags.get("R128_BASTP_BASE_GAIN")).get(0)) / 256.0f;
    if(tags.containsKey("R128_ALBUM_GAIN"))
      replaygainAlbum = 5.0f + getFloatFromString((String)((ArrayList)tags.get("R128_ALBUM_GAIN")).get(0)) / 256.0f;

  }


  private static float getFloatFromString(String rg_raw) {
    float rg_float = 0f;
    try {
      String nums = rg_raw.replaceAll("[^0-9.-]","");
      rg_float = Float.parseFloat(nums);
    } catch(Exception ignored) {}
    return rg_float;
  }
}
