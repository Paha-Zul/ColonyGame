package com.mygdx.game.helpers.managers;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.helpers.Constants;

/**
 * Created by Paha on 3/8/2015.
 */
public class SoundManager {

    public static void play(Sound sound, Vector2 soundPos, Vector2 lookPos, float falloffStart, float falloffMax){
        float dis = soundPos.dst(lookPos);
        falloffStart /= Constants.SCALE;
        falloffMax /= Constants.SCALE;
        if(dis <= falloffStart)
            sound.play(1f);
        else if(dis <= falloffMax){
            float diff = falloffMax - falloffStart;
            float vol = 1 - ((dis - falloffStart)/diff);
            sound.play(vol);
        }
    }

    public class SoundFile{

    }
}
