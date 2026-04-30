package com.elearning.service;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_videoio;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.IntBuffer;
import java.io.File;
import com.elearning.dao.UserDAO;
import com.elearning.entity.User;

/**
 * FaceRecognitionService — Moteur de reconnaissance faciale.
 *
 * Utilise opencv_videoio.VideoCapture directement (API OpenCV native)
 * à la place de JavaCV FrameGrabber, pour une stabilité maximale sur Windows.
 * Le read() natif bloque jusqu'à la vraie frame caméra — pas de frames corrompues.
 */
public class FaceRecognitionService {

    private static final String FACES_DIR     = "faces/";
    private static final int    NB_SAMPLES    = 20;
    private static final double SEUIL_CONF    = 80.0;
    private static final int    TIMEOUT_MS    = 60000;
    private static final int    LARGEUR       = 160;
    private static final int    HAUTEUR       = 160;
    private static final long   DELAI_CAPTURE = 300; // ms entre 2 captures

    private CascadeClassifier cascade;
    private boolean           nativesChargees = false;

    // --------------------------------------------------
    // Interfaces Callbacks
    // --------------------------------------------------

    public interface FrameCallback {
        void onFrame(javafx.scene.image.Image image);
    }

    public interface ProgressCallback {
        void onProgress(String message, int samplesCaptures, int samplesTotal);
        void onSuccess(String message);
        void onError(String message);
    }

    // --------------------------------------------------
    // Initialisation OpenCV
    // --------------------------------------------------

    public boolean initialiser() {
        if (nativesChargees) return true;
        try {
            try {
                Loader.load(org.bytedeco.openblas.global.openblas_nolapack.class);
                System.out.println("✅ OpenBLAS chargé.");
            } catch (Throwable t) {
                System.err.println("ℹ️ OpenBLAS skip: " + t.getMessage());
            }
            Loader.load(org.bytedeco.opencv.global.opencv_core.class);
            Loader.load(org.bytedeco.opencv.global.opencv_imgproc.class);
            Loader.load(org.bytedeco.opencv.global.opencv_objdetect.class);
            Loader.load(org.bytedeco.opencv.global.opencv_face.class);
            Loader.load(org.bytedeco.opencv.global.opencv_videoio.class);
            System.out.println("✅ OpenCV chargé.");

            cascade = chargerCascade();
            nativesChargees = (cascade != null && !cascade.empty());
            if (nativesChargees) System.out.println("✅ CascadeClassifier prêt.");
            else                 System.err.println("❌ CascadeClassifier vide.");
            return nativesChargees;
        } catch (Throwable e) {
            System.err.println("❌ Échec init OpenCV : " + e.getMessage());
            return false;
        }
    }

    // --------------------------------------------------
    // Ouvrir la caméra avec VideoCapture natif OpenCV
    // --------------------------------------------------

    private VideoCapture ouvrirCamera() {
        VideoCapture cap = new VideoCapture();
        // Essayer d'abord avec l'index 0
        cap.open(0);
        if (!cap.isOpened()) {
            System.err.println("❌ Impossible d'ouvrir la caméra 0.");
            return null;
        }
        cap.set(opencv_videoio.CAP_PROP_FRAME_WIDTH,  320);
        cap.set(opencv_videoio.CAP_PROP_FRAME_HEIGHT, 240);
        cap.set(opencv_videoio.CAP_PROP_FPS,          30);
        System.out.println("📷 Caméra ouverte : "
            + (int) cap.get(opencv_videoio.CAP_PROP_FRAME_WIDTH) + "x"
            + (int) cap.get(opencv_videoio.CAP_PROP_FRAME_HEIGHT)
            + " @" + (int) cap.get(opencv_videoio.CAP_PROP_FPS) + "fps");
        return cap;
    }

    // --------------------------------------------------
    // Convertir une Mat OpenCV → Image JavaFX
    // --------------------------------------------------

    private void envoyerMatVersUI(Mat matCouleur, FrameCallback cb) {
        try {
            if (cb == null || matCouleur == null || matCouleur.isNull() || matCouleur.empty()) return;
            OpenCVFrameConverter.ToMat conv  = new OpenCVFrameConverter.ToMat();
            Java2DFrameConverter       jconv = new Java2DFrameConverter();
            Frame f = conv.convert(matCouleur);
            BufferedImage bi = jconv.getBufferedImage(f);
            if (bi != null) {
                javafx.scene.image.WritableImage img =
                    javafx.embed.swing.SwingFXUtils.toFXImage(bi, null);
                cb.onFrame(img);
            }
        } catch (Exception ignored) {}
    }

    // --------------------------------------------------
    // ENREGISTREMENT — 20 échantillons du visage
    // --------------------------------------------------

    public void enregistrerVisage(int userId,
                                   FrameCallback frameCallback,
                                   ProgressCallback progressCb) {
        new Thread(() -> {
            VideoCapture cap = null;
            try {
                if (!initialiser()) {
                    progressCb.onError("❌ Impossible de charger OpenCV.");
                    return;
                }

                // Préparer le dossier
                Files.createDirectories(Paths.get(FACES_DIR));
                for (int i = 0; i < NB_SAMPLES; i++) {
                    Files.deleteIfExists(Paths.get(FACES_DIR + "face_" + userId + "_" + i + ".png"));
                }

                cap = ouvrirCamera();
                if (cap == null) {
                    progressCb.onError("❌ Webcam non disponible. Vérifiez qu'elle n'est pas utilisée par une autre application.");
                    return;
                }

                int  captures    = 0;
                long debut       = System.currentTimeMillis();
                long lastCapture = 0;
                Mat  frame       = new Mat();

                // --------------------------------------------------
                // VÉRIFICATION D'UNICITÉ AVANT D'ENREGISTRER
                // --------------------------------------------------
                progressCb.onProgress("🔍 Vérification de l'unicité du visage...", 0, NB_SAMPLES);
                boolean visageUniqueEtValide = false;

                while (!visageUniqueEtValide && System.currentTimeMillis() - debut < TIMEOUT_MS) {
                    boolean grabbed = cap.read(frame);
                    if (!grabbed || frame.empty()) continue;

                    Mat matGris = new Mat();
                    try {
                        opencv_imgproc.cvtColor(frame, matGris, opencv_imgproc.COLOR_BGR2GRAY);
                        opencv_imgproc.equalizeHist(matGris, matGris);

                        RectVector faces = new RectVector();
                        cascade.detectMultiScale(matGris, faces, 1.1, 4, 0, new Size(60, 60), new Size());

                        if (faces.size() > 0) {
                            org.bytedeco.opencv.opencv_core.Rect r = faces.get(0);
                            
                            // Afficher box
                            opencv_imgproc.rectangle(frame, r, new org.bytedeco.opencv.opencv_core.Scalar(255, 165, 0, 0), 3, 8, 0);

                            Mat clone = matGris.clone();
                            int nx = Math.max(0, r.x());
                            int ny = Math.max(0, r.y());
                            int nw = Math.min(r.width(), clone.cols() - nx);
                            int nh = Math.min(r.height(), clone.rows() - ny);

                            if (nw > 40 && nh > 40 && nx + nw <= clone.cols() && ny + nh <= clone.rows()) {
                                org.bytedeco.opencv.opencv_core.Rect rSafe = new org.bytedeco.opencv.opencv_core.Rect(nx, ny, nw, nh);
                                Mat roi  = new Mat(clone, rSafe);
                                Mat norm = new Mat();

                                if (!roi.empty()) {
                                    opencv_imgproc.resize(roi, norm, new Size(LARGEUR, HAUTEUR));
                                    if (!norm.empty()) {
                                        int propId = trouverProprietaireVisage(norm);
                                        if (propId != -1 && propId != userId) {
                                            // Ce visage appartient à un autre
                                            UserDAO userDAO = new UserDAO();
                                            User owner = userDAO.trouverParId(propId);
                                            String nom = owner != null ? owner.getFullName() : "Inconnu";
                                            progressCb.onError("⚠️ Ce visage appartient déjà à " + nom + ".\nUn visage ne peut être lié qu'à un seul compte.");
                                            roi.close(); norm.close(); rSafe.close(); clone.close(); frame.close(); cap.release();
                                            return;
                                        } else {
                                            // C'est bon, le visage est libre (ou appartient déjà à moi)
                                            visageUniqueEtValide = true;
                                        }
                                    }
                                }
                                roi.close(); norm.close(); rSafe.close();
                            }
                            clone.close();
                        }
                        envoyerMatVersUI(frame, frameCallback);
                    } finally {
                        if (!matGris.isNull()) matGris.close();
                    }
                }

                if (!visageUniqueEtValide) {
                    progressCb.onError("❌ Impossible de vérifier votre visage dans le temps imparti.");
                    return;
                }

                // --------------------------------------------------
                // DÉBUT DE L'ENREGISTREMENT (20 SAMPLES)
                // --------------------------------------------------
                debut = System.currentTimeMillis();
                progressCb.onProgress("📷 Placez votre visage face à la caméra...", 0, NB_SAMPLES);

                while (captures < NB_SAMPLES &&
                       System.currentTimeMillis() - debut < TIMEOUT_MS) {

                    // cap.read() BLOQUE jusqu'à la vraie frame — pas de frames corrompues
                    boolean grabbed = cap.read(frame);
                    if (!grabbed || frame.empty()) {
                        System.err.println("⏭ Frame vide, skip.");
                        continue;
                    }

                    Mat matGris = new Mat();
                    try {
                        opencv_imgproc.cvtColor(frame, matGris, opencv_imgproc.COLOR_BGR2GRAY);
                        opencv_imgproc.equalizeHist(matGris, matGris);

                        RectVector faces = new RectVector();
                        cascade.detectMultiScale(matGris, faces, 1.1, 4, 0,
                            new Size(60, 60), new Size());

                        System.out.println("[DETECT] faces=" + faces.size()
                            + " captures=" + captures
                            + " temps=" + (System.currentTimeMillis() - debut) + "ms");

                        if (faces.size() > 0) {
                            org.bytedeco.opencv.opencv_core.Rect r = faces.get(0);

                            // Rectangle vert sur le flux live
                            opencv_imgproc.rectangle(frame, r,
                                new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0), 3, 8, 0);
                            opencv_imgproc.putText(frame,
                                captures + "/" + NB_SAMPLES,
                                new org.bytedeco.opencv.opencv_core.Point(r.x(), Math.max(10, r.y() - 10)),
                                opencv_imgproc.FONT_HERSHEY_SIMPLEX, 0.7,
                                new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0), 2, 8, false);

                            long now = System.currentTimeMillis();
                            if (now - lastCapture >= DELAI_CAPTURE) {

                                // CROP DÉFENSIF
                                try {
                                    Mat clone = matGris.clone();
                                    int nx = Math.max(0, r.x());
                                    int ny = Math.max(0, r.y());
                                    int nw = Math.min(r.width(),  clone.cols() - nx);
                                    int nh = Math.min(r.height(), clone.rows() - ny);

                                    System.out.println("[CROP] nx=" + nx + " ny=" + ny
                                        + " nw=" + nw + " nh=" + nh
                                        + " cols=" + clone.cols() + " rows=" + clone.rows());

                                    if (nw > 40 && nh > 40
                                            && nx + nw <= clone.cols()
                                            && ny + nh <= clone.rows()) {

                                        org.bytedeco.opencv.opencv_core.Rect rSafe =
                                            new org.bytedeco.opencv.opencv_core.Rect(nx, ny, nw, nh);
                                        Mat roi  = new Mat(clone, rSafe);
                                        Mat norm = new Mat();

                                        if (!roi.empty()) {
                                            opencv_imgproc.resize(roi, norm, new Size(LARGEUR, HAUTEUR));
                                            if (!norm.empty()) {
                                                String chemin = FACES_DIR + "face_" + userId + "_" + captures + ".png";
                                                opencv_imgcodecs.imwrite(chemin, norm);
                                                captures++;
                                                lastCapture = now;
                                                System.out.println("💾 Échantillon " + captures + "/" + NB_SAMPLES + " → " + chemin);
                                                progressCb.onProgress("✅ Capture " + captures + "/" + NB_SAMPLES,
                                                    captures, NB_SAMPLES);
                                            }
                                        }
                                        roi.close(); norm.close(); rSafe.close();
                                    }
                                    clone.close();
                                } catch (Exception cropEx) {
                                    System.err.println("⏭ Crop skip : " + cropEx.getMessage());
                                }
                            }
                        } else {
                            if (captures == 0) {
                                progressCb.onProgress("👀 Aucun visage — restez face à la caméra.", 0, NB_SAMPLES);
                            }
                        }

                        envoyerMatVersUI(frame, frameCallback);

                    } finally {
                        if (!matGris.isNull()) matGris.close();
                    }
                }

                frame.close();

                if (captures >= NB_SAMPLES) {
                    System.out.println("✅ Enregistrement complet : " + captures + " échantillons.");
                    progressCb.onSuccess("✅ Visage enregistré avec succès !");
                } else {
                    String msg = "Enregistrement : " + captures + "/" + NB_SAMPLES
                        + " échantillons. Améliorez l'éclairage et réessayez.";
                    progressCb.onError("⚠️ " + msg);
                }

            } catch (Throwable e) {
                System.err.println("❌ Erreur enregistrement : " + e);
                e.printStackTrace();
                progressCb.onError("❌ Erreur : " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            } finally {
                try { if (cap != null) cap.release(); } catch (Exception ignored) {}
            }
        }, "FaceID-Enregistrement").start();
    }

    // --------------------------------------------------
    // VÉRIFICATION — Reconnaître un visage
    // --------------------------------------------------

    public void verifierVisage(int userId,
                                FrameCallback frameCallback,
                                ProgressCallback progressCb) {
        new Thread(() -> {
            VideoCapture cap = null;
            try {
                if (!initialiser()) {
                    progressCb.onError("❌ Impossible de charger OpenCV.");
                    return;
                }

                for (int i = 0; i < NB_SAMPLES; i++) {
                    if (!Files.exists(Paths.get(FACES_DIR + "face_" + userId + "_" + i + ".png"))) {
                        progressCb.onError("❌ Données biométriques incomplètes.\nEnregistrez d'abord votre visage depuis le Dashboard Admin.");
                        return;
                    }
                }

                // Entraîner LBPH
                MatVector images    = new MatVector(NB_SAMPLES);
                Mat       labels    = new Mat(NB_SAMPLES, 1, opencv_core.CV_32SC1);
                IntBuffer labelsBuf = labels.createBuffer();
                for (int i = 0; i < NB_SAMPLES; i++) {
                    Mat img = opencv_imgcodecs.imread(
                        FACES_DIR + "face_" + userId + "_" + i + ".png",
                        opencv_imgcodecs.IMREAD_GRAYSCALE);
                    images.put(i, img);
                    labelsBuf.put(i, userId);
                }
                LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
                recognizer.train(images, labels);
                System.out.println("🧠 Modèle LBPH entraîné sur " + NB_SAMPLES + " images.");

                cap = ouvrirCamera();
                if (cap == null) {
                    progressCb.onError("❌ Webcam non disponible.");
                    return;
                }

                long    debut   = System.currentTimeMillis();
                boolean reconnu = false;
                Mat     frame   = new Mat();

                progressCb.onProgress("🔍 Regardez la caméra...", 0, 0);

                while (!reconnu && System.currentTimeMillis() - debut < TIMEOUT_MS) {

                    boolean grabbed = cap.read(frame);
                    if (!grabbed || frame.empty()) continue;

                    Mat matGris = new Mat();
                    try {
                        opencv_imgproc.cvtColor(frame, matGris, opencv_imgproc.COLOR_BGR2GRAY);
                        opencv_imgproc.equalizeHist(matGris, matGris);

                        RectVector faces = new RectVector();
                        cascade.detectMultiScale(matGris, faces, 1.1, 4, 0,
                            new Size(60, 60), new Size());

                        if (faces.size() > 0) {
                            org.bytedeco.opencv.opencv_core.Rect r = faces.get(0);

                            try {
                                Mat clone = matGris.clone();
                                int nx = Math.max(0, r.x());
                                int ny = Math.max(0, r.y());
                                int nw = Math.min(r.width(),  clone.cols() - nx);
                                int nh = Math.min(r.height(), clone.rows() - ny);

                                if (nw > 40 && nh > 40
                                        && nx + nw <= clone.cols()
                                        && ny + nh <= clone.rows()) {

                                    org.bytedeco.opencv.opencv_core.Rect rSafe =
                                        new org.bytedeco.opencv.opencv_core.Rect(nx, ny, nw, nh);
                                    Mat roi  = new Mat(clone, rSafe);
                                    Mat norm = new Mat();

                                    if (!roi.empty()) {
                                        opencv_imgproc.resize(roi, norm, new Size(LARGEUR, HAUTEUR));
                                        if (!norm.empty()) {
                                            int[]    pred = {0};
                                            double[] conf = {0.0};
                                            recognizer.predict(norm, pred, conf);

                                            boolean match = pred[0] == userId && conf[0] < SEUIL_CONF;
                                            System.out.println("🔍 label=" + pred[0]
                                                + " conf=" + String.format("%.1f", conf[0])
                                                + " → " + (match ? "MATCH ✅" : "REJECT ❌"));

                                            org.bytedeco.opencv.opencv_core.Scalar couleur = match
                                                ? new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0)
                                                : new org.bytedeco.opencv.opencv_core.Scalar(0, 0, 255, 0);

                                            opencv_imgproc.rectangle(frame, r, couleur, 3, 8, 0);
                                            opencv_imgproc.putText(frame,
                                                match ? "✓ OK (" + String.format("%.0f", conf[0]) + ")"
                                                      : "✗ (" + String.format("%.0f", conf[0]) + ")",
                                                new org.bytedeco.opencv.opencv_core.Point(r.x(), Math.max(10, r.y() - 10)),
                                                opencv_imgproc.FONT_HERSHEY_SIMPLEX, 0.6, couleur, 2, 8, false);

                                            if (match) {
                                                reconnu = true;
                                                progressCb.onSuccess("VALIDE");
                                            } else {
                                                progressCb.onProgress(
                                                    "⚠️ Conf: " + String.format("%.0f", conf[0]) + " / seuil: " + (int) SEUIL_CONF,
                                                    0, 0);
                                            }
                                        }
                                    }
                                    roi.close(); norm.close(); rSafe.close();
                                }
                                clone.close();
                            } catch (Exception cropEx) {
                                System.err.println("⏭ Vérif crop skip : " + cropEx.getMessage());
                            }
                        } else {
                            progressCb.onProgress("👀 Aucun visage détecté...", 0, 0);
                        }

                        envoyerMatVersUI(frame, frameCallback);

                    } finally {
                        if (!matGris.isNull()) matGris.close();
                    }
                }

                frame.close();
                if (!reconnu) {
                    progressCb.onError("❌ Identité non confirmée. Réessayez ou utilisez votre mot de passe.");
                }
                recognizer.close(); images.close(); labels.close();

            } catch (Throwable e) {
                System.err.println("❌ Erreur vérification : " + e);
                e.printStackTrace();
                progressCb.onError("❌ Erreur : " + (e.getMessage() != null ? e.getMessage() : e.toString()));
            } finally {
                try { if (cap != null) cap.release(); } catch (Exception ignored) {}
            }
        }, "FaceID-Verification").start();
    }

    // --------------------------------------------------
    // Utilitaires publics
    // --------------------------------------------------

    public boolean visageEnregistre(int userId) {
        return Files.exists(Paths.get(FACES_DIR + "face_" + userId + "_0.png"));
    }

    public int trouverProprietaireVisage(Mat visageTest) {
        try {
            File dir = new File(FACES_DIR);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".png") && name.startsWith("face_"));
            if (files == null || files.length == 0) return -1;

            int nbImages = files.length;
            MatVector images = new MatVector(nbImages);
            Mat labels = new Mat(nbImages, 1, opencv_core.CV_32SC1);
            IntBuffer labelsBuf = labels.createBuffer();

            for (int i = 0; i < nbImages; i++) {
                File f = files[i];
                String[] parts = f.getName().split("_");
                if (parts.length >= 2) {
                    int id = Integer.parseInt(parts[1]);
                    Mat img = opencv_imgcodecs.imread(f.getAbsolutePath(), opencv_imgcodecs.IMREAD_GRAYSCALE);
                    images.put(i, img);
                    labelsBuf.put(i, id);
                }
            }

            LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
            recognizer.train(images, labels);

            int[] pred = {0};
            double[] conf = {0.0};
            recognizer.predict(visageTest, pred, conf);

            recognizer.close(); images.close(); labels.close();

            if (conf[0] < SEUIL_CONF) {
                return pred[0]; // Propriétaire trouvé
            }
            return -1; // Inconnu
        } catch (Exception e) {
            System.err.println("❌ Erreur trouverProprietaireVisage : " + e.getMessage());
            return -1;
        }
    }

    private CascadeClassifier chargerCascade() throws Exception {
        InputStream is = getClass().getResourceAsStream(
            "/com/elearning/cascade/haarcascade_frontalface_default.xml");
        if (is == null) { System.err.println("❌ haarcascade introuvable !"); return null; }
        Path tmp = Files.createTempFile("haarcascade_", ".xml");
        Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
        is.close();
        CascadeClassifier cc = new CascadeClassifier(tmp.toAbsolutePath().toString());
        return cc.empty() ? null : cc;
    }
}
