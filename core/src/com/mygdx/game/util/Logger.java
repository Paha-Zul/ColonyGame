package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;

import java.io.*;

/**
 * Created by Paha on 8/12/2015.
 */
public class Logger {
    public static final int NORMAL=0, WARNING=1, ERROR=2;

    private static PrintWriter writer;

    static{
        try {
            boolean append = false;
            boolean autoFlush = true;
            String charset = "UTF-8";
            String filePath = Gdx.files.internal("").file().getAbsolutePath() + "/log.txt";

            File file = new File(filePath); //Create a new file (or the already existing one)
            FileOutputStream fos = new FileOutputStream(file, append); //This allows us to choose to append or not.
            OutputStreamWriter osw = new OutputStreamWriter(fos, charset); //This allows us to choose the charset.
            BufferedWriter bw = new BufferedWriter(osw); //This buffers the output for more efficient writing.
            Logger.writer = new PrintWriter(bw, autoFlush); //This gives us easy methods like println().
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

        writer.println(text);
        if(printToConsole) System.out.println(text);
        writer.flush();
    }

    /**
     * @return The PrintWriter to use (for printing the stack trace to a file...)
     */
    public static PrintWriter getPrintWriter(){
        return Logger.writer;
    }

    public static void close(){
        Logger.writer.close();
    }
}
