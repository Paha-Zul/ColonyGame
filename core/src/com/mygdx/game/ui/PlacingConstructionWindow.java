package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    private com.badlogic.gdx.scenes.scene2d.ui.Window mainWindow;
    private Image buildingImage;
    private DataBuilder.JsonBuilding building;

    public PlacingConstructionWindow(PlayerInterface playerInterface, Entity target) {
        super(playerInterface, target);

        PlaceConstructionManager.instance().setView(this);
        this.mainWindow = new com.badlogic.gdx.scenes.scene2d.ui.Window("", new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(this.playerInterface.UIStyle.font, Color.BLACK, null));
    }

    public void setupWindow(DataBuilder.JsonBuilding building){
        if(this.buildingImage != null) this.buildingImage.remove();
        this.mainWindow.clear();
        this.building = building;
        TextureRegion region = DataManager.getTextureFromAtlas(building.image, building.spriteSheet);
        this.buildingImage = new Image(new TextureRegionDrawable(region));
        this.buildingImage.setSize(building.dimensions[0], building.dimensions[1]);
        this.playerInterface.stage.addActor(this.buildingImage);

        this.buildingImage.addCaptureListener(new ClickListener() {

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
                Entity b = new BuildingEntity(new Vector2(worldCoords.x, worldCoords.y), 0, building, 12);
                PlayerManager.getPlayer("Player").colony.addOwnedToColony(b.getComponent(Building.class));
                ListHolder.addEntity(b);
                System.out.println("Such");
                super.touchUp(event, x, y, pointer, button);
            }
        });
    }

    @Override
    public boolean update(SpriteBatch batch) {
        if(this.buildingImage != null){
            //batch.setProjectionMatrix(ColonyGame.camera.projection);
            float scale = ColonyGame.camera.zoom;
            this.buildingImage.setSize(building.dimensions[0]*(1f/scale), building.dimensions[1]*(1f/scale));
            this.buildingImage.setPosition(GH.getFixedScreenMouseCoords().x - this.buildingImage.getWidth()/2, GH.getFixedScreenMouseCoords().y - this.buildingImage.getHeight()/2);
            //this.buildingImage.setScale(1f/scale);
            //batch.setProjectionMatrix(ColonyGame.UICamera.projection);
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
    }
}
