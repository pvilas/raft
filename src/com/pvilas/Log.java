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

    private Vector entries; // entries in the log

    public Log(int numServer) {
        this.logger= new com.pvilas.Debug("LOG"+numServer, Debug.DEBUG, System.out);
        this.entries = new Vector();
        this.logger.debug("Log created");
    }

    // appends to the log and returns new index or -1 if
    // the entry is rejected
    public int append(long term,
                      Object parameters,
                      long prevLogTerm,
                      int prevLogIndex
    ) {

        // 1st -- perform entry rejection algorithm
        // --------------------------------
        if (this.lastLogIndex() == prevLogIndex && this.lastLogTerm() == prevLogTerm ) {
            logger.debug("Inserting a new log entry");

            // try to extract the command
            String command=((JsonObject)parameters).get("command").toString();
            LogEntry ne = new LogEntry(term, command, parameters);

            this.entries.add(ne);

            // return the last index
            return this.entries.size();

        }   else {
            logger.warning("Received an outsequenced entry!");
            return -1;
        }

    }

    // returns the last log index
    public int lastLogIndex() {
        return this.entries.size();
    }

    // returns the last log term or -1 if null
    public long lastLogTerm() {
        if (this.lastLogIndex()>0) {
            return ((LogEntry)this.entries.lastElement()).getTerm();
        } else return -1;
    }

}


class LogEntry {
    /*
    * an entry in the log
     */

    private long term;
    private String command;
    private Object parameters; // agnostic about

    public LogEntry(long term, String command, Object parameters) {
        this.term=term;
        this.command=command;
        this.parameters=parameters;
    }

    public String getCommand() {
        return this.command;
    }

    public int getTerm() {
        return this.term;
    }

    public String toString() {
        return "Term: "+term+" command: "+command;
    }

}