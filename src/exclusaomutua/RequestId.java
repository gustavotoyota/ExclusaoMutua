/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exclusaomutua;

/**
 *
 * @author Gustavo
 */
public class RequestId implements Comparable<RequestId> {
    private int clock;
    private int nodeId;
    
    RequestId(int clock, int nodeId) {
        this.clock = clock;
        this.nodeId = nodeId;
    }

    public int getClock() {
        return clock;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }
    
    // Sorting (For the priority queue)

    @Override
    public int compareTo(RequestId o) {
        int clockComp = Integer.compare(this.clock, o.clock);
        if (clockComp != 0)
            return clockComp;
        
        return Integer.compare(this.nodeId, o.nodeId);
    }
    
    // Hashing (For the HashMap)
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.clock;
        hash = 67 * hash + this.nodeId;
        return hash;
    }
    
    // Equality

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RequestId other = (RequestId) obj;
        if (this.clock != other.clock) {
            return false;
        }
        if (this.nodeId != other.nodeId) {
            return false;
        }
        return true;
    }
}