package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.jsf;

import jakarta.json.*;
import java.io.StringReader;

public class JsonUtilPourGemini {

    private final LlmClientPourGemini client = new LlmClientPourGemini();

    public LlmInteraction envoyerRequete(String roleSysteme, String question, String historique) throws RequeteException {
        String requeteJson = creerRequeteJson(roleSysteme, question, historique);
        String reponseJson = client.envoyerRequete(requeteJson);
        String reponseExtraite = extractReponse(reponseJson);
        return new LlmInteraction(prettyPrintJson(requeteJson), prettyPrintJson(reponseJson), reponseExtraite);
    }

    private String creerRequeteJson(String roleSysteme, String question, String historique) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("contents", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("role", "user")
                                .add("parts", Json.createArrayBuilder()
                                        .add(Json.createObjectBuilder()
                                                .add("text", (historique + (historique.isEmpty() ? "" : "\n") + question)))))
                )
                .add("system_instruction", Json.createObjectBuilder().add("content", roleSysteme));
        JsonObject obj = builder.build();
        return obj.toString();
    }

    private String extractReponse(String reponseJson) throws RequeteException {
        JsonReader reader = Json.createReader(new StringReader(reponseJson));
        JsonObject obj = reader.readObject();
        try {
            // Chemin typique: candidates[0].content.parts[0].text
            JsonObject candidate = obj.getJsonArray("candidates").getJsonObject(0);
            JsonObject content = candidate.getJsonObject("content");
            String text = content.getJsonArray("parts").getJsonObject(0).getString("text");
            return text;
        } catch (Exception e) {
            throw new RequeteException("Erreur d’extraction de la réponse Gemini : " + e.getMessage(), e);
        }
    }

    private String prettyPrintJson(String rawJson) {
        JsonReader reader = Json.createReader(new StringReader(rawJson));
        JsonObject obj = reader.readObject();
        return obj.toString(); // Pour du vrai pretty print, utiliser un outil externe ou formatter ici si besoin
    }
}
