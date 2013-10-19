package com.pvilas;

import java.sql.Timestamp;
import java.util.Date;
import java.io.*;


/*
* We make the initial supposition that the printstream is thread safe
 */

public class Debug {
    private String name;
    private int level;
    private PrintStream ps_output;
    public static final int DEBUG = 0;
    public static final int WARNING = 1;
    public static final int ERROR = 2;


    public Debug(String name, int level, PrintStream ps_output) {
        /* @param name the name of de logger (to identify wich class is debugging)
         * @param level of debugging
         * @param ps_output the pirntstream to output messages */
        this.name = name;
        this.level = level;
        this.ps_output = ps_output;
    }

    private void treu(String msg) {
        /* decides where the output goes
        *  appends the timestamp
        * */
        java.util.Date date = new java.util.Date();
        String s_date = new Timestamp(date.getTime()).toString();
        try {
            ps_output.println(this.name + ':' + s_date + ':' + msg);
        } catch (Exception e) {
            System.out.print("Exception while printing to output "+e.getMessage());
        }
    }

    public void debug(String msg) {
        if (level >= this.DEBUG) {
            this.treu("DEBUG: " + msg);
        }
    }

    public void warning(String msg) {
        if (level >= this.WARNING) {
            this.treu("WARNING: " + msg);
        }
    }

    public void error(String msg) {
        if (level >= this.ERROR) {
            this.treu("ERROR: " + msg);
        }
    }

    public void msg(String msg) {
        /* outputs a message to the actual output */
        this.treu(msg);
    }


}
