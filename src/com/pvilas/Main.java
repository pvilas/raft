package com.pvilas;

import com.pvilas.Debug;
import com.eclipsesource.json.*;

import java.io.IOException;
import java.util.*;

public class Main {

    // initialize the output logger to DEBUG and output by System.out
    // this is not de log of raft!!
    private static Debug logger = new com.pvilas.Debug("MAIN", Debug.DEBUG, System.out);
    public static final int DEFAULT_NUMBER_OF_SERVERS = 5;
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
                quit("Wrong number of arguments!");
            }
        } else {
            numberOfServers=DEFAULT_NUMBER_OF_SERVERS;
            logger.debug("Using default number of servers "+DEFAULT_NUMBER_OF_SERVERS);
        }

        if (numberOfServers<=0) {
            numberOfServers=DEFAULT_NUMBER_OF_SERVERS;
            logger.debug("Using default number of servers");
        }

        if ( (numberOfServers & 1) == 0 ) {
            quit("The number of servers must be odd");
        }

        com.pvilas.RaftServerPool sp = new RaftServerPool(
                numberOfServers,
                FIRST_PORT
        );

        // start the pool and print status
        sp.start();


        logger.debug("Main thread waiting...");

        /*

        try
        {
            Thread t = new RaftServer(1, 5001);
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
         */


        /*
        StateMachine st=new StateMachine();
        Recipe rt=new Recipe(
                new JsonObject()
                        .add( "Title", "Espárragos a la sevillana" )
                        .add( "RecipeNum", 111 )
                        .add( "Author", "pvilas")
                        .add( "Timestamp", "2013-10-19 15:00:00")
        );
        st.add(rt);
        rt=new Recipe(
                new JsonObject()
                        .add( "Title", "Espárragos a la sevillana" )
                        .add( "RecipeNum", 112 )
                        .add( "Author", "pvilas")
                        .add( "Timestamp", "2013-10-19 15:00:00")
        );
        st.add(rt);
        logger.debug(st.list());
        */

        /*
        try
        {
            Thread t = new CServer(1, 5001);
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
         */
    }

    private static void quit(String msg) {
        System.err.println(msg);
        help();
        System.exit(1);
    }

    private static void help() {
        System.err.println("Usage:");
        System.err.println("com.pvilas.Main number_of_servers");
        System.err.println(" ");
        System.err.println("Note that the number_of_servers must be odd");
    }



}
