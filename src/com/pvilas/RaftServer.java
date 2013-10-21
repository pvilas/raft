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
    public static final String RPC_VOTE = "RequestVote";
    public static final String RPC_APPEND = "AppendEntries";
    public static final String CLIENT_REQUEST = "ClientRequest";
    private int state;

    // helper class to make json responses
    public static JSonHelper response = new JSonHelper();

    // server's state machine
    private static StateMachine sm = new StateMachine();

    // latest term server has seen -- TODO: MUST BE STORED
    private int currentTerm = 0;
    // candidate that received vote in current term -1 if none -- TODO: MUST BE STORED
    private int votedFor = -1;
    // server's log --- TODO: MUST BE STORED
    private Log log;


    public RaftServer(int number, int port) throws IOException {
        super(number, port); // low-level comm machinery
        this.state = STATE_FOLLOWER; // server starts as a follower
        this.log = new Log(number);
    }


    /*
    *   structure of messages in json format
     *  "rpc-command", appendEntries or  requestVote
     *  ============================================
     *  -> appendEntries
     *
     *   request
     *   -------
     *          .add("index", 564654)
     *          .add("term", 1234)
     *          .add("command", "add" )
     *          .add("recipe", payload);
     *    payload =  .add( "Title", "Espárragos a la sevillana" )
     *          .add( "RecipeNum", 111 )
     *          .add( "Author", "pvilas")
     *          .add( "Timestamp", "2013-10-19 15:00:00");
     *   response
     *   --------
     *          .add("term", 564654)
     *          .add("success", 0)
     *
     *  -> requestVote
     *
     *          .add("term", 564654)
     *          .add("candidateId", 1234)
     *          .add("lastLogIndex", 23 )
     *          .add("lastLogTerm", 5);
     *
     *   response
     *   --------
     *          .add("term", 4)
     *          .add("voteGranted", 1)
     *
     *  -- Other: ClientRequest
     */
    protected void process() {


        // read message from stream and parse as json object
        JsonObject message = JsonObject.readFrom( this.read() );

        // extract rpc command
        String rpcCommand = message.get("rpc-command").toString();

        // I'm a follower, I accept a log replication op
        if ( state==STATE_FOLLOWER && rpcCommand == RPC_APPEND) {

        }

        // I'm a follower, I accept a client request
        // TODO: redirect to leader
        if ( state==STATE_FOLLOWER && rpcCommand == CLIENT_REQUEST) {

        }

        // I'm the leader, I accept a log operation
        if ( state==STATE_FOLLOWER && rpcCommand == CLIENT_REQUEST) {

        }

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


    // impl of AppendEntries
    // invoked by leader to replicte log entries 5.3
    // also used as heartbeat 5.2
    // @returns true if the entry has appended
    protected boolean AppendEntries(
            int termL, // leader's term
            int leaderId, // so follower can redirect clients
            int prevLogIndex, // index of log entry immediately preceding new ones
            int prevLogTerm, // term of prevLogIndex
            JsonObject entry, // log entries to store (empty for heartbeat)
            int leaderCommit // leader's commitIndex
    ) {

        boolean success = false;

        //   1. Reply false if term < currentTerm (§5.1)
        if (termL < this.currentTerm) return false;

        //    2. Reply false if log doesn’t contain an entry at prevLogIndex
        //    whose term matches prevLogTerm (§5.3)
        //    3. If an existing entry conflicts with a new one (same index
        //    but different terms), delete the existing entry and all that
        //    follow it (§5.3)
        //    4. Append any new entries not already in the log
        success=this.log.append(termL, entry, prevLogTerm, prevLogIndex);

        //    5. If leaderCommit > commitIndex, set commitIndex =
        //    min(leaderCommit, last log index)
        this.log.updateCommitIndex(leaderCommit);

        return success;
    }


    // impl of requestVote
    // @returns vote
    protected boolean requestVote(int termC, // candidate's term
                                  int candidateId, // candidate requesting vote
                                  int lastLogIndex, // index of candidate's last log entry
                                  int lastLogTerm // term of cadidates's last log entry
    ) {
        // rename parameters to match slide 14
        int lastTermV = this.log.lastLogTerm();
        int lastIndexV = this.log.lastLogIndex();
        int lastTermC = lastLogTerm;
        int lastIndexC = lastLogIndex;

        logger.debug("Being requested to vote");

        // check "step down" condition
        // this comes from slide 7 of the presentation
        // this can be also on the logic of the request vote response
        if (this.stepDown(termC)) {
            return false; // vote  NO
        } else if (termC == this.currentTerm && (this.votedFor == -1 || this.votedFor == candidateId)) {

            // check for log completeness
            if ((lastTermV > lastTermC) ||
                    (lastTermV == lastTermC && lastIndexV > lastIndexC)
                    ) {
                this.votedFor = -1;
                return false;
            } else { // his log is almost complete as mine!
                this.votedFor = candidateId;
                this.resetElectionTimeout();
                return true;
            }

        } else if (termC < this.currentTerm) {
            // this comes form page 4 of the paper
            this.votedFor = -1;
            logger.debug("Voted no for server candidate " + candidateId);
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
    private boolean stepDown(int newTerm) {
        if (newTerm > this.currentTerm) {
            if (this.state == this.STATE_LEADER || this.state == this.STATE_CANDIDATE) {
                this.setState(this.STATE_FOLLOWER);
            }
            this.currentTerm = newTerm;
            this.votedFor = -1;
        }
        return false;
    }

    // sets server state
    private void setState(int st) {
        // TODO: save on disk before set variable for the case of a crash
        this.state = st;
        this.votedFor = -1;
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

    // return a response to an append op
    public JsonObject resultAppend(int term, int success) {
        return new JsonObject()
                .add("term", term)
                .add("success", success);
    }


}