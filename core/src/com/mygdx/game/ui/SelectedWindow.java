package com.mygdx.game.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.BehaviourManagerComp;
import com.mygdx.game.component.Constructable;
import com.mygdx.game.component.CraftingStation;
import com.mygdx.game.component.Stats;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.ItemNeeded;
import com.mygdx.game.util.StateTree;
import com.mygdx.game.util.Tree;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.DataManager;

import java.util.ArrayList;

/**
 * Created by Paha on 8/12/2015.
 * A Window to display information about the selected entities.
 */
public class SelectedWindow extends Window{
    private Rectangle uiBackgroundTopRect = new Rectangle(); //What is this?
    private Rectangle infoTopRect = new Rectangle();
    private Rectangle statusTopRect = new Rectangle();
    private Rectangle tabsTopRect = new Rectangle();
    private Rectangle ordersTopRect = new Rectangle();

    private Rectangle uiBackgroundBaseRect = new Rectangle(); //What is this?
    private Rectangle infoRect = new Rectangle();
    private Rectangle statusRect = new Rectangle();
    private Rectangle tabsRect = new Rectangle();
    private Rectangle ordersRect = new Rectangle();
    private Rectangle orderButtonRect = new Rectangle();

    private final float mainHeight = 0.12f;
    private final float topHeight = 0.03f;
    private final float infoWidth = 0.13f;
    private final float statusWidth = 0.135f;
    private final float tabsWidth = 0.262f;
    private final float ordersWidth = 0.479f;

    private TextureRegion UIBackgroundBase, UIBackgroundTop;

    public SelectedWindow(PlayerInterface playerInterface) {
        super(playerInterface, null);

        this.UIBackgroundBase = new TextureRegion(ColonyGame.assetManager.get("UIBackground_base", Texture.class));
        this.UIBackgroundTop = new TextureRegion(ColonyGame.assetManager.get("UIBackground_top", Texture.class));
    }

    @Override
    public boolean update(SpriteBatch batch) {
        super.update(batch);
        //Determine if the window is active by checking the selected profile list size...
        this.active = this.playerInterface.getSelectedProfileList().size > 0;
        if(this.active) {
            this.drawSelectedEntities(batch);
        }

        return this.mousedState > 0;
    }

    private void drawSelectedEntities(SpriteBatch batch){
        //Draw stuff about the selectedEntity entity.
        if(this.playerInterface.getSelectedProfile() != null || this.playerInterface.getSelectedProfileList().size > 0){
            this.recordMouseState(GUI.Texture(this.UIBackgroundBase, batch, this.uiBackgroundBaseRect));
            this.recordMouseState(GUI.Texture(this.UIBackgroundTop, batch, this.uiBackgroundTopRect));

            this.drawMultipleProfiles(batch, this.ordersRect);
            this.drawSelected(batch);
        }
    }

    /**
     * Displays the selectedEntity Entity.
     */
    private void drawSelected(SpriteBatch batch){
        //Make sure the getSelectedProfile() we have selectedEntity isn't null!
        if(this.playerInterface.getSelectedProfile() != null){
            PlayerInterface.UnitProfile profile = this.playerInterface.getSelectedProfile();

            //If the list still has profiles and the currently selected profile is dead (not alive), choose a new profile to display.
            if(this.playerInterface.getSelectedProfileList().size > 1 && !this.playerInterface.getSelectedProfile().entity.getTags().hasTag("alive")){
                this.playerInterface.getSelectedProfileList().forEach(prof -> {if(prof.entity != this.playerInterface.getSelectedProfile().entity) this.playerInterface.setSelectedProfile(prof);});
            }

            IInteractable innerInter = profile.interactable.getInteractable(); //Get the getSelectedProfile()!
            if(innerInter == null) {
                this.playerInterface.setSelectedProfile(null);
                return;
            }

            //If it has a compName, draw the compName...
            if(innerInter.getName() != null) {
                GUI.Label(innerInter.getName(), batch, this.infoTopRect, this.playerInterface.UIStyle);
            }

            //If it has stats, draw the stats...
            if(innerInter.getStats() != null){
                GUI.Label("Stats", batch, this.statusTopRect, this.playerInterface.UIStyle);

                //GUI.Texture(statusRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                Stats stats = innerInter.getStats();
                Rectangle rect = this.statusRect;

                ArrayList<Stats.Stat> list = stats.getStatList();
                float space = ((rect.height - list.size()*20)/list.size()+1)/2;
                float x = rect.x + 10;
                float y = rect.y + rect.height - 20;
                float barWidth = rect.getWidth()*0.4f;
                float barHeight = barWidth*0.2f;
                float labelWidth = 50;

                //Draw the stats!
                for(int i=0;i<list.size();i++){
                    Stats.Stat stat = list.get(i);
                    float _y = y - (i + 1) * space - 20 * i;
                    GUI.Label(stat.name, batch, x, _y, labelWidth, barHeight);
                    GUI.DrawBar(batch, x + labelWidth, _y, barWidth, barHeight, stat.getCurrVal(), stat.getMaxVal(), true, null, null);
                }

                //If no stats, maybe it has stat text?
            }else if(innerInter.getStatsText() != null){
                GUI.Label("Resources", batch, this.statusTopRect, this.playerInterface.UIStyle);
                this.playerInterface.UIStyle.multiline = true;
                this.playerInterface.UIStyle.alignment = Align.topLeft;
                this.playerInterface.UIStyle.paddingLeft = 10;
                this.playerInterface.UIStyle.paddingTop = 5;
                GUI.Label(innerInter.getStatsText(), batch, this.statusRect, this.playerInterface.UIStyle);
                this.playerInterface.UIStyle.paddingLeft = 0;
                this.playerInterface.UIStyle.paddingTop = 0;
                this.playerInterface.UIStyle.alignment = Align.center;
                this.playerInterface.UIStyle.multiline = false;
            }

            //If it has an inventory, draw the inventory...
            if(innerInter.getInventory() != null){
                //GUI.Texture(tabsRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                GUI.Label("Inventory", batch, this.tabsTopRect, this.playerInterface.UIStyle);
                this.playerInterface.drawInventory(innerInter.getInventory(), this.tabsRect);
            }

            if(innerInter.getBehManager() != null){
                this.drawBehaviourInformation(batch, ordersRect, innerInter);

            //If it has a constructable...
            }else if(innerInter.getConstructable() != null){
                Constructable constructable = innerInter.getConstructable();
                Rectangle rect = this.ordersRect;
                GUI.Label("constructing", batch, rect.x, rect.y + rect.height-20, rect.width, 20);
                GUI.Label("Progress", batch, rect.x, rect.y + rect.height - 40, rect.width, 20);
                GUI.DrawBar(batch, rect.x + rect.width/2 - 50, rect.y + rect.height - 60, 100, 20, constructable.getPercentageDone(), true, null, null);

                GUI.Label("Items needed:", batch, rect.x, rect.y + rect.height - 50 - 10, 50, 100);
                Array<ItemNeeded> list = constructable.getItemsNeeded();
                for(int i=0;i<list.size;i++) {
                    Rectangle.tmp.set(rect.x, rect.y + rect.height - 50 - i * 25, 25, 25);
                    GUI.ImageLabel(DataManager.getData(list.get(i).itemName, DataBuilder.JsonItem.class).iconTexture, ""+list.get(i).amountNeeded, batch, Rectangle.tmp, 100);
                }

            //If it has a crafting station...
            }else if(innerInter.getCraftingStation() != null){
                CraftingStation station = innerInter.getCraftingStation();
                if(GUI.Button(batch, "Crafting", this.ordersRect, null) == GUI.JUSTUP){
                    this.playerInterface.getWindowManager().addWindowIfNotExistByTarget(CraftingWindow.class, station.getEntityOwner(), this.playerInterface);
                }
            }
        }
    }

    private void drawBehaviourInformation(SpriteBatch batch, Rectangle rect, IInteractable interactable){
        GUI.Label("currTask: "+interactable.getBehManager().getCurrentTaskName(), batch, rect.x, rect.y + 70, rect.width, rect.height - 50, this.playerInterface.UIStyle);
        GUI.Label("nextTask: "+interactable.getBehManager().getNextTaskName(), batch, rect.x, rect.y + 60, rect.width, rect.height - 50, this.playerInterface.UIStyle);
        GUI.Label("currState: "+interactable.getBehManager().getBehaviourStates().getCurrState().stateName, batch, rect.x, rect.y + 50, rect.width, rect.height - 50, this.playerInterface.UIStyle);

        //If it's a humanoid that we can control, draw some order buttons and its current path.
        if(this.playerInterface.getSelectedProfile().interactable.interType.equals("humanoid")){
            GUI.Label("Orders", batch, this.ordersTopRect, this.playerInterface.UIStyle);

            //GUI.Texture(ordersRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
            if(interactable.getBehManager() != null) {
                this.drawBehaviourButtons(batch, interactable);

                //Set to saveContainer camera and draw the path lines.
                batch.setProjectionMatrix(ColonyGame.camera.combined);
                BehaviourManagerComp.Line[] lines = interactable.getBehManager().getLines();
                for(BehaviourManagerComp.Line line : lines)
                    batch.draw(this.playerInterface.blueSquare, line.startX, line.startY, 0, 0, line.width, 0.1f, 1, 1, line.rotation, 0, 0, this.playerInterface.blueSquare.getWidth(), this.playerInterface.blueSquare.getHeight(), false, false);

                //Set back to UI camera.
                batch.setProjectionMatrix(ColonyGame.UICamera.combined);

            }
        }
    }

    //Draws the buttons for each selectedEntity colonist that we have control of.
    private void drawBehaviourButtons(SpriteBatch batch, IInteractable interactable){
        StateTree<BehaviourManagerComp.TaskInfo> tree = interactable.getBehManager().getTaskTree();

        //TODO Need to make sure multiple selections work since this was changed.
        Rectangle rect = Rectangle.tmp;
        rect.set(ordersRect.x, ordersRect.y, ordersRect.width*0.8f, ordersRect.height);

        //Set some position variables.
        float width = (rect.getWidth()/(tree.getCurrentTreeNode().getChildren().size+1));
        float height = rect.y + 25;
        float x = rect.x;

        //Get the children of the current root node. This will display all children buttons of our current selection.
        Array<Tree.TreeNode<BehaviourManagerComp.TaskInfo>> nodeList = tree.getCurrentTreeNode().getChildren();
        for(int i=0;i<nodeList.size;i++) {
            //Get the task node and its user data.
            Tree.TreeNode currTaskNode = nodeList.get(i);
            BehaviourManagerComp.TaskInfo taskInfo = (BehaviourManagerComp.TaskInfo)currTaskNode.userData;

            //Get the GUIStyle from the taskInfo's object data. If it's null, get the default style from the GUI class.
            Object userData = taskInfo.userData;
            GUI.GUIStyle style;
            if(userData != null) {
                style = (GUI.GUIStyle) userData;
                style.activated = taskInfo.active;
            }else
                style = GUI.defaultGUIStyle;

            //Set the location and draw the button. If clicked, we need to do some tricky things...
            this.orderButtonRect.set(x + (i + 1) * width, height, 50, 50);
            if (GUI.Button(batch, currTaskNode.nodeName, orderButtonRect, style) == GUI.JUSTUP) {
                taskInfo.doCallback();

                //For each profile selectedEntity, tell them to gather.
                for (PlayerInterface.UnitProfile profile : this.playerInterface.getSelectedProfileList()) {
                    //Skip over the currently selected profile because we already directly told him to do it.
                    if(profile.entity == this.playerInterface.getSelectedProfile().entity) continue;

                    //Get the BehaviourComponent and TreeNode.
                    BehaviourManagerComp comp = profile.interactable.getInteractable().getBehManager();
                    Tree.TreeNode treeNode = comp.getTaskTree().getNode(node -> node.nodeName.equals(currTaskNode.nodeName));
                    BehaviourManagerComp.TaskInfo profTaskInfo = (BehaviourManagerComp.TaskInfo)treeNode.userData;

                    //Do the callback and sync the active booleans. Assign this profile's active state to the main selectedEntity profile.
                    profTaskInfo.doCallback();
                    ((BehaviourManagerComp.TaskInfo) treeNode.userData).active = taskInfo.active; //Toggle the treeNode.

                    //If it's toggled, add the tag, otherwise, remove the tag.
                    if(((BehaviourManagerComp.TaskInfo) treeNode.userData).active) comp.getBlackBoard().resourceTypeTags.addTag(treeNode.nodeName);
                    else comp.getBlackBoard().resourceTypeTags.removeTag(treeNode.nodeName);
                }

                //Get the node from the gather TreeNode. If it has children, set the currStateNode
                Tree.TreeNode tmpNode = interactable.getBehManager().getTaskTree().getNode(node -> node.nodeName.equals(currTaskNode.nodeName));
                if (tmpNode.hasChildren()) {
                    tree.setCurrentTreeNode(tmpNode);
                }
            }
        }
    }

    /**
     * Draws the multiple selectedEntity profile buttons for selecting from the list of profiles.
     * @param rect The rectangle to draw the information inside of.
     */
    private void drawMultipleProfiles(SpriteBatch batch, Rectangle rect){

        //If there is more than one unit selectedEntity.. display in a group format.
        if(this.playerInterface.getSelectedProfileList().size > 1){
            Rectangle.tmp.set(rect.getX() + rect.getWidth() - 60, rect.getY() + rect.getHeight() - 20, 50, 20);

            //Some things.
            float buttonWidth = 50, buttonHeight = 20;
            float spacingX = 5, spacingY = 5;
            int numY = (int)(rect.getHeight()/(buttonHeight+spacingY));
            float startX = rect.getX() + rect.getWidth() - buttonWidth - 10;
            float startY = rect.getY() + spacingY;

            //For each profile, draw a button to access each individual entity.
            for(int i=0;i<this.playerInterface.getSelectedProfileList().size;i++) {
                PlayerInterface.UnitProfile profile = this.playerInterface.getSelectedProfileList().get(i);
                if(!profile.entity.getTags().hasTag("alive")){
                    profile.entity.getTags().removeTag("selected");
                    this.playerInterface.getSelectedProfileList().removeIndex(i);
                    i--;
                    continue;
                }

                //Some math that positions every correctly.
                Rectangle.tmp.set(startX - (i/numY)*(buttonWidth+spacingX), startY + (i%numY)*(buttonHeight + spacingY), buttonWidth, buttonHeight);

                //Draw the button for the individual profile. If clicked, make it our selected profile.
                if(GUI.Button(batch, Rectangle.tmp, profile.interactable.getInteractable().getName()) == GUI.UP)
                    this.playerInterface.setSelectedProfile(profile);
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        this.uiBackgroundBaseRect.set(0, 0, width, height * mainHeight);
        this.infoRect.set(0, 0, width * infoWidth, height* mainHeight);
        this.statusRect.set(infoRect.x + infoRect.width, 0, width*statusWidth, height*mainHeight);
        this.tabsRect.set(statusRect.x + statusRect.width, 0, width * tabsWidth, height * mainHeight);
        this.ordersRect.set(tabsRect.x + tabsRect.width, 0, width - (tabsRect.x + tabsRect.width), height * mainHeight);

        this.uiBackgroundTopRect.set(0, uiBackgroundBaseRect.y + uiBackgroundBaseRect.height, width, height * topHeight);
        this.infoTopRect.set(uiBackgroundTopRect.x, uiBackgroundTopRect.y, width * infoWidth, uiBackgroundTopRect.height); //The top info area
        this.statusTopRect.set(infoTopRect.x + infoTopRect.width, uiBackgroundTopRect.y, width*statusWidth, uiBackgroundTopRect.height); //The top status area
        this.tabsTopRect.set(statusTopRect.x + statusTopRect.width, uiBackgroundTopRect.y, width * tabsWidth, uiBackgroundTopRect.height); //The top tabs area
        this.ordersTopRect.set(tabsTopRect.x + tabsTopRect.width, uiBackgroundTopRect.y, width - (tabsRect.x + tabsRect.width), uiBackgroundTopRect.height); //The top orders ares
    }
}
