package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.llm;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

/**
 * Gère l'interface avec l'API de Gemini.
 * Son rôle est essentiellement de lancer une requête à chaque nouvelle
 * question qu'on veut envoyer à l'API.
 *
 * De portée dependent pour réinitialiser la conversation à chaque fois que
 * l'instance qui l'utilise est renouvelée.
 */
@Dependent
public class LlmClientPourGemini implements Serializable {
    // Clé pour l'API du LLM
    private final String key;
    // Client REST. Facilite les échanges avec une API REST.
    private Client clientRest;
    // Représente un endpoint de serveur REST
    private final WebTarget target;

    public LlmClientPourGemini() {
        // Récupère la clé secrète pour travailler avec l'API du LLM
        this.key = System.getenv("GEMINIKEY");
        if (this.key == null || this.key.isEmpty()) {
            throw new IllegalStateException("La clé Gemini n'est pas définie dans la variable d'environnement GEMINIKEY");
        }

        // Client REST pour envoyer des requêtes vers les endpoints de l'API du LLM
        this.clientRest = ClientBuilder.newClient();

        // Endpoint REST pour envoyer la question à l'API.
        // URL pour Gemini 2.0 Flash avec la clé API
        String urlEndpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + this.key;
        this.target = clientRest.target(urlEndpoint);
    }

    /**
     * Envoie une requête à l'API de Gemini.
     * @param requestEntity le corps de la requête (en JSON).
     * @return réponse REST de l'API (corps en JSON).
     */
    public Response envoyerRequete(Entity requestEntity) {
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON_TYPE);
        // Envoie la requête POST au LLM
        return request.post(requestEntity);
    }

    public void closeClient() {
        if (this.clientRest != null) {
            this.clientRest.close();
        }
    }
}
