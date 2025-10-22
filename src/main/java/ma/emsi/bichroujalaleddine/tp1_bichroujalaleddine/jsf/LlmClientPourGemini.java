package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.jsf;

import java.io.*;
import java.net.*;

public class LlmClientPourGemini {
    private final String apiKey;

    public LlmClientPourGemini() {
        // Lecture de la clé depuis une variable d'environnement
        this.apiKey = System.getenv("GEMINIKEY");
        if (apiKey == null || apiKey.isEmpty())
            throw new IllegalStateException("La clé Gemini n'est pas définie dans la variable d'environnement GEMINIKEY");
    }

    public String envoyerRequete(String jsonBody) throws RequeteException {
        try {
            // Endpoint Gemini Flash 2.0 + clé API dans l’URL
            String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST"); // POST
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            // Envoi du corps JSON
            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lecture réponse
            int responseCode = connection.getResponseCode();
            InputStream inputStream = (responseCode < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
                if (responseCode >= 400) {
                    throw new RequeteException("Erreur Gemini (" + responseCode + "): " + response.toString());
                }
                return response.toString();
            }
        } catch (Exception e) {
            throw new RequeteException("Erreur appel API Gemini : " + e.getMessage(), e);
        }
    }
}
