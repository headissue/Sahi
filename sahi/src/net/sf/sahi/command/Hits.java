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
package net.sf.sahi.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;

public class Hits {

    public static HashMap<String, Integer> hits = new HashMap<String, Integer>();

    public static void increment(final String key) {
        hits.put(key, new Integer(getCount(key) + 1));
    }

    private static int getCount(final String key) {
        final Integer value = (Integer) hits.get(key);
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    public HttpResponse print(final HttpRequest requestFromBrowser) {
        final Set<String> keySet = Hits.hits.keySet();
        Iterator<String> it = keySet.iterator();
        StringBuffer sb = new StringBuffer();
        while (it.hasNext()) {
            String key = it.next();
            sb.append("<br>").append(key).append(" ").append(Hits.hits.get(key));
        }
        return new NoCacheHttpResponse(sb.toString());
    }

    public void reset(HttpRequest requestFromBrowser) {
        Hits.hits.clear();
    }
}
