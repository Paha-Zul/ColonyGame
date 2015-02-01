package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Grid;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BlackBoard extends Component{
    public Grid.GridInstance colonyGrid;
    public Grid.Node targetNode;
    public Entity target;
    public Grid.Node[] path;
    public Resource targetResource;
    public Inventory transferToInventory;

    //My stuff
    public Inventory myInventory;

    public BlackBoard() {

        this.setActive(false);
    }
}
