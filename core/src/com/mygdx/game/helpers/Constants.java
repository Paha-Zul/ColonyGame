package com.mygdx.game.helpers;

import com.badlogic.gdx.graphics.Color;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class Constants {

	//For the Grid stuff...
	public final static int GRIDACTIVE = 0;
	public final static int GRIDSTATIC = 1;

	//For interactable types...

	//Grid stuff
	public final static int GRID_SQUARESIZE = 100;
	public final static int GRID_WIDTH = 5000;
	public final static int GRID_HEIGHT = 5000;

	//WorldGen stuff


	//Terrain stuff
	public final static int TERRAIN_WATER = 0;
	public final static int TERRAIN_GRASS = 1;

    public final static int VISIBILITY_UNEXPLORED = 0;
    public final static int VISIBILITY_EXPLORED = 1;
    public final static int VISIBILITY_VISIBLE = 2;

    public final static int WORLDGEN_GENERATESPEED = 500;


    //Entity types
    public final static int ENTITY_HUMANOID = 1;
    public final static int ENTITY_BUILDING = 2;
    public final static int ENTITY_ANIMAL = 3;
    public final static int ENTITY_RESOURCE = 4;
    public final static int ENTITY_COLONIST = 5;


    //Colors
    public final static Color COLOR_UNEXPLORED = new Color(Color.BLACK);
    public final static Color COLOR_EXPLORED = new Color(0.3f, 0.3f, 0.3f, 1f);
    public final static Color COLOR_VISIBILE = new Color(Color.WHITE);

}
