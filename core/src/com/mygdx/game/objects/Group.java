package com.mygdx.game.objects;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;

/**
 * Created by Paha on 5/8/2015.
 *
 * <p>A group of Entities with a leader.</p>
 */
public class Group extends Component{
    private Entity leader;
    private Array<Entity> groupList = new Array<>();

    private boolean stayNearLeader = true, attackLeaderTarget = true;

    @Override
    public void start() {
        super.start();
    }

    public Entity addEntityToGroup(Entity entity){
        this.groupList.add(entity);
        return entity;
    }

    public boolean removeEntityFromGroup(Entity entity){
        return this.groupList.removeValue(entity, true);
    }

    public Entity setLeader(Entity leader){
        return this.leader = leader;
    }

    public void setStayNearLeader(boolean stayNearLeader) {
        this.stayNearLeader = stayNearLeader;
    }

    public void setAttackLeaderTarget(boolean attackLeaderTarget) {
        this.attackLeaderTarget = attackLeaderTarget;
    }

    public Entity getLeader(){
        return this.leader;
    }

    public Array<Entity> getGroupList() {
        return groupList;
    }

    public boolean isStayNearLeader() {
        return stayNearLeader;
    }

    public boolean isAttackLeaderTarget() {
        return attackLeaderTarget;
    }
}
