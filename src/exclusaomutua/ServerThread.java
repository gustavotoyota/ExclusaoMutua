/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package exclusaomutua;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Renato Correa
 */
public class ServerThread extends Thread {
    public ServerThread(Node node) {
        this.node = node;
    }
    
    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(3031 + node.getId());
            
            while (this.isAlive()) {
                Socket clientSocket = serverSocket.accept();
                
                ServerConnectionThread clientThread = new ServerConnectionThread(clientSocket, node);
                clientThread.start();
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private final Node node;
}
