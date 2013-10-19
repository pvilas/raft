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
    protected Server t_server;

    public CServer(int number, int port) throws IOException
    {
        this.serverSocket = new ServerSocket(port);
        this.serverSocket.setSoTimeout(10000);

        // append the server number to the logger indicator
        this.logger = new com.pvilas.Debug("SERVER"+number, Debug.DEBUG, System.out);
    }

    public void run()
    {
        while(true)
        {
            try
            {
                logger.debug("Waiting for client on port " +serverSocket.getLocalPort() + "...");
                this.t_server = this.serverSocket.accept();
                logger.debug("Just connected to "+ server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());
                System.out.println(in.readUTF());

                DataOutputStream out =new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to "+ server.getLocalSocketAddress() + "\nGoodbye!");
                //server.close();
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

    public void close() {
        try {
            this.client.close();
            logger.debug("Client closed");
        } catch (IOException e) {
            logger.error("Closing client "+e.toString());
        }
    }



    public static void main(String [] args)
    {
        int port = Integer.parseInt(args[0]);
        try
        {
            Thread t = new GreetingServer(port);
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}