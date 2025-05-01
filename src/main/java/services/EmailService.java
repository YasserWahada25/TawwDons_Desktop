package services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final Session session;

    public EmailService() {
        var props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        String username = "amroush123@gmail.com";
        String password = "npcfowmbtolgyqfe";

        session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendCommentDisabled(String toEmail, String userName, String commentContent) {
        if (toEmail == null || toEmail.isBlank()) {
            System.err.println("❌ Impossible d’envoyer l’email : adresse vide ou null.");
            return;
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("amroush123@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Notification : Commentaire désactivé");

            String text = String.format(
                    "Bonjour %s,\n\nVotre commentaire a été désactivé.\nContenu: %s\n\nCordialement,\nL’équipe.",
                    userName, commentContent
            );

            message.setText(text);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendCommentDeleted(String toEmail, String userName, String commentContent) {
        if (toEmail == null || toEmail.isBlank()) {
            System.err.println("❌ Impossible d’envoyer l’email : adresse vide ou null.");
            return;
        }

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("amroush123@gmail.com"));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail)
            );
            message.setSubject("Notification : Commentaire supprimé");

            String text = String.format(
                    "Bonjour %s,\n\n"
                            + "Votre commentaire a été supprimé par un administrateur.\n\n"
                            + "Contenu original :\n%s\n\n"
                            + "Si vous pensez qu’il s’agit d’une erreur, n’hésitez pas à nous contacter.\n\n"
                            + "Cordialement,\n"
                            + "L’équipe TawwaDon",
                    userName,
                    commentContent
            );

            message.setText(text);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
