package com.pvilas;

import java.io.IOException;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: sistemas
 * Date: 19/10/13
 * Time: 17:26
 * To change this template use File | Settings | File Templates.
 */
public class RaftServerPool extends TimerTask {
    private int numberOfServers;
    public  static final int TIME_REPORT_POOL_STATUS = 5000; // milliseconds to report poolstatus
    private Vector ServerPool;
    private static Debug logger = new com.pvilas.Debug("RAFTSERVERPOOL", Debug.DEBUG, System.out);


    public RaftServerPool(int numberOfServers, int firstPort) {
        this.numberOfServers=numberOfServers;
        this.ServerPool= new Vector();

        // creation of servers
        logger.debug("Creating "+this.numberOfServers+" servers");
        for (int a=0; a<this.numberOfServers; a++) {
            try
            {
                Thread t = new RaftServer(a+1, firstPort++, this);
                this.ServerPool.add(t);
                //t.start();
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        logger.debug("Servers created");
        // print majority
        int m = getMajorityNumber();
    }

    // this method is executed every  TIME_REPORT_POOL_STATUS ms
    public void run() {
        this.status();
    }

    // return the number of servers that should be majority
    public int getMajorityNumber() {
        int maj = this.numberOfServers / 2;

        // if the number is odd we add up one more to get majority
        if ( (this.numberOfServers & 1) != 0 ) maj++;

        logger.debug("Majority is "+maj);
        return maj;
    }

    //start pool
    public void start() {
        Enumeration vEnum = this.ServerPool.elements();
        while(vEnum.hasMoreElements()) {
            RaftServer rs=(RaftServer)vEnum.nextElement();
            rs.start();
            logger.debug("Server number "+rs.getNumber()+" started");
        }
        this.status();
    }

    // prints the status of each server
    private void status() {
        logger.debug("Reporting pool status");
        logger.debug("=============================");
        Enumeration vEnum = this.ServerPool.elements();
        while(vEnum.hasMoreElements()) {
            RaftServer rs=(RaftServer)vEnum.nextElement();
            logger.debug("Server number "+rs.getNumber()+" is "+rs.getStatus());
        }
        logger.debug("=============================");
    }

    // returns and enumeration of the servers
    public Enumeration getServers() {
        return this.ServerPool.elements();
    }


}
