package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.mygdx.game.ColonyGame;

import java.awt.*;

public class DesktopLauncher {
	public static void main (String[] arg) {
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();

        createAtlas();

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = width;
		config.height = height;
        config.samples = 8;
        config.vSyncEnabled = false;
		new LwjglApplication(new ColonyGame(), config);
	}

    private static void createAtlas(){
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.maxWidth = 1024;
        settings.maxHeight = 1024;
        settings.combineSubdirectories = true;
        settings.paddingX = 8;
        settings.paddingY = 8;
        settings.edgePadding = true;
        settings.duplicatePadding = true;
        settings.flattenPaths = true;
        TexturePacker.process(settings, "img/terrain", "./atlas", "terrain");
        TexturePacker.process(settings, "img/ui/buttons", "./atlas", "buttons");
        TexturePacker.process(settings, "img/Interactable", "./atlas", "interactables");
    }
}
