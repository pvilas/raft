package com.pvilas;

import com.eclipsesource.json.JsonObject;

import java.util.Enumeration;
import java.util.Vector;
/*
* implements a system to send and receive requests "in parallel" unattended
* paper 5.2, paragraf 2 and to send heartbeat when a server becomes leader
 */
public class ParallelRequesterManager extends Thread {

    protected Debug logger;
    protected Vector requests; // entries in the log
    protected Enumeration servers;
    protected JsonObject message;
    private int serverId;
    protected RaftServer caller;

    public ParallelRequesterManager(RaftServer caller, Enumeration servers) {
        super("Parallel Requester Manager");
        this.logger = new com.pvilas.Debug("REQ-MANAGER", Debug.DEBUG, System.out);
        this.servers= servers;
        this.caller=caller;
        this.serverId=caller.serverId;
        this.requests=new Vector();
    }

    // to be override
    protected Requester RequesterFactory(String address, int port) {
        return  new Requester(this, address, port, this.message);
    }

    // message to be send to the other servers
    protected void setMessage(JsonObject msg) {
        this.message=msg;
    }

    protected void interruptSenders() {
        Enumeration vEnum = this.requests.elements();
        while(vEnum.hasMoreElements()) {
            Requester rt = (Requester)vEnum.nextElement();
            rt.interrupt();
        }
    }

    @Override
    // we create a requester for each server and we send the request unattended
    public void run() {
        try {
            while(servers.hasMoreElements()) {
                RaftServer rs=(RaftServer)servers.nextElement();
                // not to send to myself!
                if (rs.serverId!=this.serverId) {
                    Requester t = this.RequesterFactory("localhost", rs.port);
                    this.requests.add(t);
                    t.start();
                    // no joins here, view timed task o votescollector
                }
            }
        } catch (Exception e) {
            logger.debug("Error during request: "+e.toString());
        }
        logger.debug("Exiting requester thread.");
    }

}


class Requester extends Thread {
    protected String address;
    protected int port;
    protected JsonObject rq;
    protected JSonHelper hp;
    protected ParallelRequesterManager prm;

    public Requester(ParallelRequesterManager prm, String address, int port, JsonObject rq) {
        this.address=address;
        this.port=port;
        this.rq=rq;
        this.hp=new JSonHelper();
        this.prm=prm;
    }

    @Override
    public void run() {
        prm.logger.debug("Sending rq");
        hp.sendClient(
                address,
                port,
                rq
        );
    }


}

