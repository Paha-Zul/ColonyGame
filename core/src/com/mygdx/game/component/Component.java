package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDestroyable;


public abstract class Component implements IDestroyable {
	protected String name;
	protected int type;
	private boolean active = false, destroyed = false;
	protected Entity owner;

	public Component(String name, int type, boolean active) {
		this.name = name;
		this.type = type;
		this.active = active;
	}

	public void init(Entity owner){
		this.owner = owner;
	}

	public void start(){

	}

	public void update(float delta) {

	}

	public void fixedUpdate(float delta) {

	}

	public void lateUpdate(float delta) {

	}

	public void setActive(boolean val) {
		if (val == this.active)
			return;
		// If the val is true, remove it from the false (inactive) list and
		// re-add it.
		if (val) {
			this.owner.removeComponent(this, false);
			this.owner.addComponent(this);
			// If the val is false, remove it from the true (active) list and
			// re-add it.
		} else {
			this.owner.removeComponent(this, true);
			this.owner.addComponent(this);
		}
	}

	public boolean isActive() {
		return this.active;
	}

	public Entity getEntityOwner() {
		return this.owner;
	}

	@Override
	public void destroy() {
		this.owner = null;
		this.destroyed = true;
	}

	@Override
	public boolean isDestroyed(){
		return this.destroyed;
	}
}
