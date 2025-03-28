/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package input;

import Blockchain.Transaction;
import core.DTNHost;
import core.Message;
import core.World;

/**
 * External event for creating a message.
 */
public class TransactionCreateEvent extends MessageEvent {

    private int size;
    private int responseSize;
    private Transaction tr;

    /**
     * Creates a message creation event with a optional response request
     *
     * @param from The creator of the message
     * @param to Where the message is destined to
     * @param id ID of the message
     * @param size Size of the message
     * @param responseSize Size of the requested response message or 0 if no
     * response is requested
     * @param time Time, when the message is created
     */
    public TransactionCreateEvent(int from, int to, String id, int size,
            int responseSize, double time, Transaction tr) {
        super(from, to, id, time);
        this.size = size;
        this.responseSize = responseSize;
        this.tr = tr;
    }

    /**
     * Creates the message this event represents.
     */
    @Override
    public void processEvent(World world) {
        DTNHost to = world.getNodeByAddress(this.toAddr);
        DTNHost from = world.getNodeByAddress(this.fromAddr);
        
        Message m = new Message(from, to, this.id, this.size);
        m.setResponseSize(this.responseSize);
        if (this.tr != null) {
            m.addProperty("transaction", this.tr);
        }
        if (this.fromAddr != 0 && this.fromAddr <57 ) {
            from.createNewMessage(m);
        }
        
    }

    public Transaction getTransaction() {
        return this.tr;
    }
    
    @Override
    public String toString() {
        return super.toString() + " [" + fromAddr + "->" + toAddr + "] "
                + "size:" + size + " CREATE";
    }
}
