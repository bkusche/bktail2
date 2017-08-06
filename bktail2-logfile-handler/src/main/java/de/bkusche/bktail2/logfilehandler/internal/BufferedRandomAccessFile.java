package de.bkusche.bktail2.logfilehandler.internal;
/**
 * Copyright 2016 Björn Kusche
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author bkusche
 * 
 */
public class BufferedRandomAccessFile extends RandomAccessFile {

	private byte[] byteBuffer = null;
	private int bufferLength = -1;
	private int maxRead = -1;
	private int buffPos = -1;
	private StringBuilder sb = null;

	public BufferedRandomAccessFile(File file, String mode, int bufferlen) throws FileNotFoundException {
		super(file, mode);
		bufferLength = bufferlen;
		byteBuffer = new byte[bufferLength];
		maxRead = 0;
		buffPos = 0;
		sb = new StringBuilder("0");
	}

	public String readNextLine() throws IOException {
		sb.delete(0, sb.length());

		int c = -1;
		boolean eol = false;

		while (!eol) {
			switch (c = read()) {
			case -1:
			case '\n':
				eol = true;
				break;
			case '\r':
				eol = true;
				long cur = getFilePointer();
				if ((read()) != '\n')
					seek(cur);

				break;
			default:
				sb.append((char) c);
				break;
			}
		}

		if ((c == -1) && (sb.length() == 0))
			return "";

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#read()
	 */
	@Override
	public int read() throws IOException {
		if (buffPos >= maxRead) {
			maxRead = readChunk();
			if (maxRead == -1)
				return -1;
		}
		buffPos++;
		return byteBuffer[buffPos - 1] & 0xFF;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#getFilePointer()
	 */
	@Override
	public long getFilePointer() throws IOException {
		return super.getFilePointer() + buffPos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.RandomAccessFile#seek(long)
	 */
	@Override
	public void seek(long pos) throws IOException {
		if (maxRead != -1 && pos < (super.getFilePointer() + maxRead) && pos > super.getFilePointer()) {
			Long diff = (pos - super.getFilePointer());
			if (diff < Integer.MAX_VALUE)
				buffPos = diff.intValue();
			else
				throw new IOException("something wrong w/ seek");
		} else {
			buffPos = 0;
			super.seek(pos);
			maxRead = readChunk();
		}
	}

	public int getbuffpos() {
		return buffPos;
	}

	private int readChunk() throws IOException {
		long pos = super.getFilePointer() + buffPos;
		super.seek(pos);
		int read = super.read(byteBuffer);
		super.seek(pos);
		buffPos = 0;
		return read;
	}
}