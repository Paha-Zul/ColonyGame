package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Bbent_000 on 11/17/2014.
 */
public class GH {
    private static Vector2 tmp = new Vector2();

	public static <T> T as(Class<T> t, Object o) {
		return t.isInstance(o) ? t.cast(o) : null;
	}

    public static void writeErrorMessage(String err){
        writeErrorMessage(err, false);
    }

    public static void writeErrorMessage(String err, boolean writeStackTrace){
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
        Date date = new Date();

        FileHandle handle = Gdx.files.internal(""); //Get a path to where the game is.
        File file = new File(handle.file().getAbsolutePath()+"/"+"err_"+dateFormat.format(date)+".txt"); //Create a new file with the pathname.
        FileHandle errorLog = new FileHandle(file); //Get a file handle to the error log that we create.
        errorLog.writeString(err, false); //Write the error.
        if(writeStackTrace){
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            for(StackTraceElement elem : trace)
                errorLog.writeString(elem.toString()+"\n", true);
        }

        throw new RuntimeException("An error was written to an error file. Check the base directory. (For quick reference, err: "+err+")");
    }

    /**
     * Writes the Exception to a file.
     * @param e The exception to write.
     */
    public static void writeErrorMessage(Exception e){
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
        Date date = new Date();

        FileHandle handle = Gdx.files.internal(""); //Get a path to where the game is.
        File file = new File(handle.file().getAbsolutePath()+"/"+"err_"+dateFormat.format(date)+".txt"); //Create a new file with the pathname.
        FileHandle errorLog = new FileHandle(file); //Get a file handle to the error log that we create.

        String message = e.getMessage();
        if(message == null) message = "";
        errorLog.writeString(message, true);
        StackTraceElement[] trace = e.getStackTrace();
        for(StackTraceElement elem : trace)
            errorLog.writeString(elem.toString()+"\n", true);
    }

    /**
     * Calculates the correct ranges for X and Y.
     * @param startX The start X index.
     * @param endX The ending X index.
     * @param startY The starting Y index.
     * @param endY The ending Y index.
     * @param width The width of the allowed ranged (0 to width, ie: 0 to 200)
     * @param height The height of the allowed range (0 to height, ie: 0 to 200)
     * @return An array that contains the 4 ranges, start and end. [0]-startX, [1]-endX, [2]-startY, [3]-endY.
     */
    public static int[] fixRanges(int startX, int endX, int startY, int endY, int width, int height){
        int[] nums = new int[4];
        nums[0] = startX < 0 ? 0 : startX;
        nums[1] = endX >= width ? width - 1 : endX;
        nums[2] = startY < 0 ? 0 : startY;
        nums[3] = endY >= height ? height - 1 : endY;

        return nums;
    }

    public static int[] fixRanges(float squareSize, float startX, float startY, float endX, float endY, int width, int height){
        int[] ranges = new int[4];
        ranges[0] = (int)(startX/squareSize);
        ranges[1] = (int)(endX/squareSize);
        ranges[2] = (int)(startY/squareSize);
        ranges[3] = (int)(endY/squareSize);

        ranges[0] = ranges[0] < 0 ? 0 : ranges[0];
        ranges[1] = ranges[1] >= width ? width-1 : ranges[1];
        ranges[2] = ranges[2] < 0 ? 0 : ranges[2];
        ranges[3] = ranges[3] >= height ? height-1 : ranges[3];

        return ranges;
    }

	public static class Message implements Serializable {
        public String message;
    }

    public static void fixBleeding(TextureRegion region) {
        float x = region.getRegionX();
        float y = region.getRegionY();
        float width = region.getRegionWidth();
        float height = region.getRegionHeight();
        float invTexWidth = 1f / region.getTexture().getWidth();
        float invTexHeight = 1f / region.getTexture().getHeight();
        region.setRegion((x + .5f) * invTexWidth, (y+.5f) * invTexHeight, (x + width - .5f) * invTexWidth, (y + height - .5f) * invTexHeight);
    }

    public static String generateEventDescription(DataBuilder.JsonPlayerEvent event){
        StringBuilder builder = new StringBuilder();
        for(String desc : event.eventDescription) builder.append(desc);

        String original = builder.toString();
        original = original.replace("%et", event.eventTarget.name);
        original = original.replace("%eot", event.eventTargetOther.name);

        return original;
    }

    /**
     * @return The fixed mouse screen coordinates (don't use this for world coordinates). Uses a reusable Vector2.
     */
    public static Vector2 getFixedScreenMouseCoords(){
        return tmp.set(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }


    public static float toMeters(float value){
        return value/Constants.SCALE;
    }

    public static float toReal(float value){
        return value*Constants.SCALE;
    }

    /**
     * Bounds a value.
     * @param min The minimum the value can be.
     * @param max The maximum the value can be.
     * @param value The actual value.
     * @return The min, max, or value if within the bounds.
     */
    public static float bound(float max, float min, float value){
        if(value <= min) return min;
        if(value >= max) return max;
        return value;
    }

    public static boolean isValid(Entity entity){
        return entity != null && entity.isValid();
    }

    public static boolean isValid(Component component){
        return component != null && component.getEntityOwner() != null && component.getEntityOwner().isValid();
    }



}
