package com.mygdx.game.util;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by Paha on 8/12/2015.
 */
public class Logger {
    public static final int NORMAL=0, WARNING=1, ERROR=2;

    private static PrintWriter writer;

    static{
        try {
            Logger.writer = new PrintWriter("log.txt", "UTF-8");
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void log(int type, String text){
        Logger.log(type, text, false);
    }

    public static void log(int type, String text, boolean printToConsole){
        if(type == WARNING)
            text = "[WARNING] "+text;
        else if(type == ERROR)
            text = "[ERROR] "+text;

        writer.append(text);
        if(printToConsole) System.out.println(text);
        writer.flush();
    }
}
