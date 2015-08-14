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
import com.mygdx.game.util.GH;
import com.mygdx.game.util.worldgeneration.WorldGen;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

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

    public static final int NONE=0,OVER=1, JUSTDOWN=2, DOWN=3, JUSTUP=4, UP=5;

    private static boolean down, justDown, up, justUp;
    private static Rectangle rect1 = new Rectangle();

    private static TextureRegion whiteTexture;
    private static float boundsWidth = 0;


    static{
        font = defaultFont;
        whiteTexture = new TextureRegion(WorldGen.whiteTex);
    }

    /**
     * Draws a texture with the position and dimensions of the Rectangle passed in.
     * @param texture The Texture to draw.
     * @param batch The SpriteBatch to use.
     * @param rect The Rectangle to use for position and dimension.
     * @return 0 for not moused over, 1 for moused over, 2 for clicked down, 3 for just up.
     */
    public static int Texture(@NotNull TextureRegion texture, @NotNull SpriteBatch batch, Rectangle rect){
        return GUI.Texture(texture, batch, rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * Draws a texture.
     * @param texture The texture to draw.
     * @param batch The SpriteBatch to use.
     * @param x The x position to draw at.
     * @param y The y position to draw at.
     * @param width The width of the texture.
     * @param height The height of the texture.
     * @return 0 for not moused over, 1 for moused over, 2 for clicked down, 3 for just up.
     */
    public static int Texture(@NotNull TextureRegion texture, @NotNull SpriteBatch batch, float x, float y, float width, float height){
        batch.draw(texture, x, y, width, height);
        return GUI.getState(Rectangle.tmp.set(x, y, width, height));
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
    public static void DrawBar(SpriteBatch batch, float x, float y, float width, float height, float currVal, float maxVal, boolean displayText, @Nullable GUIStyle style, @Nullable Color color){
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

    /**
     * Draws a button using the GUIStyle passed in. If the style is null, uses a default style.
     * @param batch The SpriteBatch to use.
     * @param rect The Rectangle to use for boundaries.
     * @return GUI.NONE (0) for not moused over/clicked, GUI.OVER (1) if moused over but not clicked, GUI.DOWN (2) if clicked down,
     * GUI.UP (3) if just released.
     */
    public static int Button(SpriteBatch batch, Rectangle rect){
        return GUI.Button(batch, "", rect, null);
    }

    /**
     * Draws a button using the GUIStyle passed in. If the style is null, uses a default style.
     * @param batch The SpriteBatch to use.
     * @param text The Text to put on the button using the GUIStyle (or default if null) font properties.
     * @param rect The Rectangle to use for boundaries.
     * @return GUI.NONE (0) for not moused over/clicked, GUI.OVER (1) if moused over but not clicked, GUI.DOWN (2) if clicked down,
     * GUI.UP (3) if just released.
     */
    public static int Button(SpriteBatch batch, Rectangle rect, String text){
        return GUI.Button(batch, text, rect, null);
    }

    /**
     * Draws a button using the GUIStyle passed in. If the style is null, uses a default style.
     * @param batch The SpriteBatch to use.
     * @param text The Text to put on the button using the GUIStyle (or default if null) font properties.
     * @param x The left X position.
     * @param y The bottom Y position.
     * @param width The width of the button.
     * @param height The height of the button.
     * @param style The GUIStyle to use. If null, uses a default style.
     * @return GUI.NONE (0) for not moused over/clicked, GUI.OVER (1) if moused over but not clicked, GUI.DOWN (2) if clicked down,
     * GUI.UP (3) if just released.
     */
    public static int Button(SpriteBatch batch, String text, float x, float y, float width, float height, @Nullable GUIStyle style){
        rect1.set(x, y, width, height);
        return GUI.Button(batch, text, rect1, style);
    }

    /**
     * Draws a button using the GUIStyle passed in. If the style is null, uses a default style.
     * @param batch The SpriteBatch to use.
     * @param text The Text to put on the button using the GUIStyle (or default if null) font properties.
     * @param rect The Rectangle to use for boundaries.
     * @param style The GUIStyle to use. If null, uses a default style.
     * @return GUI.NONE (0) for not moused over/clicked, GUI.OVER (1) if moused over but not clicked, GUI.DOWN (2) if clicked down,
     * GUI.UP (3) if just released.
     */
    public static int Button(SpriteBatch batch, String text, Rectangle rect, @Nullable GUIStyle style){
        int state = NONE;

        if(style == null)
            style = defaultGUIStyle;

        Texture currTexture = style.normal;
        if(style.activated) currTexture = style.active;

        state = GUI.getState(rect);

        if(state == DOWN) //If down...
            currTexture = style.clicked;
        else if(state == UP) //If up...
            currTexture = style.moused;
        else if(state == OVER) //If over...
            currTexture = style.moused;

        batch.draw(currTexture, rect.x, rect.y, rect.getWidth(), rect.getHeight());
        BitmapFont.TextBounds bounds = font.getBounds(text);                                //Get the bounds of the text
        if(!text.isEmpty()) GUI.Label(text, batch, rect.x, rect.y, rect.getWidth(), rect.getHeight(), style);

        return state;
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

    /**
     * Draws a Label with text.
     * @param text The text for the label.
     * @param batch The SpriteBatch to use.
     * @param x The X position.
     * @param y The Y position.
     * @param width The width of the label (-1 can be used to indicate auto length)
     * @param height The height of the label.
     * @param style The GUIStyle of the label. If null, a default style will be used.
     * @return True for if the mouse is inside the label, false otherwise.
     */
    public static boolean Label(@NotNull String text, @NotNull SpriteBatch batch, float x, float y, float width, float height, GUIStyle style){
        if(style == null) style = defaultGUIStyle;
        BitmapFont.HAlignment alignment;

        //y += style.font.getLineHeight();
        BitmapFont.TextBounds bounds = style.font.getBounds(text);
        boundsWidth = bounds.width;

        if(width == -1) width = boundsWidth;

        float adjX = x, adjY = y;

        if(style.background != null)
            GUI.Texture(style.background, batch, x, y, width, height);

        adjX += style.paddingLeft; //Push to the right for left padding.
        width -= style.paddingLeft; //Since we pushed the X to the left, we must shrink the width by an equal amount.
        y -= style.paddingTop;
        height -= style.paddingBottom;

        //If center, push the X and Y towards the center by half the width/height
        if(style.alignment == Align.center){
            adjX += width/2 - boundsWidth/2;
            adjY += height/2 + bounds.height/2;
            alignment = BitmapFont.HAlignment.CENTER;

            //If left, leave X but center Y.
        }else if(style.alignment == Align.left) {
            adjY += height / 2 + bounds.height / 2;
            alignment = BitmapFont.HAlignment.LEFT;

        //If top left, leave X and Y
        }else if(style.alignment == Align.topLeft) {
            adjY += height;
            alignment = BitmapFont.HAlignment.LEFT;

        //If top, center X but leave Y.
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

        Rectangle.tmp.set(x, y, width, height);
        return Rectangle.tmp.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }

    public static int ImageLabel(TextureRegion image, String text, SpriteBatch batch, Rectangle imageRect, float textWidth){
        return GUI.ImageLabel(image, text, batch, imageRect.x, imageRect.y, imageRect.width, imageRect.height, textWidth);
    }

    public static int ImageLabel(TextureRegion image, String text, SpriteBatch batch, float x, float y, float imageWidth, float imageHeight, float textWidth){
        return GUI.ImageLabel(image, text, batch, x, y, imageWidth, imageHeight, textWidth, null);
    }

    public static int ImageLabel(TextureRegion image, String text, SpriteBatch batch, float x, float y, float imageWidth, float imageHeight, float textWidth, GUIStyle style){
        batch.draw(image, x, y, imageWidth, imageHeight);
        GUI.Label(text, batch, x + imageWidth, y, textWidth, imageHeight, style);

        return GUI.getState(Rectangle.tmp.set(x, y, imageWidth, imageHeight)); //Return if the image is being moused over.
    }

    public static String TextBox(String text, SpriteBatch batch, float x, float y){
        return "";
    }

    public static void ResetFont(){
        GUI.font = defaultFont;
        GUI.font.setColor(Color.WHITE);
        GUI.font.setScale(1f);
    }

    public static void update(){
        checkState();
    }

    private static void checkState(){
        boolean down = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        //If down but already just down or down... still down!
        if(down && (GUI.justDown || GUI.down)){
            GUI.down = true;
            GUI.up = GUI.justDown = false;

        //If down but not recently just down, justdown!
        }else if(down && !GUI.justDown) {
            GUI.justDown = true;
            GUI.up = GUI.down = false;

        }else if(!down && (GUI.down || GUI.justDown) && !GUI.justUp){
            GUI.justUp = true;
            GUI.up = GUI.down = GUI.justDown = false;
        //If not down and was recently just up
        }else if(!down && (GUI.justUp || GUI.up)){
            GUI.up = true;
            GUI.justUp = GUI.down = GUI.justDown = false;
        }
    }

    /**
     * Checks for the 'general' state of the mouse for the GUI system.
     * @return 2 (JUSTDOWN), 3 (DOWN), 4 (JUSTUP), 5 (UP).
     */
    public static int getState(){
        int state = UP;
        if(GUI.justDown){
            state = JUSTDOWN; //Set the state to just down.
        }else if(GUI.down){
            state = DOWN; //Set the state to down.
        }else if(GUI.justUp)
            state = JUSTUP; //Set the state to just up.
        else if(GUI.up)
            state = UP; //Set the state to up

        return state;
    }

    /**
     * Checks for the state of the mouse when interacting with a Rectangle area.
     * @param rect The Rectangle area to check the mouse state in.
     * @return 0 (NONE - not over), 1 (OVER), 2 (JUSTDOWN), 3 (DOWN), 4 (JUSTUP)
     */
    public static int getState(Rectangle rect){
        int state = NONE;
        if(rect.contains(GH.getFixedScreenMouseCoords())){
            state = OVER; //Set it to over if our mouse is inside.
            if(GUI.justDown){
                state = JUSTDOWN; //Set the state to just down.
            }else if(GUI.down){
                state = DOWN; //Set the state to down.
            }else if(GUI.justUp)
                state = JUSTUP; //Set the state to just up.
            else if(GUI.up)
                state = OVER; //Set the state to up
        }

        return state;
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
