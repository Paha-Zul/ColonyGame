package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.ui.UI;
import com.sun.java.accessibility.util.GUIInitializedListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class ListHolder {
	private static ArrayList<ArrayList<Entity>> entityList = new ArrayList<>();
	private static ArrayList<UI> GUIList = new ArrayList<>();

    private static LinkedList<FloatingText> floatingTexts = new LinkedList<>();

	public static void addEntity(int drawLevel, Entity e){
		//If our list doesn't have enough layers to put something at 'drawLevel', add the layers!
		if(entityList.size() <= drawLevel){
			int diff =  drawLevel - entityList.size();
			for(int i=0;i<=diff;i++){
				entityList.add(new ArrayList<Entity>());
			}
		}

		//Add the Entity
		ListHolder.entityList.get(drawLevel).add(e);
	}

	public static void iterate(Functional.Perform perform){
		entityList.forEach((e)->perform.perform(e));
	}

    /**
     * Updates the main list in the ListHolder.
     * @param delta The time between frames.
     */
	public static void update(float delta){
		//Loop over each layer.
		for(int i=0;i<entityList.size();i++){
			//For each entity in this layer
			for(int j=0;j<entityList.get(i).size(); j++){
				Entity e = entityList.get(i).get(j);
				//If it is set to be destroyed, destroy it, remove it, decrement, continue.
				if(e.isSetToBeDestroyed()){
					entityList.get(i).get(j).destroy();
					entityList.get(i).remove(j);
					j--;
					continue;
				//If it is already destroyed (an immediate destroy call from somewhere else), remove it, decrement, continue.
				}else if(e.isDestroyed()){
					entityList.get(i).remove(j);
					j--;
					continue;
				}

				//Update the Entity
				e.update(delta);
			}
		}
	}

    /**
     * Updates the GUI elements
     * @param delta The time between frames.
     */
	public static void updateGUI(float delta){
		for (int i=0;i<GUIList.size();i++) {
			UI gui = GUIList.get(i);
			if(gui.done){
				gui.destroy();
				GUIList.remove(i);
				i--;
				continue;
			}

			gui.drawGUI(delta);
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
