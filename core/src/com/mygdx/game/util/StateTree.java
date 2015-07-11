package com.mygdx.game.util;

/**
 * Created by brad on 7/11/15.
 */
public class StateTree<T> extends Tree<T>{
    private TreeNode<T> currTreeNode = null;

    public StateTree(String treeName, String rootName) {
        super(treeName, rootName);

        this.currTreeNode = this.root;
    }

    /**
     * Moves up to the parent of the current tree node. If the parent is null,
     * the current node does not change.
     * @return The node that is the new current node.
     */
    public TreeNode<T> moveUp(){
        TreeNode<T> node = this.currTreeNode.parent;
        if(node != null) this.currTreeNode = node;
        return this.currTreeNode;
    }

    /**
     * Moves down to a child of the current node. If the child could not be found (is null),
     * the current node does not change.
     * @param nodeName The name of the child node.
     * @return The new current node.
     */
    public TreeNode<T> moveDowntoChild(String nodeName){
        TreeNode<T> node = this.currTreeNode.getChild(nodeName);
        if(node != null) this.currTreeNode = node;
        return this.currTreeNode;
    }

    /**
     * Attemps to set the current node to the node passed in. If the TreeNode is not part of
     * this tree, the current tree node remains unchanged.
     * @param treeNode The TreeNode to set the current node to.
     * @return The current tree node.
     */
    public TreeNode<T> setCurrentTreeNode(TreeNode<T> treeNode){
        return this.setCurrentTreeNode(treeNode.nodeName);
    }

    /**
     * Attemps to set the current node to the node passed in. If the TreeNode is not part of
     * this tree, the current tree node remains unchanged.
     * @param nodeName The name of the node.
     * @return The current tree node.
     */
    public TreeNode<T> setCurrentTreeNode(String nodeName){
        TreeNode<T> node = this.getNode(n -> n.nodeName.equals(nodeName));
        if(node != null) this.currTreeNode = node;
        return this.currTreeNode;
    }

    /**
     * @return The current tree node.
     */
    public TreeNode<T> getCurrentTreeNode(){
        return this.currTreeNode;
    }
}
