package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Building;
import com.mygdx.game.entity.BuildingEntity;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.ListHolder;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.PlaceConstructionManager;
import com.mygdx.game.util.managers.PlayerManager;

/**
 * Created by Paha on 8/24/2015.
 * The window for constructing stuff.
 */
public class PlacingConstructionWindow extends Window{
    private Image buildingImage;

    private Vector2 buildingImagePosition, worldPosition;
    private PlaceConstructionManager placeManager;

    private TextureRegionDrawable listBackground;

    private com.badlogic.gdx.scenes.scene2d.ui.Window buildingWindow;
    private com.badlogic.gdx.scenes.scene2d.ui.Window previewWindow;

    public PlacingConstructionWindow(PlayerInterface playerInterface, Entity target) {
        super(playerInterface, target);

        PlaceConstructionManager.instance().setView(this);
        this.buildingImagePosition = new Vector2();
        this.worldPosition = new Vector2();
        this.placeManager = PlaceConstructionManager.instance();

        this.listBackground = new TextureRegionDrawable(new TextureRegion(ColonyGame.assetManager.get("craftingWindowBackground", Texture.class)));

        this.buildingWindow = new com.badlogic.gdx.scenes.scene2d.ui.Window("", new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(this.playerInterface.UIStyle.font, Color.BLACK, listBackground));
        this.buildingWindow.setBounds(0,0,100,500);


        this.setupBuildingList();

        this.playerInterface.stage.addActor(this.buildingWindow);
    }

    public void setupBuildingList(){
        this.buildingWindow.clear();
        Table buildingTable = new Table();
        this.buildingWindow.add(buildingTable).expand().fill();

        //Make buttons for all the JsonBuildings in the DataManager (only ones that have buildable == true).
        for(Object object : DataManager.getValueListForType(DataBuilder.JsonBuilding.class)){
            DataBuilder.JsonBuilding building = (DataBuilder.JsonBuilding)object;
            //If the building is buildable, get its image and add it to the building table.
            if(building.buildable){
                TextureRegionDrawable region = new TextureRegionDrawable(DataManager.getTextureFromAtlas(building.image, building.spriteSheet));
                ImageButton button = new ImageButton(region);
                buildingTable.add(button).maxSize(64).pad(5, 0, 5, 0);
                buildingTable.row();

                //On up, set the building being constructed as the building matching this button (building).
                button.addListener(new InputListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        clearPreviewWindow();

                        previewWindow = new com.badlogic.gdx.scenes.scene2d.ui.Window("", new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(playerInterface.UIStyle.font, Color.BLACK, listBackground));
                        playerInterface.stage.addActor(previewWindow);
                        playerInterface.makePreviewTable(DataManager.getData(building.name, DataBuilder.JsonRecipe.class), previewWindow);
                        previewWindow.setPosition(x, y);
                        System.out.println("entered");
                        super.enter(event, x, y, pointer, fromActor);
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if(previewWindow != null) {
                            previewWindow.clear();
                            previewWindow.remove();
                            previewWindow = null;
                        }
                        System.out.println("exited");
                        super.exit(event, x, y, pointer, toActor);
                    }

                    @Override
                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                        //We need to clear this here since our mouse will be over the new image under the mouse. This makes the preview window
                        //to get stuck on the screen, so remove it here.
                        clearPreviewWindow();
                        placeManager.setPlacingConstruction(building);
                        System.out.println("up");
                        super.touchUp(event, x, y, pointer, button);
                    }

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                        return true; //We must return true for the in touchDown for touchUp to be handled by this object.
                    }
                });
            }
        }

        buildingTable.row();
        buildingTable.add().expand().fill();
    }

    public void setBuildingBeingPlaced(DataBuilder.JsonBuilding building){
        if(this.buildingImage != null) this.buildingImage.remove();

        //Gets the TextureRegion for the building and makes a new Image from it (to add to our stage).
        TextureRegion region = DataManager.getTextureFromAtlas(building.image, building.spriteSheet);
        this.buildingImage = new Image(new TextureRegionDrawable(region));
        this.buildingImage.setSize(building.dimensions[0], building.dimensions[1]);
        this.buildingImage.addAction(Actions.alpha(0.5f));
        this.playerInterface.stage.addActor(this.buildingImage);

        //On touch up, let's try to place the building. Make sure it's a valid spot. Then create the entity, add it to the game,
        //and add it to the colony.
        //We are kinda tricky here and put an input listener on the image that is following the mouse. This is the easiest way,
        //otherwise, we'll need to use the playerInterface to clear the window and manager of placing buildings...
        this.buildingImage.addCaptureListener(new ClickListener() {
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                worldPosition.set(worldCoords.x, worldCoords.y);
                if(PlaceConstructionManager.instance().canPlace(ColonyGame.world, worldPosition)) {
                    Entity b = new BuildingEntity(worldPosition, 0, building, 12);
                    PlayerManager.getPlayer("Player").colony.addOwnedToColony(b.getComponent(Building.class));
                    ListHolder.addEntity(b);
                }
                //Null this out to cancel building after placing.
                placeManager.setPlacingConstruction(null);
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    /**
     * Clears the preview window.
     */
    public void clearPreviewWindow(){
        if(this.previewWindow != null){
            this.previewWindow.clear();
            this.previewWindow.remove();
            this.previewWindow = null;
        }
    }

    /**
     * Clears the building being placed.
     */
    public void clearBuildingBeingPlaced(){
        if(this.buildingImage != null) {
            this.buildingImage.remove();
            this.buildingImage = null;
        }
    }

    @Override
    public boolean update(SpriteBatch batch) {
        Vector2 mouseCoords = GH.getFixedScreenMouseCoords();
        if(this.buildingImage != null){
            //batch.setProjectionMatrix(ColonyGame.camera.projection);
            Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
            worldPosition.set(worldCoords.x, worldCoords.y);
            float scale = ColonyGame.camera.zoom;

            //Set the size and position of the image.
            this.buildingImage.setSize(this.placeManager.getBuildingBeingPlaced().dimensions[0]*(1f/scale), this.placeManager.getBuildingBeingPlaced().dimensions[1]*(1f/scale));
            this.buildingImagePosition.set(GH.getFixedScreenMouseCoords().x - this.buildingImage.getWidth()/2, GH.getFixedScreenMouseCoords().y - this.buildingImage.getHeight()/2);
            this.buildingImage.setPosition(buildingImagePosition.x, buildingImagePosition.y);

            //Change color to red if can't place, white if can.
            if(!PlaceConstructionManager.instance().canPlace(ColonyGame.world, worldPosition))
                this.buildingImage.setColor(Color.RED);
            else
                this.buildingImage.setColor(Color.WHITE);

            //this.buildingImage.setScale(1f/scale);
            //batch.setProjectionMatrix(ColonyGame.UICamera.projection);
        }

        if(this.previewWindow != null){
            this.previewWindow.setPosition(mouseCoords.x, mouseCoords.y);
        }

        return super.update(batch);
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void destroy() {
        super.destroy();
        PlaceConstructionManager.instance().setView(null);
        if(this.buildingImage != null) this.buildingImage.remove();
        this.clearBuildingBeingPlaced();
        this.clearPreviewWindow();
        this.buildingWindow.clear();
        this.buildingWindow.remove();
        this.buildingWindow = null;

    }
}
