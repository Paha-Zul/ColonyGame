package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;


import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.ExploreGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.Transform;
import com.mygdx.game.helpers.ListHolder;

/**
 * @author Bbent_000
 *
 */
public class Entity {
	public String name = "Entity", tag;
	public int entityType, entitySubType, drawLevel;
	public Transform transform;
	public GraphicIdentity identity;
	public boolean active = true;

	protected ArrayList<Component> newComponentList;
	protected ArrayList<Component> activeComponentList;
	protected ArrayList<Component> inactiveComponentList;
	protected boolean destroyed=false;

	/**
	 * Creates an Entity that will start with a GraphicIdentity component.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param graphic The Texture of the Entity
	 * @param batch The SpriteBatch to draw the Entity.
	 */
	public Entity(Vector2 position, float rotation, Texture graphic, SpriteBatch batch, int drawLevel){
		this.activeComponentList = new ArrayList<>();
		this.inactiveComponentList = new ArrayList<>();
		this.newComponentList = new ArrayList<>();

		this.transform = this.addComponent(new Transform(position, rotation, this));

		if(batch != null && graphic != null) {
			this.identity = this.addComponent(new GraphicIdentity(graphic, batch));
			this.newComponentList.add(this.identity);
		}

		ListHolder.addEntity(drawLevel, this);
	}

	/**
	 * Creates an Entity with a transform and identity component.
	 * such as "Paha's Market". Use as desired.
	 * @param position The starting X and Y position of this Entity.
	 * @param rotation The starting rotation of this Entity.
	 */
	public Entity(Vector2 position, float rotation, int drawLevel){
		this(position, rotation, null, null, drawLevel);
	}

	/**
	 * Creates an Entity without a GraphicIdentity, but allows for a variable amount of components to be immediately added.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param comps A variable amount of Components to construct this Entity with.
	 */
	public Entity(Vector2 position, float rotation, int drawLevel, Component... comps){
		this.activeComponentList = new ArrayList<>(); //Init the active list.
		this.inactiveComponentList = new ArrayList<>(); //Init the inactive list.
		this.newComponentList = new ArrayList<>(); //Init the inactive list.

		this.transform = this.addComponent(new Transform(position, rotation, this));
		for(Component comp : comps)
			this.addComponent(comp);

		ListHolder.addEntity(drawLevel, this);
	}

	public void update(float delta){

		//Only update if active.
		if(this.active && !this.destroyed) {
			//Start all new components.
			if (this.newComponentList.size() > 0) {
				//Call start on all new Components. This is where the component can access other
				//components on this Entity.
				for (Component comp : this.newComponentList)
					comp.start();

				this.newComponentList.clear(); //Clear the new Component list.
			}

			ArrayList<Component> activeCompCopy = new ArrayList<>(this.activeComponentList);
			//Update all Components
			for (Component comp : activeCompCopy) {
				comp.update(delta);
				comp.lateUpdate(delta);
			}
		}
	}

	/**
	 * Adds a component to this Entity. The start() method will be called on the next tick for this Entity.
	 * @param comp The Component to add to this Entity.
	 * @param <T> The Component class type of the component being added.
	 * @return The Component that was added.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> T addComponent(Component comp){
		comp.init(this); //Initialize the component with this Entity as the owner.
		this.newComponentList.add(comp); //Add it to the new list for the start() method.
		//Add it to the active or inactive list.
		if(comp.isActive()) this.activeComponentList.add(comp);
		else this.inactiveComponentList.add(comp);

		return (T) comp;
	}

	/**
	 * Retrieves a Component from this Entity.
	 * @param c The Component class type to retrieve.
	 * @return The Component if it was found, otherwise null.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> T getComponent(Class<T> c){
		for(Component comp : this.inactiveComponentList){
			if(comp.getClass() == c)
				return (T)comp;
		}

		for(Component comp : this.activeComponentList){
			if(comp.getClass() == c)
				return (T)comp;
		}

		throw new RuntimeException("Component type *" + c + "* doesn't exist.");

		//return null;
	}

	/**
	 * Removes a Component from this Entity.
	 * @param comp The Component to remove.
	 * @return True if the Component was removed, fales otherwise.
	 */
	public boolean removeComponent(Component comp){
		if(comp.isActive()) return this.inactiveComponentList.remove(comp);
		return this.activeComponentList.remove(comp);
	}

	/**
	 * @return True if this Entity has been destroyed, false otherwise.
	 */
	public boolean isDestroyed(){
		return this.destroyed;
	}

	/**
	 * Destroys this Entity. This will kill all children, components of children, and components of the parent.
	 */
	public void destroy(){
		//Destroy all children
		for(Entity child : this.transform.getChildren()){
			child.destroy();
		}

		//Remove all children
		if(this.transform.parent != null){
			this.transform.parent.transform.removeChild(this);
			this.transform.parent = null;
		}

		//Destroy active components.
		for(Component comp : this.activeComponentList)
			comp.destroy();

		//Destroy inactive components
		for(Component comp : this.inactiveComponentList)
			comp.destroy();

		//Clear both lists.
		this.activeComponentList.clear();
		this.inactiveComponentList.clear();

		//Destroy and clear identity.
		if(this.identity != null) {
			this.identity.destroy();
			this.identity = null;
		}

		//Destroy and clear transform.
		this.transform.destroy();
		this.transform = null;

		//Set destroyed to true.
		this.destroyed = true;
	}
}
