package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.screens.LoadingScreen;

/**
 * A UI that controls the main menu. This is responsible for all buttons, images, music, and sounds of the main menu.
 */
public class MainMenuInterface extends UI{
    public static Texture mainMenuTexture = ColonyGame.assetManager.get("Space2", Texture.class);
    public static Music music = Gdx.audio.newMusic(Gdx.files.internal("music/Karkarakacrrot.ogg"));

    private static TextureRegion defaultUp, defaultOver, defaultDown;

    private BitmapFont titleFont = new BitmapFont(Gdx.files.internal("fonts/titlefont.fnt"));

    private GUI.GUIStyle GUIStyle;

    private Rectangle startRect, quitRect, changelogRect;

    private Stage stage;

    public MainMenuInterface(SpriteBatch batch, ColonyGame game) {
        super(batch, game);

        defaultUp = new TextureRegion(ColonyGame.assetManager.get("defaultButton_normal", Texture.class));
        defaultOver = new TextureRegion(ColonyGame.assetManager.get("defaultButton_moused", Texture.class));
        defaultDown = new TextureRegion(ColonyGame.assetManager.get("defaultButton_clicked", Texture.class));

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        this.makeButtons();

        this.startRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25, 200, 50);
        this.quitRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25 - 100, 200, 50);
        this.changelogRect = new Rectangle();

        music.play();
        music.setLooping(true);

        this.GUIStyle = new GUI.GUIStyle();
        this.GUIStyle.font = titleFont;
    }

    private void makeButtons(){
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = new BitmapFont();
        style.up = new TextureRegionDrawable(defaultUp);
        style.over = new TextureRegionDrawable(defaultOver);
        style.down = new TextureRegionDrawable(defaultDown);

        TextButton button = new TextButton("Button", style);
        button.setBounds(Gdx.graphics.getWidth()/2 - 100,Gdx.graphics.getHeight()/2 - 25, 200, 50);

        stage.addActor(button);
    }

    @Override
    public void drawGUI(float delta) {
        super.drawGUI(delta);

        this.batch.draw(mainMenuTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Set a new font, draw "Colony Game" to the screen, and reset the font.
        GUI.font = titleFont;
        GUI.Label("Colony Game", this.batch, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight() - 75, true, GUIStyle);
        GUI.ResetFont();

        this.displayChangelog(this.changelogRect);

        //Start button.
        if(GUI.Button(startRect, "Start", this.batch, null)){
            this.done = true;
            this.game.setScreen(new LoadingScreen(this.game));
            return;
        }

        //Quit button.
        if(GUI.Button(quitRect, "Quit", this.batch, null)){
            Gdx.app.exit();
        }


        stage.act(delta);
        stage.draw();
    }

    private void displayChangelog(Rectangle rect){
        if(DataBuilder.changelog == null || DataBuilder.changelog.changes == null)
            GH.writeErrorMessage("changelog.json has something wrong with it or does not exist.");

        DataBuilder.JsonLog log = DataBuilder.changelog.changes[0];
        //Draw the version number
        GUI.Label("Version: "+ log.version + " ("+log.date+")", this.batch, rect.getX(), rect.getY() - 50, false);
        GUI.Label("Changes: ", this.batch, rect.getX(), rect.getY() - 70, false);
        for(int i=0;i<log.log.length;i++){
            GUI.Label(log.log[i], this.batch, rect.getX(), rect.getY() - 80 - (i+1)*15, false);
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
        GUIStyle = null;
    }

    @Override
    public void resize(int width, int height) {
        this.startRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25, 200, 50);
        this.quitRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25 - 100, 200, 50);
        this.changelogRect.set(width - width*0.2f, height, width*0.2f, height);
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }


}
