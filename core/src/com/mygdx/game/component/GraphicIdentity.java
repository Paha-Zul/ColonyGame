package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.Profiler;

public class GraphicIdentity extends Component{
	public Sprite sprite;

	SpriteBatch batch;

	public GraphicIdentity(Texture image, SpriteBatch batch){
		super();

		this.sprite = new Sprite(image);
		this.batch = batch;
	}

	@Override
	public void start() {
		Vector2 pos = this.owner.transform.getPosition(); //Cache the owner's position.
		this.sprite.setPosition(pos.x - this.sprite.getWidth()/2, pos.y - this.sprite.getHeight()/2);
	}

	@Override
	public void update(float delta) {
		Profiler.begin("GraphicIdentity Update");
		Vector2 pos = this.owner.transform.getPosition(); //Cache the owner's position.

		if(!ColonyGame.camera.frustum.boundsInFrustum(pos.x, pos.y, 0, sprite.getWidth()*0.5f, sprite.getHeight()*0.5f, 0)) {
			Profiler.end();
			return;
		}

		this.sprite.setScale(this.owner.transform.getScale());
		this.sprite.setRotation(this.owner.transform.getRotation());

		this.sprite.setPosition(pos.x - (sprite.getWidth())/2, pos.y - (sprite.getHeight())/2);

		this.sprite.draw(this.batch);

		Profiler.end();
	}
}
