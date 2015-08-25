package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.PlaceConstructionManager;

/**
 * Created by Paha on 8/24/2015.
 * The window for constructing stuff.
 */
public class PlacingConstructionWindow extends Window{
    private com.badlogic.gdx.scenes.scene2d.ui.Window mainWindow;
    private Image buildingImage;

    public PlacingConstructionWindow(PlayerInterface playerInterface, Entity target) {
        super(playerInterface, target);

        PlaceConstructionManager.instance().setView(this);
        this.mainWindow = new com.badlogic.gdx.scenes.scene2d.ui.Window("", new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle(this.playerInterface.UIStyle.font, Color.BLACK, null));
    }

    public void setupWindow(DataBuilder.JsonBuilding building){
        this.mainWindow.clear();
        TextureRegion region = DataManager.getTextureFromAtlas(building.image, building.spriteSheet);
        this.buildingImage = new Image(new TextureRegionDrawable(region));
        this.buildingImage.setSize(building.dimensions[0], building.dimensions[1]);
    }

    @Override
    public boolean update(SpriteBatch batch) {
        if(this.buildingImage != null){
            this.buildingImage.setPosition(GH.getFixedScreenMouseCoords().x, GH.getFixedScreenMouseCoords().y);
            this.buildingImage.draw(batch, 1);
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
    }
}
