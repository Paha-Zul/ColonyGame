package com.mygdx.game.component.graphic;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Colonist;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created by Paha on 5/24/2015.
 */
public class ColonistGraphic extends GraphicIdentity{
    @JsonIgnore
    private Sprite selectionSprite;
    @JsonIgnore
    private Sprite alertSprite;
    @JsonIgnore
    private Colonist colonist;

    @Override
    public void start() {
        super.start();
        load();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void load() {
        super.load();

        this.alertSprite = new Sprite(ColonyGame.assetManager.get("alertIcon", Texture.class));
        this.selectionSprite = new Sprite(ColonyGame.assetManager.get("selectedCircle", Texture.class));
        this.selectionSprite.setColor(Color.BLUE);

        this.configureSprite(this.alertSprite);
        this.configureSprite(this.selectionSprite);
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        Vector2 pos = this.owner.getTransform().getPosition(); //Cache the owner's position.
        this.drawSelectionSprite(batch, pos);
        super.render(delta, batch);
        this.drawAlertSprite(batch, pos);
    }

    private void drawSelectionSprite(SpriteBatch batch, Vector2 ownerPos){
        if(this.owner.getTags().hasTag("selected")) {
            if (!ColonyGame.camera.frustum.boundsInFrustum(ownerPos.x, ownerPos.y, 0, this.selectionSprite.getWidth(), this.selectionSprite.getHeight(), 0))
                return;

            this.selectionSprite.setPosition(ownerPos.x - (selectionSprite.getWidth() / 2), ownerPos.y - (selectionSprite.getHeight() / 2));
            this.selectionSprite.setRotation(this.owner.getTransform().getRotation());
            this.selectionSprite.setScale(this.owner.getTransform().getScale());
            this.selectionSprite.draw(batch);
        }
    }

    private void drawAlertSprite(SpriteBatch batch, Vector2 ownerPos){
        if(this.owner.getTags().hasTag("alert")) {
            if (!ColonyGame.camera.frustum.boundsInFrustum(ownerPos.x, ownerPos.y, 0, this.alertSprite.getWidth(), this.alertSprite.getHeight(), 0))
                return;

            this.alertSprite.setPosition(ownerPos.x - (alertSprite.getWidth() / 2), ownerPos.y + getSprite().getHeight() / 2);
            this.alertSprite.setRotation(this.owner.getTransform().getRotation());
            this.alertSprite.setScale(this.owner.getTransform().getScale() - 0.3f);
            this.alertSprite.draw(batch);
        }
    }
}
