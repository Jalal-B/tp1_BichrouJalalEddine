package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.json.*;
import java.io.Serializable;
import java.io.StringReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class Bb implements Serializable {
    private static final long serialVersionUID = 1L;

    // Variables principales
    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    // Mode debug + JSON envoyé/reçu
    private boolean debug;
    private String texteRequeteJson;
    private String texteReponseJson;

    public Bb() {}

    // --- GETTERS & SETTERS ---
    public String getRoleSysteme() { return roleSysteme; }
    public void setRoleSysteme(String roleSysteme) { this.roleSysteme = roleSysteme; }
    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }
    public void setRoleSystemeChangeable(boolean roleSystemeChangeable) { this.roleSystemeChangeable = roleSystemeChangeable; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
    public String getConversation() { return conversation.toString(); }
    public void setConversation(String conversation) { this.conversation = new StringBuilder(conversation); }
    public boolean isDebug() { return debug; }
    public void setDebug(boolean debug) { this.debug = debug; }
    public String getTexteRequeteJson() { return texteRequeteJson; }
    public void setTexteRequeteJson(String texteRequeteJson) { this.texteRequeteJson = texteRequeteJson; }
    public String getTexteReponseJson() { return texteReponseJson; }
    public void setTexteReponseJson(String texteReponseJson) { this.texteReponseJson = texteReponseJson; }
    public void toggleDebug() { setDebug(!isDebug()); }

    // Methode pour envoyer la requête à Gemini directement
    public String envoyer() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }
        try {
            // 1. Création du JSON à envoyer
            String fullPrompt = "";
            if (roleSysteme != null && !roleSysteme.isBlank()) fullPrompt += roleSysteme.trim() + "\n";
            if (conversation != null && conversation.length() > 0) fullPrompt += conversation + "\n";
            fullPrompt += question;

            JsonArrayBuilder contents = Json.createArrayBuilder();
            JsonObjectBuilder userMsg = Json.createObjectBuilder()
                    .add("parts", Json.createArrayBuilder()
                            .add(Json.createObjectBuilder()
                                    .add("text", fullPrompt)));
            contents.add(userMsg);
            JsonObject obj = Json.createObjectBuilder().add("contents", contents).build();
            String jsonBody = obj.toString();
            this.texteRequeteJson = jsonBody; // DEBUG

            // 2. Envoi HTTP POST à Gemini
            String apiKey = System.getenv("GEMINIKEY");
            String urlStr = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);

            try(OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream inputStream = (responseCode < 400)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            StringBuilder response = new StringBuilder();
            try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine);
                }
            }
            this.texteReponseJson = response.toString(); // DEBUG

            // 3. Extraction de la réponse du JSON
            JsonReader reader = Json.createReader(new StringReader(response.toString()));
            JsonObject objReponse = reader.readObject();
            try {
                JsonObject candidate = objReponse.getJsonArray("candidates").getJsonObject(0);
                JsonObject content = candidate.getJsonObject("content");
                String text = content.getJsonArray("parts").getJsonObject(0).getString("text");
                this.reponse = text;
            } catch (Exception e) {
                this.reponse = "[Erreur Gemini: " + response.toString() + "]";
            }

            conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
            this.roleSystemeChangeable = false;
        } catch (Exception e) {
            FacesMessage message =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Problème de connexion avec l'API du LLM",
                            "Problème de connexion avec l'API du LLM: " + e.getMessage());
            facesContext.addMessage(null, message);
        }
        return null;
    }

    public String nouveauChat() {
        question = "";
        reponse = "";
        conversation = new StringBuilder();
        texteRequeteJson = "";
        texteReponseJson = "";
        this.roleSystemeChangeable = true;
        return "index";
    }

    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            String role = "You are a helpful assistant. You help the user to find the information they need.\nIf the user type a question, you answer it.";
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));
            role = "You are an interpreter. You translate from English to French and from French to English.\nIf the user type a French text, you translate it into English.\nIf the user type an English text, you translate it into French.\nIf the text contains only one to three words, give some examples of usage of these words in English.";
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));
            role = "Your are a travel guide. If the user type the name of a country or of a town,\nyou tell them what are the main places to visit in the country or the town\nyou tell them the average price of a meal.";
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
            // RÔLE SYSTÈME BONUS
            role = "You are a stand-up comedian. Reply to any question by making a joke or giving an answer in a humorous way. Always keep the tone funny.";
            this.listeRolesSysteme.add(new SelectItem(role, "Humoriste"));
        }
        return this.listeRolesSysteme;
    }
}
