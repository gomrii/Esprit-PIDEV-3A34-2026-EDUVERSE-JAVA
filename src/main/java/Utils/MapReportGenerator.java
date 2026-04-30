package Utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.FileOutputStream;

public class MapReportGenerator {

    public static void main(String[] args) {
        generatePdf("Rapport_Integration_Map.pdf");
    }

    public static void generatePdf(String filename) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLUE);
            Paragraph title = new Paragraph("Rapport d'Intégration de l'API Map - Projet EduVerse", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            // Introduction
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
            document.add(new Paragraph("1. Introduction", subTitleFont));
            document.add(new Paragraph("Ce document récapitule l'implémentation de la fonctionnalité de sélection de lieu interactive pour la gestion des événements dans l'application EduVerse.\n\n"));

            // Technologies
            document.add(new Paragraph("2. Technologies Utilisées", subTitleFont));
            List techList = new List(List.UNORDERED);
            techList.add(new ListItem("JavaFX WebView : Affichage du contenu web natif."));
            techList.add(new ListItem("Leaflet.js : Bibliothèque JavaScript pour les cartes."));
            techList.add(new ListItem("OpenStreetMap (OSM) : Fournisseur de fonds de carte."));
            techList.add(new ListItem("Nominatim API : Service de géocodage inversé (Reverse Geocoding)."));
            document.add(techList);
            document.add(new Paragraph("\n"));

            // Fonctionnalités
            document.add(new Paragraph("3. Fonctionnalités Implémentées", subTitleFont));
            List funcList = new List(List.UNORDERED);
            funcList.add(new ListItem("Affichage d'une carte interactive centrée sur Tunis."));
            funcList.add(new ListItem("Sélection de lieu par simple clic sur la carte."));
            funcList.add(new ListItem("Placement d'un marqueur visuel avec popup d'adresse."));
            funcList.add(new ListItem("Traduction automatique des coordonnées en adresse via Nominatim."));
            funcList.add(new ListItem("Synchronisation en temps réel entre la carte et le formulaire JavaFX."));
            document.add(funcList);
            document.add(new Paragraph("\n"));

            // Architecture
            document.add(new Paragraph("4. Composants Clés", subTitleFont));
            document.add(new Paragraph("Le système repose sur deux composants majeurs :"));
            document.add(new Paragraph("- map.html : Contient la logique Leaflet et les appels API."));
            document.add(new Paragraph("- MapPickerController.java : Gère l'intégration du WebView et le pont JavaScript-Java via la classe interne Bridge."));
            document.add(new Paragraph("\n"));

            // Conclusion
            document.add(new Paragraph("5. Conclusion", subTitleFont));
            document.add(new Paragraph("L'intégration est fonctionnelle, robuste et offre une expérience utilisateur fluide pour la saisie des localisations d'événements."));

            document.close();
            System.out.println("Le rapport PDF a été généré avec succès : " + filename);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
