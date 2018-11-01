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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Renato Correa
 */
public class ServerConnectionThread extends Thread {
    private final Socket socket;
    private final Node node;
    
    private BufferedReader bufferedReader;
            
    public ServerConnectionThread(Socket socket, Node node) {
        this.socket = socket;
        this.node = node;
    }
    
    private void receiveRequest() throws IOException {
        // Receive request
        int clock = Integer.parseInt(bufferedReader.readLine());
        int nodeId = Integer.parseInt(bufferedReader.readLine());
        
        // Receive response flag
        boolean isResponse = Boolean.parseBoolean(bufferedReader.readLine());
        
        int resourceId;
        if (!isResponse) {
            // Receive resource id
            resourceId = Integer.parseInt(bufferedReader.readLine());
            
            // Create request id
            RequestId requestId = new RequestId(clock, nodeId);
        
            // Store request resource
            node.getRequestResourceMap().put(requestId, resourceId);

            // Push request to the queue
            node.getRequestQueue(resourceId).pushRequest(requestId);
            
            // If the request isn't immediately approved
            if (node.getRequestQueue(resourceId).peek() != requestId) {
                // Send nak response
                node.sendNackResponse(requestId);
            }
        } else {
            // Ack flag
            boolean isAck = Boolean.parseBoolean(bufferedReader.readLine());
            
            // Request message informations
            int requestClock = Integer.parseInt(bufferedReader.readLine());
            int requestNodeId = Integer.parseInt(bufferedReader.readLine());
            
            // Create request id
            RequestId requestId = new RequestId(requestClock, requestNodeId);
            
            // Get resource id
            resourceId = node.getRequestResourceMap().get(requestId);
            
            if (isAck) {
                // Increment request responses
                node.getRequestQueue(resourceId).incrementRequestResponses(requestId);
            } else {
                synchronized (System.out) {
                    System.out.print("NAK recebido para o recurso " + resourceId + ".\n");
                }
            }
        }
        
        // Deliver requests
        node.updateRequestQueue(resourceId);
        
        // Update node clock
        node.updateClock(clock);
    }
    
    @Override
    public void run() {
        try {
            InputStreamReader streamReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(streamReader);
            
            while (this.isAlive())
                receiveRequest();
        } catch (IOException ex) {
            Logger.getLogger(ServerConnectionThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
