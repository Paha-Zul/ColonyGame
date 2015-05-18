package com.mygdx.game.helpers;

import gnu.trove.list.array.TIntArrayList;

/**
 * Created by Paha on 4/8/2015.
 */
public class Tags {
    private int tagMask;
    private String type;

    public Tags(String type){
        this.type = type;
    }

    /**
     * Adds a tag to this Entity.
     * @param tag The tag to add.
     */
    public void addTag(int tag){
        this.tagMask |= (1 << tag); //OR the tag to the mask.
    }

    /**
     * Adds a tag to this Entity.
     * @param tag The tag to add.
     */
    public void addTag(String tag){
        int intTag = StringTable.StringToInt(type, tag);
        this.tagMask |= (1 << intTag); //OR the tag to the mask.
    }

    /**
     * Toggles the tag bit in this Tags object.
     * @param tag The tag to toggle.
     */
    public void toggleTag(int tag){
        tagMask ^= 1 << tag;
    }

    /**
     * Toggles the tag bit in this Tags object.
     * @param tag The tag to toggle.
     */
    public void toggleTag(String tag){
        int intTag = StringTable.StringToInt(type, tag);
        tagMask ^= 1 << intTag;
    }

    /**
     * Removes a tag from this Entity.
     * @param tag The tag to remove.
     */
    public void removeTag(int tag){
        this.tagMask &= ~(1 << tag); //AND the inverted tag to zero it out.
    }

    /**
     * Removes a tag from this Entity.
     * @param tag The tag to remove.
     */
    public void removeTag(String tag){
        int intTag = StringTable.StringToInt(type, tag);
        this.tagMask &= ~(1 << intTag); //AND the inverted tag to zero it out.
    }

    /**
     * Clears/removes all tags from this Tag.
     */
    public void clearTags(){
        this.tagMask = 0b0;
    }

    /**
     * Checks if this Entity has the tag passed in.
     * @param tag The tag to check for.
     * @return True if this Entity has the tag, false otherwise.
     */
    public boolean hasTag(int tag){
        return ((1 << tag) & tagMask) != 0;
    }

    public boolean hasTag(String tag){
        int intTag = StringTable.StringToInt(type, tag);
        return ((1 << intTag) & tagMask) != 0;
    }

    /**
     * Checks if the Entity has multiple tags.
     * @param tagsToCheck The tags to check for.
     * @return True if this Entity has all the tags, false otherwise.
     */
    public boolean hasTags(int[] tagsToCheck){
        int tags = 0b0; //0 in binary
        for (int aTagsToCheck : tagsToCheck) tags |= (1 << aTagsToCheck);
        return (tags & tagMask) == tags;
    }

    /**
     * Checks if the Entity has multiple tags.
     * @param tagsToCheck The String tags to check.
     * @return True if this Tags object has all the tags in the tagsToCheck array, false otherwise.
     */
    public boolean hasTags(String... tagsToCheck){
        int tags = 0b0; //0 in binary
        for (String tagToCheck : tagsToCheck) tags |= (1 << StringTable.StringToInt(type, tagToCheck));
        return (tags & tagMask) == tags;
    }

    /**
     * Checks if any of the passed in tags exists in this Tags object.
     * @param tagsToCheck The integer array of tags to check
     * @return True if any of the tagsToCheck exists in this Tags object.
     */
    public boolean hasAnyTag(int[] tagsToCheck){
        for (int tag : tagsToCheck) {
            int shift = 1 << tag;
            if((shift & tagMask) == shift)
                return true;
        }

        return false;
    }

    public boolean hasAnyTag(String... tagsToCheck){
        for (String tag : tagsToCheck) {
            int shift = 1 << StringTable.StringToInt(type, tag);
            if((shift & tagMask) == shift)
                return true;
        }

        return false;
    }

    /**
     * Gets the tags as an integer array from this Tags object.
     * @return An integer array containing the integer tags.
     */
    public int[] getTags(){
        TIntArrayList tagList = new TIntArrayList();
        for(int i=0;i<33;i++){
            if(((tagMask >> i) & 1) == 1)
                tagList.add(i);
        }

        return tagList.toArray();
    }

    public final int getTagMask(){
        return this.tagMask;
    }

    /**
     * @return True if this Tags object has no entries in its tags.
     */
    public boolean isEmpty(){
        return tagMask == 0;
    }

    @Override
    public String toString() {
        return ""+Integer.toBinaryString(tagMask);
    }
}
