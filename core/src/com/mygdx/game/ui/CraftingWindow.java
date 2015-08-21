package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.CraftingStation;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.EventSystem;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.ItemNeeded;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.DataManager;
import com.sun.istack.internal.Nullable;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Created by Paha on 8/16/2015.
 * A Window for interacting with Entities that provide crafting options.
 */
public class CraftingWindow extends Window{
    private TextureRegion craftBackground, selectBackground, infoBackground, stalledBackground, openBackground;
    private com.badlogic.gdx.scenes.scene2d.ui.Window craftingWindow;

    private CraftingStation craftingStation;
    private DataBuilder.JsonItem selectedItem;

    private Array<Label> availabelLabels, inProgressLabels, stalledLabels, craftingLabels;
    private List<Label> aList, ipList, sList, cList;
    private Table craftingWindowTable;

    private Consumer<Object[]> function;

    private Vector2 offset;

    private DecimalFormat percentFormat = new DecimalFormat("#.0");


    public CraftingWindow(PlayerInterface playerInterface, Entity target) {
        super(playerInterface, target);

        this.craftBackground = new TextureRegion(ColonyGame.assetManager.get("craftingWindowBackground", Texture.class));
        this.selectBackground = new TextureRegion(ColonyGame.assetManager.get("craftingWindowSelectionBackground", Texture.class));
        this.infoBackground = new TextureRegion(ColonyGame.assetManager.get("craftingWindowInfoBackground", Texture.class));
        this.stalledBackground = new TextureRegion(ColonyGame.assetManager.get("stalledJobsBackground", Texture.class));
        this.openBackground = new TextureRegion(ColonyGame.assetManager.get("openJobsBackground", Texture.class));

        this.craftingStation = this.target.getComponent(CraftingStation.class);
        this.offset = new Vector2();

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle style = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(this.playerInterface.UIStyle.font, Color.BLACK, new TextureRegionDrawable(this.craftBackground));
        this.craftingWindow = new com.badlogic.gdx.scenes.scene2d.ui.Window("WindowTop", style);
        this.craftingWindow.addListener(new ClickListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                offset.set(GH.getFixedScreenMouseCoords().x - craftingWindow.getX(), GH.getFixedScreenMouseCoords().y - craftingWindow.getY());
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                super.touchDragged(event, x, y, pointer);
                Vector2 mouse = GH.getFixedScreenMouseCoords();
                craftingWindow.setPosition(mouse.x - offset.x, mouse.y - offset.y);
            }
        });

        //Add the crafting window.
        this.craftingWindow.setSize(700, 500);
        this.playerInterface.stage.addActor(this.craftingWindow);

        this.craftingWindowTable = new Table();
        this.craftingWindow.addActor(this.craftingWindowTable);

        List.ListStyle aStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        List.ListStyle ipStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        List.ListStyle sStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        List.ListStyle cStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        aStyle.background = new TextureRegionDrawable(this.selectBackground);
        ipStyle.background = new TextureRegionDrawable(this.selectBackground);
        sStyle.background = new TextureRegionDrawable(this.selectBackground);
        cStyle.background = new TextureRegionDrawable(this.selectBackground);

        //Make the lists.
        this.aList = new List<>(aStyle);
        this.ipList = new List<>(ipStyle);
        this.sList = new List<>(sStyle);
        this.cList = new List<>(cStyle);

        this.craftingWindowTable.setFillParent(true);
        this.craftingWindowTable.left().top().pad(50, 20, 20, 20);

        //Add the lists to the crafting window
        this.craftingWindowTable.add(cList).prefSize(150, 200);
        this.craftingWindowTable.add().prefWidth(25);
        this.craftingWindowTable.add(aList).prefSize(150, 200);
        this.craftingWindowTable.add().prefWidth(25);
        this.craftingWindowTable.add(ipList).prefSize(150, 200);
        this.craftingWindowTable.add().prefWidth(25);
        this.craftingWindowTable.add(sList).prefSize(150, 200);

        this.craftingWindowTable.row();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.row();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();

        this.makeCraftButton();

        this.makeLabels();

        //TODO Really inefficient, but using for fast prototyping.
        this.function = EventSystem.onEntityEvent(this.target, "crafting_job_switched", (args) -> makeLabels());

        this.playerInterface.stage.setDebugAll(true);
    }

    private void makeLabels(){
        //Make new arrays
        this.availabelLabels = new Array<>();
        this.inProgressLabels = new Array<>();
        this.stalledLabels = new Array<>();
        this.craftingLabels = new Array<>();

        for(String item : this.craftingStation.getCraftingList()){
            String itemName = DataManager.getData(item, DataBuilder.JsonItem.class).getDisplayName();
            this.craftingLabels.add(new Label(itemName, new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }

        for(CraftingStation.CraftingJob job : this.craftingStation.getAvailableList()){
            this.availabelLabels.add(new Label(job.itemRef.getDisplayName(), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }

        for(CraftingStation.CraftingJob job : this.craftingStation.getInProgressJobs()){
            this.inProgressLabels.add(new Label(job.itemRef.getDisplayName(), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }

        for(CraftingStation.CraftingJob job : this.craftingStation.getStalledJobs()){
            this.stalledLabels.add(new Label(job.itemRef.getDisplayName()+" - "+percentFormat.format(job.percentageDone*100), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }

        //Add the stuff to the lists.
        this.aList.setItems(this.availabelLabels);
        this.ipList.setItems(this.inProgressLabels);
        this.sList.setItems(this.stalledLabels);
        this.cList.setItems(this.craftingLabels);
    }

    private void makeCraftButton(){
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("defaultButton_normal", Texture.class)));
        TextureRegionDrawable over = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("defaultButton_moused", Texture.class)));
        TextureRegionDrawable down = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("defaultButton_clicked", Texture.class)));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(up, down, up, this.playerInterface.UIStyle.font);
        buttonStyle.over = over;
        buttonStyle.checkedOver = over;

        TextButton button = new TextButton("Craft", buttonStyle);
        this.craftingWindowTable.add(button).right().bottom();
    }

    @Override
    public boolean update(SpriteBatch batch) {
        if(this.active) {

            LinkedList<CraftingStation.CraftingJob> jobList = this.craftingStation.getInProgressJobs();
            Array<String> content = new Array<>(jobList.size());
            for (CraftingStation.CraftingJob aJobList : jobList)
                content.add(aJobList.itemRef.getDisplayName() + " - " + percentFormat.format(aJobList.percentageDone * 100)+"%");

            this.ipList.setItems(content);

//            GUI.Texture(this.craftBackground, batch, this.craftRect);
//            GUI.Texture(this.selectBackground, batch, this.selectRect);
//            GUI.Texture(this.infoBackground, batch, this.infoRect);
//            GUI.Texture(this.openBackground, batch, this.openRect);
//            GUI.Texture(this.stalledBackground, batch, this.stalledRect);

            //GUI.Texture(new TextureRegion(this.playerInterface.blueSquare), ColonyGame.batch, this.dragWindowRect);
//            this.drawCraftingWindow(batch, this.playerInterface.UIStyle);
        }

        return super.update(batch);
    }

    /**
     * Draws the crafting info for an item inside a Rectangle.
     * @param itemRef The item reference.
     * @param batch The SpriteBatch to draw with.
     * @param rect The Rectangle to draw inside.
     * @param style The GUIStyle to draw with. This may be null.
     */
    private void drawCraftingInfo(DataBuilder.JsonItem itemRef, SpriteBatch batch, Rectangle rect, @Nullable GUI.GUIStyle style, TextureRegion windowTexture){
        GUI.Texture(windowTexture, batch, rect); //Draw the texture
        Array<ItemNeeded> mats = itemRef.materialsForCrafting, raw = itemRef.rawForCrafting; //Get the lists.

        float iconSize = 32;
        float labelWidth = 50;
        float startX = rect.x + labelWidth;
        float startY = rect.y + 20;
        float spacingX = iconSize + 5;
        float spacingY = iconSize + 10;
        TextureRegion _icon;
        DataBuilder.JsonItem _item;

        style.alignment = Align.left;
        style.paddingLeft = 5;
        GUI.Label("Raw:", batch, rect.x, startY, labelWidth, iconSize, style);
        style.alignment = Align.center;
        style.paddingLeft = 0;

        //Draw the raw mats first
        for(int i=0;i<raw.size;i++){
            _item = DataManager.getData(raw.get(i).itemName, DataBuilder.JsonItem.class);
            _icon = _item.iconTexture;
            float x = startX + spacingX*i, y = startY;
            GUI.Texture(_icon, batch, x, startY, iconSize, iconSize);
            GUI.Label("x"+raw.get(i).amountNeeded, batch, x, startY - 15, iconSize, 15, style);
        }

        style.alignment = Align.left;
        style.paddingLeft = 5;
        GUI.Label("Mats:", batch, rect.x, startY + spacingY, labelWidth, iconSize, style);
        style.alignment = Align.center;
        style.paddingLeft = 0;

        //Draw the materials
        for(int i=0;i<mats.size;i++){
            _item = DataManager.getData(mats.get(i).itemName, DataBuilder.JsonItem.class);
            _icon = _item.iconTexture;
            float x = startX + spacingX*i, y = startY + spacingY;
            GUI.Texture(_icon, batch, startX + spacingX*i, startY + spacingY, iconSize, iconSize);
            GUI.Label("x"+mats.get(i).amountNeeded, batch, x, y - 15, iconSize, 15, style);
        }

        //Draw the name of the item above the icon...
        GUI.Label(""+itemRef.getDisplayName(), batch, rect.x, rect.y + rect.height - 25,
                rect.width, 25);

        //Draw the icon for the item we are crafting...
        GUI.Texture(itemRef.iconTexture, batch, rect.x + rect.width / 2 - iconSize / 2,
                rect.y + rect.height - iconSize - 25, iconSize, iconSize);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void destroy() {
        this.craftingWindow.remove();
        EventSystem.unregisterEventFunction(this.target, "crafting_job_switched", this.function);
        super.destroy();
    }
}
