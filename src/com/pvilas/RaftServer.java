package com.pvilas;

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

    // server's state machine
    private static StateMachine sm = new StateMachine();

    // latest term server has seen -- MUST BE STORED
    private long currentTerm = 0;
    // candidate that received vote in current term -1 if none -- MUST BE STORED
    private int votedFor = -1;
    // server's log --- MUST BE STORED
    private Log log;


    public RaftServer(int number, int port) throws IOException {
        super(number, port); // low-level comm machinery
        this.state=STATE_FOLLOWER; // server starts as a follower
        this.log=new Log(number);
    }


    protected void process() {

        // if I'm a follower, I accept a log operation

        // if I'm the leader, I accept a log operation
        // and I will try to replicate it
        // if I will have replicated on the majority of the servers
        // the this entry is commited, I can pass it to the state machine and
        // reply the client

        // I'm being requested to vote

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
    public boolean requestVote(int candidateId, // candidate requesting vote
                               long termC, // candidate's term
                               int lastLogIndex, // index of candidate's last log entry
                               long lastLogTerm // term of cadidates's last log entry
    ) {
        // rename parameters to match slide 14
        long lastTermV=this.log.lastLogTerm();
        int  lastIndexV=this.log.lastLogIndex();
        long lastTermC=lastLogTerm;
        int  lastIndexC=lastLogIndex;

        // check "step down" condition
        if(termC>this.currentTerm) {
            if (this.state == this.STATE_LEADER || this.state == this.STATE_CANDIDATE) {
                this.setState(this.STATE_FOLLOWER);
                this.votedFor=candidateId;
                this.currentTerm=termC;
                return false;
            }
        } else if (termC==this.currentTerm) {
                this.votedFor=candidateId;

        }  else {  //termC<this.currentTerm
            return false;
        }

        // check if we need a step down
        if (lastTermV<lastTermC) {
            if (this.state == this.STATE_LEADER || this.state == this.STATE_CANDIDATE) {
                    this.setState(this.STATE_FOLLOWER);
                    this.votedFor=candidateId;
                    this.currentTerm=termC;
                    return true; // vote for candidate
            }
        } else if ( (lastTermV>lastTermC) ||
                    (lastTermV==lastTermC && lastIndexV>lastIndexC)
                ) {
                    return false;
        }


    }

    // triggered every ms
    public void run() {

    }


    // sets server state
    public void setState(int st) {
        this.state=st;
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
