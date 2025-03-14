/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package input;

import Blockchain.Inisialisasi;
import Blockchain.Transaction;
import java.util.Random;

import core.Settings;
import core.SettingsError;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Message creation -external events generator. Creates uniformly distributed
 * message creation patterns whose message size and inter-message intervals can
 * be configured.
 */
public class TransactionEventGenerator implements EventQueue {

    private int eventCount = 0;
    /**
     * Message size range -setting id ({@value}). Can be either a single value
     * or a range (min, max) of uniformly distributed random values. Defines the
     * message size (bytes).
     */
    public static final String MESSAGE_SIZE_S = "size";
    /**
     * Message creation interval range -setting id ({@value}). Can be either a
     * single value or a range (min, max) of uniformly distributed random
     * values. Defines the inter-message creation interval (seconds).
     */
    public static final String MESSAGE_INTERVAL_S = "interval";
    /**
     * Sender/receiver address range -setting id ({@value}). The lower bound is
     * inclusive and upper bound exclusive.
     */
    public static final String HOST_RANGE_S = "hosts";
    /**
     * (Optional) receiver address range -setting id ({@value}). If a value for
     * this setting is defined, the destination hosts are selected from this
     * range and the source hosts from the {@link #HOST_RANGE_S} setting's
     * range. The lower bound is inclusive and upper bound exclusive.
     */
    public static final String TO_HOST_RANGE_S = "tohosts";

    /**
     * Message ID prefix -setting id ({@value}). The value must be unique for
     * all message sources, so if you have more than one message generator, use
     * different prefix for all of them. The random number generator's seed is
     * derived from the prefix, so by changing the prefix, you'll get also a new
     * message sequence.
     */
    public static final String MESSAGE_ID_PREFIX_S = "prefix";
    /**
     * Message creation time range -setting id ({@value}). Defines the time
     * range when messages are created. No messages are created before the first
     * and after the second value. By default, messages are created for the
     * whole simulation time.
     */
    public static final String MESSAGE_TIME_S = "time";

    /**
     * Time of the next event (simulated seconds)
     */
    protected double nextEventsTime = 0;
    /**
     * Range of host addresses that can be senders or receivers
     */
    protected int[] hostRange = {0, 0};
    /**
     * Range of host addresses that can be receivers
     */
    protected int[] toHostRange = null;
    /**
     * Next identifier for a message
     */
    private int id = 0;
    /**
     * Prefix for the messages
     */
    protected String idPrefix;
    /**
     * Size range of the messages (min, max)
     */
    private int[] sizeRange;
    /**
     * Interval between messages (min, max)
     */
    private int[] msgInterval;
    /**
     * Time range for message creation (min, max)
     */
    protected double[] msgTime;

    /**
     * Random number generator for this Class
     */
    protected Random rng;

    /**
     * Constructor, initializes the interval between events, and the size of
     * messages generated, as well as number of hosts in the network.
     *
     * @param s Settings for this generator.
     */
    public TransactionEventGenerator(Settings s) {
        this.sizeRange = s.getCsvInts(MESSAGE_SIZE_S);
        this.msgInterval = s.getCsvInts(MESSAGE_INTERVAL_S);
        this.hostRange = s.getCsvInts(HOST_RANGE_S, 2);
        this.idPrefix = s.getSetting(MESSAGE_ID_PREFIX_S);

        if (s.contains(MESSAGE_TIME_S)) {
            this.msgTime = s.getCsvDoubles(MESSAGE_TIME_S, 2);
        } else {
            this.msgTime = null;
        }
        if (s.contains(TO_HOST_RANGE_S)) {
            this.toHostRange = s.getCsvInts(TO_HOST_RANGE_S, 2);
        } else {
            this.toHostRange = null;
        }

        /* if prefix is unique, so will be the rng's sequence */
        this.rng = new Random(idPrefix.hashCode());

        if (this.sizeRange.length == 1) {
            /* convert single value to range with 0 length */
            this.sizeRange = new int[]{this.sizeRange[0], this.sizeRange[0]};
        } else {
            s.assertValidRange(this.sizeRange, MESSAGE_SIZE_S);
        }
        if (this.msgInterval.length == 1) {
            this.msgInterval = new int[]{this.msgInterval[0],
                this.msgInterval[0]};
        } else {
            s.assertValidRange(this.msgInterval, MESSAGE_INTERVAL_S);
        }
        s.assertValidRange(this.hostRange, HOST_RANGE_S);

        if (this.hostRange[1] - this.hostRange[0] < 2) {
            if (this.toHostRange == null) {
                throw new SettingsError("Host range must contain at least two "
                        + "nodes unless toHostRange is defined");
            } else if (toHostRange[0] == this.hostRange[0]
                    && toHostRange[1] == this.hostRange[1]) {
                // XXX: teemuk: Since (X,X) == (X,X+1) in drawHostAddress()
                // there's still a boundary condition that can cause an
                // infinite loop.
                throw new SettingsError("If to and from host ranges contain"
                        + " only one host, they can't be the equal");
            }
        }

        /* calculate the first event's time */
        this.nextEventsTime = (this.msgTime != null ? this.msgTime[0] : 0)
                + msgInterval[0]
                + (msgInterval[0] == msgInterval[1] ? 0
                        : rng.nextInt(msgInterval[1] - msgInterval[0]));
    }

    /**
     * Draws a random host address from the configured address range
     *
     * @param hostRange The range of hosts
     * @return A random host address
     */
    protected int drawHostAddress(int hostRange[]) {
//                for (int i : hostRange) {
//            System.out.println("Host range berisi demikian : "+i);
//        }
        if (hostRange[1] == hostRange[0]) {
            return hostRange[0];
        }

        return hostRange[0] + rng.nextInt(hostRange[1] - hostRange[0]);
    }

    /**
     * Generates a (random) message size
     *
     * @return message size
     */
    protected int drawMessageSize() {
        int sizeDiff = sizeRange[0] == sizeRange[1] ? 0
                : rng.nextInt(sizeRange[1] - sizeRange[0]);
        return sizeRange[0] + sizeDiff;
    }

    /**
     * Generates a (random) time difference between two events
     *
     * @return the time difference
     */
    protected int drawNextEventTimeDiff() {
        int timeDiff = msgInterval[0] == msgInterval[1] ? 0
                : rng.nextInt(msgInterval[1] - msgInterval[0]);
        return msgInterval[0] + timeDiff;
    }

    /**
     * Draws a destination host address that is different from the "from"
     * address
     *
     * @param hostRange The range of hosts
     * @param from the "from" address
     * @return a destination address from the range, but different from "from"
     */
    protected int drawToAddress(int from) {
        int to;
////        System.out.println("Masuk ka pepe");
////        System.out.println("Kalau masuk from = "+ from);
//
//        do {
//            to = this.toHostRange != null ? drawHostAddress(this.toHostRange)
//                    : drawHostAddress(this.hostRange);
//        } while (from == to);
        if (from >= 1 && from <= 7) {
            to = 57;
        } else if (from <= 14) {
            to = 58;
        } else if (from <= 21) {
            to = 59;
        } else if (from <= 28) {
            to = 60;
        } else if (from <= 35) {
            to = 61;
        } else if (from <= 42) {
            to = 62;
        } else if (from <= 49) {
            to = 63;
        } else {
            to = 64;
        }

        return to;
    }

    /**
     * Returns the next message creation event
     *
     * @see input.EventQueue#nextEvent()
     */
    public ExternalEvent nextEvent() {
        int responseSize = 0;
        /* zero stands for one way messages */
        int msgSize;
        int interval;
        int from;
        int to;

        /* Get two *different* nodes randomly from the host ranges */
        from = drawHostAddress(this.hostRange);
        to = drawToAddress(from);

        String sender = "miner" + from;
        String[] names = Inisialisasi.names;
        String receiver = names[rng.nextInt(names.length)];
        
        double amount = ThreadLocalRandom.current().nextDouble(10, 1000);
        long timestamp = System.currentTimeMillis();
        
        msgSize = drawMessageSize();
        interval = drawNextEventTimeDiff();

        Transaction tr = new Transaction(sender, receiver, amount, timestamp, amount);
        
        /* Create event and advance to next event */
             
        TransactionCreateEvent tce = new TransactionCreateEvent(from, to, "TRX" + eventCount++, msgSize, responseSize, this.nextEventsTime, tr);
        
        this.nextEventsTime += interval;

        if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
            /* next event would be later than the end time */
            this.nextEventsTime = Double.MAX_VALUE;
        }

        return tce;
    }

    /**
     * Returns next message creation event's time
     *
     * @see input.EventQueue#nextEventsTime()
     */
    public double nextEventsTime() {
        return this.nextEventsTime;
    }

    /**
     * Returns a next free message ID
     *
     * @return next globally unique message ID
     */
    protected String getID() {
        this.id++;
        return idPrefix + this.id;
    }
}
