package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
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
    private GUI.GUIStyle GUIStyle, changeLogStyle;
    private Rectangle startRect, quitRect, changelogRect;
    private Label logLabel, versionLabel;

    public MainMenuInterface(SpriteBatch batch, ColonyGame game) {
        super(batch, game);

        defaultUp = new TextureRegion(ColonyGame.assetManager.get("menuButton_normal", Texture.class));
        defaultOver = new TextureRegion(ColonyGame.assetManager.get("menuButton_moused", Texture.class));
        defaultDown = new TextureRegion(ColonyGame.assetManager.get("menuButton_clicked", Texture.class));

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        this.startRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25, 200, 50);
        this.quitRect = new Rectangle(Gdx.graphics.getWidth()/2 - 100, Gdx.graphics.getHeight()/2 - 25 - 100, 200, 50);
        this.changelogRect = new Rectangle(width - width * 0.2f, 0, width * 0.2f, height - height*0.2f);

        music.play();
        music.setLooping(true);

        this.GUIStyle = new GUI.GUIStyle();
        this.GUIStyle.font = titleFont;

        this.changeLogStyle = new GUI.GUIStyle();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        parameter.borderWidth = 1f;
        changeLogStyle.font = generator.generateFont(parameter);
        generator.dispose();

        this.makeLabels(changelogRect);
    }

    /**
     * Makes the labels for the changelog.
     * @param rect
     */
    private void makeLabels(Rectangle rect){
        if(DataBuilder.changelog == null || DataBuilder.changelog.changes == null)
            GH.writeErrorMessage("changelog.json has something wrong with it or does not exist.");

        //Sets the font and stuff.
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = changeLogStyle.font;

        //Combines all the lines from the Json log, adding newlines.
        DataBuilder.JsonLog log = DataBuilder.changelog.changes[0];
        String text = "";
        for(String txt : log.log)
            text += txt+"\n";

        //Sets the label text and style.
        logLabel = new Label(text, style);
        logLabel.setAlignment(Align.topLeft);
        logLabel.setWrap(true);

        //Version and date label.
        versionLabel = new Label("Version: "+log.version+"\n"+log.date, style);
        versionLabel.setAlignment(Align.center);
        setLabelBounds(rect);
    }

    private void setLabelBounds(Rectangle rect){
        logLabel.setBounds(rect.x, rect.y, rect.getWidth(), rect.getHeight());
        versionLabel.setBounds(rect.x, rect.y + rect.getHeight(), rect.getWidth(), 100);
    }

    @Override
    public void drawGUI(float delta) {
        super.drawGUI(delta);

        this.batch.draw(mainMenuTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Set a new font, draw "Colony Game" to the screen, and reset the font.
        GUI.font = titleFont;
        GUI.Label("Colony Game", this.batch, Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight() - 75, true, GUIStyle);
        GUI.ResetFont();

        this.logLabel.draw(this.batch, 1f);
        this.versionLabel.draw(this.batch, 1f);

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
        this.changelogRect.set(width - width * 0.2f, 0, width * 0.2f, height - height*0.2f);
        this.setLabelBounds(this.changelogRect);
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }


}
