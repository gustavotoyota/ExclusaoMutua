package exclusaomutua;


import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

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
    public static final int NUM_NODES = 3;
    
    // Node informations
    private int clock;
    private final int id;
    
    // Client socket writers
    private BufferedWriter[] writers;
    
    // Request queue
    private final RequestQueue requestQueue;
    
    public Node(int id) throws IOException {
        this.clock = 0;
        this.id = id;
        
        requestQueue = new RequestQueue(id);
    }
    
    public void initializeServer() {
        new ServerThread(this).start();
    }
    
    public void initializeClients() throws IOException {
        Socket[] sockets = new Socket[NUM_NODES];
        for (int i = 0; i < NUM_NODES; ++i)
            sockets[i] = new Socket("localhost", 3031 + i);
        
        OutputStreamWriter[] streams = new OutputStreamWriter[NUM_NODES];
        for (int i = 0; i < NUM_NODES; ++i)
            streams[i] = new OutputStreamWriter(sockets[i].getOutputStream());
        
        writers = new BufferedWriter[NUM_NODES];
        for (int i = 0; i < NUM_NODES; ++i)
            writers[i] = new BufferedWriter(streams[i]);
    }
    
    public synchronized void updateClock(int receivedClock) {
        clock = Integer.max(clock, receivedClock) + 1;
    }

    public int getId() {
        return id;
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }
    
    public synchronized void multicastRequest() throws IOException {
        requestQueue.pushRequest(new RequestId(clock, id));
        
        for (int i = 0; i < NUM_NODES; ++i) {
            if (i == id)
                continue;
            
            // Write message
            // - Message identification
            writers[i].write(Integer.toString(clock) + "\n"); // Clock
            writers[i].write(Integer.toString(id) + "\n"); // Node id
            // - Message ack flag
            writers[i].write(Boolean.toString(false) + "\n"); // Response flag

            // Flush output stream
            writers[i].flush();
        }
        
        // Increment clock
        ++clock;
    }
    
    public synchronized void sendResponse(RequestId requestId) throws IOException {
        int nodeId = requestId.getNodeId();
        
        // Write message
        // - Message identification
        writers[nodeId].write(Integer.toString(clock) + "\n"); // Clock
        writers[nodeId].write(Integer.toString(id) + "\n"); // Node id
        // - Message ack flag
        writers[nodeId].write(Boolean.toString(true) + "\n"); // Response flag
        // - Acked request identification
        writers[nodeId].write(requestId.getClock() + "\n"); // Request clock
        writers[nodeId].write(requestId.getNodeId() + "\n"); // Request node id

        // Flush output stream
        writers[nodeId].flush();
        
        // Increment clock
        ++clock;
    }
}
