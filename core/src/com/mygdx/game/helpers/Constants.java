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
	public static int GRID_SQUARESIZE;
	public static int GRID_WIDTH;
	public static int GRID_HEIGHT;

    //CAMERA
    public final static float SCALE = 30;

	//WorldGen stuff
    public static int WORLDGEN_GENERATESPEED = 500;
    public static int WORLDGEN_RESOURCEGENERATESPEED = 500;

    //Terrain stuff
	public final static int TERRAIN_WATER = 0;
	public final static int TERRAIN_GRASS = 1;

    public final static int VISIBILITY_UNEXPLORED = 0;
    public final static int VISIBILITY_EXPLORED = 1;
    public final static int VISIBILITY_VISIBLE = 2;

    //Entity types
    public final static int ENTITY_HUMANOID = 1;
    public final static int ENTITY_BUILDING = 2;
    public final static int ENTITY_ANIMAL = 3;
    public final static int ENTITY_RESOURCE = 4;
    public final static int ENTITY_COLONIST = 5;
    public final static int ENTITY_PROJECTILE = 6;


    //Colors
    public final static Color COLOR_UNEXPLORED = new Color(Color.BLACK);
    public final static Color COLOR_EXPLORED = new Color(0.3f, 0.3f, 0.3f, 1f);
    public final static Color COLOR_VISIBILE = new Color(Color.WHITE);

}
