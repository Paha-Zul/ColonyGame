package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;

import java.util.HashMap;
import java.util.function.Predicate;

/**
 * Created by Paha on 6/15/2015.
 */
public class Effects extends Component{
    HashMap<String, Effect> effectMap = new HashMap<>();
    private Array<Effect> activeEffects = new Array<>();

    @Override
    public void init() {
        super.init();

        this.setActive(false);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void initLoad() {
        super.initLoad();
    }

    @Override
    public void load() {
        super.load();
    }

    /**
     * Creates a new effect with a predicate to test.
     * @param name The simple name of the effect, ie: "dehydration".
     * @param displayName The display name to use for tooltips and such.
     * @param imgName The image name to use for getting an image.
     * @param predicate The predicate used for testing this effect.
     * @param <T> The type of object to test for.
     */
    public <T> void addNewEffect(String name, String displayName, String imgName, Predicate<T> predicate){
        Effect<T> effect = new Effect<>(name, displayName, imgName, predicate);
        effectMap.put(name, effect);
    }

    public <T> boolean testEffect(String name, T obj){
        return effectMap.get(name).test(obj);
    }

    public <T> boolean testAndSetEffect(String name, T obj){
        Effect effect = effectMap.get(name);
        boolean before = effect.isActive(); //Get the current active state.
        boolean after = effect.test(obj); //Test the effect.
        effect.setActive(after);        //Set the new active state.
        if(before != after) {           //If the old doesn't match the new...
            if (after) activeEffects.add(effect);   //Add it to the active list if now active
            else activeEffects.removeValue(effect, true);   //Otherwise, remove it.
        }

        return after;
    }

    public <T> Effect<T> getEffect(String name){
        return effectMap.get(name);
    }

    public Array<Effect> getActiveEffects(){
        return this.activeEffects;
    }

    public static class Effect<T>{
        private String name, displayName, imgName;
        private Predicate<T> predicate;
        private boolean active;
        private TextureRegion icon;

        private Effect(String name, String displayName, String imgName, Predicate<T> predicate){
            this.name = name;
            this.displayName = displayName;
            this.imgName = imgName;
            this.predicate = predicate;
            this.icon = new TextureRegion(ColonyGame.assetManager.get(imgName, Texture.class));
        }

        public boolean test(T t){
            return this.predicate.test(t);
        }

        public void setActive(boolean active){
            this.active = active;
        }

        public boolean isActive(){
            return this.active;
        }

        public TextureRegion getIcon(){
            return this.icon;
        }
    }
}
