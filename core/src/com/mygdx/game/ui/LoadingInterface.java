package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.util.ListHolder;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.worldgeneration.WorldGen;

/**
 * Created by Bbent_000 on 12/29/2014.
 */
public class LoadingInterface extends UI{
    private SpriteBatch batch;
    private float width = 200, height = 20;
    private boolean done;

    private Rectangle loadingBar = new Rectangle(), square = new Rectangle();
    private TextureRegion outline, bar;
    private TextureRegion whiteTexture;

    public LoadingInterface(SpriteBatch batch){
        super(batch);
        this.batch = batch;

        this.whiteTexture = new TextureRegion(WorldGen.whiteTex);
        this.outline = new TextureRegion(ColonyGame.assetManager.get("LoadingBarOutline", Texture.class));
        this.bar = new TextureRegion(ColonyGame.assetManager.get("LoadingBar", Texture.class));

        this.loadingBar.set(Gdx.graphics.getWidth()/2 - width/2, Gdx.graphics.getHeight()/2 + height/2, width, height);
        this.square.set(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.addToList();
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);

        if(!done) {
            Color color = this.batch.getColor();
            this.batch.setColor(Color.BLACK);
            GUI.Texture(this.whiteTexture, this.batch, this.square);
            this.batch.setColor(color);

            GUI.Texture(this.outline, this.batch, this.loadingBar);
            GUI.Texture(this.bar, this.batch, this.loadingBar.x, this.loadingBar.y, this.loadingBar.width*WorldGen.getInstance().percentageDone, this.loadingBar.height);
            GUI.Label("Loading Terrain", this.batch, this.loadingBar.getX(), this.loadingBar.getY(), loadingBar.getWidth(), loadingBar.getHeight());
        }else
            this.destroy();
    }

    public void setDone(){
        done = true;
    }

    @Override
    public void resize(int width, int height) {
        this.loadingBar.set(Gdx.graphics.getWidth()/2 - this.width/2, Gdx.graphics.getHeight()/2 + this.height/2, this.width, this.height);
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }

    @Override
    public void destroy() {
        super.destroy();

        batch = null;
        loadingBar = null;
        square = null;
        outline = null;
        bar = null;
    }

}
