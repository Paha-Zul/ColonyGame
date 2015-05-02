package com.mygdx.game.helpers.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.sun.istack.internal.NotNull;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class GUI {
    public static BitmapFont font;
    public static Texture defaultTexture = new Texture("img/misc/background.png");

    private static BitmapFont defaultFont = new BitmapFont();
    private static Texture defaultNormalButton = new Texture("img/ui/buttons/defaultButton_normal.png");
    private static Texture defaultMousedButton = new Texture("img/ui/buttons/defaultButton_moused.png");
    private static Texture defaultClickedButton = new Texture("img/ui/buttons/defaultButton_clicked.png");

    public static GUIStyle defaultGUIStyle = new GUIStyle();

    private static boolean clicked = false;
    private static boolean up = false;
    private static Rectangle rect1 = new Rectangle();

    static{
        font = defaultFont;
    }

    public static void Texture(Texture texture, Rectangle rect, SpriteBatch batch){
        batch.draw(texture, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public static void Texture(Texture texture, float x, float y, float width, float height, SpriteBatch batch){
        batch.draw(texture, x, y, width, height);
    }

    public static void Text(String text, SpriteBatch batch, float x, float y){
        GUI.Text(text, batch, x, y, false, null);
    }

    public static void Text(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, boolean centered){
        GUI.Text(text, batch, x, y, centered, null);
    }

    public static void Text(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, boolean centered, GUIStyle style){
        if(style == null) style = defaultGUIStyle;

        if(centered){
            BitmapFont.TextBounds bounds = font.getBounds(text);
            x = x - bounds.width/2;
            y = y + bounds.height/2;
        }else
            y += style.font.getLineHeight();


        if(!style.multiline)
            style.font.draw(batch, text, x, y);
        else
            style.font.drawMultiLine(batch, text, x, y);
    }

    /**
     * Draws a bar for a current and maximum value.
     * @param x The x location to start at.
     * @param y The Y location to start at.
     * @param width The width of the bar.
     * @param height The height of the bar.
     * @param currVal THe current value.
     * @param maxVal The maximum value.
     */
    public static void DrawBar(SpriteBatch batch, float x, float y, float width, float height, float currVal, float maxVal, boolean displayText, GUIStyle style, Color color){
        if(style == null) style = defaultGUIStyle;
        if(color == null) color = Color.GREEN;

        style.alignment = Align.left;
        style.alignment = Align.center;

        float outerX = x;
        float innerX = x + 2;

        Color batchColor = batch.getColor();
        //Draw the out rectangle
        batch.setColor(Color.BLACK);
        GUI.Texture(WorldGen.whiteTex, outerX, y, width, height, batch);

        //Draw the inner rectangle (shrink it by 2 inches on all sides, 'padding')
        batch.setColor(color);
        float newWidth = (currVal/maxVal)*(width-4);
        GUI.Texture(WorldGen.whiteTex, innerX, y + 2, newWidth, height - 4, batch);

        if(displayText) GUI.Label((int)currVal+"/"+(int)maxVal, batch, outerX, y, width, height);

        batch.setColor(batchColor);
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
        if(style.activated) currTexture = style.active;

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
        style.font.draw(batch, text, rect.getX() + rect.getWidth() / 2 - bounds.width / 2, rect.getY() + rect.getHeight() / 2 + bounds.height / 2); //Draw the text

        return clicked;
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, @NotNull Rectangle rect){
        GUI.Label(text, batch, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), null);
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, @NotNull Rectangle rect, GUIStyle style){
        GUI.Label(text, batch, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), style);
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, float width, float height){
        GUI.Label(text, batch, x, y, width, height, null);
    }

    public static void Label(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, float width, float height, GUIStyle style){
        if(style == null) style = defaultGUIStyle;
        //y += style.font.getLineHeight();
        BitmapFont.TextBounds bounds = font.getBounds(text);

        x += style.paddingLeft;
        width -= style.paddingRight;
        y -= style.paddingTop;
        height -= style.paddingBottom;

        if(style.alignment == Align.center){
            x += width/2 - bounds.width/2;
            y += height/2 + bounds.height/2;
        }else if(style.alignment == Align.left)
            y += height/2 + bounds.height/2;
        else if(style.alignment == Align.topLeft)
            y += height;

        if(!style.multiline)
            style.font.draw(batch, text, x, y);
        else
            style.font.drawMultiLine(batch, text, x, y);
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
        public Texture active = GUI.defaultClickedButton;

        public BitmapFont font = defaultFont;
        public boolean multiline = false, toggled = false, activated = false;
        public int alignment = Align.center;
        public int paddingLeft, paddingRight, paddingTop, paddingBottom;

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
