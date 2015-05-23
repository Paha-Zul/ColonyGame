package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.Grid;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class GraphicIdentity extends Component{
    @JsonIgnore
	public Sprite sprite;
    @JsonProperty
    public int alignment = 0; //center
    @JsonProperty
    private int currVisibility=0;
    @JsonProperty("textureName")
    public String spriteTextureName = "";
    @JsonProperty("atlasName")
    public String atlasName = "";

    public GraphicIdentity(){

    }

	@Override
	public void start() {
        //This is initially needed for getting the sprite to be the right size. If we simply scaled it using this method, then
        //the image would draw the right size, but the offset from the width and height being unaffected causes real problems whenever the image
        //is not centered.
        if(sprite == null) return;
        this.sprite.setSize(GH.toMeters(sprite.getRegionWidth()), GH.toMeters(sprite.getRegionHeight()));
        this.sprite.setOrigin(this.sprite.getWidth() / 2, this.sprite.getHeight() / 2);
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        setSprite(this.spriteTextureName, this.atlasName);
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);
        if(sprite == null) return;

        Grid.GridInstance grid = ColonyGame.worldGrid;

        Vector2 pos = this.owner.getTransform().getPosition(); //Cache the owner's position.

        if(!ColonyGame.camera.frustum.boundsInFrustum(pos.x, pos.y, 0, sprite.getWidth(), sprite.getHeight(), 0))
            return;

        Grid.Node node = grid.getNode(this.owner);
        int visibility = grid.getVisibilityMap()[node.getX()][node.getY()].getVisibility();
        if(visibility == Constants.VISIBILITY_UNEXPLORED)
            return;

        this.changeVisibility(visibility);

        this.sprite.setRotation(this.owner.getTransform().getRotation());
        this.sprite.setScale(this.owner.getTransform().getScale());

        if(alignment == 0)
            this.sprite.setPosition(pos.x - (sprite.getWidth()/2), pos.y - (sprite.getHeight()/2));
        if(alignment == 1)
            this.sprite.setPosition(pos.x - (sprite.getWidth() / 2), pos.y);

        this.sprite.draw(batch);
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
            if(this.sprite == null) this.sprite = new Sprite(region);
            else this.sprite.setTexture(region.getTexture());

        //If no atlas is used, get it normally.
        }else {
            Texture texture = ColonyGame.assetManager.get(textureName, Texture.class);
            if(texture == null) return;
            if (this.sprite == null) this.sprite = new Sprite(texture);
            else this.sprite.setTexture(texture);
        }
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
            this.sprite.setColor(Constants.COLOR_EXPLORED);
        else if(this.currVisibility == Constants.VISIBILITY_VISIBLE)
            this.sprite.setColor(Constants.COLOR_VISIBILE);
    }
}
