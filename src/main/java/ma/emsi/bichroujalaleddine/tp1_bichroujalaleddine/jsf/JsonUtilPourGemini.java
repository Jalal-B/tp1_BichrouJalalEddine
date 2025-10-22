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

    // CORRECTION : format Gemini Flash 2.0, tout dans "text", pas de system_instruction
    private String creerRequeteJson(String roleSysteme, String question, String historique) {
        // Assemble le prompt complet
        String fullPrompt = "";
        if (roleSysteme != null && !roleSysteme.isBlank()) {
            fullPrompt += roleSysteme.trim() + "\n";
        }
        if (historique != null && !historique.isBlank()) {
            fullPrompt += historique.trim() + "\n";
        }
        fullPrompt += question;

        JsonArrayBuilder contents = Json.createArrayBuilder();
        JsonObjectBuilder userMsg = Json.createObjectBuilder()
                .add("parts", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder()
                                .add("text", fullPrompt)));
        // Optionnel : .add("role", "user") si tu veux
        contents.add(userMsg);

        JsonObject obj = Json.createObjectBuilder()
                .add("contents", contents)
                .build();

        return obj.toString();
    }

    private String extractReponse(String reponseJson) throws RequeteException {
        JsonReader reader = Json.createReader(new StringReader(reponseJson));
        JsonObject obj = reader.readObject();
        try {
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
        return obj.toString();
    }
}
