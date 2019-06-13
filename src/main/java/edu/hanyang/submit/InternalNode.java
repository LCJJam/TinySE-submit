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
    }

    InternalNode(Long offset) throws IOException {
        this.keys = new ArrayList<Integer>();
        this.children = new ArrayList<Long>();
        position = offset;
        TinySEBPlusTree.file.seek(offset);
        TinySEBPlusTree.file.readLong();
        int num_keys = TinySEBPlusTree.file.readInt();
        for(int i=0; i<num_keys; i++){
            children.add(TinySEBPlusTree.file.readLong());
            keys.add(TinySEBPlusTree.file.readInt());
        }
        if(children.size() >0){
            children.add(TinySEBPlusTree.file.readLong());
        }
    }

    @Override
    Long save(Long offset) throws IOException {
        TinySEBPlusTree.file.seek(offset);
        TinySEBPlusTree.file.writeLong(0);   // Node type: non-leaf node.
        TinySEBPlusTree.file.writeInt(keyNumber());   // Number of keys.
        int i;
        for(i=0; i<keyNumber(); i++){
            TinySEBPlusTree.file.writeLong(children.get(i));
            TinySEBPlusTree.file.writeInt(keys.get(i));
        }
        if(children.size() > 0){
            TinySEBPlusTree.file.writeLong(children.get(i));
        }
        //System.out.println("키스:"+keys+"와 칠드런:"+children+" 이 저장되는 주소:"+offset);
        return offset;
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
            children.add(childIndex+1, child.position);
        }
    }

    @Override
    Integer getValue(Integer key) throws IOException {
        return getChild(key).getValue(key);
    }

    @Override
    void insertValue(Integer key, Integer value) throws IOException {
        Node child = getChild(key);
        //System.out.println("겟 차일드 의 칠드런:"+children);
        child.insertValue(key, value);
        if (child.isOverflow()) {
        	//System.out.println("스플릿 전:"+child);
            Node sibling = child.split();
            insertChild(sibling.getFirstLeafKey(), sibling);
        }
        if (TinySEBPlusTree.root.isOverflow()) {
        	Node sibling = split();
        	TinySEBPlusTree.num_nodes += 1;
            InternalNode newRoot = new InternalNode();
            newRoot.keys.add(sibling.getFirstLeafKey());
            sibling.keys.subList(0, 1).clear();
            sibling.save(((Integer)((TinySEBPlusTree.num_nodes-1) * TinySEBPlusTree.blocksize)).longValue());
            newRoot.children.add(position);
            newRoot.children.add(sibling.position);
            newRoot.position = ((Integer)(TinySEBPlusTree.num_nodes * TinySEBPlusTree.blocksize)).longValue();
            TinySEBPlusTree.root = newRoot;
        }else{
        	child.save(child.position);
        	save(position);
        	//System.out.println(TinySEBPlusTree.root);
        	//System.out.println("차"+child.keys +"차포"+child.position);
        	//System.out.println("차"+child.getFirstLeafKey() +"차포"+child.position);
        	//System.out.println("칠"+children + "칠포"+position);
        }
    }

    @Override
    Node split() throws IOException { //인터널
        int from = keyNumber() / 2 , to = keyNumber();
        InternalNode sibling = new InternalNode();
        TinySEBPlusTree.num_nodes += 1;
        
        //System.out.println(children+""+position);
        
        sibling.keys.addAll(keys.subList(from+1, to)); //from ~ to-1
        sibling.children.addAll(children.subList(from+1, to+1)); // from ~ to
        sibling.position = ((Integer)((TinySEBPlusTree.num_nodes) * TinySEBPlusTree.blocksize)).longValue();
        sibling.writeToFileEnd();
        sibling.keys.clear();
        sibling.children.clear();
        sibling.keys.addAll(keys.subList(from, to)); //from ~ to-1
        sibling.children.addAll(children.subList(from+1, to+1)); // from ~ to
        
        keys.subList(from, to).clear();
        children.subList(from+1, to+1).clear();
        save(position);
        TinySEBPlusTree.num_nodes += 1;
        
        //System.out.println(":::" +sibling.children+sibling.position);
        //System.out.println(children+""+position);
        
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