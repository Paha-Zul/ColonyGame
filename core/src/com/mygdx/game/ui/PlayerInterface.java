package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.BehaviourManagerComp;
import com.mygdx.game.component.GridComponent;
import com.mygdx.game.component.Interactable;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.Profiler;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.ListHolder;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.server.ServerPlayer;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class PlayerInterface extends UI implements IGUI, InputProcessor {
    private SpriteBatch batch;
    private Texture background;
    private World world;
    private ServerPlayer gameScreen;

    private boolean drawingInfo = false;
    private boolean drawingProfiler = false;
    private boolean onUI = false;
    private boolean mouseDown = false;

    private Rectangle buttonRect = new Rectangle();
    private Rectangle infoRect = new Rectangle();
    private float FPS = 0;

    private static final float camMoveSpeed = 500f;
    private static final float camZoomSpeed = 1.5f;

    private final float leftRectPerc = 0.1f;
    private final float rightRectPerc = 0.1f;
    private final float centerRectPerc = 0.1f;

    private Rectangle leftRect = new Rectangle();
    private Rectangle rightRect = new Rectangle();
    private Rectangle centerRect = new Rectangle();
    private Rectangle leftCenterRect = new Rectangle();

    private Rectangle selectionBox = new Rectangle();
    private Rectangle profileButtonRect = new Rectangle();

    private Interactable interactable = null;

    private Timer FPSTimer;

    private Vector2 testPoint = new Vector2();
    private Entity selected = null;
    private GridComponent gridComp;

    private Color gray = new Color(Color.BLACK);

    private GUI.GUIStyle gatherGUIStyle = new GUI.GUIStyle();
    private GUI.GUIStyle exploreGUIStyle = new GUI.GUIStyle();


    private void loadGatherButtonStyle(){
        gatherGUIStyle = new GUI.GUIStyle();
        gatherGUIStyle.normal = ColonyGame.assetManager.get("axebutton_normal", Texture.class);
        gatherGUIStyle.moused = ColonyGame.assetManager.get("axebutton_moused", Texture.class);
        gatherGUIStyle.clicked = ColonyGame.assetManager.get("axebutton_clicked", Texture.class);
    }

    private void loadExploreButtonStyle(){
        exploreGUIStyle = new GUI.GUIStyle();
        exploreGUIStyle.normal = ColonyGame.assetManager.get("explorebutton_normal", Texture.class);
        exploreGUIStyle.moused = ColonyGame.assetManager.get("explorebutton_moused", Texture.class);
        exploreGUIStyle.clicked = ColonyGame.assetManager.get("explorebutton_clicked", Texture.class);
    }

    //For selecting a single unit.
    private QueryCallback callback = fixture -> {
        if(fixture.testPoint(testPoint.x, testPoint.y)){
            this.selected = (Entity)fixture.getBody().getUserData();
            this.interactable = this.selected.getComponent(Interactable.class);
            this.gridComp = this.selected.getComponent(GridComponent.class);
            return false;
        }

        return true;
    };

    //For selecting multiple units.
    private ArrayList<UnitProfile> selectedList = new ArrayList<>();
    private QueryCallback selectionCallback = fixture -> {
        Entity selected = (Entity)fixture.getBody().getUserData();
        if(selected != null && selected.hasTag(Constants.ENTITY_COLONIST)) {
            UnitProfile profile = new UnitProfile();
            profile.entity = this.selected = selected;
            profile.interactable = this.interactable = selected.getComponent(Interactable.class);
            profile.gridComp = this.gridComp = selected.getComponent(GridComponent.class);
            selectedList.add(profile);
            return true;
        }
        return true;
    };

    /**
     * A player interface Component that will display information on the screen.
     * @param batch The SpriteBatch for drawing to the screen.
     * @param world The Box2D world. We need to know about this for clicking on objects.
     */
    public PlayerInterface(SpriteBatch batch, ColonyGame game, ServerPlayer gameScreen, World world) {
        super(batch, game);
        this.batch = batch;
        this.world = world;
        this.gameScreen = gameScreen;

        this.background = ColonyGame.assetManager.get("background", Texture.class);

        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);

        Functional.Callback callback = () -> this.FPS = 1/Gdx.graphics.getDeltaTime();
        this.FPSTimer = new RepeatingTimer(0.5d, callback);

        //Loads and configures stuff about buttons tyles.
        loadGatherButtonStyle();
        loadExploreButtonStyle();
        gatherGUIStyle.font.setColor(new Color(0, 0, 0, 0.5f));
        exploreGUIStyle.font.setColor(new Color(0, 0, 0, 0.5f));

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void drawGUI(float delta) {
        super.drawGUI(delta);

        int height = Gdx.graphics.getHeight();
        FPSTimer.update(delta);

        this.moveCamera(); //Move the camera
        this.drawSelectionBox();
        this.checkMousedOver();
        //Draws info about the game
        this.drawInfo(height);

        if(this.drawingProfiler)
            Profiler.drawDebug(ColonyGame.batch, 200, height - 20);

        //Draw stuff about the selected entity.
        if((this.selected != null && this.interactable != null) || this.selectedList.size() > 0){
            GUI.Texture(this.infoRect, this.background, this.batch);
            this.drawMultipleProfiles(this.leftCenterRect);
            this.displaySelected(this.infoRect);
        }

    }

    /**
     * Checks if the GUI is moused over or not.
     */
    private void checkMousedOver(){
        //Determines if any UI is moused over or not.
        this.onUI = this.infoRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }

    /**
     * Draws the selection box if the user is dragging a selection box.
     */
    private void drawSelectionBox(){
        if(mouseDown) {
            //Get the start point.
            Vector3 pos = new Vector3(selectionBox.x, selectionBox.y, 0);
            Vector3 start = ColonyGame.camera.project(new Vector3(pos.x, pos.y, 0));

            //Get the end point.
            pos.set(selectionBox.x + selectionBox.getWidth(), selectionBox.y + selectionBox.getHeight(), 0);
            Vector3 end = ColonyGame.camera.project(new Vector3(pos.x, pos.y, 0));
            gray.a = 0.5f;

            //Save the color from the batch, apply the gray, draw the texture (with color), and reset the original color.
            Color saved = this.batch.getColor();
            this.batch.setColor(gray);
            this.batch.draw(WorldGen.getInstance().whiteTex, start.x, start.y, end.x - start.x, end.y - start.y);
            this.batch.setColor(saved);
        }
    }

    /**
     * Moves the camera when keys are pressed.
     */
    private void moveCamera(){
        if(Gdx.input.isKeyPressed(Input.Keys.W))
            ColonyGame.camera.translate(0, Gdx.graphics.getDeltaTime()*camMoveSpeed);
        if(Gdx.input.isKeyPressed(Input.Keys.S))
            ColonyGame.camera.translate(0, -Gdx.graphics.getDeltaTime()*camMoveSpeed);
        if(Gdx.input.isKeyPressed(Input.Keys.A))
            ColonyGame.camera.translate(-Gdx.graphics.getDeltaTime()*camMoveSpeed, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.D))
            ColonyGame.camera.translate(Gdx.graphics.getDeltaTime()*camMoveSpeed, 0);
    }

    /**
     * Draws info (like FPS) on the screen.
     * @param height
     */
    private void drawInfo(int height){
        if(this.drawingInfo) {
            GUI.Text("FPS: " + FPS, this.batch, 0, height - 20);
            GUI.Text("Zoom: " + ColonyGame.camera.zoom, this.batch, 0, height - 40);
            GUI.Text("Resolution: " + Gdx.graphics.getDesktopDisplayMode().width + "X" + Gdx.graphics.getDesktopDisplayMode().height, this.batch, 0, height - 60);
            GUI.Text("NumTrees: " + WorldGen.getInstance().numTrees(), this.batch, 0, height - 80);
            GUI.Text("NumTiles: " + WorldGen.getInstance().numTiles(), this.batch, 0, height - 100);
            GUI.Text("NumGridCols(X): " + ColonyGame.worldGrid.getNumCols(), this.batch, 0, height - 120);
            GUI.Text("NumGridRows(Y): " + ColonyGame.worldGrid.getNumRows(), this.batch, 0, height - 140);
        }
    }

    /**
     * Draws the multiple selected profiles.
     * @param rect The rectangle to draw the information inside of.
     */
    private void drawMultipleProfiles(Rectangle rect){
        //If there is more than one unit selected.. display in a group format.
        if(selectedList.size() > 1){
            profileButtonRect.set(leftCenterRect.getX(), leftCenterRect.getY() + leftCenterRect.getHeight() - 20, 50, 20);

            //Tell all to gather.
            if(GUI.Button(rect.x - 35, rect.y + rect.getHeight() - 35, 30, 30, "ALL", this.batch, gatherGUIStyle))
                for(UnitProfile prof : selectedList) prof.entity.getComponent(BehaviourManagerComp.class).gather();

            //Tell all to explore
            if(GUI.Button(rect.x - 35, rect.y + rect.getHeight() - 70, 30, 30, "ALL", this.batch, exploreGUIStyle))
                for(UnitProfile prof : selectedList) prof.entity.getComponent(BehaviourManagerComp.class).explore();

            //For each profile, draw a button to access each individual entity.
            for(UnitProfile profile : selectedList) {
                if(GUI.Button(profileButtonRect, profile.entity.name, this.batch)){
                    this.selected = profile.entity;
                    this.interactable = profile.interactable;
                    this.gridComp = profile.gridComp;
                }

                //If we go too far down the screen, move over.
                profileButtonRect.setY(profileButtonRect.getY() - 22);
                if(profileButtonRect.y <= leftCenterRect.getY() + 10)
                    profileButtonRect.set(leftCenterRect.getX() + 55, leftCenterRect.getY() + leftCenterRect.getHeight() - 20, 50, 20);

                profile.interactable.colonist.display(null, this.batch, "path");
            }
        }
    }

    /**
     * Displays the selected Entity.
     * @param rect
     */
    private void displaySelected(Rectangle rect){
        Profiler.begin("PlayerInterface displaySelected");

        float x = rect.getX() + 20;
        float y = rect.getY() + rect.getHeight() - 10;

        if(this.interactable.type.equals("resource")){
            this.interactable.resource.display(this.centerRect, this.batch, "general");
            this.interactable.resource.display(this.leftRect, this.batch, "resource");
        }else if(this.interactable.type.equals("humanoid")){
            this.interactable.colonist.display(this.centerRect, this.batch, "general");
            this.interactable.colonist.display(this.leftRect, this.batch, "health");
            this.interactable.colonist.display(this.rightRect, this.batch, "inventory");
            this.interactable.colonist.display(null, this.batch, "path");
        }else if(this.interactable.type.equals("colony")){
            this.interactable.colony.display(this.centerRect, this.batch, "general");
            this.interactable.colony.display(this.leftRect, this.batch, "colony");
            this.interactable.colony.display(this.rightRect, this.batch, "inventory");
        }else if(this.interactable.type.equals("animal")){

        }
        Profiler.end();
    }

    /**
     * The start of the dragging.
     * @param x The X location.
     * @param y The Y location.
     */
    private void startDragging(float x, float y){
        selectionBox.set(x, y, 0, 0);
    }

    /**
     * Active dragging.
     * @param x The current X location.
     * @param y The current Y location.
     */
    private void drag(float x, float y){
        selectionBox.set(selectionBox.x, selectionBox.y, x - selectionBox.x, y - selectionBox.y);
    }

    /**
     * When the mouse button is released and dragging stops.
     * @param x The X location.
     * @param y The Y location.
     */
    private void finishDragging(float x, float y){
        selectionBox.set(selectionBox.x, selectionBox.y, x - selectionBox.x, y - selectionBox.y);
        Vector2 center = new Vector2();
        selectionBox.getCenter(center);
        float halfWidth = Math.abs(selectionBox.getWidth()/2);
        float halfHeight = Math.abs(selectionBox.getHeight()/2);
        selectionBox.set(center.x - halfWidth, center.y - halfHeight, center.x + halfWidth, center.y + halfHeight);
        this.world.QueryAABB(this.selectionCallback, selectionBox.x, selectionBox.y, selectionBox.getWidth(), selectionBox.getHeight());
    }

    /**
     * If the GUI is moused over or not.
     * @return True if moused over, false otherwise.
     */
    public boolean isOnUI(){
        boolean windowActive = ((this.selected != null && this.interactable != null) || this.selectedList.size() > 0);
        return this.onUI && windowActive;
    }

    private void revealMap(){
        Grid.Node[][] grid = ColonyGame.worldGrid.getGrid();
        System.out.println("grid: "+grid.length+"/"+grid[0].length);
        WorldGen.VisibilityTile[][] visMap = WorldGen.getInstance().getVisibilityMap();
        for(int x=0;x<visMap.length;x++){
            for(int y=0;y<visMap[x].length;y++){
                visMap[x][y].addViewer();
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        this.background.dispose();
        this.batch = null;
        this.interactable = null;
        this.world = null;
        this.buttonRect = null;
        this.infoRect = null;
    }

    @Override
    public void resize(int width, int height) {
        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);

        this.infoRect.set(0,0,Gdx.graphics.getWidth(), 0.1f*Gdx.graphics.getHeight());
        this.leftRect.set(infoRect.x, infoRect.y, infoRect.width * leftRectPerc, infoRect.height);
        this.rightRect.set(infoRect.x + infoRect.width - infoRect.width * rightRectPerc, infoRect.y, infoRect.width * rightRectPerc, infoRect.height);
        this.centerRect.set(infoRect.x + infoRect.width/2 - infoRect.width * centerRectPerc, infoRect.y, infoRect.width * centerRectPerc, infoRect.height);
        this.leftCenterRect.set(infoRect.x + infoRect.width/3 - infoRect.width * centerRectPerc, infoRect.y, infoRect.width * centerRectPerc, infoRect.height);
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.F1)
            this.drawingInfo = !this.drawingInfo;
        else if(keycode == Input.Keys.F2)
            Profiler.enabled = this.drawingProfiler = !this.drawingProfiler;
        else if(keycode == Input.Keys.F3) {
            ServerPlayer.drawGrid = !ServerPlayer.drawGrid;
        }else if(keycode == Input.Keys.SPACE)
            this.gameScreen.setPaused(!this.gameScreen.getPaused());
        else if(keycode == Input.Keys.F4)
            this.revealMap();
        else
            return false;

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT) {
            if(!this.isOnUI()) {
                this.mouseDown = true;
                Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(screenX, screenY, 0));
                startDragging(worldCoords.x, worldCoords.y);
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(this.isOnUI())
            return false;

        this.mouseDown = false;
        Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(screenX, screenY, 0));

        if(button == Input.Buttons.LEFT){
            this.selectedList.clear();
            this.selected = null;
            this.interactable = null;
            this.finishDragging(worldCoords.x, worldCoords.y);

            this.testPoint.set(worldCoords.x, worldCoords.y);
            this.world.QueryAABB(this.callback, worldCoords.x - 1f, worldCoords.y - 1f, worldCoords.x + 1f, worldCoords.y + 1f);
            return true;
        }else if(button == Input.Buttons.RIGHT){
            if(this.selected != null){
//                BehaviourManagerComp manager = this.selected.getComponent(BehaviourManagerComp.class);
//                if(manager!=null){
//                    manager.move(new Vector2(worldCoords.x, worldCoords.y));
//                }
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(mouseDown) {
            Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(screenX, screenY, 0));
            drag(worldCoords.x, worldCoords.y);
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        ColonyGame.camera.zoom += amount*Gdx.graphics.getDeltaTime()*camZoomSpeed;
        if(ColonyGame.camera.zoom < 0) ColonyGame.camera.zoom = 0;
        return false;
    }

    private class UnitProfile{
        public Entity entity;
        public Interactable interactable;
        public GridComponent gridComp;
    }
}
