package com.pvilas;

import com.eclipsesource.json.JsonObject;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: sistemas
 * Date: 19/10/13
 * Time: 16:44
 * To change this template use File | Settings | File Templates.
 */

public class RaftServer extends CServer {
    /*
    * Eventually we can implement this class behind a webserver
     */
    public static final int STATE_LEADER = 0;
    public static final int STATE_FOLLOWER = 1;
    public static final int STATE_CANDIDATE = 2;
    private int state;

    // helper class to make json responses
    public static JSonHelper response = new JSonHelper();

    // server's state machine
    private static StateMachine sm = new StateMachine();

    // latest term server has seen -- TODO: MUST BE STORED
    private long currentTerm = 0;
    // candidate that received vote in current term -1 if none -- TODO: MUST BE STORED
    private int votedFor = -1;
    // server's log --- TODO: MUST BE STORED
    private Log log;


    public RaftServer(int number, int port) throws IOException {
        super(number, port); // low-level comm machinery
        this.state=STATE_FOLLOWER; // server starts as a follower
        this.log=new Log(number);
    }


    protected void process() {

        // if I'm a follower, I accept a log replication

        // if I'm the leader, I accept a log operation
        // and I will try to replicate it
        // if I will have replicated on the majority of the servers
        // the this entry is commited, I can pass it to the state machine and
        // reply the client

        // I'm being requesting votes

        /*
        try {
            String from_client=this.read();
            this.logger.debug("Read form client: "+from_client);
            String response="You said "+from_client;
            this.write(response);
        }catch(Exception e)
        {
            logger.error("process"+e.toString());
        }
         */

    }


    // impl of requestVote
    public boolean requestVote( long termC, // candidate's term
                                int  candidateId, // candidate requesting vote
                                int  lastLogIndex, // index of candidate's last log entry
                                long lastLogTerm // term of cadidates's last log entry
    ) {
        // rename parameters to match slide 14
        long lastTermV=this.log.lastLogTerm();
        int  lastIndexV=this.log.lastLogIndex();
        long lastTermC=lastLogTerm;
        int  lastIndexC=lastLogIndex;

        logger.debug("Being requested to vote");

        // check "step down" condition
        // this comes from slide 7 of the presentation
        // this can be also on the logic of the request vote response
        if (this.stepDown(termC)) {
            return false; // vote  NO
        } else if (termC==this.currentTerm && (this.votedFor == -1 || this.votedFor == candidateId) ) {

                // check for log completeness
                if ( (lastTermV>lastTermC) ||
                     (lastTermV==lastTermC && lastIndexV>lastIndexC)
                    ) {
                    this.votedFor=-1;
                    return false;
                } else { // his log is almost complete as mine!
                    this.votedFor=candidateId;
                    this.resetElectionTimeout();
                    return true;
                }

        }  else if (termC<this.currentTerm) {
            // this comes form page 4 of the paper
                this.votedFor=-1;
                logger.debug("Voted no for server candidate "+candidateId);
                return false;
        }

        return false;
    }

    // triggered every ms
    public void run() {

    }

    // resets the election timeout
    private void resetElectionTimeout() {

    }

    // performs an step down
    private boolean stepDown(long newTerm) {
        if(newTerm>this.currentTerm) {
            if (this.state == this.STATE_LEADER || this.state == this.STATE_CANDIDATE) {
                this.setState(this.STATE_FOLLOWER);
            }
            this.currentTerm=newTerm;
            this.votedFor=-1;
        }
        return false;
    }

    // sets server state
    private void setState(int st) {
        // TODO: save on disk before set variable for the case of a crash
        this.state=st;
        this.votedFor=-1;
    }

    // return server id
    public int getNumber() {
        return this.number;
    }

    // returns server status
    public String getStatus() {
        switch (this.state) {
            case STATE_LEADER:
                return "LEADER";
            case STATE_FOLLOWER:
                return "FOLLOWER";
            case STATE_CANDIDATE:
                return "CANDIDATE";
            default:
                return "ERROR";
        }
    }


}


/*
* class to help making json constructions
 */
class JSonHelper {

    public JSonHelper() {

    }

    // return a vote to a candidate
    public JsonObject resultVote(int term, int voteGranted) {
        return new JsonObject()
                .add("term", term)
                .add("voteGranted", voteGranted);
    }




}