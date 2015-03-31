package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.worldgeneration.WorldGen;

public class GraphicIdentity extends Component{
	public Sprite sprite;
    public int alignment = 0; //center

	private SpriteBatch batch;
    private int currVisibility=0;

	public GraphicIdentity(TextureRegion image, SpriteBatch batch){
		super();

		this.sprite = new Sprite(image);
		this.batch = batch;
	}

    public GraphicIdentity(Sprite sprite, SpriteBatch batch){
        super();

        this.sprite = sprite;
        this.batch = batch;
    }

	@Override
	public void start() {
        //This is initially needed for getting the sprite to be the right size. If we simply scaled it using this method, then
        //the image would draw the right size, but the offset from the width and height being unaffected causes real problems whenever the image
        //is not centered.
        this.sprite.setSize(GH.toMeters(sprite.getRegionWidth()), GH.toMeters(sprite.getRegionHeight()));
    }

	@Override
	public void update(float delta) {
		Vector2 pos = this.owner.transform.getPosition(); //Cache the owner's position.

        if(!ColonyGame.camera.frustum.boundsInFrustum(pos.x, pos.y, 0, sprite.getWidth(), sprite.getHeight(), 0)) {
			return;
		}

        Grid.Node node = ColonyGame.worldGrid.getNode(this.owner);
        int visibility = WorldGen.getInstance().getVisibilityMap()[node.getCol()][node.getRow()].getVisibility();
        if(visibility == Constants.VISIBILITY_UNEXPLORED)
            return;

        this.changeVisibility(visibility);

        this.sprite.setRotation(this.owner.transform.getRotation());

        if(alignment == 0)
		    this.sprite.setPosition(pos.x - (sprite.getWidth()/2), pos.y - (sprite.getHeight()/2));
        if(alignment == 1) {
            this.sprite.setPosition(pos.x - (sprite.getWidth() / 2), pos.y);
        }

        this.sprite.draw(this.batch);
	}

    public void setTexture(Texture texture){
        this.sprite.setTexture(texture);
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
