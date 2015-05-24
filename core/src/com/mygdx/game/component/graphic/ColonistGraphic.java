package com.mygdx.game.component.graphic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;

/**
 * Created by Paha on 5/24/2015.
 */
public class ColonistGraphic extends GraphicIdentity{
    private Sprite selectionSprite;
    private Sprite alertSprite;


    @Override
    public void start() {
        super.start();

        this.alertSprite = new Sprite(ColonyGame.assetManager.get("alertIcon", Texture.class));
        this.selectionSprite = new Sprite(ColonyGame.assetManager.get("selectionCircle", Texture.class));

        this.configureSprite(this.alertSprite);
        this.configureSprite(this.selectionSprite);
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void load() {
        super.load();
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        Vector2 pos = this.owner.getTransform().getPosition(); //Cache the owner's position.
        this.drawSelectionSprite(batch, pos);
        super.render(delta, batch);
        this.drawAlertSprite(batch, pos);
    }

    private void drawSelectionSprite(SpriteBatch batch, Vector2 ownerPos){
        if (!ColonyGame.camera.frustum.boundsInFrustum(ownerPos.x, ownerPos.y, 0, this.selectionSprite.getWidth(), this.selectionSprite.getHeight(), 0))
            return;

        this.selectionSprite.draw(batch);
    }

    private void drawAlertSprite(SpriteBatch batch, Vector2 ownerPos){
        if (!ColonyGame.camera.frustum.boundsInFrustum(ownerPos.x, ownerPos.y, 0, this.selectionSprite.getWidth(), this.selectionSprite.getHeight(), 0))
            return;

        this.selectionSprite.draw(batch);
    }
}
