package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
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

    public boolean busy = false;

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

    @Override
    public void destroy(Entity destroyer) {
        //If it is the leader, we need to find a new one!
        if(destroyer == this.leader){
            int rand = MathUtils.random(groupList.size);
            Entity newLeader = groupList.get(rand);
            groupList.removeIndex(rand);
            this.setLeader(newLeader);

        }else removeEntityFromGroup(destroyer);

        super.destroy(destroyer);
    }

    /**
     * Do not use this function for this Component. Since this Component attaches to many different Entities, the owner could be any Entity in the group.
     * @return The wrong Entity or most of the time, null.
     */
    @Deprecated
    @Override
    public Entity getEntityOwner() {
        return super.getEntityOwner();
    }
}
