package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.IGUI;

/**
 * Created by Bbent_000 on 12/29/2014.
 */
public class LoadingInterface extends UI {
    private SpriteBatch batch;
    private float width = 200, height = 20;
    private boolean done;

    private Rectangle loadingBar = new Rectangle(), square = new Rectangle();
    private Texture outline, bar;

    public LoadingInterface(SpriteBatch batch, ColonyGame game){
        super(batch, game);
        this.batch = batch;

        this.outline = new Texture("img/LoadingBarOutline.png");
        this.bar = new Texture("img/LoadingBar.png");

        this.loadingBar.set(Gdx.graphics.getWidth()/2 - width/2, Gdx.graphics.getHeight()/2 + height/2, width, height);
        this.square.set(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        this.addToList();
    }

    @Override
    public void drawGUI(float delta) {
        super.drawGUI(delta);

        if(!done) {
            Color color = this.batch.getColor();
            this.batch.setColor(Color.BLACK);
            GUI.Texture(this.square, WorldGen.getInstance().whiteTex, this.batch);
            this.batch.setColor(color);

            GUI.Texture(this.loadingBar, this.outline, this.batch);
            GUI.Texture(this.loadingBar.x, this.loadingBar.y, this.loadingBar.width*WorldGen.getInstance().percentageDone, this.loadingBar.height, this.bar, this.batch);
            GUI.Label("Loading Terrain", this.batch, this.loadingBar.getX() + this.loadingBar.width/2, this.loadingBar.getY() + 40, true);
        }
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
        outline.dispose();
        outline = null;
        bar.dispose();
        bar = null;
    }
}
