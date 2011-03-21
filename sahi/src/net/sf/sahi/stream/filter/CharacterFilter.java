package net.sf.sahi.stream.filter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import net.sf.sahi.response.HttpResponse;

public class CharacterFilter extends StreamFilter {

	private CharsetDecoder decoder;

	private byte[] leftOver = null;

	public CharacterFilter(String charset) {
		this.decoder = Charset.forName(charset).newDecoder();
	}

	@Override
	public byte[] modify(byte[] data) throws IOException {
		byte[] fullData = concatArrays(leftOver, data);
		try {
			decoder.decode(ByteBuffer.wrap(fullData)).toString();
			leftOver = null;
			return fullData;
		} catch (CharacterCodingException e1) {
			leftOver = fullData;
			return new byte[0];
		}
	}

	public byte[] getRemaining(){
		// this is leftOver because it could not be decoded. Should this be appended?
		return leftOver;
	}
	
	@Override
	public void modifyHeaders(HttpResponse response) throws IOException {
	}
	
	private byte[] concatArrays(byte[] leftOver, byte[] data) {
		if (leftOver == null) return data;
		byte[] added = new byte[leftOver.length + data.length];
		System.arraycopy(leftOver, 0, added, 0, leftOver.length);
		System.arraycopy(data, 0, added, leftOver.length, data.length);
		return added;
	}
}
