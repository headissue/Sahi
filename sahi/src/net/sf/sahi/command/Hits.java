/**
 * Sahi - Web Automation and Test Tool
 *
 * Copyright  2006  V Narayan Raman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.sahi.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;

public class Hits {
	public static HashMap hits = new HashMap();
	
	public static void increment(String key) {
		hits.put(key, new Integer(getCount(key) + 1));
	}

	private static int getCount(String key) {
		final Integer value = (Integer)hits.get(key);
		if (value == null) return 0;
		return value.intValue();
	}
	
	public HttpResponse print(HttpRequest requestFromBrowser) {
		final Set keySet = Hits.hits.keySet();
		Iterator it = keySet.iterator();
		StringBuffer sb = new StringBuffer();
		while(it.hasNext()) {
			String key = ((String)it.next());
            sb.append("<br>").append(key).append(" ").append(Hits.hits.get(key));
		}
		return new NoCacheHttpResponse(sb.toString());
	}
	public void reset(HttpRequest requestFromBrowser) {
		Hits.hits.clear();
	}
}
