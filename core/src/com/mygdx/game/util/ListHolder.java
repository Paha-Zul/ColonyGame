package com.mygdx.game.util;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.ISaveable;
import com.mygdx.game.ui.UI;
import com.mygdx.game.util.timer.Timer;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class ListHolder implements ISaveable{
	private Array<Entity> newList;
	private Array<Array<Entity>> entityList;
	private Array<UI> GUIList;
	private Array<Timer> timerList;
	private Array<ISaveable> saveableList;

    private  LinkedList<FloatingText> floatingTexts;

	@Override
	public void addedLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

	}

	@Override
	public void save() {

	}

	@Override
	public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
		this.newList = new Array<>();
		this.entityList = new Array<>();
		this.GUIList = new Array<>();
		this.timerList = new Array<>();
		this.saveableList = new Array<>();
		this.floatingTexts = new LinkedList<>();
	}

	@Override
	public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {

	}
	
	public  void init(){
		this.initLoad(null, null);
	}

	/**
	 * Adds an Entity to the global list.
	 * @param e The Entity to add.
	 */
	public void addEntity(Entity e){
		//If our list doesn't have enough layers to put something at 'drawLevel', add the layers!
		if(entityList.size <= e.drawLevel){
			int diff =  e.drawLevel - entityList.size;
			for(int i=0;i<=diff;i++){
				entityList.add(new Array<>());
			}
		}

		//Add the Entity
		newList.add(e);
	}

	/**
	 * Adds a timer to the global list.
	 * @param timer The Timer to add.
	 */
	public void addTimer(Timer timer){
		timerList.add(timer);
	}

	/**
	 * Destroys all Entities in the world.
	 */
	public void clearEntityList(){
		for(Array<Entity> list : entityList)
			for(Entity ent : list) {
				ent.destroy(ent);
			}

		//We do this to allow all the Entities to be removed. Since every Entity is being removed, no Entity will do
		//a logic tick.
		this.update(0f);
		entityList = new Array<>();
	}

    /**
     * Updates the main list in the ColonyGame.instance.listHolder.
     * @param delta The time between frames.
     */
	public void update(float delta){
		//Loop over each layer.
		for (Array<Entity> anEntityList : entityList) {
			//For each entity in this layer
			for (int j = 0; j < anEntityList.size; j++) {
				Entity e = anEntityList.get(j);
				//If it is set to be destroyed, destroy it, remove it, decrement, continue.
				if (e.isSetToBeDestroyed()) {
					anEntityList.get(j).destroy(e);
					anEntityList.removeIndex(j);
					j--;
					continue;
					//If it is already destroyed (an immediate destroy call from somewhere else), remove it, decrement, continue.
				} else if (e.isDestroyed()) {
					anEntityList.removeIndex(j);
					j--;
					continue;
				}

				if(e.active) {
					//Update the Entity
					e.update(delta);
					e.render(delta, ColonyGame.instance.batch);
				}
			}
		}

        //TODO Uh... I changed something here so that might break stuff. I think if was related to loading/saving...
		//We call start after the list has updated to allow all initial components to be started.
		if(newList.size > 0){
			newList.forEach(entity -> {
				if(entity.active) entityList.get(entity.drawLevel).add(entity);
				entity.start();
			});

			newList.clear();
		}
	}

    /**
     * Updates the GUI elements
     * @param delta The time between frames.
     */
	public void updateGUI(float delta, SpriteBatch batch){
		for (int i=0;i<GUIList.size;i++) {
			UI gui = GUIList.get(i);
			if(gui.isDestroyed()){
				GUIList.removeIndex(i);
				i--;
			}else
				gui.render(delta, batch);
		}
	}

    /**
     * Updates the list of FloatingTexts
     * @param delta The time between frames.
     * @param batch The SpriteBatch to draw with.
     */
    public void updateFloatingTexts(float delta, SpriteBatch batch){
        ListIterator<FloatingText> iter = floatingTexts.listIterator();
        while(iter.hasNext()){
            FloatingText text = iter.next();
            if(text.isDestroyed()){
                iter.remove();
            }else {
                text.update(delta);
                text.render(delta, batch);
            }
        }
    }

	/**
	 * Updates the timer list.
	 * @param delta The delta time between frames.
	 */
	public void updateTimers(float delta){
		for(int i=0;i<timerList.size;i++){
			Timer timer = timerList.get(i);
			if(timer.isOneShot() && timer.isFinished()){
				timerList.removeIndex(i);
				i--;
			}else
				timerList.get(i).update(delta);
		}
	}

	/**
	 * Adds a floating text to the global list.
	 * @param text The FloatingText to add.
	 */
    public void addFloatingText(FloatingText text){
        floatingTexts.add(text);
    }

	/**
	 * Adds a UI Object to the list.
	 * @param ui The UI Object.
	 */
	public void addGUI(UI ui){
		GUIList.add(ui);
	}

	/**
	 * Adds a ISaveable to the global list.
	 * @param saveable The ISaveable to add.
	 */
	public void addSaveable(ISaveable saveable){
		this.saveableList.add(saveable);
	}

	/**
	 * @return A List of ISaveables.
	 */
	public Array<ISaveable> getSaveableList(){
		return this.saveableList;
	}

	/**
	 * Removes a UI Component from the list.
	 * @param ui The UI Component to remove.
	 */
	public void removeGUI(UI ui){
		GUIList.removeValue(ui, false);
	}

	/**
	 * Gets the ArrayList for the GUI stuff.
	 * @return An ArrayList holding GUI elements.
	 */
	public Array<UI> getGUIList(){
		return GUIList;
	}

	public Array<Array<Entity>> getEntityList(){
		return entityList;
	}

	/**
	 * Finds an Entity by compName from the game. Don't do this every frame for performance reasons.
	 * @param name The compName that the Entity should have.
	 * @return The first Entity found with 'compName', null otherwise.
	 */
	public Entity findEntityByName(String name){
		for(int i=0; i<entityList.size; i++){
			for(int j=0; j<entityList.get(i).size; j++){
				Entity ent = entityList.get(i).get(j);
				if(ent.name == name)
					return ent;
			}
		}

		return null;
	}

}
