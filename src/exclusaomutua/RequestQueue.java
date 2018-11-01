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
    private long releaseTime;

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

    public long getReleaseTime() {
        return releaseTime;
    }
    public void setReleaseTime(long releaseTime) {
        this.releaseTime = releaseTime;
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
        
        if (requestInfo == null)
            requestInfo = null;
        
        requestInfo.incrementNumResponses();
    }
    
    public synchronized boolean tryAcquiringTopRequestResource() {
        if (requestQueue.isEmpty())
            return false;
        
        RequestId requestId = requestQueue.peek();
        
        if (requestId.getNodeId() != this.nodeId)
            return false;
        
        RequestInfo requestInfo = requestMap.get(requestId);
        
        if (requestInfo.getNumResponses() < ExclusaoMutua.NUM_NODES - 1)
            return false;
        
        if (requestInfo.getReleaseTime() != 0)
            return false;
        
        requestInfo.setReleaseTime(System.currentTimeMillis() + 4000);
        
        return true;
    }
    
    public synchronized boolean tryReleasingTopRequestResource() {
        if (requestQueue.isEmpty())
            return false;
        
        RequestId requestId = requestQueue.peek();
        RequestInfo requestInfo = requestMap.get(requestId);
        
        if (requestInfo.getReleaseTime() == 0)
            return false;
        
        if (requestInfo.getReleaseTime() > System.currentTimeMillis())
            return false;
        
        requestQueue.remove();
        requestMap.remove(requestId);
        
        return true;
    }
     
    public synchronized RequestId tryAcknowledgingTopRequest() {
        if (requestQueue.isEmpty())
            return null;
        
        RequestId requestId = requestQueue.peek();
        
        if (requestId.getNodeId() == nodeId)
            return null;
        
        RequestInfo requestInfo = requestMap.get(requestId);
        
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
    
    public synchronized RequestId peek() {
        return requestQueue.peek();
    }
}
