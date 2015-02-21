package com.mygdx.game.component;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.helpers.BehaviourManager;
import com.mygdx.game.helpers.FloatingText;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.interfaces.IDisplayable;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component implements IDisplayable{
    private Colony colony;
    private Inventory inventory;
    private Stats stats;
    private BehaviourManagerComp manager;

    private Rectangle gatherButton = new Rectangle();

    private static GUI.ButtonStyle gatherButtonStyle;
    private static GUI.ButtonStyle exploreButtonStyle;

    static{
        gatherButtonStyle = new GUI.ButtonStyle();
        gatherButtonStyle.normal = new Texture(Gdx.files.internal("img/ui/axebutton_normal.png"), true);
        gatherButtonStyle.normal.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        gatherButtonStyle.moused = new Texture(Gdx.files.internal("img/ui/axebutton_moused.png"), true);
        gatherButtonStyle.moused.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        gatherButtonStyle.clicked = new Texture(Gdx.files.internal("img/ui/axebutton_clicked.png"), true);
        gatherButtonStyle.clicked.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);

        exploreButtonStyle = new GUI.ButtonStyle();
        exploreButtonStyle.normal = new Texture(Gdx.files.internal("img/ui/explorebutton_normal.png"), true);
        exploreButtonStyle.normal.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        exploreButtonStyle.moused = new Texture(Gdx.files.internal("img/ui/explorebutton_moused.png"), true);
        exploreButtonStyle.moused.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);
        exploreButtonStyle.clicked = new Texture(Gdx.files.internal("img/ui/explorebutton_clicked.png"), true);
        exploreButtonStyle.clicked.setFilter(Texture.TextureFilter.MipMapNearestLinear, Texture.TextureFilter.Linear);

    }

    public Colonist() {
        super();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void start() {
        super.start();

        this.inventory = this.getComponent(Inventory.class);
        this.stats = this.getComponent(Stats.class);
        this.manager = this.getComponent(BehaviourManagerComp.class);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    public Colony getColony() {
        return colony;
    }

    public void setColony(Colony colony) {
        this.colony = colony;
    }

    public Inventory getInventory(){
        return this.inventory;
    }

    public Stats getStats(){
        return this.stats;
    }

    @Override
    public void display(Rectangle rect, SpriteBatch batch, String name) {
        float x = 0;
        float y = 0;

        if(rect != null){
            x = rect.getX();
            y = rect.getY() + rect.getHeight() - 5;
        }

        if(name == "general"){
            GUI.Label("Name: "+this.owner.name, batch, rect.x + rect.getWidth()/2, rect.y + rect.getHeight() - 5, true);
            GUI.Label("Current Task: "+this.manager.getCurrentTaskName(), batch, rect.x + rect.getWidth()/2, rect.y + rect.getHeight() - 25, true);

            this.gatherButton.set(rect.getX(), rect.getY() + rect.getHeight() - 75, 35, 35);
            if(GUI.Button(this.gatherButton, "", batch, gatherButtonStyle))
                this.manager.gather();

            this.gatherButton.set(rect.getX() + 40, rect.getY() + rect.getHeight() - 75, 35, 35);
            if(GUI.Button(this.gatherButton, "", batch, exploreButtonStyle)) {
                this.manager.explore();
            }

        }else if(name == "health"){
            stats.display(rect, batch, name);
        }else if(name == "inventory"){
            this.inventory.display(rect, batch, name);
        }else if(name == "path"){
            this.manager.display(rect, batch, name);
        }
    }
}
