package com.mygdx.game.server;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Animal;
import com.mygdx.game.component.Colony;
import com.mygdx.game.entity.AnimalEnt;
import com.mygdx.game.entity.ColonyEntity;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.objects.Group;
import com.mygdx.game.ui.PlayerInterface;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Bbent_000 on 11/23/2014.
 */
public class ServerPlayer {
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private ColonyGame game;
	private Grid.GridInstance grid;
    private boolean paused = false;

	public static String[] firstNames = {"Bobby","Sally","Jimmy","Bradley","Willy","Tommy","Brian",
            "Doug","Ben","Jacob","Sammy","Jason","David","Sarah","Betty","Tom","James"};

    public static String[] lastNames = {"Poopers"};

    private boolean generatedTrees = false;
    private Vector2 startLocation = new Vector2();

	public ServerPlayer(SpriteBatch batch, ShapeRenderer renderer, ColonyGame game){
		//Server.start(1337); //Start the server
		this.grid = ColonyGame.worldGrid;

		//Store spritebatch and shaperenderer.
		this.batch = batch;
		this.shapeRenderer = renderer;
		this.game = game;

		initPlayer();
    }

	public void render(float delta){

        if(!generatedTrees) {
            generatedTrees = WorldGen.getInstance().generateResources(new Vector2((ColonyGame.worldGrid.getWidth() - 1) * ColonyGame.worldGrid.getSquareSize(), (ColonyGame.worldGrid.getHeight() - 1) * ColonyGame.worldGrid.getSquareSize()), 0, Constants.WORLDGEN_RESOURCEGENERATESPEED);
            if(generatedTrees){
                startLocation.set((ColonyGame.worldGrid.getWidth()/2)*ColonyGame.worldGrid.getSquareSize(), (ColonyGame.worldGrid.getHeight()/2)*ColonyGame.worldGrid.getSquareSize());
                generateStart(startLocation);
            }
        }
	}

    public boolean getPaused(){
        return this.paused;
    }

    public void setPaused(boolean paused){
        this.paused = paused;
    }

	private void generateStart(Vector2 start){
        //Find a suitable place to spawn our Colony
        block: {
            int radius = 0;
            boolean placed = false;
            Grid.GridInstance grid = ColonyGame.worldGrid;
            int[] index = grid.getIndex(start);

            while (!placed) {
                int startX = index[0] - radius;
                int endX = index[0] + radius;
                int startY = index[1] - radius;
                int endY = index[1] + radius;

                //Loop over each tile.
                for (int x = startX; x <= endX && !placed; x++) {
                    for (int y = startY; y <= endY && !placed; y++) {
                        if (x != startX && x != endX && y != startY && y != endY)
                            continue;

                        if(startX < 0 || endX > grid.getWidth() || startY < 0 || endY > grid.getHeight())
                            GH.writeErrorMessage("Couldn't find a place to spawn the base!");

                        //For each tile, we want to check if there is a 4x4 surrounding area.
                        int innerStartX = x - 5;
                        int innerEndX = x + 5;
                        int innerStartY = y - 5;
                        int innerEndY = y + 5;

                        //If the node is null (outside the bounds), continue.
                        if (grid.getNode(innerStartX, innerStartY) == null || grid.getNode(innerEndX, innerEndY) == null)
                            continue;

                        placed = true;

                        //Check over the inner area. If all tiles are not set to avoid, we have a place we can spawn our Colony.
                        for (int innerX = innerStartX; innerX <= innerEndX && placed; innerX++) {
                            for (int innerY = innerStartY; innerY <= innerEndY && placed; innerY++) {
                                Grid.TerrainTile tile = grid.getNode(innerX, innerY).getTerrainTile();
                                if (tile.avoid)//If there is a single tile set to avoid, break!
                                    placed = false;
                            }
                        }

                        //If passed, calculate the start vector.
                        if(placed)
                            start.set(x * grid.getSquareSize(), y * grid.getSquareSize());
                    }
                }
                radius++;
            }
        }

        //Spawns the Colony Entity and centers the camera on it.
        ColonyEntity colonyEnt = new ColonyEntity(start, 0, new TextureRegion(ColonyGame.assetManager.get("Colony", Texture.class)), 10);
        Colony colony = colonyEnt.getComponent(Colony.class);
        ColonyGame.camera.position.set(colonyEnt.transform.getPosition().x, colonyEnt.transform.getPosition().y, 0);

        this.spawnAnimals();

        //Destroys resources in an area around the Colony Entity.
		int radius = 8;
        Predicate<Grid.Node> notWaterNode = node -> !node.getTerrainTile().category.equals("water");

        Consumer<Entity> treeConsumer = ent -> {
            if(ent.hasTag(Constants.ENTITY_RESOURCE)) ent.setToDestroy();
        };

        //Perform the things.
		//this.grid.perform(destroyNearbyResources);
        this.grid.performOnEntityInRadius(treeConsumer, notWaterNode, radius, grid.getIndex(colonyEnt.transform.getPosition()));
	}

    private void spawnAnimals(){

        TextureAtlas atlas = ColonyGame.assetManager.get("interactables", TextureAtlas.class);
        //Spawns some squirrels
        for(int i=0;i<100;i++) {
            Vector2 pos = new Vector2(MathUtils.random(grid.getWidth())*grid.getSquareSize(), MathUtils.random(grid.getHeight())*grid.getSquareSize());
            new AnimalEnt("squirrel", pos, 0, atlas.findRegion("squirrel"), 11);
        }

        //Spawn some angry wolf packs.
        for(int i=0;i<5;i++){
            Group group = new Group();
            Vector2 pos = new Vector2(10 + MathUtils.random(grid.getWidth()-10)*grid.getSquareSize(), 10 + MathUtils.random(grid.getHeight()-10)*grid.getSquareSize());
            AnimalEnt wolfLeader = new AnimalEnt("wolf", pos, 0, atlas.findRegion("wolf"), 11);
            group.setLeader(wolfLeader);

            DataBuilder.JsonAnimal animal = wolfLeader.getComponent(Animal.class).getAnimalRef();
            int amount = (int)(animal.packAmount[0] + Math.random()*(animal.packAmount[1] - animal.packAmount[0]));
            for(int j=0;j<amount; j++){
                Vector2 pos2 = new Vector2(pos.x + MathUtils.random()*5 - 10, pos.y + MathUtils.random()*5 - 10);
                AnimalEnt wolf = new AnimalEnt("wolf", pos2, 0, atlas.findRegion("wolf"), 11);
                wolf.addComponent(group);
                group.addEntityToGroup(wolf);
            }
        }

        //spawn big boss wolf
        Group group = new Group();
        Vector2 pos = new Vector2(10 + MathUtils.random(grid.getWidth()-10)*grid.getSquareSize(), 20 + MathUtils.random(grid.getHeight()-20)*grid.getSquareSize());
        DataBuilder.JsonAnimal bossWolfRef = DataManager.getData("bosswolf", DataBuilder.JsonAnimal.class);
        AnimalEnt bossWolf = new AnimalEnt(bossWolfRef, pos, 0, atlas.findRegion(bossWolfRef.img), 11);
        group.setLeader(bossWolf);
        bossWolf.transform.setScale(2f);

        int amount = (int)(bossWolfRef.packAmount[0] + Math.random()*(bossWolfRef.packAmount[1] - bossWolfRef.packAmount[0]));
        for(int j=0;j<amount; j++){
            DataBuilder.JsonAnimal childWolf = DataManager.getData(bossWolfRef.typeInPack[0], DataBuilder.JsonAnimal.class);

            Vector2 pos2 = new Vector2(pos.x + MathUtils.random()*5 - 10, pos.y + MathUtils.random()*5 - 10);
            AnimalEnt wolf = new AnimalEnt(childWolf, pos2, 0, atlas.findRegion(childWolf.img), 11);
            wolf.addComponent(group);
            group.addEntityToGroup(wolf);
        }
    }

	private void initPlayer(){
		new PlayerInterface(ColonyGame.batch, this.game, this, ColonyGame.world);
	}
}
