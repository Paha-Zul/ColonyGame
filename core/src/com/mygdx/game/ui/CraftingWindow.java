package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;
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

    private TextButton craftButton;
    private Container<VerticalGroup> aContainer, ipContainer, sContainer, cContainer;
    private VerticalGroup aList, ipList, sList, cList;
    private Table craftingWindowTable;

    private Consumer<Object[]> function;

    private Vector2 offset;

    private DecimalFormat percentFormat = new DecimalFormat("#.0");

    private Label selectedLabel;

    private boolean justSelected = false;


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
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if(!justSelected && selectedItem != null) {
                    selectedLabel.getStyle().background = null;
                    selectedItem = null;
                    selectedLabel = null;
                }else justSelected = false;
            }

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

        //Make the lists.
        this.aList = new VerticalGroup();
        this.ipList = new VerticalGroup();
        this.sList = new VerticalGroup();
        this.cList = new VerticalGroup();

        //Make sure they are left aligned.
        this.aList.left();
        this.ipList.left();
        this.sList.left();
        this.cList.left();

        //Make the containers...
        this.aContainer = new Container<>();
        this.ipContainer = new Container<>();
        this.sContainer = new Container<>();
        this.cContainer = new Container<>();

        this.aContainer.background(new TextureRegionDrawable(this.selectBackground));
        this.ipContainer.background(new TextureRegionDrawable(this.selectBackground));
        this.sContainer.background(new TextureRegionDrawable(this.selectBackground));
        this.cContainer.background(new TextureRegionDrawable(this.selectBackground));

        //Set the container actors as the vertical groups.
        this.aContainer.setActor(aList);
        this.ipContainer.setActor(ipList);
        this.sContainer.setActor(sList);
        this.cContainer.setActor(cList);

        //this.cContainer.getActor().setFillParent(true);
        this.cContainer.top().left().padLeft(5);
        this.aContainer.top().left().padLeft(5);
        this.ipContainer.top().left().padLeft(5);
        this.sContainer.top().left().padLeft(5);

        //Make the styles
        List.ListStyle aStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        List.ListStyle ipStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        List.ListStyle sStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        List.ListStyle cStyle = new List.ListStyle(this.playerInterface.UIStyle.font, Color.BLUE, Color.BLACK, new TextureRegionDrawable(this.selectBackground));
        aStyle.background = new TextureRegionDrawable(this.selectBackground);
        ipStyle.background = new TextureRegionDrawable(this.selectBackground);
        sStyle.background = new TextureRegionDrawable(this.selectBackground);
        cStyle.background = new TextureRegionDrawable(this.selectBackground);

        this.craftingWindowTable.setFillParent(true);
        this.craftingWindowTable.left().top().pad(50, 20, 20, 20);

        //Add the lists to the crafting window
        this.craftingWindowTable.add(cContainer).prefSize(150, 200).maxSize(150, 200);
        this.craftingWindowTable.add().prefWidth(25);
        this.craftingWindowTable.add(aContainer).prefSize(150, 200);
        this.craftingWindowTable.add().prefWidth(25);
        this.craftingWindowTable.add(ipContainer).prefSize(150, 200);
        this.craftingWindowTable.add().prefWidth(25);
        this.craftingWindowTable.add(sContainer).prefSize(150, 200);

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
        this.cList.clear();
        this.aList.clear();
        this.ipList.clear();
        this.sList.clear();

        for(String item : this.craftingStation.getCraftingList()){
            DataBuilder.JsonItem itemRef = DataManager.getData(item, DataBuilder.JsonItem.class);
            String itemName = itemRef.getDisplayName();
            Label label = new Label(itemName, new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK));
            label.addListener(new ClickListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if(selectedLabel != null) selectedLabel.getStyle().background = null;

                    selectedItem = itemRef;
                    selectedLabel = label;
                    justSelected = true;
                    label.getStyle().background = new TextureRegionDrawable(selectBackground);
                    return super.touchDown(event, x, y, pointer, button);
                }
            });

            this.cList.addActor(label);
        }

        for(CraftingStation.CraftingJob job : this.craftingStation.getAvailableList()){
            this.aList.addActor(new Label(job.itemRef.getDisplayName(), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }

        for(CraftingStation.CraftingJob job : this.craftingStation.getInProgressJobs()){
            this.ipList.addActor(new Label(job.itemRef.getDisplayName(), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }

        for(CraftingStation.CraftingJob job : this.craftingStation.getStalledJobs()){
            this.sList.addActor(new Label(job.itemRef.getDisplayName() + " - " + percentFormat.format(job.percentageDone * 100), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }
//
//        //Add the stuff to the lists.
//        this.aList.setItems(this.availabelLabels);
//        this.ipList.setItems(this.inProgressLabels);
//        this.sList.setItems(this.stalledLabels);
//        this.cList.setItems(this.craftingLabels);
    }

    private void makeCraftButton(){
        TextureRegionDrawable up = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("defaultButton_normal", Texture.class)));
        TextureRegionDrawable over = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("defaultButton_moused", Texture.class)));
        TextureRegionDrawable down = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("defaultButton_clicked", Texture.class)));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(up, down, up, this.playerInterface.UIStyle.font);
        buttonStyle.over = over;
        buttonStyle.checkedOver = over;

        this.craftButton = new TextButton("Craft", buttonStyle);
        this.craftingWindowTable.add(this.craftButton).right().bottom();

        this.craftButton.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                craftingStation.addCraftingJob(selectedItem.getItemName(), 1);
                makeLabels();
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public boolean update(SpriteBatch batch) {
        if(this.active) {

            //Get the in-progress list and the job list from the crafting station.
            SnapshotArray<Actor> content = this.ipList.getChildren();
            LinkedList<CraftingStation.CraftingJob> jobList = this.craftingStation.getInProgressJobs();

            int i=0;
            //For each in progress job, try to set it's corresponding UI content to reflect its percentage done.
            for (CraftingStation.CraftingJob aJobList : jobList) {
                if(content.size <= i) break; //If the content size is less than or equal to i, break. This can happen when jobs are being switch and the UI is not caught up.
                ((Label)content.get(i)).setText(aJobList.itemRef.getDisplayName() + " - " + percentFormat.format(aJobList.percentageDone * 100) + "%");
                i++;
            }
//
//            this.ipList.setItems(content);

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
