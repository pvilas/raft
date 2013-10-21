package com.pvilas;

import com.eclipsesource.json.JsonObject;

import java.util.Vector;

/**
 * Created with IntelliJ IDEA.
 * User: sistemas
 * Date: 19/10/13
 * Time: 19:23
 * To change this template use File | Settings | File Templates.
 */
public class Log {
    /*
    * implements the Raft log
     */
    private Debug logger;
    private int commitIndex;

    private Vector entries; // entries in the log

    public Log(int numServer) {
        this.logger = new com.pvilas.Debug("LOG" + numServer, Debug.DEBUG, System.out);
        this.entries = new Vector();
        this.logger.debug("Log created");
        this.commitIndex=-1;
    }

    // appends to the log and returns new index or -1 if
    // the entry is rejected
    public boolean append(int term,
                      Object parameters,
                      int prevLogTerm,
                      int prevLogIndex
    ) {

        // 1st -- perform entry rejection algorithm
        // --------------------------------
        // term of the entry number prevLogIndex
        boolean reply = false;
        int lTerm = this.getTermAtIndex(prevLogIndex);

        // reply false if index does not exist or entry rejected
        if (lTerm != -1 ||  // index does exist
                lTerm == prevLogTerm // terms does match
                ) {
            reply = true;
        }

        // same index but different terms
        if (lTerm != prevLogTerm) {
            logger.debug("Removing log entries from "+prevLogIndex+" to the end");
            // delete existing entry an all that follow it
            int s = this.entries.size();
            for (int a = prevLogIndex; a < s; a++) {
                this.entries.remove(prevLogIndex);
            }
        }

        logger.debug("Inserting a new log entry");

        // try to extract the command
        String command = ((JsonObject) parameters).get("command").toString();
        LogEntry ne = new LogEntry(term, command, parameters);

        this.entries.add(ne);

        // return the last index
        return reply;

    }

    // returns the last log index
    public int lastLogIndex() {
        return this.entries.size();
    }

    // returns the last log term or -1 if null
    public int lastLogTerm() {
        if (this.lastLogIndex() > 0) {
            return ((LogEntry) this.entries.lastElement()).getTerm();
        } else return -1;
    }

    // returns term at position index or -1
    public int getTermAtIndex(int index) {
        if (this.lastLogIndex() <= index) {
            LogEntry l = (LogEntry) this.entries.get(index);
            return l.getTerm();
        } else return -1;
    }

    // updates committed index
    public void updateCommitIndex(int index) {
        if (index>this.commitIndex) {
            this.commitIndex=(index<this.lastLogIndex()? index : this.lastLogIndex());
        }
    }


}


class LogEntry {
    /*
    * an entry in the log
     */

    private int term;
    private String command;
    private Object parameters; // agnostic about

    public LogEntry(int term, String command, Object parameters) {
        this.term = term;
        this.command = command;
        this.parameters = parameters;
    }

    public String getCommand() {
        return this.command;
    }

    public int getTerm() {
        return this.term;
    }

    public String toString() {
        return "Term: " + term + " command: " + command;
    }

}