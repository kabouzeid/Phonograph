package com.kabouzeid.gramophone.model.lyrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynchronizedLyricsLRC extends SynchronizedLyrics {
    private static Pattern LRC_LINE_PATTERN = Pattern.compile("((?:\\[.*?\\])+)(.*)");
    private static Pattern LRC_TIME_PATTERN = Pattern.compile("\\[(\\d\\d):(\\d\\d)(?:\\.(\\d\\d))\\]");

    public SynchronizedLyricsLRC(String data)
    {
        if(data == null || data.isEmpty()) {
            return;
        }

        String[] lines = data.split("\r?\n");

        for(String line : lines) {
            line = line.trim();
            if(line.isEmpty()) {
                continue;
            }

            Matcher matcher = SynchronizedLyricsLRC.LRC_LINE_PATTERN.matcher(line);
            if(matcher.find()) {
                String time = matcher.group(1);
                String text = matcher.group(2);

                Matcher timeMatcher = SynchronizedLyricsLRC.LRC_TIME_PATTERN.matcher(time);
                while(timeMatcher.find()) {
                    int m = 0, s = 0, x = 0;
                    try {
                        m = Integer.parseInt(timeMatcher.group(1));
                        s = Integer.parseInt(timeMatcher.group(2));
                        x = Integer.parseInt(timeMatcher.group(3));
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                    int ms = x*10 + s*1000 + m*60000;

                    this.lines.append(ms, text);
                }
            }
        }
    }

}
