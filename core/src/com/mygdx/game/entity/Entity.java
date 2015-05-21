package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.Transform;
import com.mygdx.game.helpers.EventSystem;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.Tags;
import com.mygdx.game.helpers.managers.ComponentManager;
import com.mygdx.game.interfaces.IDelayedDestroyable;

/**
 * @author Bbent_000
 *
 */
public class Entity implements IDelayedDestroyable{
	public String name = "Entity";
	public int drawLevel = 0;
	public boolean active = true;
    protected Tags tags = new Tags("entity");

	protected boolean destroyed=false, setToDestroy=false;
	protected double ID;

	/**
	 * Creates an Entity that will start with a GraphicIdentity component.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param graphic The Texture of the Entity
	 */
	public Entity(Vector2 position, float rotation, TextureRegion graphic, int drawLevel){
		this.ID = MathUtils.random()*Double.MAX_VALUE;

		ComponentManager.ComponentContainer components = ComponentManager.getComponents(this.ID);
		components.transform = components.addComponent(new Transform(position, rotation, this));
		if(graphic != null)
			components.identity = components.addComponent(new GraphicIdentity(new TextureRegion(graphic)));

		this.drawLevel = drawLevel;

		ListHolder.addEntity(drawLevel, this);
	}

	/**
	 * Creates an Entity with a transform and identity component.
	 * such as "Paha's Market". Use as desired.
	 * @param position The starting X and Y position of this Entity.
	 * @param rotation The starting rotation of this Entity.
	 */
	public Entity(Vector2 position, float rotation, int drawLevel){
		this(position, rotation, null, drawLevel);
	}

	/**
	 * Creates an Entity without a GraphicIdentity, but allows for a variable amount of components to be immediately added.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param comps A variable amount of Components to construct this Entity with.
	 */
	public Entity(Vector2 position, float rotation, int drawLevel, Component... comps){
		this.ID = MathUtils.random()*Double.MAX_VALUE;

		ComponentManager.ComponentContainer components = ComponentManager.getComponents(this.ID);
		components.transform = components.addComponent(new Transform(position, rotation, this));
		for(Component comp : comps)
			components.addComponent(comp);

		ListHolder.addEntity(drawLevel, this);
		this.drawLevel = drawLevel;
	}

	public void start(){
		EventSystem.notifyGameEvent("entity_created", this);
	}

    /**
     * Updates the Entity and all active components.
     * @param delta The time between the current and last frame.
     */
	public void update(float delta){
		ComponentManager.updateComponents(this, delta);
	}

	/**
	 * Renders the renderable components.
	 * @param delta Delta time between frames.
	 * @param batch The SpriteBatch to draw with.
	 */
	public void render(float delta, SpriteBatch batch){
		ComponentManager.ComponentContainer components = ComponentManager.getComponents(this.getID());
		if(components.identity != null) components.identity.render(delta, batch);
	}

	/**
	 * @return True if this Entity has been destroyed, false otherwise.
	 */
	@Override
	public boolean isDestroyed(){
		return this.destroyed;
	}

	/**
	 * @return True if this Entity is valid (not destroyed or set to be destroyed), false otherwise.
	 */
	public boolean isValid(){
		return !this.destroyed && !this.isSetToBeDestroyed();
	}

	public double getID(){
		return this.ID;
	}

	@Override
	public boolean isSetToBeDestroyed() {
		return this.setToDestroy;
	}

	@Override
	public void setToDestroy() {
		this.setToDestroy = true;
	}

	@Override
	public void destroy(double ID){
		EventSystem.unregisterEntity(this);
		//Set destroyed to true.
		this.destroyed = true;
	}



	public Tags getTags(){
		return this.tags;
	}
}
