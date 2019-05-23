package edu.hanyang.submit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InternalNode extends Node{
    List<Long> children;

    InternalNode() throws IOException {
        this.keys = new ArrayList<Integer>();
        this.children = new ArrayList<Long>();
        position = ((Integer)(TinySEBPlusTree.num_nodes * TinySEBPlusTree.blocksize)).longValue();
        save(position);
    }

    InternalNode(Long offset) throws IOException {
        this.keys = new ArrayList<Integer>();
        this.children = new ArrayList<Long>();
        position = offset;
        TinySEBPlusTree.file.seek(offset);
        TinySEBPlusTree.file.readInt();
        int num_keys = TinySEBPlusTree.file.readInt();
        for(int i=0; i<num_keys; i++){
            children.add(TinySEBPlusTree.file.readLong());
            keys.add(TinySEBPlusTree.file.readInt());
        }
        if(children.size() > 0){
            children.add(TinySEBPlusTree.file.readLong());
        }
    }

    @Override
    Long save(Long offset) throws IOException {
        TinySEBPlusTree.file.seek(offset);
        TinySEBPlusTree.file.writeInt(0);   // Node type: non-leaf node.
        TinySEBPlusTree.file.writeInt(keyNumber());   // Number of keys.
        int i;
        for(i=0; i<keyNumber(); i++){
            TinySEBPlusTree.file.writeLong(children.get(i));
            TinySEBPlusTree.file.writeInt(keys.get(i));
        }
        if(children.size() > 0){
            TinySEBPlusTree.file.writeLong(children.get(i));
        }
        return offset + TinySEBPlusTree.blocksize;
    }

    Node getChild(Integer key) throws IOException {
        int loc = Collections.binarySearch(keys, key);
        int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
        Long offset = children.get(childIndex);
        return TinySEBPlusTree.loadNode(offset);
    }

    void insertChild(Integer key, Node child) {
        int loc = Collections.binarySearch(keys, key);
        int childIndex = loc >= 0 ? loc + 1 : -loc - 1;
        if (loc >= 0) {
            children.set(childIndex, child.position);
        } else {
            keys.add(childIndex, key);
            children.add(childIndex + 1, child.position);
        }
    }

    @Override
    Integer getValue(Integer key) throws IOException {
        return getChild(key).getValue(key);
    }

    @Override
    void insertValue(Integer key, Integer value) throws IOException {
        Node child = getChild(key);
        child.insertValue(key, value);
        if (child.isOverflow()) {
            Node sibling = child.split();
            TinySEBPlusTree.num_nodes += 1;
            insertChild(sibling.getFirstLeafKey(), sibling);
        }
        if (TinySEBPlusTree.root.isOverflow()) {
            Node sibling = split();
            TinySEBPlusTree.num_nodes += 1;
            InternalNode newRoot = new InternalNode();
            TinySEBPlusTree.num_nodes += 1;
            newRoot.keys.add(sibling.getFirstLeafKey());
            newRoot.children.add(this.position);
            newRoot.children.add(sibling.position);
            TinySEBPlusTree.root = newRoot;
        }else{
            child.save(child.position);
        }
    }

    @Override
    Node split() throws IOException {
        int from = keyNumber() / 2 + 1, to = keyNumber();
        InternalNode sibling = new InternalNode();
        sibling.keys.addAll(keys.subList(from, to));
        sibling.children.addAll(children.subList(from, to + 1));

        keys.subList(from - 1, to).clear();
        children.subList(from, to + 1).clear();
        writeToFileEnd();

        return sibling;
    }

    void writeToFileEnd() throws IOException {
        save(((Integer)(TinySEBPlusTree.num_nodes * TinySEBPlusTree.blocksize)).longValue());
    }

    @Override
    Integer getFirstLeafKey() throws IOException {
        return getChild(0).getFirstLeafKey();
    }

    @Override
    boolean isOverflow() {
        return children.size() > TinySEBPlusTree.maxKeys;
    }

    @Override
    boolean isUnderflow() {
        return children.size() < (TinySEBPlusTree.maxKeys + 1) / 2;
    }

    void print(){
        int i;
        for(i=0; i<keyNumber(); i++){
            System.out.print(children.get(i) + "| " + keys.get(i) + " |");
        }
        System.out.print(children.get(i));
        System.out.println();
    }
}