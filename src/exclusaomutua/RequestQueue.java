/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exclusaomutua;

import java.util.HashMap;
import java.util.PriorityQueue;

class RequestInfo {
    private final RequestId requestId;
    private int numResponses;

    public RequestInfo(RequestId requestId) {
        this.requestId = requestId;
        this.numResponses = 0;
    }

    public int getNumResponses() {
        return numResponses;
    }
    public void incrementNumResponses() {
        ++this.numResponses;
    }

    public RequestId getRequestId() {
        return requestId;
    }
}

public class RequestQueue {
    private final int nodeId;
    
    private final HashMap<RequestId, RequestInfo> requestMap;
    private PriorityQueue<RequestId> requestQueue;
    
    RequestQueue(int nodeId) {
        this.nodeId = nodeId;
        
        requestMap = new HashMap<>();
        requestQueue = new PriorityQueue<>();
    }
    
    private RequestInfo insertRequest(RequestId requestId) {
        RequestInfo requestInfo = new RequestInfo(requestId);
        
        requestMap.put(requestId, requestInfo);
        requestQueue.add(requestId);
        
        return requestInfo;
    }
    
    public synchronized void pushRequest(RequestId requestId) {
        RequestInfo requestInfo = requestMap.get(requestId);
        
        if (requestInfo == null)
            insertRequest(requestId);
    }
    
    public synchronized void incrementRequestResponses(RequestId requestId) {
        RequestInfo requestInfo = requestMap.get(requestId);
        
        requestInfo.incrementNumResponses();
    }
    
    public synchronized RequestId tryDeliveringRequest() {
        if (requestQueue.isEmpty())
            return null;
        
        RequestId requestId = requestQueue.peek();
        RequestInfo requestInfo = requestMap.get(requestId);
        
        if (requestInfo.getRequestId().getNodeId() == nodeId &&
            requestInfo.getNumResponses() < Node.NUM_NODES - 1)
            return null;
        
        requestQueue.remove();
        requestMap.remove(requestId);
        
        return requestInfo.getRequestId();
    }
    
    public synchronized void print() {
        PriorityQueue<RequestId> newRequestQueue = new PriorityQueue<>();
        
        synchronized (System.out) {
            System.out.print("Request queue: ");
        }
        
        while (!requestQueue.isEmpty()) {
            RequestId requestId = requestQueue.remove();
            newRequestQueue.add(requestId);
            RequestInfo requestInfo = requestMap.get(requestId);
            
            synchronized (System.out) {
                System.out.print("(" + requestId.getClock() + ", " +
                    requestId.getNodeId() + ": " + requestInfo.getNumResponses() + ") ");
            }
        }
        
        synchronized (System.out) {
            System.out.println();
        }
        
        requestQueue = newRequestQueue;
    }
}
