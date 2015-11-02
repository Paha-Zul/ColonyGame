package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.mygdx.game.component.*;
import com.mygdx.game.entity.AnimalEnt;
import com.mygdx.game.entity.BuildingEntity;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.ui.UI;
import com.mygdx.game.util.*;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.GameEventManager;
import com.mygdx.game.util.managers.MessageEventSystem;
import com.mygdx.game.util.managers.PlayerManager;
import com.mygdx.game.util.worldgeneration.WorldGen;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class GameScreen implements Screen{
    public static String[] firstNames = {"Bobby","Sally","Jimmy","Bradley","Willy","Tommy","Brian",
            "Doug","Ben","Jacob","Sammy","Jason","David","Sarah","Betty","Tom","James"};
    public static String[] lastNames = {"Poopers"};
    public static boolean generatedNewGameStuff = false; //We only want this to change once per game life.
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private ColonyGame game;
    private Grid.GridInstance grid;
    private boolean paused = false;
    private Vector2 startLocation = new Vector2();
    private TextureRegion[][] map;

    public GameScreen(final ColonyGame game){
        //Server.start(1337); //Start the server
        this.grid = ColonyGame.instance.worldGrid;

        //Store spritebatch and shaperenderer.
        this.batch = ColonyGame.instance.batch;
        this.shapeRenderer = ColonyGame.instance.renderer;
        this.game = game;

        //Used for testing an event.
        PlayerInterface.getInstance().addKeyEvent(Input.Keys.E,
                () -> PlayerInterface.getInstance().newPlayerEvent(GameEventManager.triggerGameEventByComponents("crazylumberjacks",
                        ColonyGame.instance.playerManager.getPlayer("Player").colony.getOwnedListFromColony(Colonist.class))));
    }

    @Override
    public void show() {
        //generateLarger();
    }

    public void render(float delta){

        //Generate stuff for a new game if not done already.
        if(!generatedNewGameStuff) {
            generatedNewGameStuff = WorldGen.getInstance().generateResources(new Vector2((ColonyGame.instance.worldGrid.getWidth() - 1) * ColonyGame.instance.worldGrid.getSquareSize(),
                    (ColonyGame.instance.worldGrid.getHeight() - 1) * ColonyGame.instance.worldGrid.getSquareSize()), 0, Constants.WORLDGEN_RESOURCEGENERATESPEED);

            if(generatedNewGameStuff){
                this.startLocation.set((ColonyGame.instance.worldGrid.getWidth()/2)*ColonyGame.instance.worldGrid.getSquareSize(), (ColonyGame.instance.worldGrid.getHeight()/2)*ColonyGame.instance.worldGrid.getSquareSize());
                this.generateStart(this.startLocation);
                this.spawnAnimals();
            }
        }

        if(PlayerInterface.getInstance().paused) delta = 0;
        else delta *= PlayerInterface.getInstance().gameSpeed;

        //drawMap();
        //If we should render the world, render it! (for debugging mostly)
        if(PlayerInterface.getInstance().renderWorld) this.renderMap();
        //Updates all the entities.
        this.updateEntities(delta);
        //Updates the Notification manager.
        ColonyGame.instance.notificationManager.update(delta);
        //Updates the pathfinder.
        Pathfinder.GetInstance().update(ColonyGame.instance.currTick);
    }

    private void generateStart(Vector2 start){
        int colonistSpawn = 10;

        //Add our colony to an empty entity and create a player using the colony.
        Entity empty = new Entity(new Vector2(0,0), 0, 0);
        ColonyGame.instance.listHolder.addEntity(empty);
        Colony colony = empty.addComponent(new Colony());
        PlayerManager.Player player = ColonyGame.instance.playerManager.addPlayer("Player", colony);

        ColonyGame.instance.notificationManager.init();

        //Find a suitable place to spawn our Colony
        int radius = 0, areaToSearch = 5;
        boolean placed = false;
        Grid.GridInstance grid = ColonyGame.instance.worldGrid;
        int[] index = grid.getIndex(start);

        /**
         * For now, starts in the middle of the map. For each loop, search an area ('areaToSearch') that is suitable. This will check an area (ex: 5x5) to make sure
         * there are no obstacles or terrain problems. If the area is suitable, the building is placed. Otherwise, we increase the radius and keep searching.
         */
        while (!placed) {
            int startX = index[0] - radius;
            int endX = index[0] + radius;
            int startY = index[1] - radius;
            int endY = index[1] + radius;

            //Loop over each tile.
            for (int x = startX; x <= endX && !placed; x++) {
                for (int y = startY; y <= endY && !placed; y++) {

                    //If we're not on the edge, continue. We don't want to search the inner areas as we go.
                    if (x != startX && x != endX && y != startY && y != endY)
                        continue;

                    if(startX < 0 || endX > grid.getWidth() || startY < 0 || endY > grid.getHeight())
                        GH.writeErrorMessage("Couldn't find a place to spawn the base!");

                    //For each tile, we want to check if there is a 4x4 surrounding area.
                    int innerStartX = x - areaToSearch;
                    int innerEndX = x + areaToSearch;
                    int innerStartY = y - areaToSearch;
                    int innerEndY = y + areaToSearch;

                    //If the node is null (outside the bounds), continue.
                    if (grid.getNode(innerStartX, innerStartY) == null || grid.getNode(innerEndX, innerEndY) == null)
                        continue;

                    placed = true;

                    //Check over the inner area. If all tiles are not set to avoid, we have a place we can spawn our Colony.
                    for (int innerX = innerStartX; innerX <= innerEndX && placed; innerX++) {
                        for (int innerY = innerStartY; innerY <= innerEndY && placed; innerY++) {
                            Grid.TerrainTile tile = grid.getNode(innerX, innerY).getTerrainTile();
                            if (tile.tileRef.avoid)//If there is a single tile set to avoid, break!
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

        //Spawns the Colony Entity and centers the camera on it.
        BuildingEntity colonyEnt = new BuildingEntity(start, 0, "colony_building", 10);
        ColonyGame.instance.listHolder.addEntity(colonyEnt);
        ColonyGame.instance.camera.position.set(colonyEnt.getTransform().getPosition().x, colonyEnt.getTransform().getPosition().y, 0);
        Building colonyBuilding = colonyEnt.getComponent(Building.class);
        colony.addOwnedToColony(colonyBuilding);
        colonyEnt.getComponent(Constructable.class).setComplete();

        //Spawns the Equipment building.
//        BuildingEntity equipEnt = new BuildingEntity(new Vector2(start.x - 5, start.y - 5), 0, "workshop", 10);
//        equipEnt.getTags().addTag("constructing");
//        ColonyGame.instance.listHolder.addEntity(equipEnt);
//        Building equipBuilding = equipEnt.getComponent(Building.class);
//        colony.addOwnedToColony(equipBuilding);
//        equipBuilding.getComponent(Constructable.class).setComplete();

        //Destroys resources in an area around the Colony Entity.
        radius = 8;
        Predicate<Grid.Node> notWaterNode = node -> !node.getTerrainTile().tileRef.category.equals("water");

        //A consumer function to use. If the entity is a tree, destroy it!
        Consumer<Entity> treeConsumer = ent -> {
            if(ent.getTags().hasTag("resource")) ent.setToDestroy();
        };

        //Perform the things.
        grid.performOnEntityInRadius(treeConsumer, notWaterNode, radius, grid.getIndex(colonyEnt.getTransform().getPosition()));

        //Make some colonists!
        for(int i=0;i<colonistSpawn;i++) {
            Entity c = colony.makeColonist(colonyEnt.getTransform().getPosition(), GH.toMeters(200), "colonist");
            Colonist col = c.getComponent(Colonist.class);
            col.setName(GameScreen.firstNames[MathUtils.random(GameScreen.firstNames.length - 1)], GameScreen.lastNames[MathUtils.random(GameScreen.lastNames.length - 1)]);
            colony.addColonist(col);
            ColonyGame.instance.listHolder.addEntity(c);
        }
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
            ColonyGame.instance.listHolder.addEntity(animal);
        }

        //Spawn some angry wolf packs.
        for(int i=0;i<wolfPackSpawns;i++){
            Group group = new Group();
            Vector2 pos = new Vector2(20 + MathUtils.random(grid.getWidth()-40)*grid.getSquareSize(), 20 + MathUtils.random(grid.getHeight()-40)*grid.getSquareSize());
            AnimalEnt wolfLeader = new AnimalEnt("wolf", pos, 0, new String[]{"wolf", atlasName}, 11);
            wolfLeader.getGraphicIdentity().setSprite("wolf", "interactables");
            group.setLeader(wolfLeader);
            ColonyGame.instance.listHolder.addEntity(wolfLeader);

            DataBuilder.JsonAnimal animal = wolfLeader.getComponent(Animal.class).getAnimalRef();
            int amount = (int)(animal.packAmount[0] + Math.random()*(animal.packAmount[1] - animal.packAmount[0]));
            for(int j=0;j<amount; j++){
                Vector2 pos2 = new Vector2(pos.x + MathUtils.random()*1 - 2, pos.y + MathUtils.random()*1 - 2);
                AnimalEnt wolf = new AnimalEnt("wolf", pos2, 0, new String[]{"wolf", atlasName}, 11);
                wolf.getGraphicIdentity().setSprite("wolf", "interactables");
                wolf.addComponent(group);
                group.addEntityToGroup(wolf);
                ColonyGame.instance.listHolder.addEntity(wolf);
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
            ColonyGame.instance.listHolder.addEntity(bossWolf);

            int amount = (int) (bossWolfRef.packAmount[0] + Math.random() * (bossWolfRef.packAmount[1] - bossWolfRef.packAmount[0]));
            for (int j = 0; j < amount; j++) {
                DataBuilder.JsonAnimal childWolf = DataManager.getData(bossWolfRef.typeInPack[0], DataBuilder.JsonAnimal.class);

                Vector2 pos2 = new Vector2(pos.x + MathUtils.random() * 1 - 2, pos.y + MathUtils.random() * 1 - 2);
                AnimalEnt wolf = new AnimalEnt(childWolf, pos2, 0, new String[]{childWolf.img, atlasName}, 11);
                wolf.getGraphicIdentity().setSprite(childWolf.img, "interactables");
                wolf.addComponent(group);
                group.addEntityToGroup(wolf);
                ColonyGame.instance.listHolder.addEntity(wolf);
            }
        }
    }

    //Renders the map
    private void renderMap(){

        if(ColonyGame.instance.worldGrid == null) return;
        //if(!PlayerInterface.active || !PlayerInterface.getInstance().renderWorld) return;

        batch.setProjectionMatrix(ColonyGame.instance.camera.combined);
        batch.begin();
        int off = 5;

        float squareSize = ColonyGame.instance.worldGrid.getSquareSize();
        int halfWidth = (int)((ColonyGame.instance.camera.viewportWidth*ColonyGame.instance.camera.zoom)/2f);
        int halfHeight = (int)((ColonyGame.instance.camera.viewportHeight*ColonyGame.instance.camera.zoom)/2f);
        int xc = (int)ColonyGame.instance.camera.position.x;
        int yc = (int)ColonyGame.instance.camera.position.y;

        int startX = ((xc - halfWidth)/squareSize) - off >= 0 ? (int)((xc - halfWidth)/squareSize) - off : 0;
        int endX = ((xc + halfWidth)/squareSize) + off < ColonyGame.instance.worldGrid.getWidth() ? (int)((xc + halfWidth)/squareSize) + off : ColonyGame.instance.worldGrid.getWidth()-1;
        int startY = ((yc - halfHeight)/squareSize) - off >= 0 ? (int)((yc - halfHeight)/squareSize) - off : 0;
        int endY = ((yc + halfHeight)/squareSize) + off < ColonyGame.instance.worldGrid.getHeight() ? (int)((yc + halfHeight)/squareSize) + off : ColonyGame.instance.worldGrid.getHeight()-1;

        //Loop over the array
        for(int x=startX;x<=endX;x++) {
            for (int y = startY; y <= endY; y++) {
                Grid.TerrainTile tile = ColonyGame.instance.worldGrid.getNode(x, y).getTerrainTile();
                if(tile == null) continue;
                tile.changeVisibility(ColonyGame.instance.worldGrid.getVisibilityMap()[x][y].getVisibility());
                tile.terrainSprite.draw(batch);
            }
        }

        batch.end();
    }

    private void updateEntities(float delta){
        batch.setProjectionMatrix(ColonyGame.instance.camera.combined);
        batch.setColor(Color.WHITE); //Set the color back to white.
        batch.begin();

        ColonyGame.instance.listHolder.update(delta);
        ColonyGame.instance.listHolder.updateFloatingTexts(delta, batch);

        //Update and render events
        MessageEventSystem.notifyGameEvent("update", delta);
        MessageEventSystem.notifyGameEvent("render", delta, batch);

        //Step the Box2D simulation.
        ColonyGame.instance.world.step(1f / 60f, 8, 3);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        Vector3 pos = new Vector3(ColonyGame.instance.camera.position);
        Gdx.graphics.setDisplayMode(width, height, false);
        ColonyGame.instance.camera.setToOrtho(false, GH.toMeters(width), GH.toMeters(height));
        ColonyGame.instance.UICamera.setToOrtho(false, width, height);
        ColonyGame.instance.camera.position.set(pos);

        //Resizes all the GUI elements of the game (hopefully!)
        Array<UI> list = ColonyGame.instance.listHolder.getGUIList();
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

    private void generateLarger(){
        int scale = 1; //The scale to divide the sizes by. So if we have scale of 4, everything will be 1/4th smaller.
        //TODO Almost works, but is strangely stretched

        TextureRegion region = new TextureRegion();
        Grid.GridInstance grid = ColonyGame.instance.worldGrid;
        int squareSize = grid.getOriginalSquareSize()/scale; //The grid square size altered by the scale.
        int imageSize = 256/scale; //The total size of the image we want to generate.
        int regionSize = imageSize/squareSize; //The size of the region which is the image size divided by the size of the squares. so 256/32 = 8
        int totalX = grid.getWidth()/regionSize, totalY = grid.getHeight()/regionSize; //The total regions in the X and Y dimension.
        map = new TextureRegion[totalX+1][totalY+1]; //Initialize the map texture array to be the size of the total X and Y + 1.

        int currX = 0, currY = 0; //Some counters.

        TextureAtlas terrainAtlas = ColonyGame.instance.assetManager.get("terrain", TextureAtlas.class); //Get the terrain atlas.
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

        batch.setProjectionMatrix(ColonyGame.instance.camera.combined);
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
}
