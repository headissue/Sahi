package net.sf.sahi.ant;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import net.sf.sahi.test.TestRunner;

public class Scheduler extends TestRunner{

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                help();
            }
            String suiteName = args[0];
            String base = getBase(args);
            String browser = getBrowser(args);
            String logDir = args[3];
            if ("default".equalsIgnoreCase(logDir))
                logDir = "";
            String sahiHost = args[4];
            String port = args[5];
            String threads = args[6];
            String browserOption = "";
            if (args.length == 8)
                browserOption = args[7];
            TestRunner testRunner = new Scheduler(suiteName, browser, base, sahiHost, port, threads, browserOption);
            //testRunner.addReport(new Report("html", logDir));
            String status = testRunner.execute();
            System.out.println("Status:" + status);
        } catch (ConnectException ce) {
            System.err.println(ce.getMessage());
            System.err.println("Could not connect to Sahi Proxy.\nVerify that the Sahi Proxy is running on the specified host and port.");
            help();
        } catch (Exception e) {
            e.printStackTrace();
            help();
        }
    }

    public Scheduler(String suiteName, String browser, String base, String sahiHost, String port, String threads, String browserOption) {
        super(suiteName, browser, base, sahiHost, port, threads, browserOption);
    }

    public Scheduler(String suiteName, String browser, String base, String sahiHost, String port, String threads, String browserOption, List listReporter, CreateIssue createIssue) {
        super(suiteName, browser, base, sahiHost, port, threads, browserOption,
                listReporter, createIssue);
    }

    public String execute() throws IOException, InterruptedException {
        while(true){
            String status = super.execute();
            System.out.println(status);
            if (status != "SUCCESS"){
                rapidTest();
            }
            Thread.sleep(10 * 60 * 1000);
        }
    }

    public void rapidTest() throws InterruptedException{
        int consecutiveFailures = 1;
        for (int i=1; i<5; i++){
            Thread.sleep(30 * 1000);
            try {
                String status = super.execute();
                if (status == "SUCCESS"){
                    consecutiveFailures = 0;
                }else{
                    consecutiveFailures++;
                    if (consecutiveFailures == 3) break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (consecutiveFailures >= 3){
            onError();
        }
    }

    private void onError() {
        try {
            sendMail();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMail() throws AddressException, MessagingException{
        Mailer.send("localhost", 25, "kamlesh@localhost.com", "rohit@localhost.com", "re: dinner", "How about at 7?");
    }

}
