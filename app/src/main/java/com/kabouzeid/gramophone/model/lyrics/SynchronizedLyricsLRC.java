package com.kabouzeid.gramophone.model.lyrics;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SynchronizedLyricsLRC extends AbsSynchronizedLyrics {
    private static final Pattern LRC_LINE_PATTERN = Pattern.compile("((?:\\[.*?\\])+)(.*)");
    private static final Pattern LRC_TIME_PATTERN = Pattern.compile("\\[(\\d+):(\\d{2}(?:\\.\\d+)?)\\]");
    private static final Pattern LRC_ATTRIBUTE_PATTERN = Pattern.compile("\\[(\\D+):(.+)\\]");

    private static final float LRC_SECONDS_TO_MS_MULTIPLIER = 1000f;
    private static final int LRC_MINUTES_TO_MS_MULTIPLIER = 60000;

    @Override
    public SynchronizedLyricsLRC parse(boolean check) {
        if (this.parsed || this.data == null || this.data.isEmpty()) {
            return this;
        }

        String[] lines = this.data.split("\r?\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            Matcher attrMatcher = SynchronizedLyricsLRC.LRC_ATTRIBUTE_PATTERN.matcher(line);
            if (attrMatcher.find()) {
                try {
                    String attr = attrMatcher.group(1).toLowerCase().trim();
                    String value = attrMatcher.group(2).toLowerCase().trim();
                    switch (attr) {
                        case "offset":
                            this.offset = Integer.parseInt(value);
                            break;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
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
                        int ms = (int) (s * LRC_SECONDS_TO_MS_MULTIPLIER) + m * LRC_MINUTES_TO_MS_MULTIPLIER;

                        this.valid = true;
                        if (check) return this;

                        this.lines.append(ms, text);
                    }
                }
            }
        }

        this.parsed = true;

        return this;
    }
}
