package ma.emsi.bichroujalaleddine.tp1_bichroujalaleddine.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class Bb implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    // Mode debug + JSON
    private boolean debug;
    private String texteRequeteJson;
    private String texteReponseJson;

    @Inject
    private FacesContext facesContext;
    @Inject
    private JsonUtilPourGemini jsonUtil; // à injecter ou à instancier (new JsonUtilPourGemini())

    public Bb() {
    }

    // ... getters/setters pour les propriétés existantes (déjà codées TP0)

    public boolean isDebug() {
        return debug;
    }
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }
    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }
    public String getTexteReponseJson() {
        return texteReponseJson;
    }
    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }
    public void toggleDebug() {
        setDebug(!isDebug());
    }

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }
        try {
            // Appel direct Gemini !
            LlmInteraction interaction = jsonUtil.envoyerRequete(roleSysteme, question, conversation.toString());
            this.reponse = interaction.reponseExtraite(); // réponse à afficher
            this.texteRequeteJson = interaction.questionJson(); // JSON envoyé
            this.texteReponseJson = interaction.reponseJson(); // JSON reçu
            conversation.append("== User:\n").append(question).append("\n== Serveur:\n").append(reponse).append("\n");
            this.roleSystemeChangeable = false;
        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Problème de connexion avec l’API du LLM", e.getMessage());
            facesContext.addMessage(null, message);
        }
        return null;
    }

    public String nouveauChat() {
        // Réinitialisation conversation, réponse, debug et débloque le choix du rôle
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

            // Bonus: ajouter ton rôle original ici
        }
        return this.listeRolesSysteme;
    }
}
