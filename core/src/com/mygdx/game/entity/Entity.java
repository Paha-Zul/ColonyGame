package com.mygdx.game.entity;

import com.badlogic.gdx.math.Vector2;


import java.util.ArrayList;
import java.util.Collections;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.Transform;

/**
 * @author Bbent_000
 *
 */
public class Entity {
	public String name, tag;
	public int entityType, entitySubType, drawLevel;
	public Transform transform;
	public GraphicIdentity identity;

	protected ArrayList<Component> newComponentList;
	protected ArrayList<Component> activeComponentList;
	protected ArrayList<Component> inactiveComponentList;
	protected boolean destroyed=false;



	/**
	 * Creates an Entity that will start with a GraphicIdentity component.
	 * @param name The name of the Entity.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param graphic The Texture of the Entity
	 * @param batch The SpriteBatch to draw the Entity.
	 */
	public Entity(String name, Vector2 position, float rotation, Texture graphic, SpriteBatch batch){
		this.name = name;
		this.activeComponentList = new ArrayList<Component>();
		this.inactiveComponentList = new ArrayList<Component>();

		this.transform = new Transform(position, rotation);
		this.identity = new GraphicIdentity(graphic, batch);

		this.newComponentList.add(this.transform);
		this.newComponentList.add(this.identity);
	}

	/**
	 * Creates an Entity with a transform and identity component.
	 * @param name The name of this Entity. This could be used as a generic name such as "Building" or a specific name
	 * such as "Paha's Market". Use as desired.
	 * @param position The starting X and Y position of this Entity.
	 * @param rotation The starting rotation of this Entity.
	 * @param graphic The texture to represent this entity. If null, it will not be drawn.
	 */
	public Entity(String name, Vector2 position, float rotation, Texture graphic){
		this(name, position, rotation, graphic, null);
	}

	/**
	 * Creates an Entity without a GraphicIdentity, but allows for a variable amount of components to be immediately added.
	 * @param name The name of the Entity.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param comps A variable amount of Components to construct this Entity with.
	 */
	public Entity(String name, Vector2 position, float rotation, Component... comps){
		this.name = name;
		this.activeComponentList = new ArrayList<Component>(); //Init the active list.
		this.inactiveComponentList = new ArrayList<Component>(); //Init the inactive list.
		this.newComponentList = new ArrayList<Component>(); //Init the inactive list.

		this.transform = new Transform(position, rotation); //Create the transform.
		//Loops over the varargs and add them to the component list.
		Collections.addAll(newComponentList, comps);
	}

	public void update(float delta){
		if(this.newComponentList.size() > 0) {
			for (Component comp : this.newComponentList) {
				comp.start(this);
				if(comp.isActive())
					this.activeComponentList.add(comp);
				else
					this.inactiveComponentList.add(comp);
			}
			this.newComponentList.clear();
		}


		for(Component comp : this.activeComponentList){
			comp.update(delta);
			comp.lateUpdate(delta);
		}
	}

	/**
	 * Adds a component to this Entity.
	 * @param comp The Component to add.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> T addComponent(Component comp){
		this.newComponentList.add(comp);

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

	public boolean removeComponent(Component comp, boolean active){
		if(active) return this.inactiveComponentList.remove(comp);
		return this.activeComponentList.remove(comp);
	}

	public boolean isDestroyed(){
		return this.destroyed;
	}

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
		this.identity.destroy();
		this.identity = null;

		//Destroy and clear transform.
		this.transform.destroy();
		this.transform = null;

		//Set destroyed to true.
		this.destroyed = true;
	}
}
