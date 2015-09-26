package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.DataManager;

/**
 * Created by Paha on 8/12/2015.
 * Handles displaying Colony related stuff.
 */
public class ColonyWindow extends Window{
    private Rectangle colonyScreenRect = new Rectangle(), craftItemPreviewRectangle = new Rectangle();
    private TextureRegion colonyScreenBackground, darkBackground, textBackground;
    private GUI.GUIStyle colonyWindowStyle;

    private DataBuilder.JsonItem itemMousedOver  = null;
    private Vector2 offset = new Vector2();
    private com.badlogic.gdx.scenes.scene2d.ui.Window mainWindow;

    public ColonyWindow(PlayerInterface playerInterface, Entity target) {
        super(playerInterface, target);

        this.colonyWindowStyle = new GUI.GUIStyle();
        this.colonyScreenBackground = new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class));
        this.darkBackground = new TextureRegion(ColonyGame.assetManager.get("darkBackground", Texture.class));
        this.textBackground = new TextureRegion(ColonyGame.assetManager.get("plainBackground", Texture.class));
        this.draggable = true;

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle style = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(this.playerInterface.UIStyle.font, Color.BLACK, new TextureRegionDrawable(this.colonyScreenBackground));
        this.mainWindow = new com.badlogic.gdx.scenes.scene2d.ui.Window("", style);

        createColonyWindow();
    }

    private void createColonyWindow(){
        this.mainWindow.setBounds(200, 200, 600, 400);
        HorizontalGroup columns = new HorizontalGroup();

        mainWindow.addListener(new DragListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                offset.set(GH.getFixedScreenMouseCoords().x -  mainWindow.getX(), GH.getFixedScreenMouseCoords().y -  mainWindow.getY());
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                Vector2 mouse = GH.getFixedScreenMouseCoords();
                mainWindow.setPosition(mouse.x - offset.x, mouse.y - offset.y);
            }
        });

        Array<String> itemList = DataBuilder.JsonItem.allItems;
        int iconSize = 32, iconSpace = 10;
        int numIconsPerColumn = (int)(mainWindow.getHeight()/(iconSize+iconSpace));
        int numColumns = (int)(itemList.size/numIconsPerColumn + 0.5f); //The +0.5f is to round up!

        for(int i=0;i<numColumns;i++){
            Table table = new Table();

            for(int j=0;j<numIconsPerColumn;j++) {
                int index = j + i*numIconsPerColumn;
                DataBuilder.JsonItem itemRef = DataManager.getData(itemList.get(index), DataBuilder.JsonItem.class);

                Image icon = new Image(itemRef.iconTexture);
                table.add(icon).size(32);

                Label amount = new Label("0", new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK));
                amount.setAlignment(Align.center);
                table.add(amount).size(64,32).center();

                table.row();
            }

            columns.addActor(table);
        }

        mainWindow.add(columns).fill().expand().top().left();
        this.playerInterface.stage.addActor(mainWindow);
        this.playerInterface.stage.setDebugAll(true);

    }

    @Override
    public boolean update(SpriteBatch batch) {

        return false;
    }

    @Override
    public void destroy() {
        super.destroy();

        this.mainWindow.remove();
    }

    @Override
    public void resize(int width, int height) {
        this.colonyScreenRect.set(width / 2 - 300, height / 2 - 200, 600, 400);
        this.craftItemPreviewRectangle.set(0,0,200,200);
        this.setMainWindowRect(this.colonyScreenRect);
    }
}
