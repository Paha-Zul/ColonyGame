package com.mygdx.game.entity;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.Transform;
import com.mygdx.game.helpers.EventSystem;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.Tags;
import com.mygdx.game.interfaces.IDelayedDestroyable;
import com.mygdx.game.interfaces.IScalable;

/**
 * @author Bbent_000
 *
 */
public class Entity implements IDelayedDestroyable{
	public String name = "Entity";
	public int drawLevel = 0;
	public Transform transform;
	public GraphicIdentity identity;
	public boolean active = true;

    protected Tags tags = new Tags("entity");

	protected Array<Component> newComponentList;
	protected Array<Component> activeComponentList;
	protected Array<Component> inactiveComponentList;
	protected Array<IScalable> scalableComponents;
	protected Array<Component> destroyComponentList;

	protected boolean destroyed=false, setToDestroy=false;
	protected double ID;

	/**
	 * Creates an Entity that will start with a GraphicIdentity component.
	 * @param position The initial position of the Entity
	 * @param rotation The initial rotation of the Entity.
	 * @param graphic The Texture of the Entity
	 */
	public Entity(Vector2 position, float rotation, TextureRegion graphic, int drawLevel){
		this.activeComponentList = new Array<>();
		this.inactiveComponentList = new Array<>();
		this.newComponentList = new Array<>();
		this.scalableComponents = new Array<>();
		destroyComponentList = new Array<>();

		this.transform = this.addComponent(new Transform(position, rotation, this));

		if(graphic != null)
			this.identity = this.addComponent(new GraphicIdentity(new TextureRegion(graphic)));

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
		this.activeComponentList = new Array<>(); //Init the active list.
		this.inactiveComponentList = new Array<>(); //Init the inactive list.
		this.newComponentList = new Array<>(); //Init the inactive list.
		this.scalableComponents = new Array<>();
		destroyComponentList = new Array<>();

		this.transform = this.addComponent(new Transform(position, rotation, this));
		for(Component comp : comps)
			this.addComponent(comp);

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
		//Only update if active.
		if(this.active && !this.destroyed) {
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
	 * Renders the renderable components.
	 * @param delta Delta time between frames.
	 * @param batch The SpriteBatch to draw with.
	 */
	public void render(float delta, SpriteBatch batch){
		if(this.identity != null) this.identity.render(delta, batch);
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
		comp.init(this); //Initialize the component with this Entity as the owner.
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
	 * Removes a Component from this Entity.
	 * @param cls The class interType of the Component to remove.
	 */
	public <T extends Component> void internalDestroyComponent(Class<T> cls){
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

		component.destroy(this);
	}

	/**
	 * Removes a Component from this Entity.
	 * @param comp The Component to remove.
	 * @return True if the Component was removed, false otherwise.
	 */
	public boolean removeComponent(Component comp){
		if(comp.isActive()) return this.inactiveComponentList.removeValue(comp, false);
		return this.activeComponentList.removeValue(comp, false);
	}

    /**
     * Prints all components to the console.
     */
    public void printComponents(){
        System.out.print("Active: ");
        for(Component comp : activeComponentList)
            System.out.print(""+comp.getClass().getSimpleName()+" ");

        System.out.print("Inactive: ");

        for(Component comp : inactiveComponentList)
            System.out.print(""+comp.getClass().getSimpleName()+ " ");

		System.out.println();
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
	public void destroy(Entity destroyer){
		EventSystem.unregisterEntity(this);

		//Destroy all children
        this.transform.getChildren().forEach(ent -> ent.destroy(ent));

		//Remove myself from any parent.
		if(this.transform.parent != null){
			this.transform.parent.removeChild(this);
			this.transform.parent = null;
		}

		//Destroy active components.
		this.activeComponentList.forEach(comp -> comp.destroy(this));

		//Destroy inactive components
		this.inactiveComponentList.forEach(comp -> comp.destroy(this));

		//Clear both lists.
		this.activeComponentList.clear();
		this.inactiveComponentList.clear();

		//Destroy and clear identity.
		if(this.identity != null) {
			this.identity.destroy(this);
			this.identity = null;
		}

		//Destroy and clear transform.
		this.transform.destroy(this);
		this.transform = null;

		//Set destroyed to true.
		this.destroyed = true;
	}

	public final void registerScalable(IScalable scalable){
		this.scalableComponents.add(scalable);
	}

	public final void scaleComponents(float scale){
		for(IScalable scalable : this.scalableComponents)
			scalable.scale(scale);
	}

	public Tags getTags(){
		return this.tags;
	}
}
