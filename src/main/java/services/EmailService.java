package services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final Session session;
    private final String fromEmail;
    private static final String USERNAME = "itstimetoduel01@gmail.com";
    private static final String PASSWORD = "wyyd cybu ahxr oawi";

    public EmailService() {
        var props = new Properties();
        props.put("mail.smtp.auth",           "true");
        props.put("mail.smtp.starttls.enable","true");
        props.put("mail.smtp.host",           "smtp.gmail.com");
        props.put("mail.smtp.port",           "587");

        // Vos identifiants
        String username = "amroush123@gmail.com";
        String password = "npcfowmbtolgyqfe";

        this.fromEmail = username;
        this.session = Session.getInstance(props, new Authenticator() {
            @Override
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
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Notification : Commentaire désactivé");

            String text = String.format(
                    "Bonjour %s,%n%n" +
                            "Votre commentaire a été désactivé.%n" +
                            "Contenu: %s%n%n" +
                            "Cordialement,%nL’équipe Tawwa Dons.",
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
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Notification : Commentaire supprimé");

            String text = String.format(
                    "Bonjour %s,%n%n" +
                            "Votre commentaire a été supprimé par un administrateur.%n%n" +
                            "Contenu original :%n%s%n%n" +
                            "Si vous pensez qu’il s’agit d’une erreur, n’hésitez pas à nous contacter.%n%n" +
                            "Cordialement,%nL’équipe Tawwa Dons",
                    userName, commentContent
            );

            message.setText(text);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public void sendAcceptanceEmail(String toEmail, String nom, String prenom, String poste) {
        if (toEmail == null || toEmail.isBlank()) {
            System.err.println("❌ Impossible d’envoyer l’email : adresse vide ou null.");
            return;
        }
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(
                    Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Félicitations ! Votre candidature a été acceptée");

            String htmlContent = String.format(
                    "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                            +   "<h2 style='color:#2C3E50;'>Félicitations !</h2>"
                            +   "<p>Cher%s %s,</p>"
                            +   "<p>Nous sommes ravis de vous informer que votre candidature pour le poste de "
                            +   "<strong>%s</strong> a été acceptée !</p>"
                            +   "<p>Nous avons été impressionnés par votre profil et vos compétences, et nous sommes "
                            +   "convaincus que vous apporterez une grande valeur à notre équipe.</p>"
                            +   "<p>Nous vous contacterons très prochainement pour discuter des prochaines étapes "
                            +   "et de votre intégration.</p>"
                            +   "<p>Félicitations encore !</p>"
                            +   "<p>Cordialement,<br/>L'équipe Tawwa Dons</p>"
                            + "</div>",
                    prenom, nom, poste
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("✅ Acceptance email sent to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Failed to send acceptance email: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void sendWelcomeEmail(String recipientEmail, String name) {
        try {
            // Setup mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Create a session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Bienvenue sur TawwDons!");

            // HTML content for the email
            String htmlContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                            "<h2 style='color: #2C3E50;'>Bienvenue sur TawwDons!</h2>" +
                            "<p>Bonjour %s,</p>" +
                            "<p>Merci de vous être inscrit sur notre plateforme. Votre compte a été créé avec succès.</p>" +
                            "<p>Pour toute question, n'hésitez pas à nous contacter.</p>" +
                            "<p>Cordialement,<br>L'équipe TawwDons</p>" +
                            "</div>",
                    name
            );

            // Set email content as HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send the email
            Transport.send(message);
            System.out.println("Welcome email sent to: " + recipientEmail);

        } catch (MessagingException e) {
            System.out.println("Failed to send welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void sendPasswordResetEmail(String recipientEmail, String resetCode) {
        try {
            // Setup mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Create a session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            // Create the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Réinitialisation de votre mot de passe TawwDons");

            // HTML content for the email
            String htmlContent = String.format(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                            "<h2 style='color: #2C3E50;'>Réinitialisation de mot de passe</h2>" +
                            "<p>Vous avez demandé une réinitialisation de votre mot de passe.</p>" +
                            "<p>Voici votre code de vérification: <strong>%s</strong></p>" +
                            "<p>Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.</p>" +
                            "<p>Cordialement,<br>L'équipe TawwDons</p>" +
                            "</div>",
                    resetCode
            );

            // Set email content as HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Send the email
            Transport.send(message);
            System.out.println("Password reset email sent to: " + recipientEmail);

        } catch (MessagingException e) {
            System.out.println("Failed to send password reset email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
