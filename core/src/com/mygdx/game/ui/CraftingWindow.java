package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.SnapshotArray;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.CraftingStation;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.EventSystem;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.managers.DataManager;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Created by Paha on 8/16/2015.
 * A Window for interacting with Entities that provide crafting options.
 */
public class CraftingWindow extends Window{
    private TextureRegion craftBackground, selectBackground, infoBackground, stalledBackground, openBackground;
    private TextureRegionDrawable selectionTexture, exitTexture;
    private com.badlogic.gdx.scenes.scene2d.ui.Window craftingWindow;

    private CraftingStation craftingStation;
    private DataBuilder.JsonItem selectedItem;

    private TextButton craftButton;
    private Container<VerticalGroup> aContainer, ipContainer, sContainer, cContainer;
    private VerticalGroup aList, ipList, sList, cList;
    private Table craftingWindowTable, previewWindow;

    private Consumer<Object[]> function;

    private Vector2 offset;

    private DecimalFormat percentFormat = new DecimalFormat("#.0");

    private Label selectedLabel;

    private boolean justSelected = false, craftButtonPressed = false;


    public CraftingWindow(PlayerInterface playerInterface, Entity target) {
        super(playerInterface, target);

        this.craftBackground = new TextureRegion(ColonyGame.assetManager.get("craftingWindowBackground", Texture.class));
        this.selectBackground = new TextureRegion(ColonyGame.assetManager.get("craftingWindowSelectionBackground", Texture.class));
        this.infoBackground = new TextureRegion(ColonyGame.assetManager.get("craftingWindowInfoBackground", Texture.class));
        this.stalledBackground = new TextureRegion(ColonyGame.assetManager.get("stalledJobsBackground", Texture.class));
        this.openBackground = new TextureRegion(ColonyGame.assetManager.get("openJobsBackground", Texture.class));

        this.selectionTexture = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("selection", Texture.class)));
        this.exitTexture = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("exit", Texture.class)));

        this.craftingStation = this.target.getComponent(CraftingStation.class);
        this.offset = new Vector2();

        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle style = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(this.playerInterface.UIStyle.font, Color.BLACK, new TextureRegionDrawable(this.craftBackground));
        this.craftingWindow = new com.badlogic.gdx.scenes.scene2d.ui.Window("WindowTop", style);
        this.craftingWindow.addListener(new ClickListener() {

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                super.touchUp(event, x, y, pointer, button);
                if(!justSelected && !craftButtonPressed && selectedItem != null) {
                    selectedLabel.getStyle().background = null;
                    selectedItem = null;
                    selectedLabel = null;
                }else {
                    justSelected = false;
                    craftButtonPressed = false;
                }
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
        this.craftingWindow.add(this.craftingWindowTable).expand().fill().pad(0, 0, 0, 0);

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
        this.cContainer = new Container<>();
        this.aContainer = new Container<>();
        this.ipContainer = new Container<>();
        this.sContainer = new Container<>();

        this.cContainer.background(new TextureRegionDrawable(this.selectBackground));
        this.aContainer.background(new TextureRegionDrawable(this.selectBackground));
        this.ipContainer.background(new TextureRegionDrawable(this.selectBackground));
        this.sContainer.background(new TextureRegionDrawable(this.selectBackground));

        //Set the container actors as the vertical groups.
        this.cContainer.setActor(cList);
        this.aContainer.setActor(aList);
        this.ipContainer.setActor(ipList);
        this.sContainer.setActor(sList);

        //Fill to the parent container. This makes the VerticalGroups fill to the width of the Container.
        this.cContainer.getActor().fill();
        this.aContainer.getActor().fill();
        this.ipContainer.getActor().fill();
        this.sContainer.getActor().fill();

        //Put the containers in the top left, pad the insides some, and fill to the width of the parent (table cell).
        this.cContainer.top().left().pad(0, 3, 0, 3).fillX().setClip(true);
        this.aContainer.top().left().pad(0, 3, 0, 3).fillX().setClip(true);
        this.ipContainer.top().left().pad(0, 3, 0, 3).fillX().setClip(true);
        this.sContainer.top().left().pad(0, 3, 0, 3).fillX().setClip(true);

        this.craftingWindowTable.left().top().pad(20, 20, 20, 20);


        Label.LabelStyle titleStyle = new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK);
        Label craftTitle = new Label("Craftable", titleStyle);
        Label readyLabel = new Label("Queue", titleStyle);
        Label inProgressLabel = new Label("In Progress", titleStyle);
        Label stalledLabel = new Label("Stalled", titleStyle);

        craftTitle.setAlignment(Align.center);
        readyLabel.setAlignment(Align.center);
        inProgressLabel.setAlignment(Align.center);
        stalledLabel.setAlignment(Align.center);

        this.previewWindow = new Table();
        this.previewWindow.background(new TextureRegionDrawable(this.selectBackground));

        //Add the lists to the crafting window
        this.craftingWindowTable.add(craftTitle).prefSize(150, 25).maxSize(150, 25).expandX().fillX().center();
        this.craftingWindowTable.add().expandX().fillX();
        this.craftingWindowTable.add(readyLabel).prefSize(150, 25).expandX().fillX().center();
        this.craftingWindowTable.add().expandX().fillX();
        this.craftingWindowTable.add(inProgressLabel).prefSize(150, 25).expandX().fillX().center();
        this.craftingWindowTable.add().expandX().fillX();
        this.craftingWindowTable.add(stalledLabel).prefSize(150, 25).expandX().fillX().center();

        this.craftingWindowTable.row();
        this.craftingWindowTable.add(cContainer).prefSize(150, 200).maxSize(150, 200).expandX().fillX();
        this.craftingWindowTable.add().expandX().fillX();
        this.craftingWindowTable.add(aContainer).prefSize(150, 200).maxSize(150, 200).expandX().fillX();
        this.craftingWindowTable.add().expandX().fillX();
        this.craftingWindowTable.add(ipContainer).prefSize(150, 200).maxSize(150, 200).expandX().fillX();
        this.craftingWindowTable.add().expandX().fillX();
        this.craftingWindowTable.add(sContainer).prefSize(150, 200).maxSize(150, 200).expandX().fillX();

        this.craftingWindowTable.row();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.row();
        this.craftingWindowTable.add(this.previewWindow).prefSize(150,150).expand().fill().top();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();
        this.craftingWindowTable.add().expand().fill();

        this.makeCraftButton();

        this.buildAvailableList();
        this.buildInProgressList();
        this.buildStalledList();
        this.buildCraftingList();

        //TODO Really inefficient, but using for fast prototyping.
        this.function = EventSystem.onEntityEvent(this.target, "crafting_job_switched", (args) -> {
            String from = (String)args[0];
            String to = (String)args[1];

            if(from.equals("available") && to.equals("inProgress")){
                this.buildAvailableList();
                this.buildInProgressList();
            }else if(from.equals("inProgress")){
                this.buildInProgressList();
                if(to.equals("stalled"))
                    this.buildStalledList();
            }
        });

        this.playerInterface.stage.setDebugAll(true);
    }

    /**
     * Builds/Rebuilds the available jobs list.
     */
    private void buildAvailableList(){
        this.aList.clear();
        for(CraftingStation.CraftingJob job : this.craftingStation.getAvailableList()){
            this.aList.addActor(new Label(job.itemRef.getDisplayName(), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }
    }

    /**
     * Builds/rebuilds the in progress list.
     */
    private void buildInProgressList(){
        this.ipList.clear();
        for(CraftingStation.CraftingJob job : this.craftingStation.getInProgressJobs()){
            this.ipList.addActor(new Label(job.itemRef.getDisplayName(), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }
    }

    /**
     * Builds/rebuilds the stalled list.
     */
    private void buildStalledList(){
        this.sList.clear();
        for(CraftingStation.CraftingJob job : this.craftingStation.getStalledJobs()){
            this.sList.addActor(new Label(job.itemRef.getDisplayName()+" - "+percentFormat.format(job.percentageDone*100), new Label.LabelStyle(this.playerInterface.UIStyle.font, Color.BLACK)));
        }
    }

    /**
     * Builds/rebuilds the crafting list.
     */
    private void buildCraftingList(){
        this.cList.clear();

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
                    label.getStyle().background = selectionTexture;
                    playerInterface.makePreviewTable(DataManager.getData(itemRef.getItemName(), DataBuilder.JsonRecipe.class), previewWindow);
                    //makePreviewTable();
                    return super.touchDown(event, x, y, pointer, button);
                }
            });

            this.cList.addActor(label);
        }
    }

    private void makeCraftButton(){
        TextureRegionDrawable up = new TextureRegionDrawable(DataManager.getTextureFromAtlas("defaultButton_normal", "buttons"));
        TextureRegionDrawable over = new TextureRegionDrawable(DataManager.getTextureFromAtlas("defaultButton_moused", "buttons"));
        TextureRegionDrawable down = new TextureRegionDrawable(DataManager.getTextureFromAtlas("defaultButton_clicked", "buttons"));

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(up, down, up, this.playerInterface.UIStyle.font);
        buttonStyle.over = over;
        buttonStyle.checkedOver = over;

        this.craftButton = new TextButton("Craft", buttonStyle);
        this.craftingWindowTable.add(this.craftButton).right().bottom();

        this.craftButton.addListener(new ClickListener(){
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if(selectedItem != null) {
                    craftingStation.addCraftingJob(selectedItem.getItemName(), 1);
                    buildAvailableList();
                    craftButtonPressed = true;
                }
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
        }

        return super.update(batch);
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
