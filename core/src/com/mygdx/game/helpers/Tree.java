package com.mygdx.game.helpers;

import com.badlogic.gdx.utils.Array;

import java.util.LinkedList;

/**
 * A basic, no specialization, tree.
 */
public class Tree {
    private String treeName;
    private TreeNode root;
    private LinkedList<TreeNode> queue = new LinkedList<>() ;

    public Tree(String treeName, String rootName){
        this.treeName = treeName;
        this.root = new TreeNode(rootName);
    }

    /**
     * Adds a node to the Tree.
     * @param parent The parent name of the node to add the node to.
     * @param newNodeName The new node's name.
     * @return The TreeNode that was created to add to the tree, or null if the parent was not found.
     */
    public TreeNode addNode(String parent, String newNodeName){
        TreeNode parentNode = getNode(node -> node.nodeName.equals(parent));
        if(parentNode == null) return null;
        return parentNode.addChild(new TreeNode(newNodeName));
    }

    /**
     * Adds multiple new nodes to the tree.
     * @param parent The name of the parent node to add the new nodes to.
     * @param nodeNames The names of the new nodes to add.
     * @return This Tree object.
     */
    public Tree addNode(String parent, String... nodeNames){
        TreeNode parentNode = getNode(node -> node.nodeName.equals(parent));
        if(parentNode == null) return this;

        for(String name : nodeNames)
            parentNode.addChild(new TreeNode(name));

        return this;
    }

    /**
     * Gets a node by performing a breadth-first search on the tree.
     * @param test The Predicate function to test each node.
     * @return The TreeNode if one was found that passed the Predicate function, null otherwise.
     */
    public TreeNode getNode(java.util.function.Predicate<TreeNode> test) {
        if (this.root == null)
            return null;

        //Breadth first search....
        queue.clear();
        queue.add(this.root);

        while(!queue.isEmpty()){
            TreeNode node = queue.remove();
            if(test.test(node))
                return node;

            for(TreeNode child : node.children)
                queue.add(child);
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        //Depth first search...
        queue.clear();
        queue.add(root);

        while(!queue.isEmpty()){
            TreeNode node = queue.remove();

            if(node.children.size > 0) str.append(node.nodeName).append("->(");

            for(int i=0; i < node.children.size; i++) {
                queue.add(node.children.get(i));
                str.append(node.children.get(i).nodeName);
                if(i != node.children.size-1) str.append(",");
            }

            if(node.children.size > 0) str.append(")");
            str.append(" ");
        }

        return str.toString();
    }

    private class TreeNode {
        public String nodeName;
        public TreeNode parent;
        public Object userData;

        private Array<TreeNode> children = new Array<>();

        public TreeNode(String nodeName){
            this.nodeName = nodeName;
        }

        public TreeNode addChild(TreeNode treeNode){
            this.children.add(treeNode);
            treeNode.parent = this;
            return treeNode;
        }

        public TreeNode getChild(String nodeName){
            for(TreeNode treeNode : children)
                if(nodeName.equals(treeNode.nodeName))
                    return treeNode;

            return null;
        }
    }
}


