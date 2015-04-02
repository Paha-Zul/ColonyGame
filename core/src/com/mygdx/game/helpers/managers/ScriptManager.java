package com.mygdx.game.helpers.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.mygdx.game.interfaces.IScript;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by Paha on 4/1/2015.
 */
public class ScriptManager {

    public static void load(String path){

        // Create a File object on the root of the directory containing the class file
        FileHandle handle = Gdx.files.internal(path);
        try {
            for(FileHandle file : handle.list()) {
                if(file.isDirectory())
                    load(path+file.name());

                int index = file.name().lastIndexOf('.'); //get the index of the extension...
                if(index < 0 || !file.name().substring(index, file.name().length()).equals(".class")) //If it doesn't have the ".class" extension
                    return;

                String fileName = file.name().substring(0, index); //Get the file name without the extension.

                // Convert File to a URL
                URL url = handle.file().toURI().toURL();     // file: ./scripts
                URL[] urls = new URL[]{url};

                // Create a new class loader with the directory
                ClassLoader cl = new URLClassLoader(urls);

                // Load in the class; Test.class should be located in
                // the directory file: ./scripts/Test.class
                Class cls = cl.loadClass(fileName);

                //Start the script.
                if (IScript.class.isAssignableFrom(cls)) {
                    IScript script = (IScript) cls.newInstance();
                    script.start();
                }
            }

        } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
