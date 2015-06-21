package com.mygdx.game.component.graphic;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.Effects;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.Grid;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class GraphicIdentity extends Component {
    @JsonIgnore
    private Sprite sprite;
    @JsonIgnore
    private Effects effects;
    @JsonProperty
    public int alignment = 0; //center
    @JsonProperty
    private int currVisibility=0;
    @JsonProperty("textureName")
    public String spriteTextureName = "";
    @JsonProperty("atlasName")
    public String atlasName = "";

    public GraphicIdentity(){
        this.setActive(true);
    }

	@Override
	public void start() {
        //This is initially needed for getting the sprite to be the right size. If we simply scaled it using this method, then
        //the image would draw the right size, but the offset from the width and height being unaffected causes real problems whenever the image
        //is not centered.
        if(getSprite() == null) return;
        this.configureSprite(this.getSprite());

        this.load();
    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad() {
        super.initLoad();
        setSprite(this.spriteTextureName, this.atlasName);
    }

    @Override
    public void load() {
        super.load();
        this.effects = this.getComponent(Effects.class);
    }

    protected void configureSprite(Sprite sprite){
        sprite.setSize(GH.toMeters(sprite.getRegionWidth()), GH.toMeters(sprite.getRegionHeight()));
        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);

        if(getSprite() != null) {
            Grid.GridInstance grid = ColonyGame.worldGrid;
            Vector2 pos = this.owner.getTransform().getPosition(); //Cache the owner's position.

            if (!isWithinBounds()) return;

            Grid.Node node = grid.getNode(this.owner);
            int visibility = grid.getVisibilityMap()[node.getX()][node.getY()].getVisibility();
            if (visibility == Constants.VISIBILITY_UNEXPLORED)
                return;

            this.changeVisibility(visibility);

            this.getSprite().setRotation(this.owner.getTransform().getRotation());
            this.getSprite().setScale(this.owner.getTransform().getScale());

            if (alignment == 0)
                this.getSprite().setPosition(pos.x - (getSprite().getWidth() / 2), pos.y - (getSprite().getHeight() / 2));
            if (alignment == 1)
                this.getSprite().setPosition(pos.x - (getSprite().getWidth() / 2), pos.y);

            this.getSprite().draw(batch);

            if(effects != null){
                float size = GH.toMeters(24);
                int num = effects.getActiveEffects().size;
                float startX = pos.x - num*(size/2);
                float startY = pos.y + sprite.getHeight()/2;
                for(Effects.Effect effect : effects.getActiveEffects()){
                    batch.draw(effect.getIcon(), startX, startY, size, size);
                    startX+=size;
                }
            }
        }
    }

    public boolean isWithinBounds(){
        Vector2 pos = this.owner.getTransform().getPosition(); //Cache the owner's position.
        return ColonyGame.camera.frustum.boundsInFrustum(pos.x, pos.y, 0, getSprite().getWidth(), getSprite().getHeight(), 0);
    }

    /**
     * Sets the sprite via texture name and atlas name. If the atlas name does not apply (empty or null), the asset manager will be searched for the image.
     * @param textureName The name of the texture.
     * @param atlasName The name of the atlas (if applicable) where the texture is.
     */
    public void setSprite(String textureName, String atlasName){
        if(textureName == null) return;
        this.spriteTextureName = textureName;

        //If the atlas name is applicable, get the texture atlas from the asset manager and then get the texture by name.
        if(atlasName != null && !atlasName.isEmpty()) {
            this.atlasName = atlasName;
            TextureRegion region = ColonyGame.assetManager.get(atlasName, TextureAtlas.class).findRegion(textureName);
            if(region == null)
                GH.writeErrorMessage("TextureRegion is null when creating sprite in GraphicIdentity. Texture: "+textureName+", atlasName: "+atlasName+". Does it exist?");

            if(this.getSprite() == null) this.setSprite(new Sprite(region));
            else this.getSprite().setTexture(region.getTexture());

        //If no atlas is used, get it normally.
        }else {
            Texture texture = ColonyGame.assetManager.get(textureName, Texture.class);
            if(texture == null) return;
            if (this.getSprite() == null) this.setSprite(new Sprite(texture));
            else this.getSprite().setTexture(texture);
        }

        this.configureSprite(this.getSprite());
    }

    @JsonIgnore
    public String getSpriteTextureName(String name){
        return this.spriteTextureName;
    }

    private void changeVisibility(int visibility){
        if(this.currVisibility == visibility)
            return;

        this.currVisibility = visibility;
        if(this.currVisibility == Constants.VISIBILITY_EXPLORED)
            this.getSprite().setColor(Constants.COLOR_EXPLORED);
        else if(this.currVisibility == Constants.VISIBILITY_VISIBLE)
            this.getSprite().setColor(Constants.COLOR_VISIBILE);
    }

    @JsonIgnore
    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public Sprite getSprite() {
        return this.sprite;
    }
}
