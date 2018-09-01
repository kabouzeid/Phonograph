package com.kabouzeid.gramophone.loader.replaygain;

import java.io.IOException;
import java.io.RandomAccessFile;

public class PageInfo {

	long header_len;
	long payload_len;
	int type;
	boolean last_page;

	public static interface PageParser {
		PageInfo parse_stream_page(RandomAccessFile fh, long offset) throws IOException;
	}
}
