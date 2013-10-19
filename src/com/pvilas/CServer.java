package com.pvilas;

import java.net.*;
import java.io.*;

import com.pvilas.Debug;

/*
* just a TCP server, example taken from http://www.tutorialspoint.com/java/java_networking.htm
 */
public class CServer extends Thread
{
    protected Debug logger;
    protected int number;
    private ServerSocket serverSocket;
    protected Socket t_server;
    protected DataInputStream s_in;
    protected DataOutputStream s_out;
    public static final String ERROR = "321165sfg4022";

    public CServer(int number, int port) throws IOException
    {
        this.serverSocket = new ServerSocket(port);
        this.number=number;
        //this.serverSocket.setSoTimeout(10000);

        // append the server number to the logger indicator
        this.logger = new com.pvilas.Debug("RAFTSERVER"+number, Debug.DEBUG, System.out);
        this.logger.debug("Server created");
    }

    public void run()
    {
        while(true)
        {
            try
            {
                logger.debug("Waiting for client on port " +serverSocket.getLocalPort() + "...");
                this.t_server = this.serverSocket.accept();
                logger.debug("Just connected to "+ this.t_server.getRemoteSocketAddress());
                this.s_in = new DataInputStream(this.t_server.getInputStream());
                this.s_out =new DataOutputStream(this.t_server.getOutputStream());
                // passa-ho a processar
                this.process();
                this.t_server.close();
            }catch(SocketTimeoutException s)
            {
                logger.error("Socket timed out!");
                break;
            }catch(IOException e)
            {
                logger.error("IOException"+e.toString());
                break;
            }
            logger.warning("Server down");
        }
    }

    // read form client
    protected String read() {
        try {
            return this.s_in.readUTF();
        }catch(IOException e)
        {
            logger.error("read"+e.toString());
            return ERROR;
        }
    }

    // write onto client
    // read form client
    protected boolean write(String response) {
        try {
            this.s_out.writeUTF(response);
            return true;
        }catch(IOException e)
        {
            logger.error("write"+e.toString());
            return false;
        }
    }


    // to rewrite
    protected void process() {
        // an echo
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
}