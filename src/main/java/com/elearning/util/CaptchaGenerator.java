package com.elearning.util;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.Random;

/**
 * CAPTCHA visuel basé sur l'orientation (Puzzle).
 * L'utilisateur doit orienter une forme dans la direction demandée.
 */
public class CaptchaGenerator {

    private static final Random RANDOM = new Random();
    
    private double angleInitial;
    private double currentSliderAngle = 0;
    
    private double targetAngle = 0;
    private String instructionTexte = "";

    /**
     * Génère un nouveau puzzle d'orientation.
     */
    public int genererEtDessiner(Canvas canvas) {
        // Choix d'une direction cible (0=Haut, 90=Droite, 180=Bas, -90=Gauche)
        int dir = RANDOM.nextInt(4);
        switch (dir) {
            case 0 -> { targetAngle = 0;   instructionTexte = "Pointez la flèche vers le HAUT"; }
            case 1 -> { targetAngle = 90;  instructionTexte = "Pointez la flèche vers la DROITE"; }
            case 2 -> { targetAngle = 180; instructionTexte = "Pointez la flèche vers le BAS"; }
            case 3 -> { targetAngle = -90; instructionTexte = "Pointez la flèche vers la GAUCHE"; }
        }

        // Angle initial aléatoire, éloigné de la cible
        angleInitial = targetAngle + 90 + RANDOM.nextDouble() * 180;
        if (angleInitial > 180) angleInitial -= 360;
        
        currentSliderAngle = 0;
        dessinerRotation(canvas, 0);
        return 0; // Inutilisé
    }

    /**
     * Redessine le canvas avec la rotation appliquée par le slider.
     */
    public void dessinerRotation(Canvas canvas, double sliderAngle) {
        this.currentSliderAngle = sliderAngle;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Fond sombre
        gc.setFill(Color.web("#1a2035"));
        gc.fillRect(0, 0, w, h);

        // Dessiner le cercle central (boussole de fond)
        gc.setStroke(Color.color(1, 1, 1, 0.2));
        gc.setLineWidth(2);
        gc.strokeOval(w / 2 - 25, h / 2 - 25, 50, 50);

        // Repères
        gc.setFill(Color.color(1, 1, 1, 0.5));
        gc.fillOval(w/2 - 2, h/2 - 28, 4, 4); // Haut
        gc.fillOval(w/2 - 2, h/2 + 24, 4, 4); // Bas
        gc.fillOval(w/2 - 28, h/2 - 2, 4, 4); // Gauche
        gc.fillOval(w/2 + 24, h/2 - 2, 4, 4); // Droite

        // Dessiner la forme avec la rotation combinée
        double angleTotal = angleInitial + currentSliderAngle;

        gc.save();
        gc.translate(w / 2, h / 2);
        gc.rotate(angleTotal);
        
        // Forme : Une flèche épaisse
        gc.setFill(Color.web("#34db55")); // Vert vif
        
        // Triangle du haut
        double[] xPoints = {0, -15, 15};
        double[] yPoints = {-20, 0, 0};
        gc.fillPolygon(xPoints, yPoints, 3);
        
        // Rectangle du bas (corps de la flèche)
        gc.fillRect(-6, 0, 12, 20);

        gc.restore();
    }

    /** La réponse attendue n'est pas un texte exact mais une position. */
    public String getReponseAttendueTexte() {
        return instructionTexte;
    }

    public int getReponseAttendue() { return 0; }

    /**
     * Vérifie si l'orientation finale correspond à la cible (tolérance de ±18 degrés).
     */
    public boolean verifier(String saisie) {
        try {
            double angleSaisi = Double.parseDouble(saisie);
            // On calcule l'angle final (modulo 360)
            double angleFinal = (angleInitial + angleSaisi) % 360;
            // Normaliser entre -180 et 180
            if (angleFinal > 180) angleFinal -= 360;
            if (angleFinal < -180) angleFinal += 360;
            
            // Différence avec l'angle cible
            double diff = Math.abs(angleFinal - targetAngle);
            if (diff > 180) diff = 360 - diff;

            // Tolérance de 18 degrés
            return diff <= 18;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
