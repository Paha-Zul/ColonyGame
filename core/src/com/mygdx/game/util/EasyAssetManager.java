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
        DataReference ref = dataMap.get(commonName);
        if(ref == null) {
            if(this.isLoaded(commonName))
                return super.get(commonName, type);

            GH.writeErrorMessage("Can't find file "+commonName+". Check to make sure it exists.");
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

    private class DataReference{
        private String name, path;

        public DataReference(String name, String path){
            this.name = name;
            this.path = path;
        }
    }
}


