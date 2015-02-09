package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class Constants {

	//For the Grid stuff...
	public static int GRIDACTIVE = 0;
	public static int GRIDSTATIC = 1;

	//For interactable types...

	//Grid stuff
	public static int GRID_SIZE = 25;

	//WorldGen stuff


	//Terrain stuff
	public static int TERRAIN_WATER = 0;
	public static int TERRAIN_GRASS = 1;

    public static int VISIBILITY_UNEXPLORED = 0;
    public static int VISIBILITY_EXPLORED = 1;
    public static int VISIBILITY_VISIBLE = 2;


    //Entity types
    public static int ENTITY_HUMANOID = 1;
    public static int ENTITY_BUILDING = 2;
    public static int ENTITY_ANIMAL = 3;
    public static int ENTITY_RESOURCE = 4;
    public static int ENTITY_COLONIST = 5;


    //Colors
    public static Color COLOR_UNEXPLORED = new Color(Color.BLACK);
    public static Color COLOR_EXPLORED = new Color(0.3f, 0.3f, 0.3f, 1f);
    public static Color COLOR_VISIBILE = new Color(Color.WHITE);

}
