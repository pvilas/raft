package com.pvilas;
import com.eclipsesource.json.*;
/**
 * Created with IntelliJ IDEA.
 * User: sistemas
 * Date: 19/10/13
 * Time: 16:10
 * To change this template use File | Settings | File Templates.
 */

/*
*   A recipe class
*   can be created directly or by a json object
 */
public class Recipe {
    public String Title, RecipeNum, Author, Timestamp, Extra;

    public Recipe(String Title,String  RecipeNum,String  Author,String  TimeStamp, String Extra) {
        this.Title=Title;
        this.Author=Author;
        this.RecipeNum=RecipeNum;
        this.Timestamp=TimeStamp;
        this.Extra=Extra;
    }

    // overloaded to create the object from a jsonobject
    public Recipe(JsonObject payload) {
        this.Title=payload.get("Title").toString();
        this.RecipeNum=payload.get("RecipeNum").toString();
        this.Author=payload.get("Author").toString();
        this.Timestamp=payload.get("Timestamp").toString();
        this.Extra=payload.get("Extra").toString();
    }

    // returns as a json object
    public JsonObject toJson() {
        return new JsonObject()
                .add( "Title", this.Title )
                .add( "RecipeNum", this.RecipeNum )
                .add( "Author", this.Author)
                .add( "Timestamp", this.Timestamp)
                .add("Extra", this.Extra);
    }

    public String toString() {
        return
                "Title: "+this.Title+
                "RecipeNum: "+this.RecipeNum+
                "Author: "+this.Author+
                "Timestamp: "+this.Timestamp+
                "Extra: "+this.Extra;
    }

}
