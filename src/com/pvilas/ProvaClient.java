package com.pvilas;

import java.net.*;
import java.io.*;

public class ProvaClient
{
    public static void main(String [] args)
    {
        String serverName = args[0];
        int port = Integer.parseInt(args[1]);
        String message = args[2];
        try
        {
            System.out.println("Connecting to " + serverName+ " on port " + port);
            Socket client = new Socket(serverName, port);
            System.out.println("Just connected to "+ client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);
            Recipe rt=new Recipe("título", "452", "pepe", "2013-05-06", "info extra");
            out.writeUTF(rt.toJson().toString());
            System.out.println("Recipe send");
            //out.writeUTF("Hello from "+ client.getLocalSocketAddress());
            InputStream inFromServer = client.getInputStream();
            DataInputStream in =new DataInputStream(inFromServer);
            System.out.println("Server says " + in.readUTF());
            client.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}