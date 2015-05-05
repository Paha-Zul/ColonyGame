package com.mygdx.game.helpers.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.interfaces.IScript;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by Paha on 4/1/2015.
 */
public class ScriptManager {
    public static Array<IScript> scripts = new Array<>();

    public static void load(String path, String original){

        // Create a File object on the root of the directory containing the class file
        FileHandle handle = Gdx.files.internal(path);
        try {
            for(FileHandle file : handle.list()) {
                if(file.isDirectory())
                    load(path+"/"+file.name(), path);

                int index = file.path().lastIndexOf('.'); //get the index of the extension...
                if(index < 0 || !file.path().substring(index, file.path().length()).equals(".class")) //If it doesn't have the ".class" extension
                    continue;

                //We get the fileName without the extension, this will look like './scripts/whateverdir/ClassNameHere'
                //We then need to strip away the first and second '/' characters, which will make it look like 'wahteverdir/ClassNameHere'
                //Then we turn each '/' into a '.' because that's how packages work, which looks like 'waheteverdir.ClassNameHere'
                String fileName = file.path().substring(0, index);
                fileName = fileName.substring(fileName.indexOf('/')+1, fileName.length());
                fileName = fileName.substring(fileName.indexOf('/')+1, fileName.length());
                fileName = fileName.replace('/', '.');

                // Convert File to a URL. This should be the absolute directory to the './scripts' directory.
                URL url = Gdx.files.internal(original).file().toURI().toURL();    // file: 'absoultepath'/scripts
                URL[] urls = new URL[]{url};

                // Create a new class loader with the directory
                ClassLoader cl = new URLClassLoader(urls);

                // Load in the class; Test.class should be located in
                // the directory file: whateverdir.ClassNameHere
                Class cls = cl.loadClass(fileName);

                //Start the script.
                if (IScript.class.isAssignableFrom(cls)) {
                    scripts.add((IScript) cls.newInstance());
                }
            }

        } catch (MalformedURLException | ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
