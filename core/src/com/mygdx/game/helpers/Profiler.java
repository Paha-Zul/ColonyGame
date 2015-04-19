package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;

import java.text.DecimalFormat;
import java.util.*;

/**
 * A Class that can be used to profile parts of game loop code.
 */
public class Profiler {
    public static double interval = 0.5f; //The interval that the average time should be updated.
    public static boolean enabled = false; //If the profiler is enabled or not.

    private static HashMap<String, Profile> map = new HashMap<>(); //Holds the profile firstNames and links them with Records.
    private static ArrayList<Profile> sortedList = new ArrayList<>(); //Used to sort the map and print to the screen in order.
    private static Stack<Profile> stack = new Stack<>(); //Used to keep begins and ends in order and for parenting/adding children.

    private static float yOffset = 0; //A static Y offset for drawing to the screen.

    //A Timer of length 'interval'. The HashMap is made into an array where all the average values are calculated
    //and then sorted for screen printing.
    private static Timer timer = new RepeatingTimer(interval, () -> {
        sortedList = new ArrayList<>(map.values());

        for(Profile profile : sortedList) {
            profile.avgTimePerSecond = (profile.time*2);
            profile.avgTime = profile.avgTimePerSecond/ Gdx.graphics.getFramesPerSecond();
            profile.time = 0;
        }

        Collections.sort(sortedList, new Comparator<Profile>() {

            public int compare(Profile r1, Profile r2) {
                return (int)(r2.avgTime*1000 - r1.avgTime*1000);
            }
        });
    });

    /**
     * Updates the Profiler. This is necessary for calculating averages of the profiles and
     * sorting the profiles.
     * @param delta The time between this frame and the last.
     */
    public static void update(float delta){
        if(!enabled)
            return;

        timer.update(delta); //Update the timer.
    }

    /**
     * Begins a profile section to record time for.
     * @param name The name of the profile.
     */
    public static void begin(String name){
        if(!enabled)
            return;

        Profile currProfile = map.get(name); //Try and get an existing record.
        if(currProfile == null){ //If it doesn't exist...
            //Create a new record and put it in the map.
            currProfile = new Profile();
            currProfile.name = name;
            map.put(name, currProfile);

            //Try to get a parent record. If the stack has something, peek and get the parent.
            Profile parentProfile = null;
            if(stack.size() > 0)
                parentProfile = stack.peek();

            //If we could get a parent, add it to the children and set the parent.
            if(parentProfile != null){
                parentProfile.children.add(currProfile);
                currProfile.parent = parentProfile;
            }
        }

        //If the profile is already started, throw an error.
        if(currProfile.started)
            throw new RuntimeException("Profiler: Must call end on Record "+ currProfile.name+" before begin.");

        //Set started to true and the start time.
        currProfile.started = true;
        currProfile.startTime = System.nanoTime();

        //Push this profile on the stack.
        stack.push(currProfile);
    }

    /**
     * Begins a profile section. The function name, where this code was called, will be used for the name. This may be
     * slightly less efficient than providing a name.
     */
    public static void begin(){
        if(!enabled)
            return;

        StackTraceElement[] stackTraceElements =new Exception().getStackTrace();
        begin("Function: "+stackTraceElements[1].getMethodName());
    }

    /**
     * Ends a section. This will be tied to the most recent begin() call.
     */
    public static void end(){
        if(!enabled)
            return;

        //If there are no itemNames in the stack, throw an error.
        if(stack.size() < 1)
            throw new RuntimeException("Profiler: begin() must be called before an end()");

        Profile currProfile = stack.pop(); //Pop the top off.
        currProfile.started = false; //Set started to false.

        currProfile.time += System.nanoTime() - currProfile.startTime; //Add the new time difference.
    }

    /**
     * Draws the sorted averages to the screen.
     * @param batch The SpriteBatch to use for drawing to the screen.
     * @param x The starting X location.
     * @param y The starting Y location.
     */
    public static void drawDebug(SpriteBatch batch, float x, float y){
        if(!enabled)
            return;

        GUI.Text("Profiler", batch, x, y); //Draw the title.
        yOffset = y-20;

        //For each record...
        for(Profile profile : sortedList){
            drawRecord(profile, batch, x); //Draw the parent and all children recursively.
        }

        for(Profile profile : sortedList)
            profile.drawn = false;
    }

    /**
     * An internal draw function that will be called recursively for all records.
     * @param profile The Profile to draw.
     * @param batch The SpriteBatch to use for drawing to the screen.
     * @param x The X location to draw at.
     * @param y The Y location to draw at.
     */
    private static void drawRecord(Profile profile, SpriteBatch batch, float x){
        if(profile.drawn) return;

        double d = profile.avgTime/100000000; //Convert time into seconds.
        double d2 = profile.avgTimePerSecond/100000000; //Convert time into seconds.
        DecimalFormat df = new DecimalFormat("#.###"); //Format time.
        GUI.Text(profile.name+": "+df.format(d)+" ms ("+df.format(d2)+" ms per second)", batch, x, yOffset); //Draw time of this record.
        profile.drawn = true; //Set draw to true.
        yOffset -= 20;

        //For each child, call drawRecord again. Recursion!
        for(Profile child : profile.children){
            drawRecord(child, batch, x+15);
        }
    }

    private static class Profile {
        public double startTime = 0;
        public boolean started = false;
        public boolean drawn = false;
        public String name;
        public double time = 0;
        public double avgTime = 0; //The avg time PER FRAME
        public double avgTimePerSecond = 0; //The avg time PER SECOND
        public Profile parent = null;
        public ArrayList<Profile> children = new ArrayList<>();

    }
}
