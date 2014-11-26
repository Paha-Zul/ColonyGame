package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class GraphicIdentity extends Component{
	public Sprite sprite;

	SpriteBatch batch;

	public GraphicIdentity(boolean active, Texture image, SpriteBatch batch){
		super(active);

		this.sprite = new Sprite(image);
		this.batch = batch;
	}

	public GraphicIdentity(Texture image, SpriteBatch batch){
		this(true, image, batch);
	}

	@Override
	public void start() {
		Vector2 pos = this.owner.transform.getPosition(); //Cache the owner's position.
		this.sprite.setPosition(pos.x - this.sprite.getWidth()/2, pos.y - this.sprite.getHeight()/2);
		this.sprite.setCenter(-this.sprite.getWidth()/2, -this.sprite.getHeight()/2);
		this.sprite.setOrigin(-this.sprite.getWidth()/2, -this.sprite.getHeight()/2);
	}

	@Override
	public void update(float delta) {
		Vector2 pos = this.owner.transform.getPosition(); //Cache the owner's position.

		this.sprite.setPosition(pos.x - this.sprite.getWidth()/2, pos.y - this.sprite.getHeight()/2);
		this.sprite.draw(this.batch);
	}
}
