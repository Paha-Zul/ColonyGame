package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Animal;
import com.mygdx.game.component.Colony;
import com.mygdx.game.component.Group;
import com.mygdx.game.entity.AnimalEnt;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.ui.UI;
import com.mygdx.game.util.*;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.NotificationManager;
import com.mygdx.game.util.managers.PlayerManager;
import com.mygdx.game.util.worldgeneration.WorldGen;

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
    private TextureRegion[][] map;

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
        //generateLarger();
    }

    public void render(float delta){

        //Generate the trees if it hasn't been done already.
        if(!generatedTrees) {
            generatedTrees = WorldGen.getInstance().generateResources(new Vector2((ColonyGame.worldGrid.getWidth() - 1) * ColonyGame.worldGrid.getSquareSize(),
                    (ColonyGame.worldGrid.getHeight() - 1) * ColonyGame.worldGrid.getSquareSize()), 0, Constants.WORLDGEN_RESOURCEGENERATESPEED);
            if(generatedTrees){
                startLocation.set((ColonyGame.worldGrid.getWidth()/2)*ColonyGame.worldGrid.getSquareSize(), (ColonyGame.worldGrid.getHeight()/2)*ColonyGame.worldGrid.getSquareSize());
                generateStart(startLocation);
            }
        }

        if(PlayerInterface.getInstance().paused) delta = 0;
        else delta *= PlayerInterface.getInstance().gameSpeed;

        //drawMap();
        if(PlayerInterface.getInstance().renderWorld) renderMap();
        updateEntities(delta);
        NotificationManager.update(delta);
    }

    private void generateStart(Vector2 start){
        //Add our colony to an empty entity and create a player using the colony.
        Entity empty = new Entity(new Vector2(0,0), 0, 0);
        ListHolder.addEntity(empty);
        Colony colony = empty.addComponent(new Colony());
        PlayerManager.Player player = PlayerManager.addPlayer("Player", colony);

        this.spawnAnimals();

        NotificationManager.init(player, 1f);
    }

    private void updateEntities(float delta){
        batch.setProjectionMatrix(ColonyGame.camera.combined);
        batch.setColor(Color.WHITE); //Set the color back to white.
        batch.begin();

        ListHolder.update(delta);
        ListHolder.updateFloatingTexts(delta, batch);

        //Update and render events
        EventSystem.notifyGameEvent("update", delta);
        EventSystem.notifyGameEvent("render", delta, batch);

        //Step the Box2D simulation.
        ColonyGame.world.step(1f / 60f, 8, 3);
        batch.end();
    }

    private void spawnAnimals(){
        int wolfPackSpawns = 0;
        int squirrelSpawn = 0;
        boolean spawnBossWolf = false;

        String atlasName = "interactables";

        //Spawns some squirrels
        for(int i=0;i<squirrelSpawn;i++) {
            Vector2 pos = new Vector2(20 + MathUtils.random(grid.getWidth()-40)*grid.getSquareSize(), 20 + MathUtils.random(grid.getHeight()-40)*grid.getSquareSize());
            Entity animal = new AnimalEnt("squirrel", pos, 0, new String[]{"squirrel", atlasName}, 11);
            ListHolder.addEntity(animal);
        }

        //Spawn some angry wolf packs.
        for(int i=0;i<wolfPackSpawns;i++){
            Group group = new Group();
            Vector2 pos = new Vector2(20 + MathUtils.random(grid.getWidth()-40)*grid.getSquareSize(), 20 + MathUtils.random(grid.getHeight()-40)*grid.getSquareSize());
            AnimalEnt wolfLeader = new AnimalEnt("wolf", pos, 0, new String[]{"wolf", atlasName}, 11);
            wolfLeader.getGraphicIdentity().setSprite("wolf", "interactables");
            group.setLeader(wolfLeader);
            ListHolder.addEntity(wolfLeader);

            DataBuilder.JsonAnimal animal = wolfLeader.getComponent(Animal.class).getAnimalRef();
            int amount = (int)(animal.packAmount[0] + Math.random()*(animal.packAmount[1] - animal.packAmount[0]));
            for(int j=0;j<amount; j++){
                Vector2 pos2 = new Vector2(pos.x + MathUtils.random()*1 - 2, pos.y + MathUtils.random()*1 - 2);
                AnimalEnt wolf = new AnimalEnt("wolf", pos2, 0, new String[]{"wolf", atlasName}, 11);
                wolf.getGraphicIdentity().setSprite("wolf", "interactables");
                wolf.addComponent(group);
                group.addEntityToGroup(wolf);
                ListHolder.addEntity(wolf);
            }
        }

        if(spawnBossWolf) {
            //spawn big boss wolf
            Group group = new Group();
            Vector2 pos = new Vector2(20 + MathUtils.random(grid.getWidth() - 40) * grid.getSquareSize(), 20 + MathUtils.random(grid.getHeight() - 40) * grid.getSquareSize());
            DataBuilder.JsonAnimal bossWolfRef = DataManager.getData("bosswolf", DataBuilder.JsonAnimal.class);
            AnimalEnt bossWolf = new AnimalEnt(bossWolfRef, pos, 0, new String[]{bossWolfRef.img, atlasName}, 11);
            bossWolf.getGraphicIdentity().setSprite(bossWolfRef.img, "interactables");
            group.setLeader(bossWolf);
            bossWolf.getTransform().setScale(2f);
            bossWolf.addComponent(group);
            ListHolder.addEntity(bossWolf);

            int amount = (int) (bossWolfRef.packAmount[0] + Math.random() * (bossWolfRef.packAmount[1] - bossWolfRef.packAmount[0]));
            for (int j = 0; j < amount; j++) {
                DataBuilder.JsonAnimal childWolf = DataManager.getData(bossWolfRef.typeInPack[0], DataBuilder.JsonAnimal.class);

                Vector2 pos2 = new Vector2(pos.x + MathUtils.random() * 1 - 2, pos.y + MathUtils.random() * 1 - 2);
                AnimalEnt wolf = new AnimalEnt(childWolf, pos2, 0, new String[]{childWolf.img, atlasName}, 11);
                wolf.getGraphicIdentity().setSprite(childWolf.img, "interactables");
                wolf.addComponent(group);
                group.addEntityToGroup(wolf);
                ListHolder.addEntity(wolf);
            }
        }
    }

    private void generateLarger(){
        int scale = 1; //The scale to divide the sizes by. So if we have scale of 4, everything will be 1/4th smaller.
        //TODO Almost works, but is strangely stretched

        TextureRegion region = new TextureRegion();
        Grid.GridInstance grid = ColonyGame.worldGrid;
        int squareSize = grid.getOriginalSquareSize()/scale; //The grid square size altered by the scale.
        int imageSize = 256/scale; //The total size of the image we want to generate.
        int regionSize = imageSize/squareSize; //The size of the region which is the image size divided by the size of the squares. so 256/32 = 8
        int totalX = grid.getWidth()/regionSize, totalY = grid.getHeight()/regionSize; //The total regions in the X and Y dimension.
        map = new TextureRegion[totalX+1][totalY+1]; //Initialize the map texture array to be the size of the total X and Y + 1.

        int currX = 0, currY = 0; //Some counters.

        TextureAtlas terrainAtlas = ColonyGame.assetManager.get("terrain", TextureAtlas.class); //Get the terrain atlas.
        SpriteBatch batch = new SpriteBatch();
        batch.begin();

        //This loops over the entire map, generating large pixmaps to be made into textures.
        while(currX < 1) {
            FrameBuffer fb = new FrameBuffer(Pixmap.Format.RGBA8888, imageSize, imageSize, false);
            fb.begin();

            int counterX=0, counterY=0;
            //Loops over each terrain tile in this 'region'. The region size is how many pixels for each region there should be. Must pick a number divisible by the tiles.
            for (int ySquare = currY*regionSize; ySquare < (currY+1)*regionSize; ySquare++) {
                for (int xSquare = currX*(regionSize) ; xSquare < (currX+1)*regionSize; xSquare++) {
                    Grid.Node node = grid.getNode(xSquare, ySquare);
                    if(node != null) {
                        Grid.TerrainTile tile = node.getTerrainTile();
                        if (tile != null) {
                            batch.draw(terrainAtlas.findRegion(tile.tileTextureName), counterX * grid.getOriginalSquareSize(), counterY * grid.getOriginalSquareSize(), grid.getOriginalSquareSize(), grid.getOriginalSquareSize());
                            System.out.println("Drawing " + (xSquare) + " " + (ySquare)+" origSq: "+grid.getOriginalSquareSize());
                        }
                    }
                    counterX++;
                }
                counterX = 0;
                counterY++;
            }

            //System.out.println("Out");

            batch.flush();
            //Then retrieve the Pixmap from the buffer.
            Pixmap pm = ScreenUtils.getFrameBufferPixmap(0, 0, imageSize, imageSize);
            map[currX][currY] = new TextureRegion(new Texture(pm));
            map[currX][currY].flip(false, true);

            //tex.getTextureData().prepare();
            FileHandle levelTexture = Gdx.files.local("levelTexture"+currX+"_"+currY+".png");
            PixmapIO.writePNG(levelTexture, map[currX][currY].getTexture().getTextureData().consumePixmap());

            fb.end();
            pm.dispose();
            fb.dispose();

            currX++;
            if(currX>totalX){
                currX = 0;
                currY++;
            }
        }

        batch.end();
    }

    private void drawMap(){
        if(PlayerInterface.getInstance().renderWorld) return;

        batch.setProjectionMatrix(ColonyGame.camera.combined);
        batch.begin();
        double area = 256;
        float size = (float)area;
        for(int y=0;y<map.length;y++){
            for(int x=0;x<map[y].length;x++){
                if(map[x][y] != null)
                    batch.draw(map[x][y], x*size, y*size, size, size);
            }
        }
        batch.end();
    }

    //Renders the map
    private void renderMap(){

        if(ColonyGame.worldGrid == null) return;
        //if(!PlayerInterface.active || !PlayerInterface.getInstance().renderWorld) return;

        batch.setProjectionMatrix(ColonyGame.camera.combined);
        batch.begin();
        int off = 5;

        float squareSize = ColonyGame.worldGrid.getSquareSize();
        int halfWidth = (int)((ColonyGame.camera.viewportWidth*ColonyGame.camera.zoom)/2f);
        int halfHeight = (int)((ColonyGame.camera.viewportHeight*ColonyGame.camera.zoom)/2f);
        int xc = (int)ColonyGame.camera.position.x;
        int yc = (int)ColonyGame.camera.position.y;

        int startX = ((xc - halfWidth)/squareSize) - off >= 0 ? (int)((xc - halfWidth)/squareSize) - off : 0;
        int endX = ((xc + halfWidth)/squareSize) + off < ColonyGame.worldGrid.getWidth() ? (int)((xc + halfWidth)/squareSize) + off : ColonyGame.worldGrid.getWidth()-1;
        int startY = ((yc - halfHeight)/squareSize) - off >= 0 ? (int)((yc - halfHeight)/squareSize) - off : 0;
        int endY = ((yc + halfHeight)/squareSize) + off < ColonyGame.worldGrid.getHeight() ? (int)((yc + halfHeight)/squareSize) + off : ColonyGame.worldGrid.getHeight()-1;

        //Loop over the array
        for(int x=startX;x<=endX;x++) {
            for (int y = startY; y <= endY; y++) {
                Grid.TerrainTile tile = ColonyGame.worldGrid.getNode(x, y).getTerrainTile();
                if(tile == null) continue;
                tile.changeVisibility(ColonyGame.worldGrid.getVisibilityMap()[x][y].getVisibility());
                tile.terrainSprite.draw(batch);
            }
        }

        batch.end();
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
