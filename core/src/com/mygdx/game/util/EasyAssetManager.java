package com.mygdx.game.util;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;

import java.util.HashMap;

/**
 * Created by Paha on 2/19/2015.
 */
public class EasyAssetManager extends AssetManager{
    HashMap<String, DataReference> dataMap = new HashMap<>(100);

    public synchronized <T> T get(String commonName, Class<T> type) {
        //Get the reference from the data map.
        DataReference ref = dataMap.get(commonName);
        if(ref == null) {
            //If it's null, let's try to find it in the underlying AssetManager by the common name. This really only is useful in cases where the underlying AssetManager loads its own file,
            //for instance, when you load an Atlas file and it loads the images for you.
            Logger.log(Logger.WARNING, "Data named "+commonName+" was not found in the EasyAssetManager. This could be bad. Trying to find it by the full path reference in the asset manager.");
            if(this.isLoaded(commonName)) {
                Logger.log(Logger.WARNING, commonName + " was found by the path in the underlying AssetManager. We're good!");
                return super.get(commonName, type);
            }

            Logger.log(Logger.WARNING, "Data named "+commonName+" was not found by the full path in the AssetManager. Crash incoming?");
            return null;
        }
        return super.get(dataMap.get(commonName).path, type);
    }

    public synchronized <T> void load(String fileName, String commonName, Class<T> type) {
        super.load(fileName, type);
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    public synchronized <T> void load(String fileName, String commonName, Class<T> type, AssetLoaderParameters<T> param) {
        super.load(fileName, type, param);
        dataMap.put(commonName, new DataReference(commonName, fileName));
    }

    /**
     * Basically holds a link from a simple name to the actual path. This makes it
     * a lot easier to load an asset from the manager ("somePicture" vs "img/misc/buttons/somePicture.png")
     */
    private class DataReference{
        private String name, path;

        public DataReference(String name, String path){
            this.name = name;
            this.path = path;
        }
    }
}


