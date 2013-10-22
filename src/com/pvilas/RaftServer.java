package com.pvilas;

import com.eclipsesource.json.JsonObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Timer;
import java.util.TimerTask;

/**
 * implements a Raft server
 */

public class RaftServer extends CServer {
    /*
    * Eventually we can implement this class behind a web server
     */
    public static final int STATE_LEADER = 0;
    public static final int STATE_FOLLOWER = 1;
    public static final int STATE_CANDIDATE = 2;
    public static final int SEND_TIMEOUT = 2000; // waiting time to connect with other server
    public static final String RPC_VOTE = "RequestVote";
    public static final String RPC_APPEND = "AppendEntries";
    public static final String CLIENT_REQUEST = "ClientRequest";
    private int electionTimeout = 2000; // initialize to 5 seconds without a heartbeat => new election
    private int heartBeatPeriod = 250; // time between leader's heartbeats
    private int state; // current state of this server
    private RaftServerPool pool; // pool that this server belongs
    public int serverId; // id of this server
    private VotesCollector vc;


    private Timer timer = new Timer(); // used to election timeout

    // helper class to make json responses
    public static JSonHelper response = new JSonHelper();

    // server's state machine
    private static StateMachine sm = new StateMachine();

    // latest term server has seen -- TODO: MUST BE STORED
    public int currentTerm = 0;
    // candidate that received vote in current term -1 if none -- TODO: MUST BE STORED
    private int votedFor = -1;
    // server's log --- TODO: MUST BE STORED
    public Log log;

    // scheduled task to trigger elections
    private TimerTask electionTimeoutTask = new TimerTask() {
        public void run() {
            runElections();
        }
    };

    // scheduled task to send heartbeats to followers
    private TimerTask hbTimeoutTask = new TimerTask() {
        public void run() {
            sendHeartBeat();
        }
    };


    public RaftServer(int number,   // server id
                      int port,     // server port
                      RaftServerPool pool   // pointer to pool
    ) throws IOException {
        super(number, port); // low-level comm machinery
        this.state = STATE_FOLLOWER; // server starts as a follower
        this.serverId = number;
        this.log = new Log(number);
        this.pool = pool;
        // schedule election timeout
        this.timer.schedule(electionTimeoutTask, this.electionTimeout, this.electionTimeout);
        this.timer.schedule(hbTimeoutTask, this.heartBeatPeriod, this.heartBeatPeriod);
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
        JsonObject message = JsonObject.readFrom(this.read());

        if (message==null || message.get("rpc-command").isNull())
            return;

        // extract rpc command
        String rpcCommand = message.get("rpc-command").asString();


        // I'm a follower or a candidate, I accept a log replication op
        if ((state == STATE_FOLLOWER || state == STATE_CANDIDATE) && rpcCommand.equals(RPC_APPEND)) {

            int sTerm= message.get("term").asInt();
            //determine if it is a heartbeat from leader (void payload)
            if ("".equals(message.get("payload").asString())) {
                this.write(
                        handleHeartbeat(message).toString()
                );
            } else { // it is a regular append entries
                this.write(
                        response.resultAppend(
                                this.currentTerm,
                                this.AppendEntries(
                                        message.get("term").asInt(),
                                        message.get("leaderId").asInt(),
                                        message.get("prevLogIndex").asInt(),
                                        message.get("prevLogTerm").asInt(),
                                        message.get("payload").asObject(),
                                        message.get("leaderCommit").asInt()
                                ) ? 1 : 0
                        ).toString());
            }

            // perform an step down if necessary based on the sTerm
            stepDown(sTerm);

        }


        // I'm a follower or a candidate and I receive a request for vote
        if ((state == STATE_FOLLOWER || state == STATE_CANDIDATE) && rpcCommand.equals(RPC_VOTE)) {
            this.write(
                    response.resultVote(
                            this.currentTerm,
                            this.requestVote(
                                    message.get("term").asInt(),
                                    message.get("candidateId").asInt(),
                                    message.get("lastLogIndex").asInt(),
                                    message.get("lastLogTerm").asInt()
                            ) ? 1 : 0
                    ).toString());
        }

        // I'm a follower, I accept a client request and I redirect to the leader
        // TODO: redirect to leader
        if (state == STATE_FOLLOWER && rpcCommand.equals(CLIENT_REQUEST)) {

        }

        // I'm the leader, I accept client operation
        if (state == STATE_LEADER && rpcCommand.equals(CLIENT_REQUEST)) {
            // append to my log
            // and I will try to replicate it
            // if I will have replicated on the majority of the servers
            // the this entry is commited, I can pass it to the state machine and
            // reply the client
        }

    }


    // handles a leader's heartbeat
    private JsonObject handleHeartbeat(JsonObject message) {
        // reset election timeout
        resetElectionTimeout(this.electionTimeout);

        // if we are on elections
        if (vc != null) {
            vc.interruptSenders();
            // stop the thread
            vc.interrupt();
            // destroy object
            vc=null;
        }

        // check the leaders term
        int lTerm = message.get("term").asInt();

        // update server's term
        if (currentTerm < lTerm)
            currentTerm = lTerm;

        return response.resultAppend(this.currentTerm, 1);
    }


    // impl of AppendEntries
    // invoked by leader to replicate log entries 5.3
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
        success = this.log.append(termL, entry, prevLogTerm, prevLogIndex);

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
                this.resetElectionTimeout(this.electionTimeout);
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


    // if the task election timeouts it triggers this method
    public void runElections() {
        //logger.debug("Elections time!-----");
        try {
            // first of all we change the state to candidate
            setState(STATE_CANDIDATE);

            //increment current term
            incrementCurrentTerm();

            // reset election timeout
            resetElectionTimeout(this.electionTimeout);

            // gather votes
            // setup the votes collector
            vc = new VotesCollector(this, pool.getServers(), pool.getMajorityNumber());
            vc.start();

        } finally {
            // if something goes wrong, set server as follower
            setState(STATE_FOLLOWER);
        }

    }

    // triggered every ms
    // send heartbeats to every server if I'm the leader
    public void sendHeartBeat() {
        if (state == STATE_LEADER) {
            logger.debug("Sending heartbeats to pool");
            HeartBeatParallelSender hbs= new HeartBeatParallelSender(this, pool.getServers());
            hbs.start();
        }
    }


    // resets the election timeout
    public void resetElectionTimeout(int tout) {
        // this is the most ugly thing that I have done but I have no enough time.... argg
        try {
            sleep(tout);  // simulates an election reschedule
        }  catch(Exception s) {
            ;
        }
        //logger.debug("Election timeout updated!");
    }




    // performs an step down if necessary
    private boolean stepDown(int newTerm) {
        if (newTerm > this.currentTerm) {
            this.currentTerm = newTerm;
            this.votedFor = -1;
            if (this.state == STATE_LEADER || this.state == STATE_CANDIDATE) {
                this.setState(STATE_FOLLOWER);
            }
            return true;
        }
        return false;
    }

    // sets server state
    public void setState(int st) {
        // TODO: save on disk before set variable for the case of a crash
        synchronized (this) {
            this.state = st;
            this.votedFor = -1;
        }
    }

    // increments current term
    private void incrementCurrentTerm() {
        // TODO: save on disk before set variable for the case of a crash
        ++this.currentTerm;
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

    // returns a vote to a candidate
    // only read on sendClient, it will not pass to main process
    public JsonObject resultVote(int term, int voteGranted) {
        return new JsonObject()
                .add("term", term)
                .add("voteGranted", voteGranted);
    }

    // returns a response to an append op
    // only read on sendClient, it will not pass to main process
    public JsonObject resultAppend(int term, int success) {
        return new JsonObject()
                .add("term", term)
                .add("success", success);
    }

    // returns an entry message
    public JsonObject makeEntry(int term,
                                int leaderId,
                                int prevLogIndex,
                                int prevLogTerm,
                                JsonObject payload,
                                int leaderCommit) {
        return new JsonObject()
                .add("rpc-command", RaftServer.RPC_APPEND)
                .add("term", term)
                .add("leaderId", leaderId)
                .add("prevLogIndex", prevLogIndex)
                .add("prevLogTerm", prevLogTerm)
                .add("payload", payload)
                .add("leaderCommit", leaderCommit)
                ;
    }


    // returns request vote message
    public JsonObject makeRequestVote(int term,   // candidate's term
                                      int candidateId, // candidate requesting vote
                                      int lastLogIndex, // index of candidate's last log entry 5.4
                                      int lastLogTerm   // term of candidate's last log entry 5.4
    ) {
        return new JsonObject()
                .add("rpc-command", RaftServer.RPC_VOTE)
                .add("term", term)
                .add("candidateId", candidateId)
                .add("lastLogIndex", lastLogIndex)
                .add("lastLogTerm", lastLogTerm);
    }


    // returns an hb message
    public JsonObject makeHB(int term,
                             int leaderId) {
        return new JsonObject()
                .add("rpc-command", RaftServer.RPC_APPEND)
                .add("term", term)
                .add("leaderId", leaderId)
                .add("prevLogIndex", 0)
                .add("prevLogTerm", 0)
                .add("payload", "")
                .add("leaderCommit", 0)
                ;
    }


    // sends a message to a client,
    public JsonObject sendClient(String address,
                                 int port,
                                 JsonObject msg
    ) {
        try {
            Socket client = new Socket();
            client.connect(new InetSocketAddress(address, port),  RaftServer.SEND_TIMEOUT);
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            out.writeUTF(msg.toString());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            JsonObject r=com.eclipsesource.json.JsonObject.readFrom(in.readUTF());
            client.close();
            return r;
        } catch (IOException e) {
            return new JsonObject()
                    .add("error", e.toString());
        }
    }

}