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
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
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

    GUI.GUIStyle startButtonStyle = new GUI.GUIStyle();
    GUI.GUIStyle quitButtonStyle = new GUI.GUIStyle();
    GUI.GUIStyle blank1Style = new GUI.GUIStyle();
    GUI.GUIStyle blank2Style = new GUI.GUIStyle();
    GUI.GUIStyle blank3Style = new GUI.GUIStyle();

    Rectangle[] buttonRects = new Rectangle[5];

    private Texture titleTexture;
    private BitmapFont titleFont = new BitmapFont(Gdx.files.internal("fonts/titlefont.fnt"));
    private GUI.GUIStyle changeLogStyle = new GUI.GUIStyle();
    private Rectangle startRect = new Rectangle(), quitRect = new Rectangle(), blank1Rect = new Rectangle();
    private Rectangle blank2Rect = new Rectangle(), blank3Rect = new Rectangle(), changelogRect = new Rectangle(), titleRect = new Rectangle();
    private Label logLabel, versionLabel;

    public MainMenuInterface(SpriteBatch batch, ColonyGame game) {
        super(batch, game);

        titleTexture = ColonyGame.assetManager.get("Auroris", Texture.class);

        //Set the states for the start button
        startButtonStyle.normal = ColonyGame.assetManager.get("startbutton_normal", Texture.class);
        startButtonStyle.moused = ColonyGame.assetManager.get("startbutton_moused", Texture.class);
        startButtonStyle.clicked = ColonyGame.assetManager.get("startbutton_clicked", Texture.class);

        //States for quit button.
        quitButtonStyle.normal = ColonyGame.assetManager.get("quitbutton_normal", Texture.class);
        quitButtonStyle.moused = ColonyGame.assetManager.get("quitbutton_moused", Texture.class);
        quitButtonStyle.clicked = ColonyGame.assetManager.get("quitbutton_clicked", Texture.class);

        //For blank buttons
        blank1Style.normal = blank1Style.moused = blank1Style.clicked = blank2Style.normal = blank2Style.moused = ColonyGame.assetManager.get("blankbutton_normal", Texture.class);
        blank3Style.normal = blank3Style.moused = blank3Style.clicked = ColonyGame.assetManager.get("blankbutton_normal", Texture.class);

        //Assign all these to an array for easy displaying and resizing.
        buttonRects[0] = startRect;
        buttonRects[1] = blank1Rect;
        buttonRects[2] = blank2Rect;
        buttonRects[3] = blank3Rect;
        buttonRects[4] = quitRect;

        //Plays the main menu music.
        music.play();
        music.setLooping(true);

        //Sets up the font for the changelog area. Fancy!
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

    private void makeVersionHistory(){
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = changeLogStyle.font;

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("blueSquare", Texture.class)));

        //Gather all changes
        for(DataBuilder.JsonLog logs : DataBuilder.changelog.changes) {

            DataBuilder.JsonLog log = DataBuilder.changelog.changes[0];
            String text = "";
            for (String txt : log.log)
                text += txt + "\n";
        }

        //Label versionLabel = new Label();
        //ScrollPane scrollPane = new ScrollPane(versionLabel);
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
        GUI.Texture(titleTexture, titleRect, this.batch);
        GUI.ResetFont();

        this.logLabel.draw(this.batch, 1f);
        this.versionLabel.draw(this.batch, 1f);

        //Start button.
        if(GUI.Button(startRect, "", this.batch, startButtonStyle)){
            this.done = true;
            this.game.setScreen(new LoadingScreen(this.game));
            return;
        }

        GUI.Button(blank1Rect, "", this.batch, blank1Style);
        GUI.Button(blank2Rect, "", this.batch, blank2Style);
        GUI.Button(blank3Rect, "", this.batch, blank3Style);

        //Quit button.
        if(GUI.Button(quitRect, "", this.batch, quitButtonStyle)){
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
    }

    @Override
    public void resize(int width, int height) {
        this.changelogRect.set(width - width * 0.3f, 0, width * 0.3f, height - height * 0.1f);
        this.titleRect.set(width / 2 - titleTexture.getWidth()/2, height - titleTexture.getHeight() - height*0.02f, titleTexture.getWidth(), titleTexture.getHeight());
        this.setLabelBounds(this.changelogRect);

        for(int i=0;i<buttonRects.length;i++){
            Rectangle rect = buttonRects[i];

            rect.set(width/2 - 115, height - height*0.2f - 75*(i+1), 250, 60);
        }
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }


}
