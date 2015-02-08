package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.screens.LoadingScreen;

/**
 * Created by Paha on 1/10/2015.
 */
public class MainMenuInterface extends UI{
    public static Texture mainMenuTexture = new Texture("img/Space2.png");
    public static Music music = Gdx.audio.newMusic(Gdx.files.internal("music/Karkarakacrrot.ogg"));

    private String versionNumber = "0.1";
    private BitmapFont titleFont = new BitmapFont(Gdx.files.internal("fonts/titlefont.fnt"));

    private GUI.ButtonStyle buttonStyle;

    private Rectangle startRect;
    private Rectangle quitRect;

    public MainMenuInterface(SpriteBatch batch, ColonyGame game) {
        super(batch, game);

        this.startRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25, 200, 50);
        this.quitRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25 - 100, 200, 50);
        music.play();
        music.setLooping(true);

        this.buttonStyle = new GUI.ButtonStyle();
    }

    @Override
    public void drawGUI(float delta) {
        super.drawGUI(delta);

        this.batch.draw(mainMenuTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Set a new font, draw "Colony Game" to the screen, and reset the font.
        GUI.font = titleFont;
        GUI.Label("Colony Game", this.batch, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight() - 75, true);
        GUI.ResetFont();

        //Draw the version number
        GUI.Label("Version: "+this.versionNumber, this.batch, quitRect.getX() + quitRect.getWidth()/2, quitRect.getY() - 50, true);

        //Start button.
        if(GUI.Button(startRect, null, "Start", this.batch)){
            this.done = true;
            this.game.setScreen(new LoadingScreen(this.game));
            return;
        }

        //Quit button.
        if(GUI.Button(quitRect, null, "Quit", this.batch)){
            Gdx.app.exit();
            return;
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        music.stop();
        music.dispose();
        mainMenuTexture.dispose();
        titleFont.dispose();
        titleFont = null;
        startRect = null;
        quitRect = null;
        buttonStyle = null;

        versionNumber = null;
    }

    @Override
    public void resize(int width, int height) {
        this.startRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25, 200, 50);
        this.quitRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25 - 100, 200, 50);
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }


}
