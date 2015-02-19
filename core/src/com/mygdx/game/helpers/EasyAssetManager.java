package com.mygdx.game.helpers;

import com.badlogic.gdx.assets.AssetManager;

import javax.xml.crypto.Data;
import java.util.HashMap;

/**
 * Created by Paha on 2/19/2015.
 */
public class EasyAssetManager extends AssetManager{
    HashMap<String, DataReference> dataMap = new HashMap<>(100);

    public synchronized <T> T get(String commonName, Class<T> type) {
        System.out.println("Trying to get: '"+commonName+"'");
        DataReference ref = dataMap.get(commonName);
        return super.get(dataMap.get(commonName).path, type);
    }

    public synchronized <T> void load(String fileName, String commonName, Class<T> type) {
        super.load(fileName, type);
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


