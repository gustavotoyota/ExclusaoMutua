/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package exclusaomutua;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Renato Correa
 */
public class ServerConnectionThread extends Thread {
    private static final Object printLock = new Object();
    
    private BufferedReader bufferedReader;
            
    public ServerConnectionThread(Socket socket, Node node) {
        this.socket = socket;
        this.node = node;
    }
    
    private void receiveRequest() throws IOException, InterruptedException {
        // Receive request
        int clock = Integer.parseInt(bufferedReader.readLine());
        int nodeId = Integer.parseInt(bufferedReader.readLine());
        
        // Update node clock
        node.updateClock(clock);
        
        boolean isResponse = Boolean.parseBoolean(bufferedReader.readLine());
        
        if (!isResponse) {
            // Create request id
            RequestId requestId = new RequestId(clock, nodeId);
            
            // Push request to the queue
            node.getRequestQueue().pushRequest(requestId);
        } else {
            // Request message informations
            int requestClock = Integer.parseInt(bufferedReader.readLine());
            int requestNodeId = Integer.parseInt(bufferedReader.readLine());
            
            // Create request id
            RequestId requestId = new RequestId(requestClock, requestNodeId);
            
            // Increment request responses
            node.getRequestQueue().incrementRequestResponses(requestId);
        }

        // Try to deliver the request
        while (true) {
            RequestId requestId = node.getRequestQueue().tryDeliveringRequest();
            if (requestId == null)
                break;
            
            if (requestId.getNodeId() == node.getId()) {
                // Print the delivered request
                synchronized (System.out) {
                    System.out.print("To com o bagulho!!!\n");
                }

                ServerConnectionThread.sleep(4000);
            } else
                node.sendResponse(requestId);
        }
    }
    
    @Override
    public void run() {
        try {
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(streamReader);
            
            while (this.isAlive()) {
                receiveRequest();
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(ServerConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private final Socket socket;
    private final Node node;
}
