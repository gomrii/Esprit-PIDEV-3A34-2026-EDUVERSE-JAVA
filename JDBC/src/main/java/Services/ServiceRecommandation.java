package Services;

import Entities.Cours;
import Utils.MyDb;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class ServiceRecommandation {
    private Connection conn = MyDb.getInstance().getConn();
    private Gson gson = new Gson();

    // Chemin universel vers le dossier Documents de l'utilisateur
    private final String PATH = System.getProperty("user.home") + File.separator + "Documents" + File.separator;

    public List<Cours> genererSuggestionsML() {
        try {
            // 1. Historique
            List<Map<String, String>> historique = new ArrayList<>();
            String sqlH = "SELECT c.category, c.level FROM historique h JOIN cours c ON h.id_cours = c.id";
            Statement stH = conn.createStatement();
            ResultSet rsH = stH.executeQuery(sqlH);
            while (rsH.next()) {
                historique.add(Map.of(
                        "category", rsH.getString("category") != null ? rsH.getString("category") : "aucun",
                        "level", rsH.getString("level") != null ? rsH.getString("level") : "0"
                ));
            }

            // 2. Disponibles (On force le statut 'approuved')
            List<Map<String, String>> disponibles = new ArrayList<>();
            String sqlD = "SELECT id, title, category, level FROM cours " +
                    "WHERE status = 'approuved' " +
                    "AND id NOT IN (SELECT id_cours FROM historique)";

            Statement stD = conn.createStatement();
            ResultSet rsD = stD.executeQuery(sqlD);
            while (rsD.next()) {
                disponibles.add(Map.of(
                        "id", String.valueOf(rsD.getInt("id")),
                        "title", rsD.getString("title"),
                        "category", rsD.getString("category") != null ? rsD.getString("category") : "aucun",
                        "level", rsD.getString("level") != null ? rsD.getString("level") : "0"
                ));
            }

            if (historique.isEmpty() || disponibles.isEmpty()) {
                System.out.println("[INFO] Pas assez de données. Hist: " + historique.size() + " Dispo: " + disponibles.size());
                return new ArrayList<>();
            }

            // 3. Écriture forcée
            writeJson(PATH + "hist.json", historique);
            writeJson(PATH + "dispo.json", disponibles);
            System.out.println("[DEBUG] Fichiers créés dans : " + PATH);

            // 4. Exécution Python
            String pyExe = "python"; // Ou ton chemin complet vers python.exe
            String pyScript = "C:\\Users\\Sahar\\Downloads\\recommder.py";

            ProcessBuilder pb = new ProcessBuilder(pyExe, pyScript, PATH); // On envoie le PATH en argument
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            String result = response.toString().trim();
            System.out.println("[DEBUG ML] Résultat : " + result);

            return gson.fromJson(result, new TypeToken<List<Cours>>(){}.getType());

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void writeJson(String fullPath, Object data) throws IOException {
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fullPath), StandardCharsets.UTF_8)) {
            writer.write(gson.toJson(data));
            writer.flush();
        }
    }

    public List<String> getListeTitresHistorique() {
        List<String> titres = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery("SELECT c.title FROM historique h JOIN cours c ON h.id_cours = c.id");
            while (rs.next()) titres.add(rs.getString("title"));
        } catch (SQLException e) { e.printStackTrace(); }
        return titres;
    }
}