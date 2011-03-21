package net.sf.sahi.stream.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import net.sf.sahi.config.Configuration;
import net.sf.sahi.response.HttpResponse;

/**
 * Sahi - Web Automation and Test Tool
 * 
 * Copyright  2006  V Narayan Raman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


public class JSModifierFilter extends StreamFilter {

	String last2Words = "";

	private final String charset;

	public JSModifierFilter(String charset) {
		this.charset = charset;
	}

	public byte[] modify(byte[] data) throws IOException {
		return substituteIEActiveX(data);
	}

	public String modify(String dataStr) {
		return replace(dataStr);
	}

	public void modifyHeaders(HttpResponse response) throws IOException {
	}

	byte[] substituteIEActiveX(final byte[] data) {
		if (!Configuration.modifyActiveX()) {
			return data;
		}
		try {
			String dataStr = last2Words + new String(data, charset);
			dataStr = replace(dataStr);
			String dataStrCopy = dataStr.replace('\t', ' ');
			String newLast2Words = "";
			int ix1 = dataStrCopy.lastIndexOf(" ");
			int ix2 = -1;
			if (ix1 != -1) {
				ix2 = dataStrCopy.lastIndexOf(" ", ix1 - 1);
				if (ix2 == ix1 - 1) {
					while (true) {
						int temp = dataStrCopy.lastIndexOf(" ", ix2 - 1);
						if (temp != ix2 - 1) {
							ix2 = temp;
							break;
						}
						ix2 = temp;
					}
				}
			}
			if (ix2 != -1) {
				newLast2Words = dataStr.substring(ix2 + 1);
				dataStr = dataStr.substring(0, ix2 + 1);
			} else {
				newLast2Words = dataStr;
				dataStr = "";
			}
			last2Words = newLast2Words;
			byte[] modifiedBytes = dataStr.getBytes(charset);
			return modifiedBytes;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			last2Words = "";
			return data;
		}
	}

	public byte[] getRemaining() {
		try {
			return replace(last2Words).getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return last2Words.getBytes();
		}
	}

	private String replace(String toModify) {
		return toModify.replaceAll("new[\\s]*ActiveXObject", "new_ActiveXObject");//.replaceAll("}function", "}\r\nfunction");
	}
}
