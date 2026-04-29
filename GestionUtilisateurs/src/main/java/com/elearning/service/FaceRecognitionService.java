package com.elearning.service;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_face.LBPHFaceRecognizer;
import org.bytedeco.javacv.FrameGrabber;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.IntBuffer;

public class FaceRecognitionService {

    private static final String FACES_DIR   = "faces/";
    private static final int    NB_SAMPLES  = 15;
    private static final double SEUIL_CONF  = 65.0;
    private static final int    TIMEOUT_MS  = 15000;
    private static final int    LARGEUR     = 160;
    private static final int    HAUTEUR     = 160;

    private CascadeClassifier cascade;
    private boolean           nativesChargees = false;

    public interface FrameCallback {
        void onFrame(javafx.scene.image.Image image);
    }

    public interface ProgressCallback {
        void onProgress(String message, int samplesCaptures, int samplesTotal);
        void onSuccess(String message);
        void onError(String message);
    }

    public boolean initialiser() {
        if (nativesChargees) return true;
        try {
            // Tentative de chargement d'OpenBLAS en premier (souvent la cause du crash sur Windows)
            try {
                Loader.load(org.bytedeco.openblas.global.openblas_nolapack.class);
            } catch (Throwable t) {
                System.err.println("Note: OpenBLAS load failed, continuing... " + t.getMessage());
            }

            // Chargement des modules OpenCV
            Loader.load(org.bytedeco.opencv.global.opencv_core.class);
            Loader.load(org.bytedeco.opencv.global.opencv_imgproc.class);
            Loader.load(org.bytedeco.opencv.global.opencv_objdetect.class);
            Loader.load(org.bytedeco.opencv.global.opencv_face.class);
            
            cascade = chargerCascade();
            nativesChargees = (cascade != null && !cascade.empty());
            return nativesChargees;
        } catch (Throwable e) {
            System.err.println("⚠ OpenCV critical failure: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void enregistrerVisage(int userId,
                                   FrameCallback frameCallback,
                                   ProgressCallback progressCb) {
        new Thread(() -> {
            FrameGrabber grabber = null;
            try {
                if (!initialiser()) {
                    progressCb.onError("Erreur d'initialisation des bibliothèques natives (OpenCV/OpenBLAS).");
                    return;
                }
                Files.createDirectories(Paths.get(FACES_DIR));
                for (int i = 0; i < NB_SAMPLES; i++) {
                    Files.deleteIfExists(Paths.get(FACES_DIR + "face_" + userId + "_" + i + ".png"));
                }

                grabber = FrameGrabber.createDefault(0);
                grabber.setImageWidth(320);
                grabber.setImageHeight(240);
                grabber.start();

                OpenCVFrameConverter.ToMat matConv = new OpenCVFrameConverter.ToMat();
                Java2DFrameConverter swingConv = new Java2DFrameConverter();
                int captures = 0;
                long debut = System.currentTimeMillis();

                progressCb.onProgress("📷 Placez votre visage face à la caméra...", 0, NB_SAMPLES);

                while (captures < NB_SAMPLES &&
                       System.currentTimeMillis() - debut < TIMEOUT_MS) {

                    Frame frame = grabber.grab();
                    if (frame == null || frame.image == null) continue;

                    Mat matCouleur = matConv.convert(frame);
                    Mat matGris = new Mat();
                    opencv_imgproc.cvtColor(matCouleur, matGris, opencv_imgproc.COLOR_BGR2GRAY);
                    opencv_imgproc.equalizeHist(matGris, matGris);

                    RectVector faces = new RectVector();
                    cascade.detectMultiScale(matGris, faces, 1.1, 5, 0,
                        new Size(80, 80), new Size(300, 300));

                    if (faces.size() > 0) {
                        org.bytedeco.opencv.opencv_core.Rect r = faces.get(0);

                        opencv_imgproc.rectangle(matCouleur, r,
                            new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0), 3, 8, 0);

                        opencv_imgproc.putText(matCouleur,
                            "Capture " + (captures + 1) + "/" + NB_SAMPLES,
                            new org.bytedeco.opencv.opencv_core.Point(r.x(), r.y() - 10),
                            opencv_imgproc.FONT_HERSHEY_SIMPLEX, 0.6,
                            new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0), 2, 8, false);

                        Mat faceRoi = new Mat(matGris, r);
                        Mat faceNorm = new Mat();
                        opencv_imgproc.resize(faceRoi, faceNorm, new Size(LARGEUR, HAUTEUR));
                        String chemin = FACES_DIR + "face_" + userId + "_" + captures + ".png";
                        opencv_imgcodecs.imwrite(chemin, faceNorm);
                        faceRoi.close(); faceNorm.close();
                        captures++;

                        progressCb.onProgress("✅ Capture " + captures + "/" + NB_SAMPLES,
                            captures, NB_SAMPLES);
                        Thread.sleep(200);
                    }

                    Frame frameAnnotee = matConv.convert(matCouleur);
                    BufferedImage bi = swingConv.getBufferedImage(frameAnnotee);
                    if (bi != null && frameCallback != null) {
                        javafx.scene.image.WritableImage fxImg =
                            javafx.embed.swing.SwingFXUtils.toFXImage(bi, null);
                        frameCallback.onFrame(fxImg);
                    }

                    matGris.close(); matCouleur.close();
                }

                if (captures >= NB_SAMPLES) {
                    progressCb.onSuccess("✅ Visage enregistré avec succès ! (" + NB_SAMPLES + " échantillons)");
                } else {
                    progressCb.onError("❌ Enregistrement incomplet. Réessayez dans un endroit mieux éclairé.");
                }

            } catch (Throwable e) {
                progressCb.onError("❌ Erreur technique : " + (e.getMessage() != null ? e.getMessage() : e.toString()));
                e.printStackTrace();
            } finally {
                if (grabber != null) try { grabber.stop(); grabber.release(); } catch (Exception ignored) {}
            }
        }).start();
    }

    public void verifierVisage(int userId,
                                FrameCallback frameCallback,
                                ProgressCallback progressCb) {
        new Thread(() -> {
            FrameGrabber grabber = null;
            try {
                if (!initialiser()) {
                    progressCb.onError("Erreur d'initialisation des bibliothèques natives (OpenCV/OpenBLAS).");
                    return;
                }

                MatVector images = new MatVector(NB_SAMPLES);
                Mat labels = new Mat(NB_SAMPLES, 1, opencv_core.CV_32SC1);
                IntBuffer labelsBuf = labels.createBuffer();

                for (int i = 0; i < NB_SAMPLES; i++) {
                    String p = FACES_DIR + "face_" + userId + "_" + i + ".png";
                    if (!Files.exists(Paths.get(p)))
                        throw new Exception("Données biométriques incomplètes. Enregistrez d'abord votre visage.");
                    Mat img = opencv_imgcodecs.imread(p, opencv_imgcodecs.IMREAD_GRAYSCALE);
                    images.put(i, img);
                    labelsBuf.put(i, userId);
                }

                LBPHFaceRecognizer recognizer = LBPHFaceRecognizer.create();
                recognizer.train(images, labels);

                grabber = FrameGrabber.createDefault(0);
                grabber.setImageWidth(320);
                grabber.setImageHeight(240);
                grabber.start();

                OpenCVFrameConverter.ToMat matConv = new OpenCVFrameConverter.ToMat();
                Java2DFrameConverter swingConv = new Java2DFrameConverter();
                long debut = System.currentTimeMillis();
                boolean reconnu = false;

                progressCb.onProgress("🔍 Regardez la caméra...", 0, 0);

                while (!reconnu && System.currentTimeMillis() - debut < TIMEOUT_MS) {

                    Frame frame = grabber.grab();
                    if (frame == null || frame.image == null) continue;

                    Mat matCouleur = matConv.convert(frame);
                    Mat matGris = new Mat();
                    opencv_imgproc.cvtColor(matCouleur, matGris, opencv_imgproc.COLOR_BGR2GRAY);
                    opencv_imgproc.equalizeHist(matGris, matGris);

                    RectVector faces = new RectVector();
                    cascade.detectMultiScale(matGris, faces, 1.1, 5, 0,
                        new Size(80, 80), new Size(300, 300));

                    if (faces.size() > 0) {
                        org.bytedeco.opencv.opencv_core.Rect r = faces.get(0);
                        Mat faceRoi = new Mat(matGris, r);
                        Mat faceNorm = new Mat();
                        opencv_imgproc.resize(faceRoi, faceNorm, new Size(LARGEUR, HAUTEUR));

                        int[] predictedLabel = {0};
                        double[] confidence = {0.0};
                        recognizer.predict(faceNorm, predictedLabel, confidence);

                        boolean match = predictedLabel[0] == userId && confidence[0] < SEUIL_CONF;

                        org.bytedeco.opencv.opencv_core.Scalar couleur = match
                            ? new org.bytedeco.opencv.opencv_core.Scalar(0, 255, 0, 0)
                            : new org.bytedeco.opencv.opencv_core.Scalar(0, 0, 255, 0);

                        opencv_imgproc.rectangle(matCouleur, r, couleur, 3, 8, 0);

                        String texte = match
                            ? "✓ Reconnu (" + String.format("%.0f", confidence[0]) + ")"
                            : "✗ Non reconnu (" + String.format("%.0f", confidence[0]) + ")";

                        opencv_imgproc.putText(matCouleur, texte,
                            new org.bytedeco.opencv.opencv_core.Point(r.x(), r.y() - 10),
                            opencv_imgproc.FONT_HERSHEY_SIMPLEX, 0.5, couleur, 2, 8, false);

                        if (match) {
                            reconnu = true;
                            progressCb.onSuccess("VALIDE");
                        } else {
                            progressCb.onProgress("⚠️ Visage détecté, vérification...", 0, 0);
                        }

                        faceRoi.close(); faceNorm.close();
                    } else {
                        progressCb.onProgress("👀 Aucun visage détecté...", 0, 0);
                    }

                    Frame frameAnnotee = matConv.convert(matCouleur);
                    BufferedImage bi = swingConv.getBufferedImage(frameAnnotee);
                    if (bi != null && frameCallback != null) {
                        javafx.scene.image.WritableImage fxImg =
                            javafx.embed.swing.SwingFXUtils.toFXImage(bi, null);
                        frameCallback.onFrame(fxImg);
                    }

                    matGris.close(); matCouleur.close();
                    Thread.sleep(50);
                }

                if (!reconnu) {
                    progressCb.onError("❌ Visage non reconnu. Réessayez.");
                }

                recognizer.close(); images.close(); labels.close();

            } catch (Throwable e) {
                progressCb.onError("❌ Erreur technique : " + (e.getMessage() != null ? e.getMessage() : e.toString()));
                e.printStackTrace();
            } finally {
                if (grabber != null) try { grabber.stop(); grabber.release(); } catch (Exception ignored) {}
            }
        }).start();
    }

    public boolean visageEnregistre(int userId) {
        return Files.exists(Paths.get(FACES_DIR + "face_" + userId + "_0.png"));
    }

    private CascadeClassifier chargerCascade() throws Exception {
        InputStream is = getClass().getResourceAsStream("/com/elearning/cascade/haarcascade_frontalface_default.xml");
        if (is == null) return null;

        Path tmp = Files.createTempFile("haarcascade_", ".xml");
        Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
        is.close();

        CascadeClassifier cc = new CascadeClassifier(tmp.toAbsolutePath().toString());
        return cc.empty() ? null : cc;
    }
}
