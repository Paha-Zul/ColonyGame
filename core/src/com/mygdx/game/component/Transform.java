package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDelayedDestroyable;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.ArrayList;

public class Transform extends Component implements IDelayedDestroyable {
    @JsonIgnore
	public Transform parent;
    @JsonProperty
	private Vector2 worldPosition = new Vector2(), localPosition = new Vector2();
    @JsonProperty
	private float worldRotation, localRotation, rotationOffset=0, distFromParent=0;
    @JsonProperty
	private float worldScale = 1, localScale = 1;
    @JsonIgnore
	private ArrayList<Transform> children = new ArrayList<>();

	public Transform(){

	}

	/**
	 * Creates a new transform.
	 * @param position The saveContainer position of the owner Entity.
	 * @param rotation The saveContainer rotation of the owner Entity.
	 */
	public Transform(Vector2 position, float rotation){
		super();

        this.worldPosition = new Vector2(position.x, position.y);
        this.localPosition = new Vector2(0, 0);
        this.setRotation(rotation);
	}

	public static float normalizeAngle(float angle)
	{
		float newAngle = angle;
		while (newAngle < 0) newAngle += 360;
		while (newAngle >= 360) newAngle -= 360;
		return newAngle;
	}

	public static float normalizeAngleRadians(float angle)
	{
		float newAngle = angle;
		while (newAngle < 0) newAngle += MathUtils.PI2;
		while (newAngle >= MathUtils.PI2) newAngle -= MathUtils.PI2;
		return newAngle;
	}

    @Override
    public void save() {

    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

    }

    @Override
    public void start() {
        super.start();

    }

    @Override
	public void update(float delta){
		//System.out.println(this.owner.compName+" local in update: "+this.localPosition);

		if(this.parent!=null){
			float parentRot = this.parent.getRotation();
			float adjRot = Transform.normalizeAngle(parentRot + this.rotationOffset)*MathUtils.degreesToRadians;

			float locX = (float)Math.cos(adjRot)*this.distFromParent + this.parent.getPosition().x;
			float locY = (float)Math.sin(adjRot)*this.distFromParent + this.parent.getPosition().y;

			this.setPosition(locX, locY);

			this.setRotation(Transform.normalizeAngle(parentRot + this.localRotation));
		}
	}

	/**
	 * Gets the saveContainer rotation of this Transform.
	 * @return A float which is the saveContainer rotation of this Transform.
	 */
    @JsonIgnore
	public float getRotation(){
		return this.worldRotation;
	}

	/**
	 * Gets the local position of the transform.
	 * @return A Vector2 holding the world X and Y coordinate of the transform.
	 */
	public Vector2 getPosition(){
		return this.worldPosition;
	}

	/**
	 * Sets the saveContainer position of the transform. This will update the local position of the transform.
	 * @param pos A Vector2 holding the new saveContainer position.
	 */
    @JsonIgnore
	public Transform setPosition(Vector2 pos){
		setPosition(pos.x, pos.y);
		return this;
	}

	/**
	 * Sets the position of the transform using X and Y float coordinates.
	 * @param x A float which is the X coordinate.
	 * @param y A float which is the Y coordinate.
	 */
    @JsonIgnore
	public void setPosition(float x, float y){
		this.worldPosition.x = x;
		this.worldPosition.y = y;
	}

	/**
	 * Sets the saveContainer rotation of this transform. This will update the local rotation of the transform.
	 * @param rot A float which is the new saveContainer rotation.
	 */
    @JsonIgnore
	public Transform setRotation(float rot){
		this.worldRotation = this.normalizeAngle(rot);

		if(this.parent == null)
			this.localRotation = this.worldRotation; //If the parent is null, assign the local the same as the global.
		else {
			this.localRotation = Transform.normalizeAngle(this.worldRotation - this.parent.worldRotation);
		}
		return this;
	}

	@Override
	public void destroy(Entity destroyer) {
		this.parent = null;
		this.worldPosition = null;
		this.localPosition = null;
		this.owner = null;
		this.children.clear();
		//this.children = null;
	}

    @JsonIgnore
	public void setParent(Transform futureParent){
		futureParent.addChild(this);
		this.parent = futureParent;
		this.setPosition(this.getPosition());
		this.setRotation(this.getRotation());
	}

	/**
	 * Adds a child to this transform. Will update the local position and rotation of the
	 * child Entity being added.
	 * @param child The Entity being added as a child to this transform.
	 */
	public void addChild(Transform child){
		Vector2 thisPos = new Vector2(this.worldPosition.x, this.worldPosition.y);
		Vector2 childPos = new Vector2(child.getPosition().x, child.getPosition().y);

		float rot = MathUtils.atan2(childPos.y - thisPos.y, childPos.x - thisPos.x)*MathUtils.radDeg;
		rot = Transform.normalizeAngle(rot);

		child.rotationOffset = rot - this.worldRotation;
		child.distFromParent = thisPos.dst(childPos);

		this.children.add(child); //Add the child to this transform
		child.parent = this; //Make the child's parent this transform.

		child.setLocalPosition(thisPos.x - childPos.x, thisPos.y - childPos.y);
		child.localScale = child.worldScale/this.getScale();
	}

	/**
	 * Sets the local position of the transform using X and Y float coordinates
	 * @param x A float which is the X Coordinate.
	 * @param y A float which is the Y Coordinate.
	 */
    @JsonIgnore
	public void setLocalPosition(float x, float y){
		this.localPosition.set(x,y);
	}

	/**
	 * Gets the scale of this Transform.
	 * @return A float which is the scale of this Transform
	 */
    @JsonIgnore
	public float getScale(){
		return this.worldScale;
	}

	/**
	 * Sets the scale of this Transform.
	 * @param scale The scale for this Transform to be.
	 */
    @JsonIgnore
	public void setScale(float scale){
		//If no parent, set the saveContainer scale AND localscale
		if(this.parent == null){
			this.worldScale = this.localScale = scale;
		//If we do have a parent, only set the saveContainer scale.
		}else {
			this.worldScale = this.localScale * scale; //Set saveContainer scale. We multiply our local scale with the new saveContainer scale (ie: 2 * 0.5 = 1 saveContainer scale).

			Vector2 localCopy = new Vector2(localPosition.x, localPosition.y); //Get a copy of our local position to work on (so we don't edit ours!).
			//Set the distance. We first scale our local position by our saveContainer position, add it to our current position
			//Calc the distance to the parent. Scale the local position by the parents scale, then add to the parent's position, then get the dst.
			this.distFromParent = (localCopy.scl(this.parent.getScale()).add(this.parent.getPosition()).dst(this.parent.getPosition()));

		}

		this.owner.getComponents().scaleComponents(this.worldScale);
		//For each child, set a new position and scale.
		for(Transform child : this.children)
			child.setScale(scale);
	}

	/**
	 * Adds a child to this transform and sets the child's position as the relative position to the parent passed in.
	 * @param child The child Entity to add as a child.
	 * @param relative The relative position to the parent that the child should be put at.
	 */
	public void addChild(Transform child, Vector2 relative){
		Vector2 thisPos = new Vector2(this.worldPosition.x, this.worldPosition.y); //Get the current entity's position.
		child.setPosition(thisPos.x + relative.x, thisPos.y + relative.y); //Set the child's position.
		Vector2 childPos = new Vector2(child.getPosition().x, child.getPosition().y); //Cache the child's position.

		float rot = MathUtils.atan2(childPos.y - thisPos.y, childPos.x - thisPos.x)*MathUtils.radDeg;
		rot = Transform.normalizeAngle(rot);

		child.rotationOffset = rot - this.worldRotation;
		child.distFromParent = thisPos.dst(childPos);

		this.children.add(child); //Add the child to this transform
		child.parent = this; //Make the child's parent this entity.

		child.setLocalPosition(thisPos.x - childPos.x, thisPos.y - childPos.y);
		child.localScale = child.worldScale/this.getScale();

	}

    @JsonIgnore
	public ArrayList<Transform> getChildren(){
		return this.children;
	}

	/**
	 * Gets the local position relative to this Transform's parent
	 * @return A Vector2 which is the local X and Y of this Transform relative to its parent.
	 */
    @JsonIgnore
	public Vector2 getLocalPosition(){
		return this.localPosition;
	}

	/**
	 * Gets the local rotation of this Transform.
	 * @return A float which is the relative rotation of this Transform in relation to its parent.
	 */
    @JsonIgnore
	public float getLocalRotation(){
		return this.localRotation;
	}

	/**
	 * Gets the local scale of this Transform.
	 * @return A float which is the local scale of this Transform
	 */
    @JsonIgnore
	public float getLocalScale(){
		return this.localScale;
	}

	/**
	 * Sets the local scale of this Transform. If this Transform has no parent, it also sets the saveContainer scale.
	 * @param scale The value to use as the local scale of this Transform.
	 */
    @JsonIgnore
	public void setLocalScale(float scale){
		this.localScale = scale;
		if(this.parent == null) this.worldScale = scale;
		else{
			this.worldScale = this.localScale * scale; //Set saveContainer scale. We multiply our local scale with the new saveContainer scale (ie: 2 * 0.5 = 1 saveContainer scale).
			Vector2 localCopy = new Vector2(localPosition.x, localPosition.y); //Get a copy of our local position to work on (so we don't edit ours!).
			//Set the distance. We first scale our local position by our saveContainer position, add it to our current position
			this.distFromParent = (localCopy.scl(this.parent.getScale()).add(this.parent.getPosition()).dst(this.parent.getPosition()));

		}

		this.owner.getComponents().scaleComponents(this.worldScale);

		//For each child, set a new position and scale.
		for(Transform child : this.children)
			child.setScale(scale);
	}

	public void rotate(float rot){
		this.setRotation(this.worldRotation + rot);
	}

	public void clearParent(){
		this.parent.removeChild(this);
	}

	/**
	 * Removes a child from this transform. The local position and rotation will be updated.
	 * @param child The Entity child being removed from this transform.
	 * @throws Exception If the child doesn't exist.
	 */
	public void removeChild(Transform child){
		for(int i=this.children.size()-1;i>=0;i--)
			if(child == this.children.get(i)){
				Transform tempChild = this.children.remove(i); //Removes it from the child list.
				tempChild.parent = null; //Sets the parent to null.
//				tempChild.transform.setPosition(tempChild.transform.getPosition()); //Reset position.
//				tempChild.transform.setRotation(tempChild.transform.getRotation()); //Reset rotation.

				return;
			}

		try {
			throw new Exception("Child doesn't exist under this parent");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds the X and Y value passed in to the saveContainer position, effectively moving the Entity.
	 * @param x The X value to move.
	 * @param y The Y value to move.
	 */
	public void translate(float x, float y){
		this.setPosition(this.worldPosition.x + x, this.worldPosition.y + y);
	}
}
