package services;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Service for generating and validating text-based captchas
 */
public class CaptchaService {
    // Store captcha codes with their IDs (in a real application, use a database or cache)
    private static final Map<String, String> activeCaptchas = new HashMap<>();

    // Captcha expiration time in milliseconds (5 minutes)
    private static final long EXPIRATION_TIME = 5 * 60 * 1000;

    // Store captcha creation timestamps
    private static final Map<String, Long> captchaTimestamps = new HashMap<>();

    /**
     * Generate a new captcha image and return it as a Base64 encoded string
     * @return Map containing the captchaId and the base64Image
     */
    public static Map<String, String> generateCaptcha() {
        // Generate a random captcha code (6 alphanumeric characters)
        String captchaCode = generateRandomCode(6);

        // Generate a unique ID for this captcha
        String captchaId = UUID.randomUUID().toString();

        // Store the captcha code with its ID
        activeCaptchas.put(captchaId, captchaCode);
        captchaTimestamps.put(captchaId, System.currentTimeMillis());

        // Create the captcha image
        BufferedImage image = createCaptchaImage(captchaCode);

        // Convert the image to Base64 string
        String base64Image = imageToBase64(image);

        // Clean up expired captchas
        cleanupExpiredCaptchas();

        // Return the captcha ID and the Base64 encoded image
        Map<String, String> result = new HashMap<>();
        result.put("captchaId", captchaId);
        result.put("base64Image", base64Image);
        return result;
    }

    /**
     * Validate a captcha code against a given captcha ID
     * @param captchaId The ID of the captcha
     * @param userInput The user's input
     * @return true if the captcha is valid, false otherwise
     */
    public static boolean validateCaptcha(String captchaId, String userInput) {
        // Check if the captcha exists
        if (!activeCaptchas.containsKey(captchaId)) {
            return false;
        }

        // Check if the captcha has expired
        if (System.currentTimeMillis() - captchaTimestamps.get(captchaId) > EXPIRATION_TIME) {
            // Remove expired captcha
            activeCaptchas.remove(captchaId);
            captchaTimestamps.remove(captchaId);
            return false;
        }

        // Get the stored captcha code
        String storedCode = activeCaptchas.get(captchaId);

        // Remove the used captcha to prevent reuse
        activeCaptchas.remove(captchaId);
        captchaTimestamps.remove(captchaId);

        // Compare the user's input with the stored captcha code (case insensitive)
        return storedCode.equalsIgnoreCase(userInput);
    }

    /**
     * Generate a random alphanumeric code
     * @param length The length of the code
     * @return The generated code
     */
    private static String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        return sb.toString();
    }

    /**
     * Create a captcha image with the given code
     * @param code The captcha code
     * @return The generated image
     */
    private static BufferedImage createCaptchaImage(String code) {
        int width = 200;
        int height = 80;

        // Create a buffered image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Fill background with white
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);

        // Add noise (random lines)
        Random random = new Random();
        g2d.setColor(new Color(220, 220, 220));
        for (int i = 0; i < 20; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Set font for the captcha text
        Font font = new Font("Arial", Font.BOLD, 40);
        g2d.setFont(font);

        // Draw the captcha code with slight rotation for each character
        int x = 25;
        for (int i = 0; i < code.length(); i++) {
            // Randomly rotate each character slightly
            int angle = random.nextInt(60) - 30; // -30 to 30 degrees
            g2d.rotate(Math.toRadians(angle), x, height / 2);

            // Random color for each character
            g2d.setColor(new Color(
                    random.nextInt(100),
                    random.nextInt(100),
                    random.nextInt(100)
            ));

            g2d.drawString(String.valueOf(code.charAt(i)), x, height / 2 + 10);

            // Reset rotation
            g2d.rotate(Math.toRadians(-angle), x, height / 2);

            x += 30;
        }

        g2d.dispose();
        return image;
    }

    /**
     * Convert a BufferedImage to a Base64 encoded string
     * @param image The image to convert
     * @return The Base64 encoded string
     */
    private static String imageToBase64(BufferedImage image) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            System.out.println("Failed to convert image to Base64: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Clean up expired captchas
     */
    private static void cleanupExpiredCaptchas() {
        long currentTime = System.currentTimeMillis();

        // Remove captchas that have expired
        captchaTimestamps.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > EXPIRATION_TIME
        );

        // Remove captcha codes for IDs that are no longer in the timestamps map
        activeCaptchas.keySet().removeIf(id -> !captchaTimestamps.containsKey(id));
    }
}