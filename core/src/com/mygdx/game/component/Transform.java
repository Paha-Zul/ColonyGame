package com.mygdx.game.component;

import java.util.ArrayList;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDestroyable;

public class Transform extends Component implements IDestroyable {
	public Entity parent;
	private Vector2 worldPosition, localPosition;
	public float worldRotation, localRotation;

	private Entity owner;
	private ArrayList<Entity> children;

	/**
	 * Creates a new transform.
	 * @param position The world position of the owner Entity.
	 * @param rotation The world rotation of the owner Entity.
	 */
	public Transform(Vector2 position, float rotation){
		super("Transform", 0, true);
		this.worldPosition = new Vector2(position.x, position.y);
		this.localPosition = new Vector2(position.x, position.y);
		this.worldRotation = rotation;
		this.localRotation = rotation;
		this.children = new ArrayList<Entity>();
	}

	@Override
	public void start() {
		super.start();

	}

	/**
	 * Adds a child to this transform. Will update the local position and rotation of the 
	 * child Entity being added.
	 * @param child The Entity being added as a child to this transform.
	 */
	public void addChild(Entity child){
		this.children.add(child); //Add the child to this transform
		child.transform.parent = this.owner; //Make the child's parent this entity.
		//Set the local position of the child. This simply recalculates the local position using the world position
		//and any parent that may exist.
		child.transform.setPosition(child.transform.getWorldPosition());
		child.transform.setRotation(child.transform.getWorldRotation());
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
				tempChild.transform.setPosition(tempChild.transform.getWorldPosition()); //Reset position.
				tempChild.transform.setRotation(tempChild.transform.getWorldRotation()); //Reset rotation.

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
		return this.localPosition;
	}

	/**
	 * Gets the world position of this transform.
	 * @return A Vector2 holding the world X and Y coordinate of this transform.
	 */
	public Vector2 getWorldPosition(){
		return this.worldPosition;
	}

	/**
	 * Gets the local rotation of this transform.
	 * @return A float which is the rotation.
	 */
	public float getRotation(){
		return this.localRotation;
	}

	/**
	 * Gets the world rotation of this transform.
	 * @return A float which is the rotation.
	 */
	public float getWorldRotation(){
		return this.worldRotation;
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

		//Sets the local position according to the parent's world position.
		Vector2 parentPos = this.parent.transform.getWorldPosition();
		this.localPosition.x = x - parentPos.x;
		this.localPosition.y = y - parentPos.y;
	}

	/**
	 * Sets the world rotation of this transform. This will update the local rotation of the transform.
	 * @param rot A float which is the new world rotation.
	 */
	public void setRotation(float rot){
		this.worldRotation = rot;
		if(this.parent == null){
			this.localRotation = rot; //If the parent is null, assign the local the same as the global.
			return; //If the parent is null, return here.
		}

		this.localRotation = rot - this.parent.transform.getRotation();
		if(this.localRotation >= 360) this.localRotation = this.localRotation - 360;
		if(this.localRotation < 0) this.localRotation = this.localRotation + 360;
	}

	/**
	 * Adds the X and Y value passed in to the world position, effectively moving the Entity.
	 * @param x The X value to move.
	 * @param y The Y value to move.
	 */
	public void translate(float x, float y){
		this.worldPosition.x += x;
		this.worldPosition.y += y;
	}

	@Override
	public void destroy() {
		this.parent = null;
		this.worldPosition = null;
		this.localPosition = null;
		this.owner = null;
		this.children.clear();
		this.children = null;
	}
}
