package com.mygdx.game.util.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.interfaces.Functional;
import com.sun.istack.internal.Nullable;

/**
 * Created by Paha on 8/12/2015.
 * Experimental button class.
 */
public class Button {
    public Functional.Callback onOver, onDown, onUp, onOut;
    public TextureRegion overImage, downImage, normalImage;
    public boolean consumeInput = false; //If false, the UI will keep checking for elements under this even if we are moused over this.

    private int x, y, width, height;
    private String imageName, textureAtlasName;
    private TextureAtlas atlas;
    private int lastState = 0;

    public Button(int x, int y, int width, int height, @Nullable String imageName, @Nullable String textureAtlasName){
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.imageName = imageName;
        this.textureAtlasName = textureAtlasName;

        if(this.imageName == null){
            this.normalImage = new TextureRegion(ColonyGame.assetManager.get("defaultButton", Texture.class));
            this.overImage = new TextureRegion(ColonyGame.assetManager.get("defaultButton_moused", Texture.class));
            this.downImage = new TextureRegion(ColonyGame.assetManager.get("defaultButton_clicked", Texture.class));
        }else{
            if(this.textureAtlasName == null){
                this.normalImage = new TextureRegion(ColonyGame.assetManager.get(imageName, Texture.class));
            }else{
                this.atlas = ColonyGame.assetManager.get(textureAtlasName, TextureAtlas.class);
                this.normalImage = this.atlas.findRegion(this.imageName);
            }
        }
    }

    public boolean render(SpriteBatch batch){
        int state = GUI.Button(batch, "", x, y, width, height, null);
        if(state != this.lastState){
            if(state == GUI.NONE && onOut != null) onOut.callback();
            else if(state == GUI.OVER && onOver != null) onOver.callback();
            else if(state == GUI.DOWN && onDown != null) onDown.callback();
            else if(state == GUI.UP && onUp != null) onUp.callback();

            this.lastState = state;
        }

        return consumeInput && state > 0;
    }
}
