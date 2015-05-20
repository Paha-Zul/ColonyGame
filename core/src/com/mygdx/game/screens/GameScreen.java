package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Animal;
import com.mygdx.game.component.Colony;
import com.mygdx.game.component.Group;
import com.mygdx.game.entity.AnimalEnt;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.*;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.helpers.managers.NotificationManager;
import com.mygdx.game.helpers.managers.PlayerManager;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.ui.UI;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class GameScreen implements Screen{
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

    public GameScreen(final ColonyGame game){
        //Server.start(1337); //Start the server
        this.grid = ColonyGame.worldGrid;

        //Store spritebatch and shaperenderer.
        this.batch = ColonyGame.batch;
        this.shapeRenderer = ColonyGame.renderer;
        this.game = game;
    }

    @Override
    public void show() {

    }

    public void render(float delta){

        if(!generatedTrees) {
            generatedTrees = WorldGen.getInstance().generateResources(new Vector2((ColonyGame.worldGrid.getWidth() - 1) * ColonyGame.worldGrid.getSquareSize(), (ColonyGame.worldGrid.getHeight() - 1) * ColonyGame.worldGrid.getSquareSize()), 0, Constants.WORLDGEN_RESOURCEGENERATESPEED);
            if(generatedTrees){
                startLocation.set((ColonyGame.worldGrid.getWidth()/2)*ColonyGame.worldGrid.getSquareSize(), (ColonyGame.worldGrid.getHeight()/2)*ColonyGame.worldGrid.getSquareSize());
                generateStart(startLocation);
            }
        }

        NotificationManager.update(delta);
    }

    private void generateStart(Vector2 start){
        //Add our colony to an empty entity.
        Entity empty = new Entity(new Vector2(0,0), 0, 0);
        Colony colony = empty.addComponent(new Colony());
        PlayerManager.Player player = PlayerManager.addPlayer("Player", colony);

        this.spawnAnimals();

        NotificationManager.init(player, 1f);
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
            Vector2 pos = new Vector2(20 + MathUtils.random(grid.getWidth()-40)*grid.getSquareSize(), 20 + MathUtils.random(grid.getHeight()-40)*grid.getSquareSize());
            AnimalEnt wolfLeader = new AnimalEnt("wolf", pos, 0, atlas.findRegion("wolf"), 11);
            group.setLeader(wolfLeader);

            DataBuilder.JsonAnimal animal = wolfLeader.getComponent(Animal.class).getAnimalRef();
            int amount = (int)(animal.packAmount[0] + Math.random()*(animal.packAmount[1] - animal.packAmount[0]));
            for(int j=0;j<amount; j++){
                Vector2 pos2 = new Vector2(pos.x + MathUtils.random()*1 - 2, pos.y + MathUtils.random()*1 - 2);
                AnimalEnt wolf = new AnimalEnt("wolf", pos2, 0, atlas.findRegion("wolf"), 11);
                wolf.addComponent(group);
                group.addEntityToGroup(wolf);
            }
        }

        //spawn big boss wolf
        Group group = new Group();
        Vector2 pos = new Vector2(20 + MathUtils.random(grid.getWidth()-40)*grid.getSquareSize(), 20 + MathUtils.random(grid.getHeight()-40)*grid.getSquareSize());
        DataBuilder.JsonAnimal bossWolfRef = DataManager.getData("bosswolf", DataBuilder.JsonAnimal.class);
        AnimalEnt bossWolf = new AnimalEnt(bossWolfRef, pos, 0, atlas.findRegion(bossWolfRef.img), 11);
        group.setLeader(bossWolf);
        bossWolf.transform.setScale(2f);
        bossWolf.addComponent(group);

        int amount = (int)(bossWolfRef.packAmount[0] + Math.random()*(bossWolfRef.packAmount[1] - bossWolfRef.packAmount[0]));
        for(int j=0;j<amount; j++){
            DataBuilder.JsonAnimal childWolf = DataManager.getData(bossWolfRef.typeInPack[0], DataBuilder.JsonAnimal.class);

            Vector2 pos2 = new Vector2(pos.x + MathUtils.random()*1 - 2, pos.y + MathUtils.random()*1 - 2);
            AnimalEnt wolf = new AnimalEnt(childWolf, pos2, 0, atlas.findRegion(childWolf.img), 11);
            wolf.addComponent(group);
            group.addEntityToGroup(wolf);
        }
    }

    @Override
    public void resize(int width, int height) {
        Vector3 pos = new Vector3(ColonyGame.camera.position);
        Gdx.graphics.setDisplayMode(width, height, false);
        ColonyGame.camera.setToOrtho(false, GH.toMeters(width), GH.toMeters(height));
        ColonyGame.UICamera.setToOrtho(false, width, height);
        ColonyGame.camera.position.set(pos);

        //Resizes all the GUI elements of the game (hopefully!)
        Array<UI> list = ListHolder.getGUIList();
        for(int i=0;i< list.size;i++)
            list.get(i).resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
