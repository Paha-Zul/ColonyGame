package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.Transform;
import com.mygdx.game.interfaces.IDelayedDestroyable;
import com.mygdx.game.interfaces.IScalable;
import com.mygdx.game.util.EventSystem;
import com.mygdx.game.util.ListHolder;
import com.mygdx.game.util.Tags;

/**
 * @author Bbent_000
 *
 */
public class Entity implements IDelayedDestroyable{
	public String name = "Entity";
	public int drawLevel = 0;
	public boolean active = true;
    protected Tags tags = new Tags("entity");
	protected Components components;


    protected boolean destroyed=false, setToDestroy=false;
	protected double ID;

	/**
	 * Creates an Entity that will start with a GraphicIdentity component.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param graphic The Texture of the Entity
	 */
	public Entity(Vector2 position, float rotation, TextureRegion graphic, int drawLevel){
		this.components = new Components(this);
		this.components.transform = this.components.addComponent(new Transform(position, rotation, this));
		if(graphic != null)
			this.components.identity = this.components.addComponent(new GraphicIdentity(new TextureRegion(graphic)));

		this.ID = MathUtils.random()*Double.MAX_VALUE;
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
		this.components = new Components(this);

		this.components.transform = this.components.addComponent(new Transform(position, rotation, this));
		for(Component comp : comps)
			this.components.addComponent(comp);

		ListHolder.addEntity(drawLevel, this);
		this.ID = MathUtils.random()*Double.MAX_VALUE;
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
		this.components.update(delta);
	}

	/**
	 * Renders the renderable components.
	 * @param delta Delta time between frames.
	 * @param batch The SpriteBatch to draw with.
	 */
	public void render(float delta, SpriteBatch batch){
		if(this.components.identity != null) this.components.identity.render(delta, batch);
	}

    public final <T extends Component> T addComponent(Component comp){
        return components.addComponent(comp);
    }

    public final <T extends Component> T getComponent(Class<T> c){
        return components.getComponent(c);
    }

    public void destroyComponent(Component component){
        components.destroyComponent(component);
    }

    public <T extends Component> void destroyComponent(Class<T> cls){
        components.destroyComponent(cls);
    }

    public boolean removeComponent(Component comp){
        return components.removeComponent(comp);
    }

    public Transform getTransform(){
        return components.transform;
    }

    public GraphicIdentity getGraphicIdentity(){
        return components.identity;
    }

    public Components getComponents(){
        return this.components;
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

    /**
     * @return The double ID of this Entity.
     */
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
	public void destroy(Entity destroyer){
		EventSystem.unregisterEntity(this);

        components.destroy(destroyer);

		//Set destroyed to true.
		this.destroyed = true;
	}


	public Tags getTags(){
		return this.tags;
	}


    public static class Components implements IDelayedDestroyable{
		public Transform transform;
		public GraphicIdentity identity;
		protected Array<Component> newComponentList = new Array<>();
		protected Array<Component> activeComponentList = new Array<>();
		protected Array<Component> inactiveComponentList = new Array<>();
		protected Array<IScalable> scalableComponents = new Array<>();
		protected Array<Component> destroyComponentList = new Array<>();
		protected Entity owner;
		protected boolean setToDestroy, destroyed;

		public Components(Entity owner){
			this.owner = owner;
		}

		public void update(float delta){
			//Only update if active.
			if(this.owner.active && !this.destroyed) {
				if(destroyComponentList.size > 0) {
					for (Component comp : destroyComponentList) internalDestroyComponent(comp); //Destory the component
					destroyComponentList.clear(); //Clear the list.
				}

				//Start all new components.
				if (this.newComponentList.size > 0) {
					Array<Component> newCompCopy = new Array<>(this.newComponentList);
					//Call start on all new Components. This is where the component can access other
					//components on this Entity.
					newCompCopy.forEach(com.mygdx.game.component.Component::start);

					this.newComponentList.clear(); //Clear the new Component list.
				}

				Array<Component> activeCompCopy = new Array<>(this.activeComponentList);
				//Update all Components
				for (Component comp : activeCompCopy) {
					comp.update(delta);
					comp.lateUpdate(delta);
				}
			}
		}

		/**
		 * Adds a component to this Entity. The Component is immediately added to the Entity but the 'start' method is not called
		 * until the next update frame for the Entity. This means any values set in the 'start' method are not set in the same frame.
		 * @param comp The Component to add to this Entity.
		 * @param <T> The Component class interType of the component being added.
		 * @return The Component that was added.
		 */
		@SuppressWarnings("unchecked")
		public final <T extends Component> T addComponent(Component comp){
			comp.init(owner); //Initialize the component with this Entity as the owner.
			this.newComponentList.add(comp); //Add it to the new list for the start() method.
			//Add it to the active or inactive list.
			if(comp.isActive()) this.activeComponentList.add(comp);
			else this.inactiveComponentList.add(comp);

			return (T) comp;
		}

		/**
		 * Retrieves a Component from this Entity.
		 * @param c The Component class interType to retrieve.
		 * @return The Component if it was found, otherwise null.
		 */
		@SuppressWarnings("unchecked")
		public final <T extends Component> T getComponent(Class<T> c){
			for(Component comp : this.inactiveComponentList){
				if(comp.getClass() == c)
					return (T)comp;
			}

			for(Component comp : this.activeComponentList){
				if(comp.getClass() == c)
					return (T)comp;
			}

			return null;
		}

		/**
		 * Destroys and removes a component from this entity.
		 * @param cls The class interType of the Component to remove.
		 */
		public <T extends Component> void destroyComponent(Class<T> cls){
			//Search the inactive list.
			for (Component comp : this.inactiveComponentList)
				if (comp.getClass() == cls) {
					destroyComponentList.add(comp);
					return;
				}

			//Search the active list.
			for (Component comp : this.activeComponentList)
				if (comp.getClass() == cls) {
					destroyComponentList.add(comp);
					return;
				}
		}

		/**
		 * Destroys the passed in component and removes it from this Entity.
		 * @param component The Component to destroy and remove.
		 */
		public void destroyComponent(Component component){
			destroyComponentList.add(component);
		}

		/**
		 *	Destroys and removes a Component from this Entity.
		 * @param component The Component to destroy and remove.
		 */
		private void internalDestroyComponent(Component component){
			if(component.isActive())
				this.activeComponentList.removeValue(component, false);
			else
				this.inactiveComponentList.removeValue(component, false);

			component.destroy(owner);
		}

		/**
		 * Removes a Component from this Entity without destroying it.
		 * @param comp The Component to remove.
		 * @return True if the Component was removed, false otherwise.
		 */
		public boolean removeComponent(Component comp){
			if(comp.isActive()) return this.inactiveComponentList.removeValue(comp, false);
			return this.activeComponentList.removeValue(comp, false);
		}


		public final void registerScalable(IScalable scalable){
			this.scalableComponents.add(scalable);
		}

		public final void scaleComponents(float scale){
			for(IScalable scalable : this.scalableComponents)
				scalable.scale(scale);
		}

		@Override
		public void setToDestroy() {
			this.setToDestroy = true;
		}

		@Override
		public void destroy(Entity destroyer) {
			//Destroy all children
			this.transform.getChildren().forEach(tranform -> tranform.destroy(tranform.getEntityOwner()));

			//Remove myself from any parent.
			if(this.transform.parent != null){
				this.transform.parent.removeChild(this.transform);
				this.transform.parent = null;
			}

			//Destroy active components.
			this.activeComponentList.forEach(comp -> comp.destroy(this.owner));

			//Destroy inactive components
			this.inactiveComponentList.forEach(comp -> comp.destroy(this.owner));

			//Clear both lists.
			this.activeComponentList.clear();
			this.inactiveComponentList.clear();

			//Destroy and clear identity.
			if(this.identity != null) {
				this.identity.destroy(this.owner);
				this.identity = null;
			}

			//Destroy and clear transform.
			this.transform.destroy(this.owner);
			this.transform = null;

			this.destroyed = true;
		}

		@Override
		public boolean isDestroyed() {
			return this.destroyed;
		}

		@Override
		public boolean isSetToBeDestroyed() {
			return this.setToDestroy;
		}
	}
}
