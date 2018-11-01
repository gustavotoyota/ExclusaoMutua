package exclusaomutua;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gustavo
 */
public class Node {
    // Node informations
    private int clock;
    private final int id;
    
    // Client socket writers
    private BufferedWriter[] writers;
    
    // Request map
    private final ConcurrentHashMap<RequestId, Integer> requestResourceMap;
    
    // Request queue
    private final RequestQueue[] requestQueues;
    
    public Node(int id) throws IOException {
        this.clock = 0;
        this.id = id;
        
        // Request map
        requestResourceMap = new ConcurrentHashMap<>();
        
        // Request queue
        requestQueues = new RequestQueue[ExclusaoMutua.NUM_RESOURCES];
        for (int i = 0; i < ExclusaoMutua.NUM_RESOURCES; ++i)
            requestQueues[i] = new RequestQueue(id);
        
        new ResourceReleaserThread(this).start();
    }
    
    public void initializeServer() {
        new ServerThread(this).start();
    }
    
    public void initializeClients() throws IOException {
        Socket[] sockets = new Socket[ExclusaoMutua.NUM_NODES];
        for (int i = 0; i < ExclusaoMutua.NUM_NODES; ++i)
            sockets[i] = new Socket("localhost", 3031 + i);
        
        OutputStreamWriter[] streams = new OutputStreamWriter[ExclusaoMutua.NUM_NODES];
        for (int i = 0; i < ExclusaoMutua.NUM_NODES; ++i)
            streams[i] = new OutputStreamWriter(sockets[i].getOutputStream());
        
        writers = new BufferedWriter[ExclusaoMutua.NUM_NODES];
        for (int i = 0; i < ExclusaoMutua.NUM_NODES; ++i)
            writers[i] = new BufferedWriter(streams[i]);
    }
    
    public synchronized void updateClock(int receivedClock) {
        clock = Integer.max(clock, receivedClock) + 1;
    }

    public int getId() {
        return id;
    }

    public ConcurrentHashMap<RequestId, Integer> getRequestResourceMap() {
        return requestResourceMap;
    }

    public RequestQueue getRequestQueue(int resourceId) {
        return requestQueues[resourceId];
    }
    
    public synchronized void multicastRequest(int resourceId) throws IOException {
        RequestId requestId = new RequestId(clock, id);
        
        requestResourceMap.put(requestId, resourceId);
        requestQueues[resourceId].pushRequest(requestId);
        
        for (int i = 0; i < ExclusaoMutua.NUM_NODES; ++i) {
            if (i == id)
                continue;
            
            // Write message
            // - Identification
            writers[i].write(Integer.toString(clock) + "\n"); // Clock
            writers[i].write(Integer.toString(id) + "\n"); // Node id
            // - Response flag
            writers[i].write(Boolean.toString(false) + "\n");
            // - Resource id
            writers[i].write(Integer.toString(resourceId) + "\n");

            // Flush output stream
            writers[i].flush();
        }
        
        // Increment clock
        ++clock;
    }
    
    public synchronized void sendAckResponse(RequestId requestId) throws IOException {
        int nodeId = requestId.getNodeId();
        
        // Write message
        // - Identification
        writers[nodeId].write(Integer.toString(clock) + "\n"); // Clock
        writers[nodeId].write(Integer.toString(id) + "\n"); // Node id
        // - Response flag
        writers[nodeId].write(Boolean.toString(true) + "\n"); // Response flag
        // - Ack flag
        writers[nodeId].write(Boolean.toString(true) + "\n"); // Ack flag
        // - Acked request identification
        writers[nodeId].write(Integer.toString(requestId.getClock()) + "\n"); // Request clock
        writers[nodeId].write(Integer.toString(requestId.getNodeId()) + "\n"); // Request node id

        // Flush output stream
        writers[nodeId].flush();
        
        // Increment clock
        ++clock;
    }
    
    public synchronized void sendNackResponse(RequestId requestId) throws IOException {
        int nodeId = requestId.getNodeId();
        
        // Write message
        // - Identification
        writers[nodeId].write(Integer.toString(clock) + "\n"); // Clock
        writers[nodeId].write(Integer.toString(id) + "\n"); // Node id
        // - Response flag
        writers[nodeId].write(Boolean.toString(true) + "\n"); // Response flag
        // - Ack flag
        writers[nodeId].write(Boolean.toString(false) + "\n"); // Ack flag
        // - Nacked request identification
        writers[nodeId].write(Integer.toString(requestId.getClock()) + "\n"); // Request clock
        writers[nodeId].write(Integer.toString(requestId.getNodeId()) + "\n"); // Request node id

        // Flush output stream
        writers[nodeId].flush();
        
        // Increment clock
        ++clock;
    }
    
    public synchronized void updateRequestQueue(int resourceId) throws IOException {
        while (true) {
            // Try to acquire resource
            if (getRequestQueue(resourceId).tryAcquiringTopRequestResource()) {
                synchronized (System.out) {
                    System.out.print("Recurso " + resourceId + " obtido.\n");
                }
                break;
            }
            
            // Try to deliver request
            RequestId requestId = getRequestQueue(resourceId).tryAcknowledgingTopRequest();
            if (requestId == null)
                break;
            
            sendAckResponse(requestId);
        }
    }
}
