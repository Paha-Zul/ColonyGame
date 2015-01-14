package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Paha on 1/13/2015.
 */
public class Profiler {
    public static double interval = 0.5f;
    public static boolean enabled = false;

    private static HashMap<String, Record> map = new HashMap<>();
    private static ArrayList<Record> sortedList = new ArrayList<>();
    private static Stack<Record> stack = new Stack<>();

    private static float yOffset = 0;

    private static Timer timer = new RepeatingTimer(interval, () -> {
        sortedList = new ArrayList<>(map.values());

        for(Record record : sortedList) {
            record.avgTime = (record.time*2)/60;
            record.time = 0;
        }

        Collections.sort(sortedList, new Comparator<Record>() {

            public int compare(Record r1, Record r2) {
                return (int)(r2.avgTime*1000 - r1.avgTime*1000);
            }
        });
    });

    public static void update(float delta){
        if(!enabled)
            return;

        timer.update(delta);
    }

    public static void begin(String name){
        if(!enabled)
            return;

        Record currRecord = map.get(name); //Try and get an existing record.
        if(currRecord == null){ //If it doesn't exist...
            //Create a new record and put it in the map.
            currRecord = new Record();
            currRecord.name = name;
            map.put(name, currRecord);

            //Try to get a parent record. If the stack has something, peek and get the parent.
            Record parentRecord = null;
            if(stack.size() > 0)
                parentRecord = stack.peek();

            //If we could get a parent, add it to the children and set the parent.
            if(parentRecord != null){
                parentRecord.children.add(currRecord);
                currRecord.parent = parentRecord;
                System.out.println("added child");
            }
        }

        if(currRecord.started)
            throw new RuntimeException("Profiler: Must call end on Record "+currRecord.name+" before begin.");

        currRecord.started = true;
        currRecord.startTime = System.nanoTime();



        stack.push(currRecord);
    }

    public static void begin(){
        if(!enabled)
            return;

        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        begin("Function: "+stackTraceElements[2].getMethodName());
    }

    public static void end(){
        if(!enabled)
            return;

        Record currRecord = stack.pop();
        currRecord.started = false;

        currRecord.time += System.nanoTime() - currRecord.startTime;
    }

    public static void drawDebug(SpriteBatch batch, float x, float y){
        if(!enabled)
            return;

        GUI.Text("Profiler", batch, x, y);
        yOffset = y-20;

        //For each record...
        for(Record record : sortedList){
            drawRecord(record, batch, x, yOffset); //Draw the parent and all children recursively.
        }

        for(Record record : sortedList)
            record.drawn = false;
    }

    private static void drawRecord(Record record, SpriteBatch batch, float x, float y){
        if(record.drawn) return;

        double d = record.avgTime/100000000; //Convert time into seconds.
        DecimalFormat df = new DecimalFormat("#.###"); //Format time.
        GUI.Text(record.name+": "+df.format(d)+" ms", batch, x, yOffset); //Draw time of this record.
        record.drawn = true; //Set draw to true.
        yOffset -= 20;

        //For each child, call drawRecord again. Recursion!
        for(Record child : record.children){
            drawRecord(child, batch, x+15, yOffset);
        }
    }

    private static class Record{
        public double startTime = 0;
        public boolean started = false;
        public boolean drawn = false;
        public String name;
        public double time = 0;
        public double avgTime = 0;
        public Record parent = null;
        public ArrayList<Record> children = new ArrayList<>();

    }
}
