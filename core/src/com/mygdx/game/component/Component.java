package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDestroyable;


public abstract class Component implements IDestroyable {
	protected String name;
	protected int type;
	private boolean active = true, destroyed = false;
	protected Entity owner;
	protected boolean initiated = false;

	public Component() {

	}

	/**
	 * The initialization of this Component. This should only be called by the Entity owner. This function will
	 * set the owner of this Component and other Components of the owner may not be available to access.
	 * @param owner The Entity owner of this Component.
	 */
	public final void init(Entity owner){
		this.owner = owner;
		this.initiated = true;
	}

	/**
	 * The start of this Component. This is where other Components of the owner can be accessed and stored as a reference.
	 */
	public void start(){

	}

	/**
	 * Called every frame.
	 * @param delta The time between frames.
	 */
	public void update(float delta) {

	}

	public void fixedUpdate(float delta) {

	}

	/**
	 * Called every frame after update().
	 * @param delta The time between frames.
	 */
	public void lateUpdate(float delta) {

	}

	/**
	 * Sets this component to active or inactive.
	 * @param val A boolean indicating active or inactive.
	 */
	public void setActive(boolean val) {
		//If the value isn't different, simply return
		if (val == this.active)
			return;

		if(this.initiated) {
			this.owner.removeComponent(this);   //Remove it from the current active/inactive list.
			this.active = val;                  //Sets it's active value.
			this.owner.addComponent(this);		//Add it to the correct list.
		}

	}

	/**
	 * If this Component is active or not.
	 * @return True if the Component is active, false if not.
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Gets the Entity owner of this Component.
	 * @return The Entity owner of this Component.
	 */
	public Entity getEntityOwner() {
		return this.owner;
	}

	@Override
	public void destroy() {
		this.owner = null;
		this.destroyed = true;
		this.active = false;
		this.name = null;
	}

	@Override
	public boolean isDestroyed(){
		return this.destroyed;
	}
}
