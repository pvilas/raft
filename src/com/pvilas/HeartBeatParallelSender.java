package com.pvilas;

import com.eclipsesource.json.JsonObject;

import java.util.Enumeration;

/**
 * Sends heartbeats in parallel
 */
public class HeartBeatParallelSender  extends ParallelRequesterManager {
    public HeartBeatParallelSender(RaftServer caller, Enumeration servers) {
        super(caller, servers);

        JSonHelper hp = new JSonHelper();

        JsonObject rq =
                hp.makeHB(
                        caller.currentTerm,
                        caller.serverId);
        setMessage(rq);
    }

}
