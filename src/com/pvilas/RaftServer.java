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



    public RaftServer(int number, int port) throws IOException {
        super(number, port); // low-level comm machinery
        this.state=STATE_FOLLOWER; // server starts as a follower

    }


    protected void process() {
        try {
            String from_client=this.read();
            this.logger.debug("Read form client: "+from_client);
            String response="You said "+from_client;
            this.write(response);
        }catch(Exception e)
        {
            logger.error("process"+e.toString());
        }
    }

    public int getNumber() {
        return this.number;
    }

    public int getStatus() {
        return this.state;
    }


}
