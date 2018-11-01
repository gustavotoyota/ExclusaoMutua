/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exclusaomutua;

import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Gustavo
 */
public class ExclusaoMutua {
    public static final int NUM_NODES = 3;
    public static final int NUM_RESOURCES = 2;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // Read node id
        System.out.print("Id: ");
        Scanner scanner = new Scanner(System.in);
        int id = scanner.nextInt();
        scanner.nextLine();
        
        // Initialize node
        Node node = new Node(id);
        
        // Initialize server first
        node.initializeServer();
        
        // Wait for all servers to be initialized
        System.out.println("Esperando inicialização dos servidores.");
        System.out.print("Digite enter para continuar.");
        scanner.nextLine();
        
        // Initialize clients later
        node.initializeClients();
        
        // Handle message writing
        while (true) {
            int resourceId;
            synchronized (System.in) {
                // Read user input message
                resourceId = scanner.nextInt();
            }
            
            // Multicast the message
            node.multicastRequest(resourceId);
        }
    }
    
}
