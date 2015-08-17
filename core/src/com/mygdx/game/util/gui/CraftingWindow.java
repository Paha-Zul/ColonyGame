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
import com.mygdx.game.util.ItemNeeded;
import com.mygdx.game.util.managers.DataManager;
import com.sun.istack.internal.Nullable;

/**
 * Created by Paha on 8/16/2015.
 * A Window for interacting with Entities that provide crafting options.
 */
public class CraftingWindow extends Window{
    private Rectangle craftingWindowRect;
    private TextureRegion craftingBackground, darkBackground;

    public CraftingWindow(PlayerInterface playerInterface) {
        super(playerInterface);

        this.craftingWindowRect = new Rectangle();
        this.craftingBackground = new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class));
        this.darkBackground = new TextureRegion(ColonyGame.assetManager.get("darkBackground", Texture.class));

    }

    @Override
    public boolean update(SpriteBatch batch) {
        if(this.active) {
            GUI.Texture(this.craftingBackground, batch, this.craftingWindowRect);
        }

        return super.update(batch);
    }

    private void drawColonyInventory(SpriteBatch batch, Inventory inventory, Rectangle rect, GUI.GUIStyle style, TextureRegion windowBackground, TextureRegion darkBackground){
        //We need to use the global list of items.
        Array<String> itemList = DataBuilder.JsonItem.allItems;

        batch.setColor(Color.WHITE);
        style.alignment = Align.center;
        style.paddingTop = 0;
        int iconSize =  32;
        float labelWidth = 50;

        float craftButtonWidth = 50, craftButtonHeight = 32*0.5f;

        //Starting X and Y pos.
        float xPos = rect.x + 10;
        float yPos = rect.y + rect.height - iconSize - 10;
        style.background = new TextureRegion(ColonyGame.assetManager.get("background", Texture.class));
        DataBuilder.JsonItem itemMousedOver = null;
        this.playerInterface.UIStyle.alignment = Align.center;

//        batch.flush(); //Flush the current contents to it isn't affected by the clipping bounds...

//        Rectangle scissors = new Rectangle(); //The scissor area.
//        Rectangle clipBounds = new Rectangle(this.colonyScreenRect); //The bounds to use.
//        ScissorStack.calculateScissors(ColonyGame.UICamera, batch.getTransformMatrix(), clipBounds, scissors);
//        ScissorStack.pushScissors(scissors); //Push the scissors onto the stack.

        DataBuilder.JsonItem mousedItem = null;

        //Draw each item.
        for(int i=0;i<itemList.size;i++){
            //Get the itemName and itemAmount and the _icon
            String itemName = itemList.get(i);
            int itemAmount = inventory.getItemAmount(itemName);
            String label = ""+itemAmount;
            DataBuilder.JsonItem _itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
            TextureRegion _icon = _itemRef.iconTexture;

            //Draw the background texture, icon texture, and a label...
            int _state = GUI.Texture(this.darkBackground, batch, xPos, yPos, iconSize, iconSize);
            GUI.Texture(_icon, batch, xPos, yPos, iconSize, iconSize);
            GUI.Label(label, batch, xPos + iconSize, yPos, iconSize, iconSize, this.playerInterface.UIStyle);

            if(_state == GUI.OVER) mousedItem = _itemRef;

            if(!_itemRef.getItemCategory().equals("raw")) {
                //iconSize*2 cause 1 for the icon and 1 for the label, which also uses iconSize
                if(GUI.Button(batch, "Craft", xPos + iconSize*2 + 10, yPos, craftButtonWidth, iconSize, null) > 0)
                    itemMousedOver = _itemRef;
            }

            //Increment the yPos, if we're too far down, reset Y and shift X.
            yPos -= iconSize + 5;
            if(yPos < rect.y){
                xPos += iconSize + labelWidth + craftButtonWidth + 20;
                yPos = rect.y+rect.height - iconSize- 10;
            }
        }

        if(mousedItem != null) {
            this.playerInterface.UIStyle.background = this.darkBackground;
            Vector2 mouse = GH.getFixedScreenMouseCoords();
            GUI.Label(mousedItem.getDisplayName(), batch, mouse.x, mouse.y, -1, 25, this.playerInterface.UIStyle);
            this.playerInterface.UIStyle.background = null;
        }

        //Flush the contents and pop the scissors.
//        batch.flush();
//        ScissorStack.popScissors();

        if(itemMousedOver != null){
            //TODO Add a timer to delay the crafting popup?
            this.drawCraftingInfo(itemMousedOver, batch, this.craftingWindowRect, this.playerInterface.UIStyle, this.craftingBackground);
        }

        //Reset color and padding/alignment
        batch.setColor(Color.WHITE);
        style.alignment = Align.center;
        style.paddingLeft = 0;
        style.paddingTop = 0;
        style.background = null;
    }

    /**
     * Draws the crafting info for an item inside a Rectangle.
     * @param itemRef The item reference.
     * @param batch The SpriteBatch to draw with.
     * @param rect The Rectangle to draw inside.
     * @param style The GUIStyle to draw with. This may be null.
     */
    private void drawCraftingInfo(DataBuilder.JsonItem itemRef, SpriteBatch batch, Rectangle rect, @Nullable GUI.GUIStyle style, TextureRegion windowTexture){
        Vector2 mouse = GH.getFixedScreenMouseCoords(); //Get fixed mouse coords
        rect.setPosition(mouse.x, mouse.y); //Set position of the rectangle
        GUI.Texture(windowTexture, batch, rect); //Draw the texture
        Array<ItemNeeded> mats = itemRef.materialsForCrafting, raw = itemRef.rawForCrafting; //Get the lists.

        float iconSize = 32;
        float labelWidth = 50;
        float startX = rect.x + labelWidth;
        float startY = rect.y + 20;
        float spacingX = iconSize + 5;
        float spacingY = iconSize + 10;
        TextureRegion _icon;
        DataBuilder.JsonItem _item;

        style.alignment = Align.left;
        style.paddingLeft = 5;
        GUI.Label("Raw:", batch, rect.x, startY, labelWidth, iconSize, style);
        style.alignment = Align.center;
        style.paddingLeft = 0;

        //Draw the raw mats first
        for(int i=0;i<raw.size;i++){
            _item = DataManager.getData(raw.get(i).itemName, DataBuilder.JsonItem.class);
            _icon = _item.iconTexture;
            float x = startX + spacingX*i, y = startY;
            GUI.Texture(_icon, batch, x, startY, iconSize, iconSize);
            GUI.Label("x"+raw.get(i).amountNeeded, batch, x, startY - 15, iconSize, 15, style);
        }

        style.alignment = Align.left;
        style.paddingLeft = 5;
        GUI.Label("Mats:", batch, rect.x, startY + spacingY, labelWidth, iconSize, style);
        style.alignment = Align.center;
        style.paddingLeft = 0;

        //Draw the materials
        for(int i=0;i<mats.size;i++){
            _item = DataManager.getData(mats.get(i).itemName, DataBuilder.JsonItem.class);
            _icon = _item.iconTexture;
            float x = startX + spacingX*i, y = startY + spacingY;
            GUI.Texture(_icon, batch, startX + spacingX*i, startY + spacingY, iconSize, iconSize);
            GUI.Label("x"+mats.get(i).amountNeeded, batch, x, y - 15, iconSize, 15, style);
        }

        //Draw the name of the item above the icon...
        GUI.Label(""+itemRef.getDisplayName(), batch, rect.x, rect.y + rect.height - 25,
                rect.width, 25);

        //Draw the icon for the item we are crafting...
        GUI.Texture(itemRef.iconTexture, batch, rect.x + rect.width / 2 - iconSize / 2,
                rect.y + rect.height - iconSize - 25, iconSize, iconSize);
    }


    @Override
    public void resize(int width, int height) {
        this.craftingWindowRect = new Rectangle(width/2 - 400/2, height/2 - 400/2, 400, 400);
        this.setMainWindowRect(this.craftingWindowRect);
    }
}
