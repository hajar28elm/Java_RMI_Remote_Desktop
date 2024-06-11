package org.example;

import java.awt.*;
import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Classe SharingImpl qui implémente SharingInterface pour gérer les interactions de partage d'écran et les événements de souris/clavier.
 * Extends UnicastRemoteObject et implémente SharingInterface.
 */
public class SharingImpl extends UnicastRemoteObject implements SharingInterface {

    private Robot robot; // Pour automatiser les interactions avec l'interface utilisateur.
    private String serverId; // Identifiant du serveur.
    BufferedImage screenshot; // Pour stocker les captures d'écran.
    byte[] imageInByte; // Pour stocker les captures d'écran en format byte array.
    boolean isConnected = true; // Indique si le client est correctement connecté.

    // Constructeur de la classe. Initialise le robot et définit l'identifiant du serveur.
    public SharingImpl(String serverId) throws RemoteException, AWTException {
        super();
        robot = new Robot();
        this.serverId = serverId;
    }

    // Méthode pour capturer une capture d'écran.
    @Override
    public byte[] captureScreenshot() throws RemoteException {
        if (isConnected) {
            try {
                // Capture l'écran entier.
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Rectangle screenSize = new Rectangle(toolkit.getScreenSize());
                screenshot = robot.createScreenCapture(screenSize);

                // Convertit l'image en un tableau de bytes.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();
                return imageInByte;
            } catch (IOException e) {
                throw new RemoteException("Failed to capture screenshot", e);
            }
        } else {
            System.out.println("Invalid registration attempt: Client ID does not match Server ID.");
            return imageInByte;
        }
    }

    // Méthode pour recevoir et traiter les positions et événements de la souris.
    @Override
    public void receiveMousePosition(int x, int y, MouseEvent event) throws RemoteException {
        robot.mouseMove(x, y); // Déplace la souris aux coordonnées spécifiées.
        int button = event.getButton();
        switch (event.getID()) {
            case MouseEvent.MOUSE_MOVED:
                System.out.println("Mouse moved to (" + x + ", " + y + ")");
                break;
            case MouseEvent.MOUSE_CLICKED:
                System.out.println("Mouse clicked at (" + x + ", " + y + ")");
                handleMouseClick(button);
                break;
            case MouseEvent.MOUSE_PRESSED:
                System.out.println("Mouse pressed at (" + x + ", " + y + ")");
                handleMousePress(button);
                break;
            case MouseEvent.MOUSE_RELEASED:
                System.out.println("Mouse released at (" + x + ", " + y + ")");
                handleMouseRelease(button);
                break;
            case MouseEvent.MOUSE_ENTERED:
                System.out.println("Mouse entered at (" + x + ", " + y + ")");
                robot.mouseMove(x, y);
                break;
            case MouseEvent.MOUSE_EXITED:
                System.out.println("Mouse exited at (" + x + ", " + y + ")");
                robot.mouseMove(x, y);
                break;
            default:
                System.out.println("Unknown mouse event type: " + event);
                break;
        }
    }

    // Gère les clics de souris.
    private void handleMouseClick(int button) {
        if (button == MouseEvent.BUTTON1) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } else if (button == MouseEvent.BUTTON3) {
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }
    }

    // Gère les pressions de souris.
    private void handleMousePress(int button) {
        if (button == MouseEvent.BUTTON1) {
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        } else if (button == MouseEvent.BUTTON3) {
            robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        }
    }

    // Gère les relâchements de souris.
    private void handleMouseRelease(int button) {
        if (button == MouseEvent.BUTTON1) {
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } else if (button == MouseEvent.BUTTON3) {
            robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
        }
    }

    // Enregistre un client en validant son identifiant.
    @Override
    public void registerClient(String serverId) throws RemoteException {
        if (serverId.equals(this.serverId)) {
            isConnected = true;
        } else {
            isConnected = false;
        }
    }

    // Reçoit et traite les événements de touches clavier.
    @Override
    public void receiveKeyPress(int keyCode, int eventType) throws RemoteException {
        switch (eventType) {
            case KeyEvent.KEY_PRESSED:
                System.out.println("Key pressed: " + KeyEvent.getKeyText(keyCode));
                robot.keyPress(keyCode);
                break;
            case KeyEvent.KEY_RELEASED:
                System.out.println("Key released: " + KeyEvent.getKeyText(keyCode));
                robot.keyRelease(keyCode);
                break;
            default:
                System.out.println("Unknown key event type: " + eventType);
                break;
        }
    }

    // Retourne la largeur de l'écran.
    @Override
    public int getScreenWidth() throws RemoteException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.width;
    }

    // Retourne la hauteur de l'écran.
    @Override
    public int getScreenHeight() throws RemoteException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return screenSize.height;
    }

    // Reçoit un fichier du client et le sauvegarde dans le répertoire courant.
    @Override
    public void receiveFile(byte[] fileData, String fileName) throws RemoteException {
        String currentDir = System.getProperty("user.dir"); // Récupère le répertoire de travail actuel.
        String filePath = currentDir + File.separator + fileName; // Crée le chemin complet du fichier.

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileData); // Écrit les données du fichier.
            System.out.println("File received and saved to current directory: " + fileName);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
            throw new RemoteException("Error saving file: " + e.getMessage());
        }
    }

    // Ouvre un sélecteur de fichiers pour permettre au client de choisir un fichier à envoyer.
    @Override
    public FileTransfer openFileChooser() throws RemoteException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to send");
        int result = fileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                byte[] fileData = fis.readAllBytes(); // Lit les données du fichier.
                String fileName = selectedFile.getName(); // Récupère le nom du fichier.
                return new FileTransfer(fileData, fileName); // Retourne les données du fichier et son nom.
            } catch (IOException e) {
                e.printStackTrace();
                throw new RemoteException("Error reading file: " + e.getMessage());
            }
        }
        return null; // Retourne null si aucun fichier n'est sélectionné.
    }
}




