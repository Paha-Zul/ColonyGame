package com.mygdx.game.util.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.PlayerManager;

/**
 * Created by Paha on 8/12/2015.
 * Handles displaying Colony related stuff.
 */
public class ColonyWindow extends Window{
    private Rectangle colonyScreenRect = new Rectangle(), craftItemPreviewRectangle = new Rectangle();
    private TextureRegion colonyScreenBackground, darkBackground, textBackground;
    private GUI.GUIStyle colonyWindowStyle;

    private DataBuilder.JsonItem itemMousedOver  = null;

    public ColonyWindow(PlayerInterface playerInterface) {
        super(playerInterface);

        this.colonyWindowStyle = new GUI.GUIStyle();
        this.colonyScreenBackground = new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class));
        this.darkBackground = new TextureRegion(ColonyGame.assetManager.get("darkBackground", Texture.class));
        this.textBackground = new TextureRegion(ColonyGame.assetManager.get("plainBackground", Texture.class));
        this.draggable = true;
    }

    @Override
    public boolean update(SpriteBatch batch) {
        this.active = this.playerInterface.drawingColony;
        super.update(batch);

        if(this.active) {
            this.drawColonyScreen(batch);
            //This was for debugging...
            //GUI.Texture(new TextureRegion(this.playerInterface.blueSquare), batch, this.dragWindowRect);
        }

        return this.mousedState > 0;
    }

    private void drawColonyScreen(SpriteBatch batch){
        this.recordMouseState(GUI.Texture(this.colonyScreenBackground, batch, this.colonyScreenRect));

        this.drawColonyInventory(batch, PlayerManager.getPlayer("Player").colony.getInventory(), this.colonyScreenRect, this.colonyWindowStyle, this.colonyScreenBackground, this.darkBackground);
    }

    public DataBuilder.JsonItem drawColonyInventory(SpriteBatch batch, Inventory inventory, Rectangle rect, GUI.GUIStyle style, TextureRegion windowBackground, TextureRegion darkBackground) {
        //We need to use the global list of items.
        Array<String> itemList = DataBuilder.JsonItem.allItems;

        batch.setColor(Color.WHITE);
        style.alignment = Align.center;
        style.paddingTop = 0;
        int iconSize = 32;
        float labelWidth = 50;

        //Starting X and Y pos.
        float startX = rect.x + 10;
        float startY = rect.y + rect.height - iconSize - 10;
        int spacingY = 10, spacingX = 0;

        style.background = new TextureRegion(ColonyGame.assetManager.get("background", Texture.class));
        DataBuilder.JsonItem itemMousedOver = null;
        style.alignment = Align.center;

        //The number of icons we can draw in the Y direction that will fit the window.
        int numY = (int)(rect.height/(iconSize+spacingY));
        int numX = (int)(rect.width/(iconSize+labelWidth+spacingX));

//        batch.flush(); //Flush the current contents to it isn't affected by the clipping bounds...

//        Rectangle scissors = new Rectangle(); //The scissor area.
//        Rectangle clipBounds = new Rectangle(this.colonyScreenRect); //The bounds to use.
//        ScissorStack.calculateScissors(ColonyGame.UICamera, batch.getTransformMatrix(), clipBounds, scissors);
//        ScissorStack.pushScissors(scissors); //Push the scissors onto the stack.

        DataBuilder.JsonItem mousedItem = null;

        //Draw each item.
        for (int i = 0; i < itemList.size; i++) {
            //Get the itemName and itemAmount and the _icon
            String itemName = itemList.get(i);
            int itemAmount = inventory.getItemAmount(itemName);
            String label = "" + itemAmount;
            DataBuilder.JsonItem _itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
            TextureRegion _icon = _itemRef.iconTexture;

            float yPos = startY - (i%numY)*(iconSize+spacingY);
            float xPos = startX + (i/numY)*(iconSize+labelWidth+spacingX);

            //Draw the background texture, icon texture, and a label...
            int _state = GUI.Texture(darkBackground, batch, xPos, yPos, iconSize, iconSize);
            GUI.Texture(_icon, batch, xPos, yPos, iconSize, iconSize);
            GUI.Label(label, batch, xPos + iconSize, yPos, iconSize, iconSize, style);

            if (_state == GUI.OVER) mousedItem = _itemRef;
        }

        //Flush the contents and pop the scissors.
//        batch.flush();
//        ScissorStack.popScissors();

        if(mousedItem != null) {
            style.background = this.textBackground;
            Vector2 mouse = GH.getFixedScreenMouseCoords();
            GUI.Label(mousedItem.getDisplayName(), batch, mouse.x, mouse.y, -1, 25, style);
            style.background = null;
        }

        //Reset color and padding/alignment
        batch.setColor(Color.WHITE);
        style.alignment = Align.center;
        style.paddingLeft = 0;
        style.paddingTop = 0;
        style.background = null;

        return itemMousedOver;
    }

    @Override
    public void resize(int width, int height) {
        this.colonyScreenRect.set(width / 2 - 300, height / 2 - 200, 600, 400);
        this.craftItemPreviewRectangle.set(0,0,200,200);
        this.setMainWindowRect(this.colonyScreenRect);
    }
}
