package Test;

import Entities.Cours;
import Entities.Chapitre;
import Services.ServiceCours;
import Services.ServiceChapitre;
import Utils.MyDb;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MainP {
    public static void main(String[] args) {

        Cours c = new Cours("Java Basics", "Programming", "Learn Java fundamentals", "java.png", "Beginner", "Active", new Date(), new Date());
        ServiceCours s = new ServiceCours();

        System.out.println(MyDb.getInstance().getConn().hashCode()+"!!!!"+MyDb.getInstance().getConn().hashCode());
        try {
            // Ajouter un cours
            s.add(c);
            System.out.println("Cours ajouté avec succès !");

            // Récupérer tous les cours
            List<Cours> coursList = s.display();
            System.out.println("Liste des cours : " + coursList);

            // Créer des chapitres pour le premier cours
            if (!coursList.isEmpty()) {
                Cours premierCours = coursList.get(0);

                Chapitre ch1 = new Chapitre(0, "Introduction à Java", "Contenu du chapitre 1 sur les bases de Java", "intro_java.mp4", "intro_java.pdf", premierCours.getId());
                Chapitre ch2 = new Chapitre(0, "Variables et Types", "Contenu du chapitre 2 sur les variables", "variables.mp4", "variables.pdf", premierCours.getId());

                ServiceChapitre serviceChapitre = new ServiceChapitre();

                // Ajouter les chapitres
                serviceChapitre.add(ch1);
                serviceChapitre.add(ch2);
                System.out.println("Chapitres ajoutés avec succès !");

                // Récupérer tous les chapitres
                List<Chapitre> chapitreList = serviceChapitre.display();
                System.out.println("Liste de tous les chapitres : " + chapitreList);

                // Récupérer les chapitres d'un cours spécifique
                List<Chapitre> chapitresDuCours = s.getChapitresByCours(premierCours);
                System.out.println("Chapitres du cours '" + premierCours.getTitle() + "' : " + chapitresDuCours);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }


    }
}
