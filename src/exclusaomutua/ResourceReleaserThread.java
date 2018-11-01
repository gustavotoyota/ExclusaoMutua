/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exclusaomutua;

import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gustavo
 */
public class ResourceReleaserThread extends Thread {
    private Node node;
            
    public ResourceReleaserThread(Node node) {
        this.node = node;
    }
    
    private void tryReleasingResource(int resourceId) throws IOException {
        boolean resource = node.getRequestQueue(resourceId).tryReleasingTopRequestResource();
        if (resource == false)
            return;

        synchronized (System.out) {
            System.out.print("Recurso " + resourceId + " liberado.\n");
        }
        
        node.updateRequestQueue(resourceId);
    }
    
    @Override
    public void run() {
        try {
            while (this.isAlive()) {
                for (int resourceId = 0; resourceId < ExclusaoMutua.NUM_RESOURCES; ++resourceId)
                    tryReleasingResource(resourceId);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResourceReleaserThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
