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
