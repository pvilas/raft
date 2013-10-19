package com.pvilas;

/**
 * Created with IntelliJ IDEA.
 * User: sistemas
 * Date: 19/10/13
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */

import com.pvilas.Recipe;

import java.util.*;

/*
* this class acts a "State Machine" it simply "executes" two commands
* append and remove with the object recipe
 */
public class StateMachine {
    private Map recipes;
    com.pvilas.Debug logger;

    public StateMachine() {
        this.recipes=new HashMap();
        this.logger = new com.pvilas.Debug("STATEMACHINE", Debug.DEBUG, System.out);
    }

    // adds a recipe
    public void add(Recipe p) {
        this.recipes.put(p.RecipeNum, p);
        this.logger.debug("Recipe "+p.RecipeNum+" added");
    }

    // removes a recipe
    public void remove(Recipe p) {
        if (this.recipes.containsKey(p.RecipeNum)) {
            this.recipes.remove(p.RecipeNum);
            this.logger.debug("Recipe "+p.RecipeNum+" removed");
        } else {
            this.logger.debug("Recipe "+p.RecipeNum+" does not exist");
        }

    }

    // list all stored recipes
    public String list() {
        return this.recipes.toString();
    }
}
