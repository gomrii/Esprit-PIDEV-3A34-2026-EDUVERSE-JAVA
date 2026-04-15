package com.elearning.service;

import com.elearning.entity.User;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * PdfService — Génération de PDF avec iText.
 *
 * On sépare la logique de création du PDF de l'interface graphique.
 */
public class PdfService {

    /**
     * Génère un PDF contenant la liste des utilisateurs.
     *
     * @param users Liste des utilisateurs à exporter (ex: issue d'une recherche ou tri).
     * @param filePath Chemin absolu où le fichier PDF sera enregistré.
     */
    public void exporterListeUtilisateurs(List<User> users, String filePath) throws Exception {

        // 1. Initialisation du document iText avec des marges
        Document document = new Document(PageSize.A4, 30, 30, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // 2. Définition des polices
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

        // 3. Titre et sous-titre
        Paragraph titre = new Paragraph("Liste des Utilisateurs — Eduverse", titleFont);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        String dateExport = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        Paragraph sousTitre = new Paragraph("Généré le : " + dateExport + " | " + users.size() + " utilisateurs", subtitleFont);
        sousTitre.setAlignment(Element.ALIGN_CENTER);
        sousTitre.setSpacingAfter(30f); // Espace avant le tableau
        document.add(sousTitre);

        // 4. Création du tableau (5 colonnes)
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 4f, 2f, 2f}); // Largeur relative des colonnes

        // 5. En-têtes du tableau
        String[] headers = {"ID", "Nom complet", "Email", "Rôle", "Statut"};
        for (String headerLabel : headers) {
            PdfPCell header = new PdfPCell(new Phrase(headerLabel, headerFont));
            header.setBackgroundColor(new BaseColor(52, 152, 219)); // Bleu #3498db
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(8f);
            table.addCell(header);
        }

        // 6. Remplissage des données
        for (User u : users) {
            table.addCell(creerCellule(String.valueOf(u.getId()), cellFont));
            table.addCell(creerCellule(u.getFullName(), cellFont));
            table.addCell(creerCellule(u.getEmail(), cellFont));
            table.addCell(creerCellule(u.getRole(), cellFont));
            table.addCell(creerCellule(u.getStatut(), cellFont));
        }

        document.add(table);
        document.close();
    }

    /**
     * Crée une cellule standardisée pour le tableau.
     */
    private PdfPCell creerCellule(String texte, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(texte != null ? texte : "-", font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6f);
        return cell;
    }
}

