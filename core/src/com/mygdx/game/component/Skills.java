package com.mygdx.game.component;

import com.badlogic.gdx.utils.Array;

import java.util.HashMap;

/**
 * Created by Paha on 1/30/2015.
 */
public class Skills extends Component{
    private HashMap<String, Skill> skillMap = new HashMap<>();
    private Array<Skill> skillList = new Array<>(10);

    public Skills() {
        super();
        this.setActive(false);
    }

    @Override
    public void start() {
        super.start();
    }

    /**
     * Adds a Skill to this Skills Component.
     * @param skillName The name of the skill.
     * @param maxLevel The maximum level the skill can have.
     * @param baseXP The base experience that the skill starts with.
     * @param multiplierPerLevel THe multiplier to increase the skill cap by every level.
     * @return The Skill that was created and added to the Skills Component.
     */
    public Skill addSkill(String skillName, int maxLevel, float baseXP, float multiplierPerLevel, float multiplierEffect){
        Skill skill = new Skill(skillName, maxLevel, baseXP, multiplierPerLevel, multiplierEffect);
        skillMap.put(skillName, skill);
        skillList.add(skill);
        return skill;
    }

    /**
     * Adds a Skill to this Skills Component
     * @param skill The Skill to add.
     * @return The Skill that was added.
     */
    public Skill addSkill(Skill skill){
        skillMap.put(skill.getSkillName(), skill);
        return skill;
    }

    /**
     * Gets a Skill from this Component by name.
     * @param skillName The name of the Skill.
     * @return The Skill if it exists, or null otherwise.
     */
    public Skill getSkill(String skillName){
        return skillMap.get(skillName);
    }

    public Array<Skill> getSkillList(){
        return this.skillList;
    }


    @Override
    public void destroy(double id) {
        super.destroy(id);
    }

    /**
     * A Class that represents a skill of an Entity.
     */
    public static class Skill{
        private String skillName;
        private int currLevel, maxLevel;
        private float baseXP, multiplierPerLevel, currXP, multiplierEffect;

        private float currLvlMinXP, currLvlMaxXp;

        public Skill(String skillName, int maxLevel, float baseXP, float multiplierPerLevel, float multiplierEffect) {
            this.skillName = skillName;
            this.maxLevel = maxLevel;
            this.baseXP = baseXP;
            this.multiplierPerLevel = multiplierPerLevel;
            this.multiplierEffect = multiplierEffect;

            this.currLevel = 0;
            this.currLvlMinXP = 0;
            this.currLvlMaxXp = baseXP;
        }

        //region getters
        public String getSkillName() {
            return skillName;
        }

        public int getCurrLevel() {
            return currLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }

        public float getBaseXP() {
            return baseXP;
        }

        public float getMultiplierPerLevel() {
            return multiplierPerLevel;
        }

        public float getCurrXP() {
            return currXP;
        }

        public float getCurrLvlMaxXp(){
            return this.currLvlMaxXp;
        }

        public float getCurrLvlMinXP(){
            return this.currLvlMinXP;
        }

        public float getMultiplierEffect() {
            return multiplierEffect;
        }

        //endregion

        public void addXP(float amt){
            this.currXP += amt;
            if(this.currXP >= this.currLvlMaxXp) {
                this.currLevel++;
                this.currLvlMinXP = this.currLvlMaxXp;
                this.currLvlMaxXp *= this.multiplierPerLevel;
            }
        }
    }
}
