package org.example;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.rmi.*;
import java.rmi.registry.*;
import javax.imageio.ImageIO;

/**
 * Classe Client qui représente l'application cliente pour recevoir les captures d'écran,
 * envoyer les positions de souris, les clics de clavier et gérer le transfert de fichiers.
 * Extends JFrame et implémente MouseListener, KeyListener, MouseMotionListener, Serializable.
 */

public class Client extends JFrame implements MouseListener, KeyListener, MouseMotionListener, Serializable {
    private SharingInterface server; // Interface de communication avec le serveur.
    private JPanel panel; // Panel pour afficher les captures d'écran.
    private JMenuBar menuBar; // Barre de menu pour les options de fichier.
    private BufferedImage currentScreenshot; // Image courante de la capture d'écran reçue.

    // Constructeur du client.
    public Client() {
        super("Received Screenshot");
        // Demande à l'utilisateur d'entrer l'ID du serveur.
        String serverId = JOptionPane.showInputDialog(null, "Enter Sender ID:", "Sender ID Input", JOptionPane.PLAIN_MESSAGE);
        if (serverId != null && !serverId.trim().isEmpty()) {
            try {
                // Connexion au registre RMI.
                // Obtient une référence au registre RMI en utilisant l'adresse IP et le port spécifiés.
                Registry registry = LocateRegistry.getRegistry("100.70.34.172", 1099); // Remplacer par l'adresse IP réelle du serveur.
                // Recherche et récupère l'objet distant "Server" dans le registre RMI.
                server = (SharingInterface) registry.lookup("Server");
                server.registerClient(serverId);
            } catch (NotBoundException | RemoteException e) {
                JOptionPane.showMessageDialog(this, "Could not connect to server. Please check the server IP and try again.", "Connection Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1);
            }

            // Initialisation du panneau pour afficher les captures d'écran.
            panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (currentScreenshot != null) {
                        // Redimensionne l'image à la taille du panneau.
                        Image scaledImage = currentScreenshot.getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
                        g.drawImage(scaledImage, 0, 0, null);
                    }
                }
            };

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            getContentPane().add(panel);

            // Configuration de la barre de menu.
            menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem sendFileItem = new JMenuItem("Send File");
            JMenuItem receiveFileItem = new JMenuItem("Receive File");
            sendFileItem.addActionListener(e -> sendFile());
            receiveFileItem.addActionListener(e -> {
                try {
                    receiveFile();
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            });
            fileMenu.add(sendFileItem);
            fileMenu.add(receiveFileItem);
            menuBar.add(fileMenu);
            setJMenuBar(menuBar);

            // Configuration pour le plein écran.
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            if (gd.isFullScreenSupported()) {
                setUndecorated(true);
                gd.setFullScreenWindow(this);
            } else {
                System.err.println("Full screen not supported");
                setSize(800, 600);
                setLocationRelativeTo(null);
            }

            // Ajout des écouteurs d'événements pour la souris et le clavier.
            panel.addMouseListener(this);
            panel.addMouseMotionListener(this);
            panel.addKeyListener(this);
            panel.setFocusable(true);

            // Thread pour recevoir en continu les captures d'écran du serveur.
            new Thread(() -> {
                while (true) {
                    try {
                        byte[] screenshotData = server.captureScreenshot();
                        if (screenshotData != null) {
                            currentScreenshot = receiveScreenshot(screenshotData);
                            panel.repaint();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            JOptionPane.showMessageDialog(this, "Sender ID cannot be empty. Exiting...", "Input Error", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }

    // Méthode pour recevoir et traiter une capture d'écran.
    public BufferedImage receiveScreenshot(byte[] imageData) throws RemoteException {
        try (InputStream in = new ByteArrayInputStream(imageData)) {
            BufferedImage screenshot = ImageIO.read(in);
            Dimension clientScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int clientScreenWidth = clientScreenSize.width;
            int clientScreenHeight = clientScreenSize.height;
            int serverScreenWidth = server.getScreenWidth();
            int serverScreenHeight = server.getScreenHeight();
            double scaleX = (double) clientScreenWidth / serverScreenWidth;
            double scaleY = (double) clientScreenHeight / serverScreenHeight;
            double scale = Math.min(scaleX, scaleY);
            AffineTransform tx = AffineTransform.getScaleInstance(scale, scale);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage scaledImage = op.filter(screenshot, null);
            return scaledImage;
        } catch (IOException e) {
            throw new RemoteException("Failed to process received screenshot", e);
        }
    }

    // Méthode pour envoyer la position de la souris au serveur.
    private void sendMousePosition(MouseEvent e, int eventType) {
        try {
            Point panelPoint = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), panel);
            int panelWidth = panel.getWidth();
            int panelHeight = panel.getHeight();
            int serverScreenWidth = server.getScreenWidth();
            int serverScreenHeight = server.getScreenHeight();
            double xScaleFactor = (double) serverScreenWidth / panelWidth;
            double yScaleFactor = (double) serverScreenHeight / panelHeight;
            int scaledX = (int) (panelPoint.x * xScaleFactor);
            int scaledY = (int) (panelPoint.y * yScaleFactor);

            server.receiveMousePosition(scaledX, scaledY, e);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    // Implémentations des méthodes de MouseListener.
    @Override
    public void mouseClicked(MouseEvent e) {
        sendMousePosition(e, MouseEvent.MOUSE_CLICKED);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        sendMousePosition(e, MouseEvent.MOUSE_PRESSED);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        sendMousePosition(e, MouseEvent.MOUSE_RELEASED);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        sendMousePosition(e, MouseEvent.MOUSE_ENTERED);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        sendMousePosition(e, MouseEvent.MOUSE_EXITED);
    }

    // Implémentations des méthodes de MouseMotionListener.
    @Override
    public void mouseDragged(MouseEvent e) {
        sendMousePosition(e, MouseEvent.MOUSE_DRAGGED);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        sendMousePosition(e, MouseEvent.MOUSE_MOVED);
    }

    // Implémentations des méthodes de KeyListener.
    @Override
    public void keyPressed(KeyEvent e) {
        try {
            server.receiveKeyPress(e.getKeyCode(), KeyEvent.KEY_PRESSED);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            server.receiveKeyPress(e.getKeyCode(), KeyEvent.KEY_RELEASED);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Généralement, pas besoin de traiter keyTyped pour ce cas d'utilisation.
    }

    // Méthode pour envoyer un fichier au serveur.
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(selectedFile)) {
                byte[] fileData = fis.readAllBytes();
                server.receiveFile(fileData, selectedFile.getName());
                JOptionPane.showMessageDialog(this, "File sent successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to send file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Méthode pour recevoir un fichier du serveur.
    public void receiveFile() throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            new Thread(() -> {
                FileTransfer fileTransfer = null;
                try {
                    fileTransfer = server.openFileChooser();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                byte[] fileData = fileTransfer.getFileData();
                String fileName = fileTransfer.getFileName();
                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                    fos.write(fileData);
                    JOptionPane.showMessageDialog(null, "File received: " + fileName, "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Failed to save file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        });
    }
}
