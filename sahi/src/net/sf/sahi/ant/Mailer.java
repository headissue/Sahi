package net.sf.sahi.ant;

import javax.mail.*;
import javax.mail.internet.*;

public class Mailer {
    public static void send(String smtpHost, int smtpPort, String from,
            String to, String subject, String content) throws AddressException,
            MessagingException {
        // Create a mail session
        java.util.Properties props = new java.util.Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", "" + smtpPort);
        Session session = Session.getDefaultInstance(props, null);

        // Construct the message
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setText(content);

        // Send the message
        Transport.send(msg);
    }

    public static void main(String[] args) throws Exception {
        // Send a test message
        send("localhost", 25, "kamlesh@localhost.com", "rohit@localhost.com", "re: dinner", "How about at 7?");
    }
}