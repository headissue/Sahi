package net.sf.sahi.ant;

import javax.mail.*;
import javax.mail.internet.*;

public class Mailer {
    private java.util.Properties props;
    private String host;
    private int port;
    private String username;
    private String password;
    private boolean auth;

    public Mailer(String host, int port) {
        this(host, port, null, null);
    }

    public Mailer(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        props = new java.util.Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "" + port);
        if (username != null && username != ""){
            this.username = username;
            this.password = password;
            this.auth = true;
            props.put("mail.smtp.user", username);
            props.put("mail.smtp.password", password);
            props.put("mail.smtp.auth", "true");
        }
    }

    public void send(String from, String to, String subject, String content)
            throws AddressException, MessagingException {
        Session session = Session.getDefaultInstance(props, null);

        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        String[] toAddresses = to.split(",");
        for (int i=0; i<toAddresses.length; i++){
            msg.setRecipient(Message.RecipientType.TO, new InternetAddress(toAddresses[i]));
        }
        msg.setSubject(subject);
        msg.setText(content);

        if (auth) {
            Transport transport = session.getTransport("smtp");
            transport.connect(host, port, username, password);
            msg.saveChanges();
            transport.sendMessage(msg, msg.getAllRecipients());
            transport.close();
        }
        else {
            Transport.send(msg);
        }
    }

    public static void main(String[] args) throws Exception {
        // Send a test message
        try{
            Mailer mailer;
            if (args.length == 8){
                mailer = new Mailer(args[0], Integer.parseInt(args[1]), args[6], args[7]);
            }else{
                mailer = new Mailer(args[0], Integer.parseInt(args[1]));
            }
            mailer.send(args[2], args[3], args[4], args[5]);
        }catch(Exception e){
            System.out.println("-------------------------------------------------");
            System.out.println("Mailer <host> <port> <from> <to> <subject> <body> <username> <password>");
            System.out.println("-------------------------------------------------");
            e.printStackTrace();
        }
    }
}