package services;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Simple service for sending emails
 */
public class EmailService {
    private static final String USERNAME = "itstimetoduel01@gmail.com";
    private static final String PASSWORD = "wyyd cybu ahxr oawi";
    
    private static final String EMAIL_FROM = "tawwadons@gmail.com"; // Remplacez par votre email
    private static final String EMAIL_PASSWORD = "votre_mot_de_passe_app"; // Remplacez par votre mot de passe d'application
    
    /**
     * Send a welcome email to a newly registered user
     */
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
    
    /**
     * Send a reset password email with verification code
     */
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

    public static void sendAcceptanceEmail(String toEmail, String nom, String prenom, String poste) {
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
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Félicitations ! Votre candidature a été acceptée");
            
            // HTML content for the email
            String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<h2 style='color: #2C3E50;'>Félicitations !</h2>" +
                "<p>Cher(e) %s %s,</p>" +
                "<p>Nous sommes ravis de vous informer que votre candidature pour le poste de <strong>%s</strong> a été acceptée !</p>" +
                "<p>Nous avons été impressionnés par votre profil et vos compétences, et nous sommes convaincus que vous apporterez une grande valeur à notre équipe.</p>" +
                "<p>Nous vous contacterons très prochainement pour discuter des prochaines étapes et de votre intégration.</p>" +
                "<p>Félicitations encore !</p>" +
                "<p>Cordialement,<br>L'équipe Tawwa Dons</p>" +
                "</div>",
                prenom, nom, poste
            );
            
            // Set email content as HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Send the email
            Transport.send(message);
            System.out.println("Acceptance email sent to: " + toEmail);
            
        } catch (MessagingException e) {
            System.out.println("Failed to send acceptance email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }
} 