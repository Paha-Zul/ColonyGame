package com.mygdx.game.component;

import java.util.ArrayList;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDestroyable;

public class Transform extends Component implements IDestroyable {
	public Entity parent;
	private Vector2 worldPosition, localPosition;
	private float worldRotation, localRotation, rotationOffset=0, distFromParent=0;
	private float scale = 1;

	private ArrayList<Entity> children;

	/**
	 * Creates a new transform.
	 * @param position The world position of the owner Entity.
	 * @param rotation The world rotation of the owner Entity.
	 */
	public Transform(Vector2 position, float rotation, Entity owner){
		super(true);
		this.owner = owner;
		this.worldPosition = new Vector2(position.x, position.y);
		this.localPosition = new Vector2(0, 0);
		this.setRotation(rotation);
	}

	@Override
	public void init(Entity owner){
		this.owner = owner;
		this.children = new ArrayList<Entity>();

		//new Exception().printStackTrace();
	}

	@Override
	public void update(float delta){
		//System.out.println(this.owner.name+" local in update: "+this.localPosition);

		if(this.parent!=null){
			float parentRot = this.parent.transform.getRotation();
			float adjRot = Transform.normalizeAngle(parentRot + this.rotationOffset)*MathUtils.degreesToRadians;

			float locX = (float)Math.cos(adjRot)*this.distFromParent + this.parent.transform.getPosition().x;
			float locY = (float)Math.sin(adjRot)*this.distFromParent + this.parent.transform.getPosition().y;

			this.setPosition(locX, locY);

			this.setRotation(Transform.normalizeAngle(parentRot + this.localRotation));
		}
	}

	public void setRotationOffset(float rotation){
		this.rotationOffset = rotation;
	}

	public void setParent(Entity futureParent){
		futureParent.transform.addChild(this.owner);
		this.parent = futureParent;
		this.setPosition(this.getPosition());
		this.setRotation(this.getRotation());
	}

	/**
	 * Adds a child to this transform. Will update the local position and rotation of the 
	 * child Entity being added.
	 * @param child The Entity being added as a child to this transform.
	 */
	public void addChild(Entity child){
		Vector2 thisPos = new Vector2(this.worldPosition.x, this.worldPosition.y);
		Vector2 childPos = new Vector2(child.transform.getPosition().x, child.transform.getPosition().y);

		float rot = MathUtils.atan2(childPos.y - thisPos.y, childPos.x - thisPos.x)*MathUtils.radDeg;
		rot = Transform.normalizeAngle(rot);

		child.transform.rotationOffset = rot - this.worldRotation;
		child.transform.distFromParent = thisPos.dst(childPos);

		this.children.add(child); //Add the child to this transform
		child.transform.parent = this.owner; //Make the child's parent this entity.

		child.transform.setLocalPosition(thisPos.x - childPos.x, thisPos.y - childPos.y);

	}

	/**
	 * Removes a child from this transform. The local position and rotation will be updated.
	 * @param child The Entity child being removed from this transform.
	 * @throws Exception If the child doesn't exist.
	 */
	public void removeChild(Entity child){
		for(int i=this.children.size()-1;i>=0;i--)
			if(child == this.children.get(i)){
				Entity tempChild = this.children.remove(i); //Removes it from the child list.
				tempChild.transform.parent = null; //Sets the parent to null.
				tempChild.transform.setPosition(tempChild.transform.getPosition()); //Reset position.
				tempChild.transform.setRotation(tempChild.transform.getRotation()); //Reset rotation.

				return;
			}

		try {
			throw new Exception("Child doesn't exist under this parent");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Entity> getChildren(){
		return this.children;
	}

	/**
	 * Gets the local position of the transform.
	 * @return A Vector2 holding the local X and Y coordinate of the transform.
	 */
	public Vector2 getPosition(){
		return this.worldPosition;
	}

	/**
	 * Gets the local position relative to this Transform's parent
	 * @return A Vector2 which is the local X and Y of this Transform relative to its parent.
	 */
	public Vector2 getLocalPosition(){
		return this.localPosition;
	}

	/**
	 * Gets the world rotation of this Transform.
	 * @return A float which is the world rotation of this Transform.
	 */
	public float getRotation(){
		return this.worldRotation;
	}

	/**
	 * Gets the local rotation of this Transform.
	 * @return A float which is the relative rotation of this Transform in relation to its parent.
	 */
	public float getLocalRotation(){
		return this.localRotation;
	}

	/**
	 * Gets the scale of this Transform.
	 * @return A float which is the scale of this Transform
	 */
	public float getScale(){
		return this.scale;
	}

	/**
	 * Sets the scale of this Transform
	 * @param scale The scale for this Transform to be.
	 */
	public void setScale(float scale){
		this.scale = scale;
	}

	/**
	 * Sets the world position of the transform. This will update the local position of the transform.
	 * @param pos A Vector2 holding the new world position.
	 */
	public void setPosition(Vector2 pos){
		setPosition(pos.x, pos.y);
	}

	/**
	 * Sets the position of the transform using X and Y float coordinates.
	 * @param x A float which is the X coordinate.
	 * @param y A float which is the Y coordinate.
	 */
	public void setPosition(float x, float y){
		this.worldPosition.x = x;
		this.worldPosition.y = y;

		if(this.parent == null){
			this.localPosition.x = x; //If the parent is null, assign the local the same as the global.
			this.localPosition.y = y; //If the parent is null, assign the local the same as the global.
			return; //If the parent is null, return here.
		}
	}

	/**
	 * Sets the local position of the transform using X and Y float coordinates
	 * @param x A float which is the X Coordinate.
	 * @param y A float which is the Y Coordinate.
	 */
	public void setLocalPosition(float x, float y){
		this.localPosition.set(x,y);
	}

	/**
	 * Sets the world rotation of this transform. This will update the local rotation of the transform.
	 * @param rot A float which is the new world rotation.
	 */
	public void setRotation(float rot){
		this.worldRotation = this.normalizeAngle(rot);

		if(this.parent == null)
			this.localRotation = this.worldRotation; //If the parent is null, assign the local the same as the global.
		else {
			this.localRotation = Transform.normalizeAngle(this.worldRotation - this.parent.transform.worldRotation);
		}

		if(this.owner != null) System.out.print(this.owner.name);

	}

	public void rotate(float rot){
		this.setRotation(this.worldRotation + rot);
	}

	public void clearParent(){
		this.parent.transform.removeChild(this.owner);
	}

	/**
	 * Adds the X and Y value passed in to the world position, effectively moving the Entity.
	 * @param x The X value to move.
	 * @param y The Y value to move.
	 */
	public void translate(float x, float y){
		this.setPosition(this.worldPosition.x + x, this.worldPosition.y + y);
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
	public void destroy() {
		this.parent = null;
		this.worldPosition = null;
		this.localPosition = null;
		this.owner = null;
		this.children.clear();
		//this.children = null;
	}
}
