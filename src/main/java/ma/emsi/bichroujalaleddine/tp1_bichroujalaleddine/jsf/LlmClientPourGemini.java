package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.jsf;

import java.io.*;
import java.net.*;
import javax.net.ssl.HttpsURLConnection;

public class LlmClientPourGemini {
    private final String apiKey;

    public LlmClientPourGemini() {
        // Lecture de la clé depuis variable d'environnement GEMINIKEY
        this.apiKey = System.getenv("GEMINIKEY");
        if (apiKey == null || apiKey.isEmpty())
            throw new IllegalStateException("La clé Gemini n'est pas définie dans la variable d'environnement GEMINIKEY");
    }

    public String envoyerRequete(String jsonBody) throws RequeteException {
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Envoi JSON
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            // Lecture réponse
            try(BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
                return response.toString();
            }
        } catch (Exception e) {
            throw new RequeteException("Erreur appel API Gemini : " + e.getMessage(), e);
        }
    }
}
