package Services;

import Entities.Cours;
import Utils.MyDb;

import java.sql.*;
import java.util.*;

public class ServiceRecommandation {
    private Connection conn = MyDb.getInstance().getConn(); // Utilise ta connexion
    private OpenAIService openAIService = new OpenAIService();

    public Map<Cours, String> genererSuggestions() {
        Map<Cours, String> suggestions = new LinkedHashMap<>();
        List<Cours> candidats = new ArrayList<>();
        String historiqueTitres = "";

        try {
            // 1. Récupérer l'historique pour le contexte
            String sqlH = "SELECT c.title FROM historique h JOIN cours c ON h.id_cours = c.id";
            PreparedStatement psH = conn.prepareStatement(sqlH);
            ResultSet rsH = psH.executeQuery();
            while(rsH.next()) historiqueTitres += rsH.getString("title") + ", ";

            // 2. Trouver des cours similaires par catégorie ou niveau non encore vus
            String sqlC = "SELECT * FROM cours WHERE (category IN (SELECT category FROM cours WHERE id IN (SELECT id_cours FROM historique)) " +
                    "OR level IN (SELECT level FROM cours WHERE id IN (SELECT id_cours FROM historique))) " +
                    "AND id NOT IN (SELECT id_cours FROM historique) LIMIT 3";

            ResultSet rsC = conn.createStatement().executeQuery(sqlC);
            while(rsC.next()) {
                Cours c = new Cours();
                c.setId(rsC.getInt("id"));
                c.setTitle(rsC.getString("title"));
                c.setCategory(rsC.getString("category"));
                c.setLevel(rsC.getString("level"));
                c.setDescrption(rsC.getString("descrption"));
                candidats.add(c);
            }

            // 3. Demander une explication à l'IA pour chaque candidat
            for (Cours c : candidats) {
                String raison = openAIService.obtenirRaisonIA(c.getTitle(), c.getCategory(), historiqueTitres);
                suggestions.put(c, raison);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return suggestions;
    }
}