package com.mygdx.game.helpers;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;
import com.sun.java.accessibility.util.GUIInitializedListener;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class ListHolder {
	private static ArrayList<ArrayList<Entity>> entityList = new ArrayList<>();
	private static ArrayList<IGUI> GUIList = new ArrayList<>();

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

	public static void update(float delta){
		//Loop over each layer.
		for(int i=0;i<entityList.size();i++){
			//For each entity in this layer
			for(int j=0;j<entityList.get(i).size(); j++){
				Entity e = entityList.get(i).get(j);
				//If it is destroyed, remove it, decrement, and continue;
				if(e.isDestroyed()){
					entityList.get(i).remove(j);
					j--;
					continue;
				}

				//Update the Entity
				e.update(delta);
			}
		}
	}

	public static void updateGUI(float delta){
		for (IGUI gui : GUIList) {
			gui.drawGUI(delta);
		}
	}

	/**
	 * Adds a GUI Component that implements IGUI to a list.
	 * @param GUI the IGUI interface component.
	 */
	public static void addGUI(IGUI GUI){
		GUIList.add(GUI);
	}

	/**
	 * Removes a IGUI Component from the list.
	 * @param GUI The IGUI Component to remove.
	 */
	public static void removeInterface(IGUI GUI){
		GUIList.remove(GUI);
	}

	public static ArrayList<IGUI> getGUIList(){
		return GUIList;
	}


}
