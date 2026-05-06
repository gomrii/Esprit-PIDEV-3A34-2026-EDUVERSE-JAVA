package Test;

import Entities.Club;
import Entities.Event;
import Services.ServiceClub;
import Services.ServiceEvent;

import java.sql.SQLException;
import java.util.Date;

public class Main {
    public static void main(String[] args) {

        ServiceClub serviceClub = new ServiceClub();
        ServiceEvent serviceEvent = new ServiceEvent();

        try {
            // ── TEST CLUB ──────────────────────────────────────
            Club club = new Club("Tech Club", "Club de technologie", "active", 1);
            serviceClub.add(club);

            System.out.println("\n=== Liste des Clubs ===");
            for (Club c : serviceClub.display()) {
                System.out.println(c);
            }

            // ── TEST EVENT ─────────────────────────────────────
            int clubId = club.getId(); // utiliser l'ID auto-généré du club
            Event event = new Event("Hackathon 2025", "Compétition de code", new Date(), "Salle A101", "upcoming", clubId, 1);
            serviceEvent.add(event);

            System.out.println("\n=== Liste des Événements ===");
            for (Event e : serviceEvent.display()) {
                System.out.println(e);
            }

        } catch (SQLException e) {
            System.out.println("Erreur SQL : " + e.getMessage());
        }
    }
}
