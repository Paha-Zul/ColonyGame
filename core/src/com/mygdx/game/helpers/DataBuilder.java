package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.IDestroyable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paha on 2/19/2015.
 */
public class DataBuilder implements IDestroyable{
    String filePath = "files/";
    String itemPath = "items.json";
    String resourcePath = "resources.json";
    String animalPath = "animals.json";
    String weaponPath = "weapon.json";
    String ammoPath = "ammunition.json";
    String tilePath = "tiles.json";
    String worldPath = "worldgen.json";
    String changeLogPath = "changelog.json";
    String imgPath = "img/misc";
    String soundPath = "sounds/";
    String atlasPath = "atlas/";

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

        buildFilesInDir(Gdx.files.internal(this.imgPath), Texture.class, param, new String[]{"png"});
        buildFilesInDir(Gdx.files.internal(this.soundPath), Sound.class, null, new String[]{"ogg"});
        buildFilesInDir(Gdx.files.internal(this.atlasPath), TextureAtlas.class, null, new String[]{"atlas"});
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
        buildAnimals();
        buildWeapons();
        buildAmmuntion();
    }

    private void buildFilesInDir(FileHandle dirHandle, Class<?> type, AssetLoaderParameters param, String[] extensions) {
        for (FileHandle entry : dirHandle.list()) {
            if (entry.isDirectory()) //For every directory, call this function again to load the images.
                buildFilesInDir(new FileHandle(entry.path() + "/"), type, param, extensions); //A bit of recursion.

            loadFile(entry, type, param, extensions);
        }
    }

    private void loadFile(FileHandle entry, Class<?> type, AssetLoaderParameters param, String[] extensions){
        String extension = "";
        String commonName = "";

        int i = entry.name().lastIndexOf('.');
        if (i > 0) {
            extension = entry.name().substring(i + 1);
            commonName = entry.name().substring(0, i); //Get the common name (no path or extension).
        }

        //If it matches one of the extensions, load it!
        for(String ext : extensions)
            if(extension.equals(ext)) {
                if(param != null)
                    assetManager.load(entry.path(), commonName, type, param);
                else
                    assetManager.load(entry.path(), commonName, type);
                return;
            }

    }

    /**
     * Gets all the file firstNames from the directory passed in. Adds them to the list passed in.
     * @param dirHandle The Handle to the directory.
     * @param list The ArrayList to add the firstNames to.
     */
    private void getFileNamesFromDir(FileHandle dirHandle, ArrayList<String> list){
        for (FileHandle entry: dirHandle.list()) {
            if(entry.isDirectory()) //For every directory, call this function again to load the images.
                getFileNamesFromDir(new FileHandle(entry.path()+"/"), list); //A bit of recursion.

            //Get the name. If it's empty, don't add it.
            String name = getFileName(entry);
            if(!name.equals(""))
                list.add(name);
        }
    }

    /**
     * Gets all the file firstNames from the directory passed in. Adds them to the list passed in. Must match the base string passed in (ie: "palmtree_dark", base = "palmtree")
     * @param dirHandle The Handle to the directory.
     * @param list The ArrayList to add the firstNames to.
     * @param base The base to match.
     */
    private void getFileNamesFromDir(FileHandle dirHandle, ArrayList<String> list, String base){
        for (FileHandle entry: dirHandle.list()) {
            if(entry.isDirectory()) //For every directory, call this function again to load the images.
                getFileNamesFromDir(new FileHandle(entry.path()+"/"), list, base); //A bit of recursion.

            String name = getFileName(entry, base);
            if(!name.equals(""))
                list.add(name);
        }
    }

    /**
     * Gets the file name from the Handle passed in.
     * @param entry The File Handle of the file.
     * @return The File name if it succeeded, empty if it didn't.
     */
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

    /**
     * Gets the file name from the Handle passed in.
     * @param entry The File Handle of the file.
     * @param base The base the file name must match/
     * @return The name of the file if succeeded, empty otherwise.
     */
    private String getFileName(FileHandle entry, String base){
        String extension = "";
        String commonName = "";

        //Get the index of the extension.
        int i = entry.name().lastIndexOf('.');
        if (i > 0) {
            extension = entry.name().substring(i + 1); //Get the extension.
            commonName = entry.name().substring(0, i); //Get the common name.
        }

        //Get the index of the base
        int baseIndex = entry.name().lastIndexOf('_');
        if(baseIndex > 0){
            if(!entry.name().substring(0, baseIndex).equals(base)) { //If the substring doesn't equal the base, return empty.
                return "";
            }
        }else
            return "";

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

        for(JsonItem jsonItem : items.items)
            DataManager.addData(jsonItem.itemName, jsonItem, JsonItem.class);
    }

    /**
     * Builds the resource Json stuff.
     */
    private void buildResources(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonResources resources = json.fromJson(JsonResources.class, Gdx.files.internal(filePath+resourcePath));

        for(JsonResource jRes : resources.resources){
            //If the dir field is not null, we have a directory to pull images from.
            if(jRes.dir != null){
                ArrayList<String> list = new ArrayList<>();

                //If we have the field 'allimgwith', loop through all of them and get the images from the dir.
                if(jRes.allimgwith != null && jRes.allimgwith.length != 0){
                    for(String base : jRes.allimgwith)
                        getFileNamesFromDir(Gdx.files.internal(jRes.dir), list, base);

                //Otherwise, just load all from the dir.
                }else
                    this.getFileNamesFromDir(Gdx.files.internal(jRes.dir), list);

                //Set the img array as the list.
                jRes.img = list.toArray(new String[list.size()]);
                if(jRes.img.length == 0 && !jRes.noimg) GH.writeErrorMessage("No images loaded for "+jRes.resourceName+". Check that the directory "+jRes.dir+" has files and they are named correctly.");

                list.clear();
            }

            if(jRes.itemAmounts == null)
                GH.writeErrorMessage("No item amounts for the resource: "+jRes.resourceName, true);

            //Build the itemAmounts array
            int[][] amounts = new int[jRes.itemAmounts.length][2];
            for(int i=0;i<jRes.itemAmounts.length;i++) {
                amounts[i][0] = jRes.itemAmounts[i][0];
                amounts[i][1] = jRes.itemAmounts[i][1];
            }

            DataManager.addData(jRes.resourceName, jRes, JsonResource.class);
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
                        tile.tileNames = tile.img; //Assign the tileNames the same as the img firstNames.
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
                //Goes over each character and adds to the seed.
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

    /**
     * Builds the animal Json stuff.
     */
    private void buildAnimals(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonAnimals animals = json.fromJson(JsonAnimals.class, Gdx.files.internal(filePath+animalPath));

        for(JsonAnimal animal : animals.animals){
            DataManager.addData(animal.name, animal, JsonAnimal.class);
        }

    }

    /**
     * Builds the weapon Json stuff.
     */
    private void buildWeapons(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonWeapons weapons = json.fromJson(JsonWeapons.class, Gdx.files.internal(filePath+weaponPath));

        for(JsonWeapon weapon : weapons.weapons){
            DataManager.addData(weapon.name, weapon, JsonWeapon.class);
        }
    }

    /**
     * Builds the ammunition Json stuff.
     */
    private void buildAmmuntion(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonAmmunitions ammunitions = json.fromJson(JsonAmmunitions.class, Gdx.files.internal(filePath+ammoPath));

        for(JsonAmmunition ammo : ammunitions.ammunitions){
            DataManager.addData(ammo.name, ammo, JsonAmmunition.class);
        }
    }

    private static class JsonItems{
        public Array<JsonItem> items;
    }

    public static class JsonItem{
        private String itemName, displayName, itemType, description, img;
        private String[] effects;
        private int[] strengths;

        public String getItemName() {
            return itemName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getItemType() {
            return itemType;
        }

        public String getDescription() {
            return description;
        }

        public String getImg() {
            return img;
        }

        public String[] getEffects() {
            return effects;
        }

        public int[] getStrengths() {
            return strengths;
        }

        public boolean hasEffect(String effect){
            if(effects != null) {
                for (String eff : effects)
                    if (eff.equals(effect))
                        return true;
            }

            return false;
        }
    }

    private static class JsonResources{
        public Array<JsonResource> resources;
    }

    public static class JsonResource{
        public String resourceName, displayName, resourceType, description, dir;
        public String[] img, allimgwith, itemNames;
        public int[][] itemAmounts;
        public int gatherTime;
        public boolean noimg, infinite;
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

    private static class JsonAnimals{
        public JsonAnimal[] animals;
    }

    public static class JsonAnimal{
        public String name, img, displayName, resourceName;
        public String[] itemNames;
        public boolean aggressive, pack;
        public int[] packAmount;
        public int[][] itemAmounts;
    }

    private static class JsonWeapons{
        public JsonWeapon[] weapons;
    }

    public static class JsonWeapon{
        public String name, displayName, weaponType, ammunition;
        public float reloadTime;
    }

    private static class JsonAmmunitions{
        public JsonAmmunition[] ammunitions;
    }

    public static class JsonAmmunition{
        public String name, displayName, ammoType;
        public float damage, travelSpeed, accuracy;
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
