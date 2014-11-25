package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entity.Entity;

public class GraphicIdentity extends Component{
	public Sprite sprite;
	SpriteBatch batch;

	public GraphicIdentity(String name, int type, boolean active, Texture image, SpriteBatch batch){
		super(name, type, active);

		this.sprite = new Sprite(image);
		this.batch = batch;
	}

	public GraphicIdentity(Texture image, SpriteBatch batch){
		this("Identity", 0, true, image, batch);
	}

	@Override
	public void start(Entity owner) {
		super.start(owner);

		Vector2 pos = this.owner.transform.getWorldPosition(); //Cache the owner's position.
		this.sprite.setPosition(pos.x - this.sprite.getWidth()/2, pos.y - this.sprite.getHeight()/2);
		this.sprite.setCenter(-this.sprite.getWidth()/2, -this.sprite.getHeight()/2);
		this.sprite.setOrigin(-this.sprite.getWidth()/2, -this.sprite.getHeight()/2);
	}

	@Override
	public void update(float delta) {
		Vector2 pos = this.owner.transform.getWorldPosition(); //Cache the owner's position.

		this.sprite.setPosition(pos.x - this.sprite.getWidth()/2, pos.y - this.sprite.getHeight()/2);
		this.sprite.draw(this.batch);
	}
}
