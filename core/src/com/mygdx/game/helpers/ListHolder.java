package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.ui.UI;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class ListHolder {
	private static ArrayList<Entity> newList = new ArrayList<>();
	private static ArrayList<ArrayList<Entity>> entityList = new ArrayList<>();
	private static ArrayList<UI> GUIList = new ArrayList<>();
	private static ArrayList<Timer> timerList = new ArrayList<>();

    private static LinkedList<FloatingText> floatingTexts = new LinkedList<>();

	public static void addEntity(int drawLevel, Entity e){
		//If our list doesn't have enough layers to put something at 'drawLevel', add the layers!
		if(entityList.size() <= drawLevel){
			int diff =  drawLevel - entityList.size();
			for(int i=0;i<=diff;i++){
				entityList.add(new ArrayList<>());
			}
		}

		//Add the Entity
		newList.add(e);
		entityList.get(drawLevel).add(e);
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
		for (ArrayList<Entity> anEntityList : entityList) {
			//For each entity in this layer
			for (int j = 0; j < anEntityList.size(); j++) {
				Entity e = anEntityList.get(j);
				//If it is set to be destroyed, destroy it, remove it, decrement, continue.
				if (e.isSetToBeDestroyed()) {
					anEntityList.get(j).destroy();
					anEntityList.remove(j);
					j--;
					continue;
					//If it is already destroyed (an immediate destroy call from somewhere else), remove it, decrement, continue.
				} else if (e.isDestroyed()) {
					anEntityList.remove(j);
					j--;
					continue;
				}

				//Update the Entity
				e.update(delta);
				e.render(delta, ColonyGame.batch);
			}
		}

		//We call init after the list has updated to allow all initial components to be started.
		if(newList.size() > 0){
			newList.forEach(Entity::init);
			newList.clear();
		}
	}

    /**
     * Updates the GUI elements
     * @param delta The time between frames.
     */
	public static void updateGUI(float delta, SpriteBatch batch){
		for (int i=0;i<GUIList.size();i++) {
			UI gui = GUIList.get(i);
			if(gui.isDestroyed()){
				GUIList.remove(i);
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
		for(int i=0;i<timerList.size();i++){
			Timer timer = timerList.get(i);
			if(timer.isOneShot() && timer.isFinished()){
				timerList.remove(i);
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
		GUIList.remove(ui);
	}

	/**
	 * Gets the ArrayList for the GUI stuff.
	 * @return An ArrayList holding GUI elements.
	 */
	public static ArrayList<UI> getGUIList(){
		return GUIList;
	}

	/**
	 * Finds an Entity by name from the game. Don't do this every frame for performance reasons.
	 * @param name The name that the Entity should have.
	 * @return The first Entity found with 'name', null otherwise.
	 */
	public static Entity findEntityByName(String name){
		for(int i=0; i<entityList.size(); i++){
			for(int j=0; j<entityList.get(i).size(); j++){
				Entity ent = entityList.get(i).get(j);
				if(ent.name == name)
					return ent;
			}
		}

		return null;
	}

}
