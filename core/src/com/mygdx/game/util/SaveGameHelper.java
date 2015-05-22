package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import gnu.trove.map.hash.TDoubleObjectHashMap;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

import java.io.IOException;

public class SaveGameHelper {

    //Information about components linked to an entity.
    @JsonIgnoreProperties({"size", "ordered", "iterable", "predicateIterable"})
    public static class JsonComponents{
        @JsonProperty
        long entityID;
        @JsonProperty
        Array<Long> newComponentList = new Array<>();
        @JsonProperty
        Array<Long> activeComponentList = new Array<>();
        @JsonProperty
        Array<Long> inactiveComponentList = new Array<>();
        @JsonProperty
        Array<Long> scalableComponents = new Array<>();
        @JsonProperty
        Array<Long> destroyComponentList = new Array<>();

        public JsonComponents() {

        }
    }

    private static class JsonComponent{
        public String className;
        public Component component;

        public JsonComponent(Component comp){
            this.className = comp.getClass().getName();
            this.component = comp;
        }
    }

    private static class JsonWorld{
        @JsonProperty
        public Entity[] entities;
        @JsonProperty
        public JsonComponents[] entComps;
        @JsonProperty
        public JsonComponent[] allComps;

    }

    public static JsonWorld world = new JsonWorld();

    private static ObjectMapper mapper;

    private static TDoubleObjectHashMap<Entity> giantEntityMap = new TDoubleObjectHashMap<>();
    private static TDoubleObjectHashMap<Component> giantCompMap = new TDoubleObjectHashMap<>();
    private static TDoubleObjectHashMap<JsonComponents> giantCompContainerMap = new TDoubleObjectHashMap<>();

    public static void saveWorld() {
        Json json = new Json();
        mapper = new ObjectMapper();
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE));
        mapper.getSerializationConfig().addMixInAnnotations(Array.class, MixIn.class);

        jsonEntities();

        FileHandle file = Gdx.files.local("game.sav");
        file.writeString("", false); //Clear the file.

        Runnable task = () -> {
            try {
                writeFile(file, mapper.defaultPrettyPrintingWriter().writeValueAsString(world));
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ColonyGame.threadPool.submit(task);

        //writeFile(file, json.toJson(entComps, JsonComponents[].class));
        //writeFile(file, json.toJson(allComps, Component[].class));
    }

    private static void jsonEntities(){
        Array<Entity> entList = new Array<>(false, 100, Entity.class); //The entity list.
        Array<JsonComponents> entCompList = new Array<>(false, 100, JsonComponents.class); //The component lists for each entity.
        Array<JsonComponent> compList = new Array<>(false, 100, JsonComponent.class); //All of the existing components in the game.

        for(Array<Entity> list : ListHolder.getEntityList())
            for(Entity ent : list) {
                entList.add(ent);

                Entity.Components comps = ent.getComponents(); //Get the components object from the entity.
                JsonComponents entComps = new JsonComponents(); //Holds minimal information about the components object.
                entComps.entityID = ent.getID();

                /*
                * For each list of components, lets store them... This stores them by ID for relinking later.
                */

                for(Component comp : comps.getNewComponentList()) {
                    entComps.newComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(new JsonComponent(comp)); //Add the comp to this list.
                }
                entComps.newComponentList.shrink();

                for(Component comp : comps.getActiveComponentList()) {
                    entComps.activeComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(new JsonComponent(comp)); //Add the comp to this list.
                }
                entComps.activeComponentList.shrink();

                for(Component comp : comps.getInactiveComponentList()) {
                    entComps.inactiveComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(new JsonComponent(comp)); //Add the comp to this list.
                }
                entComps.inactiveComponentList.shrink();

                for(Component comp : comps.getDestroyComponentList()) {
                    entComps.destroyComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(new JsonComponent(comp)); //Add the comp to this list.
                }
                entComps.destroyComponentList.shrink();

                for(Component comp : comps.getScalableComponents()) {
                    entComps.scalableComponents.add(comp.getCompID()); //Store each by ID.
                    compList.add(new JsonComponent(comp)); //Add the comp to this list.
                }
                entComps.scalableComponents.shrink();

                entCompList.add(entComps);
            }

        world.entComps = entCompList.toArray(JsonComponents.class);
        world.allComps = compList.toArray(JsonComponent.class);
        world.entities = entList.toArray(Entity.class);
    }

    public static void loadWorld(){
        loadWorld(Gdx.files.local("game.sav"));
    }

    private static void loadWorld(FileHandle file) {
        String save = readFile(file);
        if (!save.isEmpty()) {
            Runnable task = () -> {
                try {
                    world = mapper.readValue(file.file(), JsonWorld.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
            ColonyGame.threadPool.submit(task);
        }

        System.out.println("Done!");
    }

    public static void writeFile(FileHandle file, String s) {
        //file.writeString(com.badlogic.gdx.utils.Base64Coder.encodeString(s), false);
        file.writeString(s, true);
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

    private abstract class MixIn{

        @JsonIgnore int size;
        @JsonIgnore boolean ordered;
        @JsonIgnore Object iterable;
        @JsonIgnore Object predicateIterable;

        @JsonIgnore
        abstract int getSize();
        @JsonIgnore
        abstract boolean isOrdered();
        @JsonIgnore
        abstract Object getIterable();
        @JsonIgnore
        abstract Object getPredicateIterable();
    }
}
