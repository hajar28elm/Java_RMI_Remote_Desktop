package org.example;



// Classe principale pour le client RMI.
// Cette classe contient le point d'entrée principal (main) pour le démarrage de l'application cliente.
public class ClientMain {

  // Méthode principale qui sert de point d'entrée pour l'application cliente.
  public static void main(String[] args) {
    // Crée une nouvelle instance de la classe Client et rend la fenêtre de l'application visible.
    // La classe Client hérite de JFrame et met en œuvre les interfaces nécessaires pour gérer
    // les événements de souris et de clavier, ainsi que pour interagir avec le serveur via RMI.
    new Client().setVisible(true);
  }
}

   