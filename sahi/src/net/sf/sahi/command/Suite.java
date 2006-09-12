/**
 * Copyright V Narayan Raman
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

import net.sf.sahi.request.HttpRequest;
import net.sf.sahi.response.HttpResponse;
import net.sf.sahi.response.NoCacheHttpResponse;
import net.sf.sahi.session.Session;
import net.sf.sahi.test.SahiTestSuite;

public class Suite {

    public void start(HttpRequest request) {
        SahiTestSuite.startSuite(request);
    }

    public HttpResponse status(HttpRequest request) {
        Session session = request.session();
        SahiTestSuite suite = SahiTestSuite.getSuite(session.id());
        String status = "NONE";
        if (suite != null) {
            status = session.getPlayBackStatus();
        }
        return new NoCacheHttpResponse(status);
    }

}
