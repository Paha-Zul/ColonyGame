package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.ISaveable;
import com.mygdx.game.util.worldgeneration.WorldGen;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public class SaveGameHelper {
    public static JsonWorld world = new JsonWorld();

    private static ObjectMapper mapper;

    private static TLongObjectHashMap<Entity> giantEntityMap = new TLongObjectHashMap<>(10000, 0.75f);
    private static TLongObjectHashMap<Component> giantCompMap = new TLongObjectHashMap<>(100000, 0.75f);

    static{
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.addMixIn(Vector2.class, Vector2MixIn.class);
    }

    public static void saveWorld() {
        //mapper.getSerializationConfig().addMixInAnnotations(Array.class, MixIn.class);
        //mapper.getSerializationConfig().addMixInAnnotations(Vector2.class, MixIn.class);

        //Get all the entities.
        getJsonEntities();
        saveManagers();

        FileHandle file = Gdx.files.local("game.sav");
        file.writeString("", false); //Clear the file.

        //Submit a task and write all the data to a file.
        Runnable task = () -> {
            try {
                writeFile(file, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(world));
                world.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        ColonyGame.instance.threadPool.submit(task);

        //writeFile(file, json.toJson(entComps, JsonComponents[].class));
        //writeFile(file, json.toJson(allComps, Component[].class));
    }

    private static void getJsonEntities(){
        world.clear();
        for(Array<Entity> list : ColonyGame.instance.listHolder.getEntityList())
            for(Entity ent : list) {
                world.entities.add(new JsonEntity(ent, ent.getComponents().getComponentIDs()));
                Collections.addAll(world.allComps, ent.getComponents().getAllComponents().toArray(Component.class));
            }
    }

    private static void saveManagers(){
        world.worldGen = WorldGen.getInstance();
        world.colonyGame = ColonyGame.instance;
    }

    public static void writeFile(FileHandle file, String s) {
        file.writeString(s, true);
    }

    public static void writeFileEncode(FileHandle file, String s) {
        file.writeString(com.badlogic.gdx.utils.Base64Coder.encodeString(s), false);
    }

    public static void writeFile(FileHandle file, byte[] bytes) {
        file.writeBytes(bytes, false);
    }

    public static void loadWorld(){
        loadWorld(Gdx.files.local("game.sav"));
    }

    private static void loadWorld(FileHandle file) {
        String save = readFile(file);

        //Clear everything from the world.
        ColonyGame.instance.listHolder.clearEntityList();

        try {
            world = mapper.readValue(file.file(), JsonWorld.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Make the three giant hash maps.
        for(Component comp : world.allComps) giantCompMap.put(comp.getCompID(), comp);
        for(JsonEntity ent : world.entities) giantEntityMap.put(ent.entity.getID(), ent.entity);

        loadManagers();

        //Load it all back in (sync as we go!).
        for(JsonEntity ent : world.entities){
            //ent.getComponents().transform = (Transform)giantCompMap.get(ent.getTrasnformID());
            //if(ent.getGraphicIdentityID() != 0) ent.getComponents().identity = (GraphicIdentity)giantCompMap.get(ent.getGraphicIdentityID());
            for(long id : ent.compIDs){
                Component comp = giantCompMap.get(id);
                comp.setOwner(ent.entity);
                ent.entity.addComponent(comp);
                comp.addedLoad(giantEntityMap, giantCompMap);
            }

            ent.entity.initLoad(giantEntityMap, giantCompMap); //Load the entity.
            ent.entity.load(giantEntityMap, giantCompMap); //Load the entity.

            ent.entity.getComponents().getAllComponents().forEach(comp -> comp.initLoad(giantEntityMap, giantCompMap)); //Load all the components on the Entity.
            ent.entity.getComponents().getAllComponents().forEach(comp -> comp.load(giantEntityMap, giantCompMap));
            ColonyGame.instance.listHolder.addEntity(ent.entity);
        }


        //Clear the collections
        world.allComps = new ArrayList<>();
        world.entities = new ArrayList<>();
        giantCompMap = new TLongObjectHashMap<>();
        giantEntityMap = new TLongObjectHashMap<>();
    }

    public static String readFile(FileHandle file) {
        if (file != null && file.exists()) {
            String s = file.readString();
            if (!s.isEmpty()) {
                return s;
            }
        }
        return "";
    }

    /**
     * Loads all the managers. Modify as such.
     */
    private static void loadManagers(){
        ColonyGame.instance.notificationManager.destroy();
        ColonyGame.instance.playerManager.destroy();
        ColonyGame.instance.worldGrid.destroyAndRecycle();

        ColonyGame.instance.playerManager = world.colonyGame.playerManager;

        ColonyGame.instance.playerManager.initLoad(giantEntityMap, giantCompMap);
        ColonyGame.instance.notificationManager.initLoad(giantEntityMap, giantCompMap);

        ColonyGame.instance.playerManager.load(giantEntityMap, giantCompMap);
        ColonyGame.instance.notificationManager.load(giantEntityMap, giantCompMap);

        ColonyGame.instance.worldGrid.createGrid(Constants.GRID_WIDTH, Constants.GRID_HEIGHT, Constants.GRID_SQUARESIZE);
        WorldGen.getInstance().init(ColonyGame.instance);

        boolean flag = false;
        while(!flag){
            flag = WorldGen.getInstance().generateWorld();
        }
    }


    /**
     * Such
     * @param POJO
     * @param instance
     */
    private static void copyDataToPOJO(Object POJO, Object instance){
        //Get all the fields o the POJO class.
        Field[] fields = POJO.getClass().getFields();

        try {
            //For each field of the POJO class, get the value from the instance field and set the POJO field.
            for(Field field : fields){
                Field instanceField = instance.getClass().getField(field.getName());
                field.set(POJO, instanceField.get(instanceField));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * This interface is to mix in some rules for the Vector2 class. Since we don't have control of the source,
     * we use this mixin to tell Jackson to serialize all fields (because Vector2 only has an X and Y field and we want both)
     */
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonIgnoreProperties({"X", "Y", "Zero", "zero"})
    private interface Vector2MixIn{}

    private static class JsonEntity{
        @JsonProperty
        public Entity entity;
        @JsonProperty
        public Long[] compIDs;

        public JsonEntity(){

        }

        public JsonEntity(Entity entity, Long[] compIDs){
            this.entity = entity;
            this.compIDs = compIDs;
        }
    }

    private static class JsonWorld{
        @JsonProperty
        public WorldGen worldGen;
        @JsonProperty
        public ColonyGame colonyGame;
        @JsonProperty
        public ArrayList<ISaveable> saveables = new ArrayList<>();
        @JsonProperty
        public ArrayList<JsonEntity> entities = new ArrayList<>();
        @JsonProperty
        public ArrayList<Component> allComps = new ArrayList<>();


        public JsonWorld(){

        }

        public void clear(){
            entities = new ArrayList<>();
            allComps = new ArrayList<>();
            saveables = new ArrayList<>();
            colonyGame = null;
        }
    }

    private static class SaveJsonPlayerManager{
        @JsonProperty("playerManagerData")
        ArrayList<String[]> data = new ArrayList<>();
    }

    private static class PlayerManagerPOJO{

    }
}
