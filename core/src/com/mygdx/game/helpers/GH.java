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
        DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy HH-mm-ss");
        Date date = new Date();

        FileHandle handle = Gdx.files.internal(""); //Get a path to where the game is.
        File file = new File(handle.file().getAbsolutePath()+"/"+"err_"+dateFormat.format(date)+".txt"); //Create a new file with the pathname.
        FileHandle errorLog = new FileHandle(file); //Get a file handle to the error log that we create.
        errorLog.writeString(err, false); //Write the error.

        throw new RuntimeException("An error was written to an error file. Check the base directory. (For quick reference, err: "+err+")");
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

    public static float toMeters(float value){
        return value/Constants.SCALE;
    }

    public static float toReal(float value){
        return value*Constants.SCALE;
    }

}
