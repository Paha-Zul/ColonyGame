package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Bbent_000 on 11/17/2014.
 */
public class GH {
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
                errorLog.writeString(elem.toString(), true);
        }

        throw new RuntimeException("An error was written to an error file. Check the base directory. (For quick reference, err: "+err+")");
    }

    public static int[] fixRanges(int startX, int endX, int startY, int endY, int width, int height){
        int[] nums = new int[4];
        nums[0] = startX < 0 ? 0 : startX;
        nums[1] = endX >= width ? width - 1 : endX;
        nums[2] = startY < 0 ? 0 : startY;
        nums[3] = endY >= height ? height - 1 : endY;

        return nums;
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
        original = original.replace("%t", event.eventTargetOther.name);

        return original;
    }

    public static float toMeters(float value){
        return value/Constants.SCALE;
    }

    public static float toReal(float value){
        return value*Constants.SCALE;
    }

}
