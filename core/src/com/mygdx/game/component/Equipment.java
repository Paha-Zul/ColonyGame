package com.mygdx.game.component;

import com.mygdx.game.util.DataBuilder;

/**
 * Created by Paha on 5/25/2015.
 */
public class Equipment extends Component{
    String head, body, arms, hands, feet;
    DataBuilder.JsonTool tool;

    @Override
    public void start() {
        super.start();

        this.setActive(false);
    }

    public Equipment() {
        super();
    }
}
