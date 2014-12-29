package com.mygdx.game.component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.helpers.GUI;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class PlayerInterface extends Component implements IGUI {
    SpriteBatch batch;
    Texture background;

    Rectangle buttonRect = new Rectangle();
    Rectangle infoRect = new Rectangle();
    float FPS = 0;

    Timer FPSTimer;

    public PlayerInterface(SpriteBatch batch) {
        this.batch = batch;

        this.addToList();
    }

    @Override
    public void start() {
        super.start();

        this.background = new Texture("img/background.png");

        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);
        this.infoRect.set(0,0,Gdx.graphics.getWidth(), 0.1f*Gdx.graphics.getHeight());

        Functional.Callback callback = () -> this.FPS = 1/Gdx.graphics.getDeltaTime();
        this.FPSTimer = new RepeatingTimer(0.5d, callback);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        FPSTimer.update(delta);

        if(GUI.Button(this.buttonRect, this.background, "FREAKING SUCHAS", this.batch))
            Gdx.app.exit();

        GUI.Texture(this.infoRect, this.background, this.batch);

        GUI.Text("FPS: "+FPS, this.batch, Gdx.graphics.getWidth() - 70, Gdx.graphics.getHeight() - 0);
    }

    @Override
    public void destroy() {
        super.destroy();

    }

    @Override
    public void resize(int width, int height) {
        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);
        this.infoRect.set(0,0,Gdx.graphics.getWidth(), 0.1f*Gdx.graphics.getHeight());
        //System.out.println("Resized");
    }

    @Override
    public void addToList() {
        ListHolder.addInterface(this);
    }
}
