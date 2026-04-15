

import edu.connexion3a36.entities.Personne;
import edu.connexion3a36.services.PersonneService;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersonneServiceTest {

    PersonneService service = new PersonneService();

    @Test
    public void testAddEntity2() {

        Personne p = new Personne();
        p.setNom("Monia");
        p.setPrenom("TestPrenom");

        try {
            service.addEntity2(p);

            List<Personne> personnes = service.getData();

            boolean found = false;

            for (Personne per : personnes) {
                if (per.getNom().equals("Monia") &&
                        per.getPrenom().equals("TestPrenom")) {
                    found = true;
                    break;
                }
            }

            assertTrue(found);

        } catch (SQLException e) {
            fail("Error while inserting person: " + e.getMessage());
        }
    }

    @Test
    public void testGetData() {

        try {

            List<Personne> personnes = service.getData();

            assertNotNull(personnes);

        } catch (SQLException e) {
            fail("Error retrieving data: " + e.getMessage());
        }
    }

}