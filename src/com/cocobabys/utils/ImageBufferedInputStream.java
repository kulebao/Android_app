package com.cocobabys.utils;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;

public class ImageBufferedInputStream extends BufferedInputStream {
	private String url;

	public ImageBufferedInputStream(InputStream in, String url) {
		super(in, 32 * 1024);
		this.url = url;
	}

	private IOException streamClosed() throws IOException {
		throw new IOException("BufferedInputStream is closed");
	}

	@Override
	public synchronized int read(byte[] buffer, int offset, int length)
			throws IOException {
		// Use local ref since buf may be invalidated by an unsynchronized
		// close()
		byte[] localBuf = buf;
		if (localBuf == null) {
			throw streamClosed();
		}
		// avoid int overflow
		// BEGIN android-changed
		// Exception priorities (in case of multiple errors) differ from
		// RI, but are spec-compliant.
		// made implicit null check explicit, used (offset | length) < 0
		// instead of (offset < 0) || (length < 0) to safe one operation
		if (buffer == null) {
			throw new NullPointerException("buffer == null");
		}
		if ((offset | length) < 0 || offset > buffer.length - length) {
			throw new IndexOutOfBoundsException();
		}
		// END android-changed
		if (length == 0) {
			return 0;
		}
		InputStream localIn = in;
		if (localIn == null) {
			throw streamClosed();
		}

		int required;
		if (pos < count) {
			/* There are bytes available in the buffer. */
			int copylength = count - pos >= length ? length : count - pos;
			System.arraycopy(localBuf, pos, buffer, offset, copylength);
			pos += copylength;
			if (copylength == length || localIn.available() == 0) {
				return copylength;
			}
			offset += copylength;
			required = length - copylength;
		} else {
			required = length;
		}

		while (true) {
			int read;
			/*
			 * If we're not marked and the required size is greater than the
			 * buffer, simply read the bytes directly bypassing the buffer.
			 */
			if (markpos == -1 && required >= localBuf.length) {
				read = localIn.read(buffer, offset, required);
				if (read == -1) {
					return required == length ? -1 : length - required;
				}
			} else {
				if (fillbuf(localIn, localBuf) == -1) {
					return required == length ? -1 : length - required;
				}
				// localBuf may have been invalidated by fillbuf
				if (localBuf != buf) {
					localBuf = buf;
					if (localBuf == null) {
						throw streamClosed();
					}
				}

				read = count - pos >= required ? required : count - pos;
				System.arraycopy(localBuf, pos, buffer, offset, read);
				pos += read;
			}
			required -= read;
			if (required == 0) {
				return length;
			}
			if (localIn.available() == 0) {
				return length - required;
			}
			offset += read;
		}
	}

	private int fillbuf(InputStream localIn, byte[] localBuf)
			throws IOException {
		//just for fix mark and reset bugs from BitmapFactory.decodeStream
		//BitmapFactory only set marklimit = 1024,but it not big enough,so when 
		//we call reset,we got exception.here we always set marklimit = 32*1024
		marklimit = 32 * 1024;
		if (markpos == -1 || (pos - markpos >= marklimit)) {
			/* Mark position not set or exceeded readlimit */
			int result = localIn.read(localBuf);
			if (result > 0) {
				if (markpos != -1) {
					Log.w("", "error reset markpos = -1 url =" + url);
					Log.w("", "count =" + count + " pos=" + pos + " markpos="
							+ markpos + " marklimit=" + marklimit);
				}

				markpos = -1;
				pos = 0;
			}
			count = (result == -1 ? 0 : result);
			return result;
		}
		if (markpos == 0 && marklimit > localBuf.length) {
			/* Increase buffer size to accommodate the readlimit */
			int newLength = localBuf.length * 2;
			if (newLength > marklimit) {
				newLength = marklimit;
			}
			byte[] newbuf = new byte[newLength];
			System.arraycopy(localBuf, 0, newbuf, 0, localBuf.length);
			// Reassign buf, which will invalidate any local references
			// FIXME: what if buf was null?
			localBuf = buf = newbuf;
		} else if (markpos > 0) {
			System.arraycopy(localBuf, markpos, localBuf, 0, localBuf.length
					- markpos);
		}
		/* Set the new position and mark position */
		pos -= markpos;
		count = markpos = 0;
		int bytesread = localIn.read(localBuf, pos, localBuf.length - pos);
		count = bytesread <= 0 ? pos : pos + bytesread;
		return bytesread;
	}
}