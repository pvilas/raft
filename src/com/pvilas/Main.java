package com.pvilas;

import com.pvilas.Debug;
import java.util.*;

public class Main {

    // initialize the output logger to DEBUG and output by System.out
    // this is not de log of raft!!
    private static Debug logger = new com.pvilas.Debug("MAIN", Debug.DEBUG, System.out);

    // initialize general properties manager
    private static Properties props = new Properties();

    public static void main(String[] args) {
	    logger.debug("Raft Initialize");

    }
}
