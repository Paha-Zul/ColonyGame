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

    private static HashMap<String, Record> map = new HashMap<>();
    private static ArrayList<Record> sortedList = new ArrayList<>();
    private static Stack<Record> stack = new Stack<>();

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
        timer.update(delta);
    }

    public static void begin(String name){
        Record currRecord = map.get(name);
        if(currRecord == null){
            currRecord = new Record();
            currRecord.name = name;
            map.put(name, currRecord);
        }

        if(currRecord.started)
            throw new RuntimeException("Profiler: Must call end on Record "+currRecord.name+" before begin.");

        currRecord.started = true;
        currRecord.startTime = System.nanoTime();

        stack.push(currRecord);
    }

    public static void end(){
        Record currRecord = stack.pop();
        currRecord.started = false;

        currRecord.time += System.nanoTime() - currRecord.startTime;
    }

    public static void drawDebug(SpriteBatch batch, float x, float y){
        GUI.Text("Profiler", batch, x, y);
        y-=20;
        for(Record record : sortedList){
            double d = record.avgTime/100000000;
            DecimalFormat df = new DecimalFormat("#.###");
            GUI.Text(record.name+": "+df.format(d)+" ms", batch, x, y);
            y-=20;
        }
    }

    private static class Record{
        public double startTime = 0;
        public boolean started = false;
        public String name;
        public double time = 0;
        public double avgTime = 0;

    }
}
