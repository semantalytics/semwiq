/**
 * Copyright 2007-2008 Institute for Applied Knowledge Processing, Johannes Kepler University Linz
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.jku.semwiq.endpoint;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;

/**
 * @author dorgon
 *
 */
class LineFilterReader extends FilterReader {
	private LineReplacer replacer;
	
	public LineFilterReader(BufferedReader in, LineReplacer r) {
		super(in);
		this.replacer = r;
	}
	
	private String lineAhead;
	private int len = 0;
	private int idx = 0;

	@Override
	public int read() throws IOException {
		if (idx >= len) {
			if (readAhead())
				idx = 0;
			else
				return -1;
		}
		return lineAhead.charAt(idx++);
	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
            ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0)
            return 0;
        
		int i = off;
		int c;
		while (i < len + off && i < cbuf.length) {
			c = read();
			if (c < 0) {
				if (i == 0)  //EOF
					return -1;
				else
					break;
			} else
				cbuf[i++] = (char) c;
		}
		return i;
	}
	
	/** read ahead next line */
	public boolean readAhead() throws IOException {
		lineAhead = ((BufferedReader) in).readLine();
		if (lineAhead != null) {
			lineAhead = replacer.replaceLine(lineAhead) + "\n";
			len = lineAhead.length();
			return true;
		} else
			return false;
	}

}
