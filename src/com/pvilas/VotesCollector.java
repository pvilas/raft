package com.pvilas;

import com.eclipsesource.json.JsonObject;

import java.util.Enumeration;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/*
* collect votes in parallel as said in paper section 5.2 Leader election
* it is a parallelrequester manager that can handle the account of votes
 */

public class VotesCollector extends ParallelRequesterManager {

    public int votes;
    public int majority;

    // max time without finish elections
    public static final int ELECTIONS_TIMEOUT = 1000;

    private Timer timer = new Timer(); // used to election timeout

    private TimerTask etTimeoutTask = new TimerTask() {
        public void run() {
            resetElections();
        }
    };


    public VotesCollector(RaftServer caller, Enumeration servers, int majority) {
        super(caller, servers);
        this.majority = majority;
        JSonHelper hp = new JSonHelper();
        // make a requestVote message
        JsonObject rq = hp.makeRequestVote(
                caller.currentTerm,
                caller.serverId,
                caller.log.lastLogIndex(),
                caller.log.lastLogTerm()
        );
        setMessage(rq);
        votes++; // I vote for myself
        // elections timeout
        this.timer.schedule(etTimeoutTask, this.ELECTIONS_TIMEOUT,  this.ELECTIONS_TIMEOUT);
    }

    // override to specialize in VoteRequester instead of Requester
    protected Requester RequesterFactory(String address, int port) {
        return new VoteRequester(this, address, port, this.message);
    }

    // too much time without finish elections
    public void resetElections() {
        //logger.debug("Elections timeout, restarting");
        interruptSenders();
        // reset elections timeout on caller  100 to 300 ms according paper
        caller.resetElectionTimeout(randInt(300,1000));
    }

    // taken from  http://stackoverflow.com/questions/363681/generating-random-numbers-in-a-range-with-java
    public static int randInt(int min, int max) {

        // Usually this can be a field rather than a method variable
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    // a vote requester sets vote
    public synchronized void setVote(int term, boolean voteGranted) {
        // synchronize because multiple threads can access to this section at the same time
        if (voteGranted)
            this.votes++;

        // if we reached majority
        if (this.votes == this.majority) {
            logger.debug("Win!! I'm the new leader!!");
            // set new state
            caller.setState(RaftServer.STATE_LEADER);
            // send hb,s immediately to claim to be the new leader
            caller.sendHeartBeat();
            // interrupt all senders
            interruptSenders();
        }
    }

}


// requester specialiced
class VoteRequester extends Requester {

    public VoteRequester(ParallelRequesterManager prm, String address, int port, JsonObject rq) {
        super(prm, address, port, rq);
    }

    @Override
    public void run() {
        this.prm.logger.debug("Sending requestvote to "+port);

        JsonObject resp = hp.sendClient(
                address,
                port,
                rq
        );

        this.prm.logger.debug("Response to requestvote is "+resp.toString());
        if (resp.get("error")!=null)
            this.prm.logger.debug("Error from requestvote");
        else {
            if (resp!=null && !resp.get("term").isNull() && !resp.get("voteGranted").isNull())
                ((VotesCollector)prm).setVote(
                    resp.get("term").asInt(),
                    resp.get("voteGranted").asInt() != 0);
        }
    }


}