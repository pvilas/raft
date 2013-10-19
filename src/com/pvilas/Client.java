package com.pvilas;

import java.net.*;
import java.io.*;

import com.pvilas.Debug;

/*
 * Just a TCP client
 *  initial example taken from http://www.tutorialspoint.com/java/java_networking.htm
 */

public class Client {
    private String server;
    private int port;
    private Debug logger;
    private Socket client;
    private DataOutputStream out;
    private DataInputStream in;
    public static final String ERROR = "$%$%443215";


    public Client(String server, int port) {
        this.server = server;
        this.port = port;
        this.logger = new com.pvilas.Debug("CLIENT", Debug.DEBUG, System.out);

        try {
            logger.debug("Connecting to " + server + " on port " + port);
            this.client = new Socket(server, port);
            logger.debug("Just connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            this.out = new DataOutputStream(outToServer);

            //out.writeUTF("Hello from "+ client.getLocalSocketAddress());

            InputStream inFromServer = client.getInputStream();
            this.in = new DataInputStream(inFromServer);
            //logger.debug("Server says " + in.readUTF());
            //client.close();
        } catch (IOException e) {
            logger.error("Creating client "+e.toString());
        }
    }

    public void close() {
        try {
            this.client.close();
            logger.debug("Client closed");
        } catch (IOException e) {
            logger.error("Closing client "+e.toString());
        }
    }

    /* writes message onto server
    * returns false if exception
    * */
    public boolean write(String msg) {
        try {
            this.out.writeUTF("Hello from "+ client.getLocalSocketAddress());
            return true;
        } catch (IOException e) {
            logger.error("Closing client "+e.toString());
            return false;
        }
    }

    /* writes message onto server
       * returns false if exception
       * */
    public String read() {
        try {
            return this.in.readUTF();
        } catch (IOException e) {
            logger.error("Closing client "+e.toString());
            return ERROR;
        }
    }



}