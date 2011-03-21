package net.sf.sahi.ant;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import net.sf.sahi.test.TestRunner;

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
public class Scheduler {

    private Properties properties;
    private TestRunner runner;
    private int successiveFailureCount;
    private int repeatCount;
    private int repeatInterval;
    private int failureRepeatInterval;

    public static void main(String[] args) throws IOException, InterruptedException {
        String filePath = "";
        if (args.length == 0) {
            System.out.println("Usage: java net.sf.sahi.ant.Scheduler [path to scheduler.properties]");
            System.out.println("No scheduler.properties specified. Using ../config/scheduler.properties");
            filePath = "../config/scheduler.properties";
        } else {
            filePath = args[1];
        }
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        Scheduler scheduler = new Scheduler(properties);
        scheduler.execute();
    }

    public String g(final String key) {
        String value = properties.getProperty(key);
        System.out.println(key + " " + value);
        return value;
    }

    public Scheduler(Properties properties) {
        this.properties = properties;
        this.runner = new TestRunner(g("suite_path"), g("browser_exe"), g("base_url"), g("sahi_host"), g("sahi_port"), g("threads"), g("browser_option"), g("browser_process_name"));
        this.repeatCount = i("retry_count_on_failure");
        this.successiveFailureCount = i("successive_failures");
        this.repeatInterval = i("repeat_interval_in_minutes") * 60000;
        this.failureRepeatInterval = i("repeat_interval_on_failure_in_seconds") * 1000;
    }

    private int i(final String key) {
        return Integer.parseInt(g(key));
    }

    public String execute() throws InterruptedException {
        while (true) {
            try {
                String status = "FAILURE";
                status = runner.execute();
                System.out.println(status);
                if (!status.trim().equals("SUCCESS")) {
                    rapidTest();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Retrying in " + repeatInterval + " milliseconds");
            Thread.sleep(repeatInterval);
        }
    }

    public void rapidTest() throws InterruptedException {
        int consecutiveFailures = 1;
        for (int i = 1; i < repeatCount; i++) {
            System.out.println("Retrying in " + failureRepeatInterval + " milliseconds");
            Thread.sleep(failureRepeatInterval);
            try {
                String status = runner.execute();
                System.out.println(status);
                if (status.trim().equals("SUCCESS")) {
                    consecutiveFailures = 0;
                } else {
                    consecutiveFailures++;
                    if (consecutiveFailures == successiveFailureCount) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (consecutiveFailures >= successiveFailureCount) {
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

    private void sendMail() throws AddressException, MessagingException {
        System.out.println("Sending out email ...");
        Mailer mailer = new Mailer(g("smtp_host"), i("smtp_port"), g("smtp_user"), g("smtp_password"));
        mailer.send(g("email_from"), g("email_to"),
                g("email_subject"), g("email_content"));
    }
}
