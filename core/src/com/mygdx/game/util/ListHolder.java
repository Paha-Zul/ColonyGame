package com.mygdx.game.util;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.ui.UI;
import com.mygdx.game.util.timer.Timer;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class ListHolder {
	private static Array<Entity> newList = new Array<>();
	private static Array<Array<Entity>> entityList = new Array<>();
	private static Array<UI> GUIList = new Array<>();
	private static Array<Timer> timerList = new Array<>();

    private static LinkedList<FloatingText> floatingTexts = new LinkedList<>();

	public static void addEntity(int drawLevel, Entity e){
		//If our list doesn't have enough layers to put something at 'drawLevel', add the layers!
		if(entityList.size <= drawLevel){
			int diff =  drawLevel - entityList.size;
			for(int i=0;i<=diff;i++){
				entityList.add(new Array<>());
			}
		}

		//Add the Entity
		newList.add(e);
	}

	public static void addTimer(Timer timer){
		timerList.add(timer);
	}

	public static void iterate(Functional.Perform perform){
		entityList.forEach((e) -> perform.perform(e));
	}

    /**
     * Updates the main list in the ListHolder.
     * @param delta The time between frames.
     */
	public static void update(float delta){
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

				//Update the Entity
				e.update(delta);
				e.render(delta, ColonyGame.batch);
			}
		}

		//We call start after the list has updated to allow all initial components to be started.
		if(newList.size > 0){
			newList.forEach(entity -> {
				if(entity.active) entityList.get(entity.drawLevel).add(entity);
				entity.update(delta); //Do one update even if the Entity is not active. This will allows initial stuff to be set up.
				entity.start();
			});

			newList.clear();
		}
	}

	public static void clearEntityList(){
		for(Array<Entity> list : entityList)
			for(Entity ent : list)
				ent.setToDestroy();

		entityList = new Array<>();
	}

    /**
     * Updates the GUI elements
     * @param delta The time between frames.
     */
	public static void updateGUI(float delta, SpriteBatch batch){
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
    public static void updateFloatingTexts(float delta, SpriteBatch batch){
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
	public static void updateTimers(float delta){
		for(int i=0;i<timerList.size;i++){
			Timer timer = timerList.get(i);
			if(timer.isOneShot() && timer.isFinished()){
				timerList.removeIndex(i);
				i--;
			}else
				timerList.get(i).update(delta);
		}
	}

    public static void addFloatingText(FloatingText text){
        floatingTexts.add(text);
    }

	/**
	 * Adds a UI Object to the list.
	 * @param ui The UI Object.
	 */
	public static void addGUI(UI ui){
		GUIList.add(ui);
	}

	/**
	 * Removes a UI Component from the list.
	 * @param ui The UI Component to remove.
	 */
	public static void removeGUI(UI ui){
		GUIList.removeValue(ui, false);
	}

	/**
	 * Gets the ArrayList for the GUI stuff.
	 * @return An ArrayList holding GUI elements.
	 */
	public static Array<UI> getGUIList(){
		return GUIList;
	}

	public static Array<Array<Entity>> getEntityList(){
		return entityList;
	}

	/**
	 * Finds an Entity by compName from the game. Don't do this every frame for performance reasons.
	 * @param name The compName that the Entity should have.
	 * @return The first Entity found with 'compName', null otherwise.
	 */
	public static Entity findEntityByName(String name){
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
