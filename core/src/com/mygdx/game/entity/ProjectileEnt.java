package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.Projectile;

/**
 * Created by Bbent_000 on 11/29/2014.
 */
public class ProjectileEnt extends Entity{
	public ProjectileEnt(Vector2 position, float rotation, Texture graphic, SpriteBatch batch, int drawLevel) {
		super(position, rotation, graphic, batch, drawLevel);

		Projectile proj = new Projectile();
		this.addComponent(proj);
	}
}
