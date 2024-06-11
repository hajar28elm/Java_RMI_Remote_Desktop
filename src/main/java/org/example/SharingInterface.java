package org.example;


import java.awt.event.MouseEvent;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 * Déclaration de l'interface SharingInterface qui étend l'interface Remote.
 * Cette interface définit les méthodes que le serveur doit implémenter pour être accessible à distance via RMI.
 */


public interface SharingInterface extends Remote {

    // Méthode pour capturer une capture d'écran sur le serveur et retourner l'image en tant que tableau d'octets.
    // Doit être implémentée par le serveur et peut lancer une RemoteException en cas de problème de communication.
    byte[] captureScreenshot() throws RemoteException;

    // Méthode pour recevoir la position de la souris et les événements de souris du client.
    // Prend les coordonnées x et y de la souris ainsi que l'événement de souris.
    void receiveMousePosition(int x, int y, MouseEvent z) throws RemoteException;

    // Méthode pour enregistrer un client auprès du serveur en utilisant un identifiant de client.
    // Doit être appelée par le client pour s'identifier au serveur.
    void registerClient(String clientId) throws RemoteException;

    // Méthode pour recevoir les événements de touches de clavier du client.
    // Prend le code de la touche et le type d'événement (appui ou relâchement).
    void receiveKeyPress(int keyCode, int eventType) throws RemoteException;

    // Méthode pour obtenir la largeur de l'écran du serveur.
    // Retourne la largeur de l'écran en pixels.
    int getScreenWidth() throws RemoteException;

    // Méthode pour obtenir la hauteur de l'écran du serveur.
    // Retourne la hauteur de l'écran en pixels.
    int getScreenHeight() throws RemoteException;

    // Méthode pour recevoir un fichier du client.
    // Prend les données du fichier sous forme de tableau d'octets et le nom du fichier.
    void receiveFile(byte[] fileData, String fileName) throws RemoteException;

    // Méthode pour ouvrir un sélecteur de fichiers sur le serveur et retourner les données du fichier sélectionné.
    // Retourne un objet FileTransfer contenant les données du fichier et son nom.
    FileTransfer openFileChooser() throws RemoteException;
}

