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
        try{
            send(args[0], Integer.parseInt(args[1]), args[2], args[3], args[4], args[5]);
        }catch(Exception e){
            System.out.println("-------------------------------------------------");
            System.out.println("Mailer <host> <port> <from> <to> <subject> <body>");
            System.out.println("-------------------------------------------------");
            e.printStackTrace();
        }
    }
}