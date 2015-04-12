package com.mygdx.game.helpers;

import com.badlogic.gdx.utils.Array;

/**
 * Created by Paha on 4/12/2015.
 */
public class Tree {
    private String treeName;
    private Node baseNode;

    public Tree(String treeName, Node baseNode){
        this.treeName = treeName;
        this.baseNode = baseNode;
    }

    public void addNode(String parent, String newNodeName){

    }


    private class Node{
        public String nodeName;
        public Node parent;

        private Array<Node> children = new Array<>();

        public Node(String nodeName){
            this.nodeName = nodeName;
        }

        public void addChild(Node node){
            this.children.add(node);
            node.parent = this;
        }

        public Node getChild(String nodeName){
            for(Node node : children)
                if(nodeName.equals(node.nodeName))
                    return node;

            return null;
        }
    }
}


