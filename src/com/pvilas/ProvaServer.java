package com.pvilas;

import com.eclipsesource.json.JsonObject;

import java.net.*;
import java.io.*;

public class ProvaServer extends Thread
{
    private ServerSocket serverSocket;

    public ProvaServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
    }

    public void run()
    {
        while(true)
        {
            try
            {
                System.out.println("Waiting for client on port " +
                        serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to "
                        + server.getRemoteSocketAddress());
                DataInputStream in =
                        new DataInputStream(server.getInputStream());
                Recipe rt = new Recipe(
                        JsonObject.readFrom(in.readUTF())
                );
                System.out.println("recipe read");
                System.out.println(rt);
                //System.out.println(in.readUTF());
                DataOutputStream out =
                        new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to "
                        + server.getLocalSocketAddress() + "\nGoodbye!");
                server.close();
            }catch(SocketTimeoutException s)
            {
                System.out.println("Socket timed out!");
                break;
            }catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }
    public static void main(String [] args)
    {
        int port = Integer.parseInt(args[0]);
        try
        {
            Thread t = new ProvaServer(port);
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}