package com.mygdx.game.component.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.GUI;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;

import java.util.function.Function;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class PlayerInterface extends Component implements IGUI, InputProcessor {
    private SpriteBatch batch;
    private Texture background;
    private World world;

    private Rectangle buttonRect = new Rectangle();
    private Rectangle infoRect = new Rectangle();
    private float FPS = 0;

    private Interactable interactable;

    private Timer FPSTimer;

    private Vector2 testPoint = new Vector2();
    private Entity selected = null;
    private QueryCallback callback = fixture -> {
        System.out.println("Querying at location: "+fixture.getBody().getPosition());
        if(fixture.testPoint(testPoint.x, testPoint.y)){
            this.selected = (Entity)fixture.getBody().getUserData();
            System.out.println(this.selected.name+" was selected");
            return false;
        }

        return true;
    };

    /**
     * A player interface Component that will display information on the screen.
     * @param batch The SpriteBatch for drawing to the screen.
     * @param world The Box2D world. We need to know about this for clicking on objects.
     */
    public PlayerInterface(SpriteBatch batch, World world) {
        this.batch = batch;
        this.world = world;

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

        Gdx.input.setInputProcessor(this);

        this.interactable = this.owner.getComponent(Interactable.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        FPSTimer.update(delta);

//        if(GUI.Button(this.buttonRect, this.background, "FREAKING SUCHAS", this.batch))
//            Gdx.app.exit();

        GUI.Texture(this.infoRect, this.background, this.batch);

        GUI.Text("FPS: "+FPS, this.batch, 0, Gdx.graphics.getHeight() - 40);
        GUI.Text("Resolution: "+Gdx.graphics.getDesktopDisplayMode().width+"X"+Gdx.graphics.getDesktopDisplayMode().height, this.batch, 0, Gdx.graphics.getHeight() - 60);
        GUI.Text("NumTrees: "+ WorldGen.numTrees(), this.batch, 0, Gdx.graphics.getHeight() - 80);
        GUI.Text("NumTiles: "+WorldGen.numTiles(), this.batch, 0, Gdx.graphics.getHeight() - 100);

    }

    @Override
    public void destroy() {
        super.destroy();

    }

    @Override
    public void resize(int width, int height) {
        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);
        this.infoRect.set(0,0,Gdx.graphics.getWidth(), 0.1f*Gdx.graphics.getHeight());
    }

    @Override
    public void addToList() {
        ListHolder.addInterface(this);
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
//        System.out.println("There was a click: "+button);
        int fixedY = Gdx.graphics.getHeight()-screenY;
        if(button == Input.Buttons.LEFT){
//            System.out.println("Inside, X/Y: "+screenX+" "+fixedY);
            this.testPoint.set(screenX, fixedY);
            this.world.QueryAABB(this.callback, screenX - 1f, fixedY - 1f, screenX + 1f, fixedY + 1f);
            return true;
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        ColonyGame.camera.zoom += amount*Gdx.graphics.getDeltaTime();
        if(ColonyGame.camera.zoom < 0) ColonyGame.camera.zoom = 0;
        return false;
    }
}
