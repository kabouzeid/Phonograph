package com.kabouzeid.gramophone.model.lyrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynchronizedLyricsLRC extends AbsSynchronizedLyrics {
    private static Pattern LRC_LINE_PATTERN = Pattern.compile("((?:\\[.*?\\])+)(.*)");
    private static Pattern LRC_TIME_PATTERN = Pattern.compile("\\[(\\d+):(\\d{2}(?:\\.\\d+)?)\\]");

    public SynchronizedLyricsLRC(String data, boolean justCheck) {
        if (data == null || data.isEmpty()) {
            return;
        }

        String[] lines = data.split("\r?\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher matcher = SynchronizedLyricsLRC.LRC_LINE_PATTERN.matcher(line);
            if (matcher.find()) {
                String time = matcher.group(1);
                String text = matcher.group(2);

                Matcher timeMatcher = SynchronizedLyricsLRC.LRC_TIME_PATTERN.matcher(time);
                while (timeMatcher.find()) {
                    int m = 0;
                    float s = 0f;
                    try {
                        m = Integer.parseInt(timeMatcher.group(1));
                        s = Float.parseFloat(timeMatcher.group(2));
                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();
                    }
                    int ms = (int) (s * 1000f) + m * 60000;

                    this.isValid = true;
                    if (justCheck) return;

                    this.lines.append(ms, text);
                }
            }
        }
    }

}
