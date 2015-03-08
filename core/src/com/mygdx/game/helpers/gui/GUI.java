package com.mygdx.game.helpers.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.sun.istack.internal.NotNull;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class GUI {
    public static BitmapFont font;
    public static Texture defaultTexture = new Texture("img/background.png");

    private static BitmapFont defaultFont = new BitmapFont();
    private static Texture defaultNormalButton = new Texture("img/ui/defaultButton_normal.png");
    private static Texture defaultMousedButton = new Texture("img/ui/defaultButton_moused.png");
    private static Texture defaultClickedButton = new Texture("img/ui/defaultButton_clicked.png");

    private static GUIStyle defaultGUIStyle = new GUIStyle();

    private static boolean clicked = false;
    private static boolean up = false;
    private static Rectangle rect1 = new Rectangle();

    public static GUIStyle gatherGUIStyle;
    public static GUIStyle exploreGUIStyle;

    static{
        font = defaultFont;
        loadGatherButtonStyle();
        loadExploreButtonStyle();
    }

    private static void loadGatherButtonStyle(){
        gatherGUIStyle = new GUIStyle();
        gatherGUIStyle.normal = new Texture(Gdx.files.internal("img/ui/axebutton_normal.png"), true);
        gatherGUIStyle.normal.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        gatherGUIStyle.moused = new Texture(Gdx.files.internal("img/ui/axebutton_moused.png"), true);
        gatherGUIStyle.moused.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        gatherGUIStyle.clicked = new Texture(Gdx.files.internal("img/ui/axebutton_clicked.png"), true);
        gatherGUIStyle.clicked.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
    }

    private static void loadExploreButtonStyle(){
        exploreGUIStyle = new GUIStyle();
        exploreGUIStyle.normal = new Texture(Gdx.files.internal("img/ui/explorebutton_normal.png"), true);
        exploreGUIStyle.normal.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        exploreGUIStyle.moused = new Texture(Gdx.files.internal("img/ui/explorebutton_moused.png"), true);
        exploreGUIStyle.moused.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        exploreGUIStyle.clicked = new Texture(Gdx.files.internal("img/ui/explorebutton_clicked.png"), true);
        exploreGUIStyle.clicked.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
    }

    public static void Texture(Rectangle rect, Texture texture, SpriteBatch batch){
        batch.draw(texture, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public static void Texture(float x, float y, float width, float height, Texture texture, SpriteBatch batch){
        batch.draw(texture, x, y, width, height);
    }

    public static void Text(String text, SpriteBatch batch, float x, float y){
        font.draw(batch, text, x, y);
    }

    public static boolean Button(Rectangle rect, SpriteBatch batch){
        return GUI.Button(rect, "", batch, null);
    }

    public static boolean Button(Rectangle rect, String text, SpriteBatch batch){
        return GUI.Button(rect, text, batch, null);
    }

    public static boolean Button(float x, float y, float width, float height, String text, SpriteBatch batch, GUIStyle style){
        rect1.set(x, y, width, height);
        return GUI.Button(rect1, text, batch, style);
    }

    public static boolean Button(Rectangle rect, String text, SpriteBatch batch, GUIStyle style){
        boolean clicked = false;

        if(style == null)
            style = defaultGUIStyle;

        Texture currTexture = style.normal;

        if(rect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY())){
            if(GUI.clicked){
                currTexture = style.clicked;
            }else if(GUI.up){
                currTexture = style.moused;
                clicked = true;
            }else {
                currTexture = style.moused;
            }
        }

        batch.draw(currTexture, rect.x, rect.y, rect.getWidth(), rect.getHeight());
        BitmapFont.TextBounds bounds = font.getBounds(text);                                //Get the bounds of the text
        style.font.draw(batch, text, rect.getX() + rect.getWidth()/2 - bounds.width/2, rect.getY() + rect.getHeight()/2 + bounds.height/2); //Draw the text

        return clicked;
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, @NotNull Rectangle rect, boolean centered){
        GUI.Label(text, batch, rect.getX() + rect.getWidth()/2, rect.getY() + rect.getHeight()/2, centered, null);
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, @NotNull Rectangle rect, boolean centered, GUIStyle style){
        GUI.Label(text, batch, rect.getX() + rect.getWidth()/2, rect.getY() + rect.getHeight()/2, centered, style);
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, boolean centered){
        GUI.Label(text, batch, x, y, centered, null);
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, boolean centered, GUIStyle style){
        if(style == null) style = defaultGUIStyle;

        if(centered){
            BitmapFont.TextBounds bounds = font.getBounds(text);
            x = x - bounds.width/2;
            y = y + bounds.height/2;
        }else
            y += style.font.getLineHeight();

        style.font.draw(batch, text, x, y);
    }

    public static String TextBox(String text, SpriteBatch batch, float x, float y){
        return "";
    }

    public static void ResetFont(){
        GUI.font = defaultFont;
        GUI.font.setColor(Color.WHITE);
        GUI.font.setScale(1f);
    }

    public static class GUIStyle {
        public Texture normal = GUI.defaultNormalButton;
        public Texture moused = GUI.defaultMousedButton;
        public Texture clicked = GUI.defaultClickedButton;

        public BitmapFont font = new BitmapFont();

        public GUIStyle(){

        }
    }

    public static void checkState(){
        boolean down = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        if(down){
            GUI.clicked = true;
            GUI.up = false;
        }else if(GUI.clicked){
            GUI.up = true;
            GUI.clicked = false;
        }else if(GUI.up){
            GUI.up = false;
            GUI.clicked = false;
        }
    }
}
