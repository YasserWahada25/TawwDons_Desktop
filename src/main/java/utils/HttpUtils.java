package utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class HttpUtils {

    private static final String GEMINI_API_KEY = "AIzaSyDcEqB85aEpEQrA7I_amNINr-wXXjBU3ZQ";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + GEMINI_API_KEY;

    private static final String SYSTEM_CONTEXT = """
Tu es l'assistant officiel de l'application **Tawwa Dons Desktop**.

Voici ce que tu dois savoir :
- C'est une application JavaFX qui permet aux utilisateurs de répondre à des évaluations santé et d'utiliser un chatbot santé.
- Il existe 2 rôles : **Utilisateur** (répondre aux évaluations, mini-jeux) et **Administrateur** (gérer évaluations, questions, utilisateurs, statistiques).
- Pour **ajouter une évaluation**, il faut être administrateur, aller dans l'interface Admin > Ajouter une évaluation, remplir le nom, description, type, puis valider.
- Pour **répondre à une évaluation**, il faut choisir une évaluation, entrer son nom, répondre aux questions et soumettre.
- Les administrateurs peuvent exporter des résultats en PDF et modérer les utilisateurs.

Si l'utilisateur pose une question sur :
- L'ajout d'évaluations ou de questions 👉 explique clairement la procédure.
- Le rôle d'utilisateur ou d'administrateur 👉 explique les actions possibles.
- La santé en général 👉 réponds de manière simple, fiable et pédagogique.

Sois toujours **bienveillant**, **clair** et **précis**. Si tu ne sais pas, invite l'utilisateur à contacter un administrateur.
""";

    public static String post(String userQuestion) throws IOException {
        URL url = new URL(GEMINI_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);

        // Fusionner contexte système + question utilisateur
        String body = "{"
                + "\"contents\": [{\"parts\": [{\"text\": \""
                + SYSTEM_CONTEXT.replace("\"", "\\\"") + "\\nQuestion utilisateur : "
                + userQuestion.replace("\"", "\\\"") + "\"}]}]"
                + "}";

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line.trim());
            }

            if (status >= 400) {
                throw new IOException("Erreur HTTP " + status + ": " + response.toString());
            }

            return extractTextFromJson(response.toString());
        } finally {
            conn.disconnect();
        }
    }

    private static String extractTextFromJson(String json) throws IOException {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray candidates = obj.getJSONArray("candidates");
            if (candidates.length() == 0) {
                throw new IOException("Pas de candidats dans la réponse");
            }
            JSONObject firstCandidate = candidates.getJSONObject(0);
            JSONObject content = firstCandidate.getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            if (parts.length() == 0) {
                throw new IOException("Pas de 'parts' dans la réponse");
            }
            JSONObject firstPart = parts.getJSONObject(0);
            String text = firstPart.getString("text");

            return text.replace("\\n", "\n");
        } catch (Exception e) {
            throw new IOException("Erreur de parsing JSON: " + e.getMessage(), e);
        }
    }
}
