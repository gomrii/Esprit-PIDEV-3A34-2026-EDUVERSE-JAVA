package Controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class HomeController {

    @FXML
    private VBox teacherCard;

    @FXML
    private VBox adminCard;

    @FXML
    private VBox studentCard;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label pageSubtitleLabel;

    @FXML
    private void initialize() {
        boolean isAdmin = ControllerUtils.isAdminRole();
        boolean isTeacher = ControllerUtils.isTeacherRole();
        boolean isStudent = !isAdmin && !isTeacher;

        setVisible(adminCard, isAdmin);
        setVisible(teacherCard, isTeacher);
        setVisible(studentCard, isStudent);

        if (pageTitleLabel != null) {
            pageTitleLabel.setText(ControllerUtils.getRoleBasedQuizTitle());
        }
        if (pageSubtitleLabel != null) {
            pageSubtitleLabel.setText(switch (ControllerUtils.getCurrentRole()) {
                case "ADMIN" -> "Vue d'ensemble, validation des quiz et supervision globale de l'espace evaluation.";
                case "ENSEIGNANT" -> "Creation, generation IA, gestion de vos quiz et suivi de validation.";
                default -> "Consultation des quiz valides, passage, resultats et progression personnelle.";
            });
        }
    }

    @FXML
    private void openTeacherSpace(ActionEvent event) {
        open(event, "/teacher_quiz_list.fxml");
    }

    @FXML
    private void openAdminSpace(ActionEvent event) {
        open(event, "/admin_quiz_list.fxml");
    }

    @FXML
    private void openStudentSpace(ActionEvent event) {
        open(event, "/student_quiz_list.fxml");
    }

    private void open(ActionEvent event, String fxml) {
        ControllerUtils.navigateTo(
                (Node) event.getSource(),
                fxml,
                "Impossible d'ouvrir l'espace selectionne."
        );
    }

    private void setVisible(VBox box, boolean visible) {
        if (box == null) {
            return;
        }
        box.setVisible(visible);
        box.setManaged(visible);
    }
}
