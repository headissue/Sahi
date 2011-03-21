package net.sf.sahi.stream.filter;

/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright 2006 V Narayan Raman
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.sahi.util.Utils;

/**
 * 
 * @author Richard Li
 *
 */
public class StreamFilterInputStream extends FilterInputStream {
	private StreamFilter filter;
	private byte[] data = null;
	private int idx = 0;
	private boolean endOfStream = false;

	public StreamFilterInputStream(InputStream in, StreamFilter filter) {
		super(in);
		this.filter = filter;
	}

	@Override
	public int available() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public synchronized void mark(int readlimit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public int read() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int read(byte[] b) throws IOException {
		return this.read(b, 0, b.length);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		this.readBlock();

		if (this.data != null) {
			int availableLength = this.data.length - this.idx;

			if (availableLength <= len) {
				System.arraycopy(this.data, this.idx, b, off, availableLength);
				this.data = null;

				return availableLength;
			} else {
				System.arraycopy(this.data, this.idx, b, off, len);
				this.idx += len;

				return len;
			}
		} else {
			return -1;
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long skip(long n) throws IOException {
		throw new UnsupportedOperationException();
	}

	private void readBlock() throws IOException {
		if ((this.data == null) && !this.endOfStream) {
			byte[] buffer = new byte[Utils.BUFFER_SIZE];
			int bytesRead = this.in.read(buffer);

			if (bytesRead > 0) {
				byte[] trimmedBuffer = new byte[bytesRead];
				System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
				this.data = this.filter.modify(trimmedBuffer);
				this.idx = 0;
			} else if (bytesRead == 0) {
				this.readBlock();
			} else {
				this.data = this.filter.getRemaining();
				this.idx = 0;
				this.endOfStream = true;
			}
		}
	}
}
