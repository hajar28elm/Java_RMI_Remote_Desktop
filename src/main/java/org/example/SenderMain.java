package org.example;

import java.rmi.registry.Registry;

import javax.swing.JOptionPane;

import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;

/**
 * Classe SenderMain qui initialise le serveur RMI pour le partage d'écran.
 */
public class SenderMain {
    /**
     * Méthode principale qui démarre le serveur RMI.
     * @param args Arguments de la ligne de commande.
     */

    public static void main(String[] args) {
        try {
            // Demande à l'utilisateur d'entrer un ID pour le serveur via une boîte de dialogue.
            String serverId = JOptionPane.showInputDialog(null, "Enter Server ID:", "Server ID Input", JOptionPane.PLAIN_MESSAGE);

            // Vérifie que l'utilisateur a bien entré un ID de serveur.
            if (serverId != null && !serverId.trim().isEmpty()) {
                // Crée une instance de l'implémentation du serveur avec l'ID fourni.
                SharingImpl server = new SharingImpl(serverId);

                // Crée un registre RMI sur le port 1099.
                Registry registry = LocateRegistry.createRegistry(1099);

                // Lie l'instance du serveur au registre sous le nom "Server".
                registry.rebind("Server", server);

                // Obtient l'adresse IP locale du serveur.
                InetAddress ipAddress = InetAddress.getLocalHost();
                String serverIpAddress = ipAddress.getHostAddress();

                // Affiche l'adresse IP du serveur et un message de confirmation de démarrage.
                System.out.println("Server is running on IP address: " + serverIpAddress);
                System.out.println("Server is running...");
            } else {
                // Affiche un message d'erreur si l'ID du serveur est vide ou nul.
                System.out.println("Server ID cannot be empty. Exiting...");
            }
        } catch (Exception e) {
            // En cas d'exception, affiche le message d'erreur et la pile d'appels.
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}


