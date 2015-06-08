package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.interfaces.IScript;
import com.mygdx.game.screens.LoadingScreen;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.ListHolder;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.ScriptManager;
import com.mygdx.game.util.worldgeneration.WorldGen;

/**
 * A UI that controls the main menu. This is responsible for all buttons, images, music, and sounds of the main menu.
 */
public class MainMenuInterface extends UI{
    public static Texture mainMenuTexture;
    public static Music music;

    GUI.GUIStyle startButtonStyle = new GUI.GUIStyle();
    GUI.GUIStyle quitButtonStyle = new GUI.GUIStyle();
    GUI.GUIStyle blank1Style = new GUI.GUIStyle();
    GUI.GUIStyle blank2Style = new GUI.GUIStyle();
    GUI.GUIStyle blank3Style = new GUI.GUIStyle();

    Rectangle[] buttonRects = new Rectangle[5];

    private TextureRegion titleTexture;
    private BitmapFont titleFont;
    private GUI.GUIStyle changeLogStyle = new GUI.GUIStyle();
    private Rectangle startRect = new Rectangle(), quitRect = new Rectangle(), blank1Rect = new Rectangle();
    private Rectangle blank2Rect = new Rectangle(), blank3Rect = new Rectangle(), changelogRect = new Rectangle(), titleRect = new Rectangle();
    private Container<ScrollPane> outsideScrollContainer;

    private Array<CheckBox> scriptCheckBoxList = new Array<>();

    private Stage stage;
    private ColonyGame game;

    public MainMenuInterface(SpriteBatch batch, ColonyGame game) {
        super(batch);
        this.game = game;

        titleTexture = new TextureRegion(ColonyGame.assetManager.get("Auroris", Texture.class));
        music = Gdx.audio.newMusic(Gdx.files.internal(DataManager.getData("misc", DataBuilder.JsonMisc.class).mainMenuMusic));
        titleFont = new BitmapFont(Gdx.files.internal("fonts/titlefont.fnt"));
        mainMenuTexture = ColonyGame.assetManager.get("Space2", Texture.class);

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
        DataManager.addData("changelogFont", changeLogStyle.font, BitmapFont.class);
        generator.dispose();

        //Create a new stage and configure it!
        stage = new Stage(new ScreenViewport(ColonyGame.UICamera), this.batch);
        //stage.setDebugAll(true);
        Gdx.input.setInputProcessor(stage);

        this.makeVersionHistoryScrollbar();
        this.addScriptCheckBoxes();
    }

    private void makeVersionHistoryScrollbar(){
        Label.LabelStyle historyStyle = new Label.LabelStyle();
        historyStyle.font = changeLogStyle.font;

        //Create the table for this inside text.
        Table insideScrollTextTable = new Table();

        //The scrollpane that holds the saveContainer.
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.vScrollKnob = new TextureRegionDrawable(new TextureRegion(WorldGen.whiteTex));
        ScrollPane versionHistoryScroll = new ScrollPane(insideScrollTextTable, scrollStyle);

        //Create the outside table that will hold the scrollpane
        this.outsideScrollContainer = new Container<>(versionHistoryScroll).fill();

        StringBuilder str;
        for(int i = 0;i<DataBuilder.changelog.changes.length;i++){
            DataBuilder.JsonLog log = DataBuilder.changelog.changes[i];

            Label title = new Label("Version: " + log.version+"\nDate: "+log.date, historyStyle);
            title.setAlignment(Align.center);

            Label text = new Label("", historyStyle);
            text.setWrap(true);

            str = new StringBuilder();
            for(String txt : log.log)
                str.append(txt).append("\n");

            text.setText(str.toString());

            insideScrollTextTable.add(title).fillX().expandX();
            insideScrollTextTable.row();
            insideScrollTextTable.add(text).fillX().expandX();
            insideScrollTextTable.row().padTop(20f);
        }

        stage.addActor(outsideScrollContainer);

//        Table table = new Table();
//        table.add(versionHistoryScroll).width(500).height(Gdx.graphics.getHeight() * 0.8f);
//        table.setBounds(0, Gdx.graphics.getHeight() - Gdx.graphics.getHeight() * 0.8f, 500, Gdx.graphics.getHeight() * 0.8f);
    }

    private void addScriptCheckBoxes(){
        Table table = new Table();
        table.setBounds(0, 0, 200, 500);

        TextureRegion over = new TextureRegion(WorldGen.whiteTex);
        over.setRegion(0,0,20,20);

        TextureRegion on = new TextureRegion(WorldGen.makeTexture(Color.GREEN));
        on.setRegion(0,0,20,20);

        TextureRegion off = new TextureRegion(WorldGen.makeTexture(Color.GRAY));
        off.setRegion(0, 0, 20, 20);

        for(IScript script : ScriptManager.scripts){
            CheckBox.CheckBoxStyle style = new CheckBox.CheckBoxStyle();
            style.font = changeLogStyle.font;

            style.checkboxOver = new TextureRegionDrawable(over);
            style.checkboxOn = new TextureRegionDrawable(on);
            style.checkboxOff = new TextureRegionDrawable(off);

            CheckBox checkBox = new CheckBox(script.getClass().getSimpleName(), style);
            scriptCheckBoxList.add(checkBox);

            table.add(checkBox).left();
            table.row().left();
        }

        stage.addActor(table);
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);

        this.batch.draw(mainMenuTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Set a new font, draw "Colony Game" to the screen, and reset the font.
        GUI.font = titleFont;
        GUI.Texture(titleTexture, this.batch, titleRect);
        GUI.ResetFont();

        //Start button.
        if(GUI.Button(this.batch, "", startRect, startButtonStyle)){
            for(int i=0;i<scriptCheckBoxList.size;i++){
                if(scriptCheckBoxList.get(i).isChecked())
                    ScriptManager.scripts.get(i).start();
            }

            this.game.setScreen(new LoadingScreen(this.game));
            this.destroy();
            return;
        }

        GUI.Button(this.batch, "", blank1Rect, blank1Style);
        GUI.Button(this.batch, "", blank2Rect, blank2Style);
        GUI.Button(this.batch, "", blank3Rect, blank3Style);

        //Quit button.
        if(GUI.Button(this.batch, "", quitRect, quitButtonStyle)){
            Gdx.app.exit();
        }

        this.batch.end();

        stage.act(delta);
        stage.draw();

        batch.setColor(Color.WHITE);

        this.batch.begin();
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

        stage.dispose();
    }

    @Override
    public void resize(int width, int height) {
        this.changelogRect.set(width - width * 0.3f, 0, width * 0.3f, height - height * 0.1f);
        this.titleRect.set(width / 2 - titleTexture.getRegionWidth() / 2, height - titleTexture.getRegionHeight() - height * 0.02f, titleTexture.getRegionWidth(), titleTexture.getRegionHeight());

        this.outsideScrollContainer.setBounds(width * 0.66f, height * 0.1f, width * 0.3f, height * 0.8f);
        this.outsideScrollContainer.invalidate();

        stage.getViewport().update(width, height);

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
