package Controllers;

import Entities.Event;
import Services.ServiceEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class EventCalendarController {

    @FXML private Label lblMonthYear;
    @FXML private GridPane calendarGrid;

    private final ServiceEvent serviceEvent = new ServiceEvent();
    private YearMonth currentYearMonth;
    private List<Event> allEvents;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
        loadEvents();
        drawCalendar();
    }

    private void loadEvents() {
        try {
            allEvents = serviceEvent.display();
        } catch (SQLException e) {
            e.printStackTrace();
            allEvents = new ArrayList<>();
        }
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();
        
        // Header Label
        String monthName = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        lblMonthYear.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + currentYearMonth.getYear());

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeekOffset = firstOfMonth.getDayOfWeek().getValue() - 1; // 0 for Monday
        int daysInMonth = currentYearMonth.lengthOfMonth();

        LocalDate today = LocalDate.now();

        // Group events by day of the month
        Map<Integer, List<Event>> eventsByDay = allEvents.stream()
                .filter(e -> {
                    if (e.getEventDate() == null) return false;
                    
                    LocalDate date;
                    if (e.getEventDate() instanceof java.sql.Date) {
                        date = ((java.sql.Date) e.getEventDate()).toLocalDate();
                    } else {
                        date = e.getEventDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    
                    return YearMonth.from(date).equals(currentYearMonth);
                })
                .collect(Collectors.groupingBy(e -> {
                    LocalDate date;
                    if (e.getEventDate() instanceof java.sql.Date) {
                        date = ((java.sql.Date) e.getEventDate()).toLocalDate();
                    } else {
                        date = e.getEventDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    }
                    return date.getDayOfMonth();
                }));

        int row = 0;
        int col = dayOfWeekOffset;

        for (int day = 1; day <= daysInMonth; day++) {
            VBox cell = createDayCell(day, eventsByDay.getOrDefault(day, new ArrayList<>()), today);
            
            calendarGrid.add(cell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createDayCell(int day, List<Event> events, LocalDate today) {
        VBox cell = new VBox(5);
        cell.getStyleClass().add("calendar-cell");
        
        LocalDate cellDate = currentYearMonth.atDay(day);
        
        // Highlight Today
        if (cellDate.equals(today)) {
            cell.getStyleClass().add("calendar-cell-today");
        }

        // Highlight Days with Events
        if (!events.isEmpty()) {
            cell.getStyleClass().add("calendar-cell-has-event");
        }

        // Day Number
        Label lblDay = new Label(String.valueOf(day));
        lblDay.getStyleClass().add("day-number");
        
        HBox topRow = new HBox(lblDay);
        topRow.setAlignment(Pos.TOP_LEFT);
        
        // Event badge if many events
        if (events.size() > 1) {
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
            Label badge = new Label(String.valueOf(events.size()));
            badge.getStyleClass().add("event-count-badge");
            topRow.getChildren().addAll(spacer, badge);
        }
        
        cell.getChildren().add(topRow);

        // Event Titles (first 2)
        for (int i = 0; i < Math.min(events.size(), 2); i++) {
            Label lblEvent = new Label("• " + events.get(i).getTitle());
            lblEvent.getStyleClass().add("event-title-preview");
            lblEvent.setEllipsisString("...");
            lblEvent.setMaxWidth(80);
            cell.getChildren().add(lblEvent);
        }
        
        if (events.size() > 2) {
            Label lblMore = new Label("+" + (events.size() - 2) + " autres...");
            lblMore.getStyleClass().add("event-title-preview");
            cell.getChildren().add(lblMore);
        }

        return cell;
    }

    @FXML
    private void prevMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }

    @FXML
    private void nextMonth(ActionEvent event) {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }

    @FXML
    private void goBack(ActionEvent event) {
        MainDashboardController.getInstance().loadView("AfficherEvent.fxml", "Gestion des Événements");
    }
}
