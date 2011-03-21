package net.sf.sahi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import net.sf.sahi.util.CaseInsensitiveString;

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
public class HttpHeaders {
	LinkedHashMap<CaseInsensitiveString, List<String>> headers = new LinkedHashMap<CaseInsensitiveString, List<String>>(15);
	public void addHeader(String key, String value){
		CaseInsensitiveString keyIgnoreCase = new CaseInsensitiveString(key);
        List<String> entry = (List<String>) headers.get(keyIgnoreCase);
        if (entry == null) {
            entry = new ArrayList<String>();
            headers.put(keyIgnoreCase, entry);
        }
        entry.add(value);
	}
	public void setHeader(String key, String value){
		CaseInsensitiveString keyIgnoreCase = new CaseInsensitiveString(key);
        List<String> entry = new ArrayList<String>();
        entry.add(value);
        headers.put(keyIgnoreCase, entry);
	}
	
	public boolean hasHeader(String key){
		return headers.containsKey(new CaseInsensitiveString(key));
	}
	
	public String getHeader(String key){
		CaseInsensitiveString keyIgnoreCase = new CaseInsensitiveString(key);
        List<String> values = headers.get(keyIgnoreCase);
        if (values == null) return null;
        StringBuffer sb = new StringBuffer();
        int size = values.size();
        for (int i = 0; i < size; i++) {
            String value = (String) values.get(i);
            if (i > 0) sb.append(",");
			sb.append(value);
        }
        return sb.toString();
	}
	
	public List<String> getHeaders(String key){
		CaseInsensitiveString keyIgnoreCase = new CaseInsensitiveString(key);
        return headers.get(keyIgnoreCase);
	}
	
	public void addHeaders(String key, List<String> newHeaders){
		if (newHeaders == null) return;
		CaseInsensitiveString keyIgnoreCase = new CaseInsensitiveString(key);
        List<String> entry = headers.get(keyIgnoreCase);
        if (entry == null) {
            entry = new ArrayList<String>();
            headers.put(keyIgnoreCase, entry);
        }
        entry.addAll(newHeaders);
	}
	
	public String getLastHeader(String key){
        List<String> entry = headers.get(new CaseInsensitiveString(key));
        if (entry == null)
            return null;
        return (String) entry.get(entry.size() - 1);
	}
	
	public byte[] getBytes(){
		return null;
	}
	
    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator<CaseInsensitiveString> keys = headers.keySet().iterator();
        while (keys.hasNext()) {
            CaseInsensitiveString key = (CaseInsensitiveString) keys.next();
            if (key.isNull()) continue;
            List<String> values = headers.get(key);
            int size = values.size();
            for (int i = 0; i < size; i++) {
                String value = (String) values.get(i);
                sb.append(key).append(": ").append(value).append("\r\n");
            }
        }
        return sb.toString();
    }
	public void removeHeader(String key) {
		headers.remove(new CaseInsensitiveString(key));
	}
	public Set<CaseInsensitiveString> keySet() {
		return headers.keySet();
	}	

	class HttpHeadersIterator implements Iterator<String>{	
		private Iterator<CaseInsensitiveString> iterator;

		public HttpHeadersIterator(Iterator<CaseInsensitiveString> iterator) {
			this.iterator = iterator;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public String next() {
			return iterator.next().toString();
		}

		public void remove() {
			iterator.remove();
		}
	}

	public Iterator<String> keysIterator() {
		return new HttpHeadersIterator(keySet().iterator());
	}
}
