package com.mygdx.game.component;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDelayedDestroyable;
import com.mygdx.game.interfaces.ISaveable;
import com.mygdx.game.util.managers.MessageEventSystem;
import gnu.trove.map.hash.TLongObjectHashMap;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT, property = "@class")
public abstract class Component implements IDelayedDestroyable, ISaveable {
    public static long currID = (long)(-Long.MAX_VALUE*0.5);
    @JsonProperty
	protected String compName;
    @JsonIgnore
	protected Entity owner;
    @JsonProperty
	protected boolean initiated = false, started = false;
    @JsonProperty
	protected long compID;
    @JsonProperty
    private boolean active = true, destroyed = false, setToDestroy = false;

	public Component() {
		this.compID = currID++;
        if(this.compID == 0) this.compID = currID++;
	}

    /**
     * Called as soon as the Component has been added. Use this to either interact with the Entity owner
     * or perform any task that does not rely on fields or other Components. Init is called after (after all Components during that tick have been added,
	 * and init is called on all new components at once), then start (immediately after the init batch).
     * @param owner The Entity owner of this Component.
     */
	public void added(Entity owner){
		this.owner = owner;
		MessageEventSystem.notifyGameEvent("component_created", this.getClass(), this);
	}

	@Override
	public void addedLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

	}

	@Override
	public void save() {

	}

	@Override
	public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

	}

	@Override
	public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

	}

	/**
     * Called after added() but before the start() batch. Use this to interact with any fields that may have been set on this Component
     * but not other Components on the Entity. This is called on all new components of the Entity at the same time. Afterwards, start is called.
     */
	public void init(){
		this.initiated = true;
	}

	/**
	 * The start of this Component. This is where other Components of the owner can be accessed and stored as a reference. Called after all new components have been added() and init().
	 */
	public void start(){
		this.started = true;
        MessageEventSystem.notifyGameEvent("component_started", this.getClass(), this);
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
     * Called every frame for drawing stuff.
     * @param delta The time between the current and last frame.
     * @param batch The SpriteBatch to batch the draw call.
     */
    public void render(float delta, SpriteBatch batch){

    }

	/**
	 * A shortcut function to call 'addComponent' on the Entity owner. This cannot be called in the constructor
	 * as the Entity owner will be null. Use in the 'start' function.
	 * @param comp The Component to add.
	 * @param <T> The class.
	 * @return The Component that was added.
	 */
	public final <T extends Component> T addComponent(T comp){
		return this.owner.addComponent(comp);
	}

	/**
	 * A shortcut function to call 'getComponent' on the Entity owner. This cannot be called in the constructor
	 * as the Entity owner will be null. Use in the 'start' function.
	 * @param c The Class interType to remove.
	 * @param <T> The class.
	 * @return The Component that was retrieved, or null if it could not be found.
	 */
    @JsonIgnore
    public final <T extends Component> T getComponent(Class<T> c){
		return this.owner.getComponent(c);
	}

	public void setOwner(Entity owner){
		this.owner = owner;
	}

	@JsonIgnore
	public String getCompName(){
		return this.compName;
	}

	/**
	 * If this Component is active or not.
	 * @return True if the Component is active, false if not.
	 */
    @JsonIgnore
	public final boolean isActive() {
		return this.active;
	}

	/**
	 * Sets this component to active or inactive.
	 * @param val A boolean indicating active or inactive.
	 */
	@JsonIgnore
	public void setActive(boolean val) {
		//If the value isn't different, simply return
		if (val == this.active)
			return;

		if(this.initiated) {
			this.owner.removeComponent(this);   //Remove it from the current active/inactive list.
			this.active = val; 					//Sets it's active value.
			this.owner.addComponent(this);		//Add it to the correct list.
		}
        this.active = val;
    }

    @JsonIgnore
    public final boolean isInitiated() {
        return initiated;
    }

	public final boolean isStarted(){
		return started;
	}

    /**
	 * Gets the Entity owner of this Component.
	 * @return The Entity owner of this Component.
	 */
    @JsonIgnore
	public Entity getEntityOwner() {
		return this.owner;
	}

	@Override
	public void setToDestroy() {
		this.setToDestroy = true;
	}

	@Override
	public void destroy(Entity destroyer) {
		this.owner = null;
		this.destroyed = true;
		this.active = false;
		this.compName = null;
	}

    @Override
    @JsonIgnore
	public final boolean isDestroyed(){
		return this.destroyed;
	}

	@Override
	@JsonIgnore
	public final boolean isSetToBeDestroyed() {
		return this.setToDestroy;
	}

    @JsonIgnore
    public long getCompID(){
        return this.compID;
    }

    @JsonIgnore
	public boolean isValid(){
		return !isDestroyed() && !isSetToBeDestroyed();
	}
}
