package com.pvilas;

import com.pvilas.Debug;
import com.eclipsesource.json.*;

import java.io.IOException;
import java.util.*;

public class Main {

    // initialize the output logger to DEBUG and output by System.out
    // this is not de log of raft!!
    private static Debug logger = new com.pvilas.Debug("MAIN", Debug.DEBUG, System.out);
    public static final int DEFAULT_NUMBER_OF_SERVERS = 4;
    public static final int FIRST_PORT = 5001;

    // initialize general properties manager
    private static Properties props = new Properties();

    public static void main(String[] args) {
	    logger.debug("Raft Initialize");
        int numberOfServers = 5;

        // take arguments
        int firstArg;
        if (args.length > 0) {
            try {
                numberOfServers = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                quit("Wrong arguments!");
            }
        } else {
            numberOfServers=DEFAULT_NUMBER_OF_SERVERS;
            logger.debug("Using default number of servers "+DEFAULT_NUMBER_OF_SERVERS);
        }

        if (numberOfServers<=0) {
            numberOfServers=DEFAULT_NUMBER_OF_SERVERS;
            logger.debug("Using default number of servers");
        }

        // timer of the serverpool
        Timer timer=new Timer();

        // create the serverpool
        com.pvilas.RaftServerPool sp = new RaftServerPool(
                numberOfServers,
                FIRST_PORT
        );

        // do maintenance processes every TIME_REPORT_POOL_STATUS ms
        timer.schedule(sp, RaftServerPool.TIME_REPORT_POOL_STATUS, RaftServerPool.TIME_REPORT_POOL_STATUS);

        // start the pool and print status
        sp.start();


        logger.debug("Main thread waiting...");

    }

    private static void quit(String msg) {
        System.err.println(msg);
        help();
        System.exit(1);
    }

    private static void help() {
        System.err.println("Usage:");
        System.err.println("com.pvilas.Main number_of_servers");
    }



}
