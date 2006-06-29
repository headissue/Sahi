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
