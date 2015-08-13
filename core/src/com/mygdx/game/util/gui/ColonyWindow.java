package com.mygdx.game.util.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.PlayerManager;

/**
 * Created by Paha on 8/12/2015.
 * Handles displaying Colony related stuff.
 */
public class ColonyWindow extends Window{
    private Rectangle colonyScreenRect = new Rectangle();
    private TextureRegion colonyScreen;
    private GUI.GUIStyle colonyWindowStyle;

    public ColonyWindow(PlayerInterface playerInterface) {
        super(playerInterface);

        this.colonyWindowStyle = new GUI.GUIStyle();
        this.colonyScreen = new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class));
        this.draggable = true;
    }

    @Override
    public void update(float delta, SpriteBatch batch) {
        super.update(delta, batch);
        this.active = this.playerInterface.drawingColony;

        if(this.active)
            this.drawColonyScreen(batch);

        GUI.Texture(new TextureRegion(this.playerInterface.blueSquare), batch, this.dragWindowRect);
    }

    private void drawColonyScreen(SpriteBatch batch){
        this.recordMouseState(GUI.Texture(this.colonyScreen, batch, this.colonyScreenRect));

        this.drawColonyInventory(PlayerManager.getPlayer("Player").colony.getInventory(), this.colonyScreenRect, batch);
    }

    private void drawColonyInventory(Inventory inventory, Rectangle rect, SpriteBatch batch){
        //We need to use the global list of items.
        Array<String> itemList = DataBuilder.JsonItem.allItems;

        batch.setColor(Color.WHITE);
        this.colonyWindowStyle.alignment = Align.center;
        this.colonyWindowStyle.paddingTop = 0;
        int iconSize =  32;
        float labelWidth = 50;

        float craftButtonWidth = 50, craftButtonHeight = 32*0.5f;

        //Starting X and Y pos.
        float xPos = rect.x + 10;
        float yPos = rect.y + rect.height - iconSize - 10;
        this.colonyWindowStyle.background = new TextureRegion(ColonyGame.assetManager.get("background", Texture.class));
        TextureRegion _icon;

        //Draw each item.
        for(int i=0;i<itemList.size;i++){
            //Get the itemName and itemAmount and the _icon
            String itemName = itemList.get(i);
            int itemAmount = inventory.getItemAmount(itemName);
            String label = ""+itemAmount;
            DataBuilder.JsonItem _itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
            _icon = _itemRef.iconTexture;

            //Draw the label of the amount and the icon.
            GUI.ImageLabel(_icon, label, batch, xPos, yPos, iconSize, iconSize, labelWidth, this.colonyWindowStyle);
            if(!_itemRef.getItemCategory().equals("raw")) {
                int state = GUI.Button(batch, "Craft", xPos + iconSize + labelWidth + 10, yPos, craftButtonWidth, iconSize, null);
            }

            //Increment the yPos, if we're too far down, reset Y and shift X.
            yPos -= iconSize + 5;
            if(yPos < rect.y){
                xPos += iconSize + labelWidth + craftButtonWidth + 20;
                yPos = rect.y+rect.height - iconSize- 10;
            }
        }

        //Reset color and padding/alignment
        batch.setColor(Color.WHITE);
        this.colonyWindowStyle.alignment = Align.center;
        this.colonyWindowStyle.paddingLeft = 0;
        this.colonyWindowStyle.paddingTop = 0;
        this.colonyWindowStyle.background = null;
    }

    @Override
    public void resize(int width, int height) {
        this.colonyScreenRect.set(width / 2 - 300, height / 2 - 200, 600, 400);
        this.setMainWindowRect(this.colonyScreenRect);

    }
}
