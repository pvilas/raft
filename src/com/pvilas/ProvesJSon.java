package com.pvilas;

import java.net.*;
import java.io.*;
import com.eclipsesource.json.*;

public class ProvesJSon
{
    public static void main(String [] args)
    {
        try
        {
            JsonObject payload = new JsonObject()
                    .add( "Title", "Espárragos a la sevillana" )
                    .add( "RecipeNum", 111 )
                    .add( "Author", "pvilas")
                    .add( "Timestamp", "2013-10-19 15:00:00");

            JsonObject entry = new JsonObject()
                    .add("index", 564654)
                    .add("term", 1234)
                    .add("command", "add" )
                    .add("recipe", payload);

            System.out.println(entry.toString());

            System.out.println("Llegint ------------------");
            com.eclipsesource.json.JsonObject recuperat = com.eclipsesource.json.JsonObject.readFrom(payload.toString());

            System.out.println("el titol es "+recuperat.get("Title"));
            System.out.println("aquesta clau no existeix "+recuperat.get("xyz"));


        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}