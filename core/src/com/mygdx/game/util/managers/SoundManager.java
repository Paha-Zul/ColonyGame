package com.mygdx.game.util.managers;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 3/8/2015.
 */
public class SoundManager {
    private static Array<SoundFile> currentSounds = new Array<>();

    /**
     *
     * @param sound The sound to play.
     * @param soundPos The location of the sound.
     * @param lookPos Where the player is currently looking.
     * @param falloffStart The distance at which to begin tapering the sound.
     * @param falloffMax The distance at which no sound is heard.
     */
    public static void play(Sound sound, Vector2 soundPos, Vector2 lookPos, float falloffStart, float falloffMax){
        falloffStart = GH.toMeters(falloffStart);
        falloffMax = GH.toMeters(falloffMax);

        //The effect of the zoom...
        float zoomMult = (1 - GH.bound(1, 0, 2-ColonyGame.camera.zoom))*falloffMax;

        //The distance with zoom factored in.
        float dis = soundPos.dst(lookPos) - (2/ColonyGame.camera.zoom) + zoomMult;
        if(dis <= falloffStart)
            sound.play(1f);
        else if(dis <= falloffMax){
            float diff = falloffMax - falloffStart;
            float vol = 1 - ((dis - falloffStart)/diff);
            sound.play(vol);
        }
    }

    public static void play(SoundFile sound, Vector2 soundPos, Vector2 lookPos, float falloffStart, float falloffMax){
        currentSounds.add(sound);
    }

    public class SoundFile{

    }
}
