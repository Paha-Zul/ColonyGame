package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.mygdx.game.component.Item;
import com.mygdx.game.component.Resource;
import com.mygdx.game.helpers.managers.ItemManager;
import com.mygdx.game.helpers.managers.ResourceManager;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.IDestroyable;

import java.util.*;

/**
 * Created by Paha on 2/19/2015.
 */
public class DataBuilder implements IDestroyable{
    String filePath = "files/";
    String itemPath = "items.json";
    String resourcePath = "resources.json";
    String tilePath = "tiles.json";
    String worldPath = "worldgen.json";
    String changeLogPath = "changelog.json";
    String imgPath = "img/";

    private EasyAssetManager assetManager;

    public static HashMap<String, JsonTileGroup> tileGroupsMap = new HashMap<>();
    public static JsonChangeLog changelog;
    public static JsonWorld worldData;

    public DataBuilder(EasyAssetManager assetManager){
        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        param.magFilter = Texture.TextureFilter.MipMapLinearLinear;
        param.genMipMaps = true;

        this.assetManager = assetManager;
        buildImages(Gdx.files.internal(this.imgPath), param);
    }

    public boolean update(){
        return assetManager.update();
    }

    public void loadFiles(){
        buildItems();
        buildResources();
        buildTiles();
        buildWorldGen();
        buildChangeLog();
    }

    private void buildImages(FileHandle dirHandle, TextureLoader.TextureParameter param){
        for (FileHandle entry: dirHandle.list()) {
            if(entry.isDirectory()) //For every directory, call this function again to load the images.
                buildImages(new FileHandle(entry.path()+"/"), param); //A bit of recursion.

            loadImage(entry, param);
        }
    }

    private void loadImage(FileHandle entry, TextureLoader.TextureParameter param){
        String extension = "";
        String commonName = "";

        int i = entry.name().lastIndexOf('.');
        if (i > 0) {
            extension = entry.name().substring(i + 1);
            commonName = entry.name().substring(0, i);
        }

        if(!extension.equals("png"))
            return;

        assetManager.load(entry.path(), commonName, Texture.class, param);
    }

    private void getFileNamesFromDir(FileHandle dirHandle, ArrayList<String> list){
        for (FileHandle entry: dirHandle.list()) {
            if(entry.isDirectory()) //For every directory, call this function again to load the images.
                getFileNamesFromDir(new FileHandle(entry.path()+"/"), list); //A bit of recursion.

            String name = getFileName(entry);
            if(!name.equals(""))
                list.add(name);
        }
    }

    private String getFileName(FileHandle entry){
        String extension = "";
        String commonName = "";

        int i = entry.name().lastIndexOf('.');
        if (i > 0) {
            extension = entry.name().substring(i + 1);
            commonName = entry.name().substring(0, i);
        }

        if(extension.equals("png"))
            return commonName;

        return "";
    }

    private ArrayList<FolderStructure> buildFolderStructure(FileHandle handle){
        ArrayList<FolderStructure> list = new ArrayList<>();
        ArrayList<String> fileList = new ArrayList<>();

        for(FileHandle entry : handle.list()){
            if(entry.isDirectory()){
                fileList.clear();
                int index = entry.nameWithoutExtension().lastIndexOf('_'); //Get the index of the last _.
                if(index == -1) GH.writeErrorMessage("Using 'autoLayered' in tiles.json and "+entry.nameWithoutExtension() + " has no rank attached to it! (ex: 'file_1, file_2'"); //Throw an error if it doesn't exist.
                int rank = Integer.parseInt(entry.nameWithoutExtension().substring(index+1)); //Record the rank of the folder.
                String fullName = entry.nameWithoutExtension(); //Record the full name.
                this.getFileNamesFromDir(entry, fileList);
                String[] img = fileList.toArray(new String[fileList.size()]);

                list.add(new FolderStructure(rank, fullName, img));
            }
        }

        list.sort((fs1, fs2) -> fs1.rank - fs2.rank);
        return list;
    }

    private void buildItems() {
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonItems items = json.fromJson(JsonItems.class, Gdx.files.internal(filePath+itemPath));

        for(JsonItem jsonItem : items.items){
            Item item = new Item(jsonItem.itemName, jsonItem.itemType, true, 10000, 1);
            item.setDisplayName(jsonItem.displayName);
            item.setDescription(jsonItem.description);
            ItemManager.addItemInstance(item);
        }
    }

    private void buildResources(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonResources resources = json.fromJson(JsonResources.class, Gdx.files.internal(filePath+resourcePath));

        for(JsonResource jRes : resources.resources){
            Resource resource = new Resource();
            resource.setResourceName(jRes.resourceName);
            resource.setDisplayName(jRes.displayName);
            resource.setResourceType(jRes.resourceType);

            //Build the item array
            String[] items = new String[jRes.items.size];
            for(int i=0;i<jRes.items.size;i++)
                items[i] = jRes.items.get(i);

            //Build the amounts array
            int[][] amounts = new int[jRes.amounts.length][2];
            for(int i=0;i<jRes.amounts.length;i++) {
                amounts[i][0] = jRes.amounts[i][0];
                amounts[i][1] = jRes.amounts[i][1];
            }

            resource.setItemNames(items);
            resource.setItemAmounts(amounts);
            resource.setTextureName(jRes.img);
            ResourceManager.addResourceInstance(resource);
        }
    }

    /**
     * Builds the tile data from the json file.
     */
    private void buildTiles(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonTiles tiles = json.fromJson(JsonTiles.class, Gdx.files.internal(filePath+tilePath));

        //For each group of tiles
        for (JsonTileGroup group : tiles.tileGroups) {
            //If the group of tiles is auto layered...
            if(group.autoLayered && group.dir != null){
                ArrayList<FolderStructure> list = this.buildFolderStructure(Gdx.files.internal(group.dir)); //Build the folder structure.
                ArrayList<JsonTile> tileList = new ArrayList<>(); //a list.
                for(int i=0;i<list.size();i++){
                    FolderStructure struct = list.get(i);
                    JsonTile tile = new JsonTile();
                    tile.img = struct.img; //Set the images.
                    tile.height = new float[]{-1 + i*(2f/list.size()), -1 + (i+1)*(2f/list.size())}; //Set the heights
                    tileList.add(tile);
                }
                group.tiles = tileList.toArray(new JsonTile[tileList.size()]);

            //Otherwise, for each tile we check if we have a 'dir' to get images from. If so, get images from the directory. Otherwise, they listed the images manually.
            }else {
                //For each tile.
                for (JsonTile tile : group.tiles) {
                    //If the dir field was assigned.
                    if (tile.dir != null) {
                        //Loop over each file in the dir and grab it. We use this for our img list instead.
                        ArrayList<String> fileNames = new ArrayList<>();
                        this.getFileNamesFromDir(Gdx.files.internal(tile.dir), fileNames);
                        tile.img = fileNames.toArray(new String[fileNames.size()]);
                        tile.tileNames = tile.img; //Assign the tileNames the same as the img names.
                        if (tile.img.length == 0)
                            GH.writeErrorMessage("No files in folder '" + tile.dir + "'");
                    }

                    //Throw an error message if we have no images/files for this tile.
                    if(tile.img == null || tile.img.length <= 0)
                        GH.writeErrorMessage("No files were generated for "+tile.tileNames[0]+". Either there are no files in the directory specified or no files manually listed in tiles.json");
                }

                //Throw an error if something went wrong with the tile groups.
                if(group.tiles == null || group.tiles.length <= 0)
                    GH.writeErrorMessage("Something is wrong with group "+group.noiseMap+" in tiles.json");
            }
            tileGroupsMap.put(group.noiseMap, group);
        }
    }

    /**
     * Builds the WorldGen data for things like noise maps, frequency of noise maps, size, tile sizes....
     */
    private void buildWorldGen(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonWorld world = json.fromJson(JsonWorld.class, Gdx.files.internal(filePath+worldPath));
        for(NoiseMap map : world.noiseMaps) {
            world.noiseMapHashMap.put(map.rank, map);
            //If the seed is null or empty, generate one. Otherwise, use the seed from worldgen.json.
            if(map.seed == null || map.seed.isEmpty()) map.noiseSeed = (long)(Math.random()*Long.MAX_VALUE);
            else{
                for(int i=0;i<map.seed.length();i++){
                    char ch = map.seed.charAt(i);
                    map.noiseSeed = (map.noiseSeed + (long)ch*1000000000000000000l)%Long.MAX_VALUE;
                }
            }
        }

        WorldGen.getInstance().treeScale = world.treeScale;
        WorldGen.getInstance().freq = world.noiseMapHashMap.get(0).freq;
        Constants.GRID_SQUARESIZE = world.tileSize;
        Constants.GRID_WIDTH = world.worldWidth;
        Constants.GRID_HEIGHT = world.worldHeight;
        Constants.WORLDGEN_GENERATESPEED = world.worldGenSpeed;
        Constants.WORLDGEN_RESOURCEGENERATESPEED = world.resourceGenerateSpeed;

        worldData = world;
    }

    /**
     * Builds the changelog data for displaying in game.
     */
    private void buildChangeLog(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        DataBuilder.changelog = json.fromJson(JsonChangeLog.class, Gdx.files.internal(filePath+changeLogPath));
    }

    private static class JsonItems{
        public Array<JsonItem> items;
    }

    private static class JsonItem{
        public String itemName, displayName, itemType, description, img;
    }

    private static class JsonResources{
        public Array<JsonResource> resources;
    }

    private static class JsonResource{
        public String resourceName, displayName, resourceType, description, img;
        public Array<String> items;
        public int[][] amounts;
    }

    private static class JsonTiles{
        public JsonTileGroup[] tileGroups;

    }

    public static class JsonTileGroup{
        public int rank = 0;
        public boolean autoLayered = false;
        public String dir;
        public String noiseMap;
        public JsonTile[] tiles;
    }

    public static class JsonTile{
        public String[] tileNames, img, resources;
        public String category=null, dir=null;
        public float[] height;
        public float[][] resourcesChance;
        public boolean avoid;
    }

    public static class JsonWorld{
        public int tileSize = 25;
        public int worldWidth, worldHeight, worldGenSpeed, resourceGenerateSpeed;
        public float treeScale=0;
        public NoiseMap[] noiseMaps;
        public HashMap<Integer, NoiseMap> noiseMapHashMap = new HashMap<>(); //A hasmap that stores NoiseMaps by ranks.
    }

    public static class NoiseMap{
        public long noiseSeed;
        public int rank;
        public String name, seed;
        public float freq;
    }

    public static class JsonChangeLog{
        public JsonLog changes[];
    }

    public static class JsonLog{
        public String version, date;
        public String[] log;
    }

    private static class FolderStructure{
        public int rank=0;
        public String fullName = "";
        public String[] img;

        public FolderStructure(int rank, String fullName, String[] img) {
            this.rank = rank;
            this.fullName = fullName;
            this.img = img;
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
