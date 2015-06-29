package com.mygdx.game.util.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.mygdx.game.util.worldgeneration.WorldGen;
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

    private static TextureRegion whiteTexture;
    private static float boundsWidth = 0;

    static{
        font = defaultFont;
        whiteTexture = new TextureRegion(WorldGen.whiteTex);
    }

    public static void Texture(@NotNull TextureRegion texture, @NotNull SpriteBatch batch, Rectangle rect){
        batch.draw(texture, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    public static void Texture(@NotNull TextureRegion texture, @NotNull SpriteBatch batch, float x, float y, float width, float height){
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
        GUI.Texture(new TextureRegion(WorldGen.whiteTex), batch, outerX, y, width, height);

        //Draw the inner rectangle (shrink it by 2 inches on all sides, 'padding')
        batch.setColor(color);
        float newWidth = (currVal/maxVal)*(width-4);
        GUI.Texture(whiteTexture, batch, innerX, y + 2, newWidth, height - 4);

        if(displayText) GUI.Label((int)currVal+"/"+(int)maxVal, batch, outerX, y, width, height);

        batch.setColor(batchColor);
    }

    /**
     * Draws a bar for a percentage
     * @param x The x location to start at.
     * @param y The Y location to start at.
     * @param width The width of the bar.
     * @param height The height of the bar.
     * @param percentage The percentage completed.
     */
    public static void DrawBar(SpriteBatch batch, float x, float y, float width, float height, float percentage, boolean displayText, GUIStyle style, Color color){
        if(style == null) style = defaultGUIStyle;
        if(color == null) color = Color.GREEN;

        style.alignment = Align.left;
        style.alignment = Align.center;

        float outerX = x;
        float innerX = x + 2;

        Color batchColor = batch.getColor();
        //Draw the out rectangle
        batch.setColor(Color.BLACK);
        GUI.Texture(new TextureRegion(WorldGen.whiteTex), batch, outerX, y, width, height);

        //Draw the inner rectangle (shrink it by 2 inches on all sides, 'padding')
        batch.setColor(color);
        float newWidth = percentage*(width-4);
        GUI.Texture(whiteTexture, batch, innerX, y + 2, newWidth, height - 4);

        if(displayText) GUI.Label(""+(int)(percentage*100), batch, outerX, y, width, height);

        batch.setColor(batchColor);
    }

    public static boolean Button(SpriteBatch batch, Rectangle rect){
        return GUI.Button(batch, "", rect, null);
    }

    public static boolean Button(SpriteBatch batch, Rectangle rect, String text){
        return GUI.Button(batch, text, rect, null);
    }

    public static boolean Button(SpriteBatch batch, String text, float x, float y, float width, float height, GUIStyle style){
        rect1.set(x, y, width, height);
        return GUI.Button(batch, text, rect1, style);
    }

    public static boolean Button(SpriteBatch batch, String text, Rectangle rect, GUIStyle style){
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
        if(!text.isEmpty()) GUI.Label(text, batch, rect.x, rect.y, rect.getWidth(), rect.getHeight(), style);

        return clicked;
    }

    public static boolean Label(@NotNull String text, @NotNull SpriteBatch batch, @NotNull Rectangle rect){
        return GUI.Label(text, batch, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), null);
    }

    public static boolean Label(@NotNull String text, @NotNull SpriteBatch batch, @NotNull Rectangle rect, GUIStyle style){
        return GUI.Label(text, batch, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight(), style);
    }

    public static boolean Label(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, float width, float height){
        return GUI.Label(text, batch, x, y, width, height, null);
    }

    public static boolean Label(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, float width, float height, GUIStyle style){
        if(style == null) style = defaultGUIStyle;
        BitmapFont.HAlignment alignment;
        boolean mouseInside = false;
        Rectangle.tmp.set(x, y, width, height);
        if(Rectangle.tmp.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()))
            mouseInside = true;

        if(style.background != null)
            GUI.Texture(style.background, batch, x, y, width, height);

        //y += style.font.getLineHeight();
        BitmapFont.TextBounds bounds = font.getBounds(text);
        boundsWidth = bounds.width;

        float adjX = x, adjY = y;

        adjX += style.paddingLeft; //Push to the right for left padding.
        width -= style.paddingLeft; //Since we pushed the X to the left, we must shrink the width by an equal amount.
        y -= style.paddingTop;
        height -= style.paddingBottom;


        if(style.alignment == Align.center){
            adjX += width/2 - boundsWidth/2;
            adjY += height/2 + bounds.height/2;
            alignment = BitmapFont.HAlignment.CENTER;

        }else if(style.alignment == Align.left) {
            adjY += height / 2 + bounds.height / 2;
            alignment = BitmapFont.HAlignment.LEFT;

        }else if(style.alignment == Align.topLeft) {
            adjY += height;
            alignment = BitmapFont.HAlignment.LEFT;

        }else if(style.alignment == Align.top){
            adjY += height;
            adjX += width/2 - boundsWidth/2;
            alignment = BitmapFont.HAlignment.CENTER;

        }else
            alignment = BitmapFont.HAlignment.LEFT;

        if(adjX >= x) x = adjX;
        if(adjY >= y) y = adjY;

        //Shrink these after...
        width -= style.paddingRight; //Shrink the width by the right padding.
        height -= style.paddingTop; //Shrink the height by the top padding.

        if(!style.multiline && !style.wrap) style.font.draw(batch, text, x, y);
        else if(!style.wrap) style.font.drawMultiLine(batch, text, x, y);
        else style.font.drawWrapped(batch, text, x, y, width, alignment);

        return mouseInside;
    }

    public static String TextBox(String text, SpriteBatch batch, float x, float y){
        return "";
    }

    public static void ResetFont(){
        GUI.font = defaultFont;
        GUI.font.setColor(Color.WHITE);
        GUI.font.setScale(1f);
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

    public static class GUIStyle {
        public Texture normal = GUI.defaultNormalButton;
        public Texture moused = GUI.defaultMousedButton;
        public Texture clicked = GUI.defaultClickedButton;
        public Texture active = GUI.defaultClickedButton;

        public BitmapFont font = defaultFont;
        public boolean multiline = false, toggled = false, activated = false, wrap = false;
        public int alignment = Align.center;
        public int paddingLeft, paddingRight, paddingTop, paddingBottom;
        public TextureRegion background;

        public GUIStyle(){

        }

        public GUIStyle(GUIStyle style) {
            this.normal = style.normal;
            this.moused = style.moused;
            this.clicked = style.clicked;
            this.active = style.active;
            this.font = new BitmapFont(style.font.getData().fontFile);
            this.multiline = style.multiline;
            this.toggled = style.toggled;
            this.activated = style.activated;
            this.wrap = style.wrap;
            this.alignment = style.alignment;
            this.paddingLeft = style.paddingLeft;
            this.paddingRight = style.paddingRight;
            this.paddingTop = style.paddingTop;
            this.paddingBottom = style.paddingBottom;
            this.background = style.background;
        }

        public void padding(int amt){
            paddingLeft = paddingBottom = paddingRight = paddingTop = amt;
        }
    }
}
