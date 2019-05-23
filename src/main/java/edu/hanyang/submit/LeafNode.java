package edu.hanyang.submit;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeafNode extends Node{
    List<Integer> values;
    LeafNode next;

    // create a new Node at the end of the file
    LeafNode() throws IOException {
        keys = new ArrayList<Integer>();
        values = new ArrayList<Integer>();
        position = ((Integer)(TinySEBPlusTree.num_nodes * TinySEBPlusTree.blocksize)).longValue();
        save(position);
    }

    // load the Node from the file at the offset
    LeafNode(Long offset) throws IOException {
        keys = new ArrayList<Integer>();
        values = new ArrayList<Integer>();
        position = offset;
        TinySEBPlusTree.file.seek(offset);
        TinySEBPlusTree.file.readInt();
        int num_keys = TinySEBPlusTree.file.readInt();
        for(int i=0; i<num_keys; i++){
            keys.add(TinySEBPlusTree.file.readInt());
            values.add(TinySEBPlusTree.file.readInt());
        }
    }

    @Override
    Long save(Long offset) throws IOException {
        TinySEBPlusTree.file.seek(offset);
        TinySEBPlusTree.file.writeInt(1);
        TinySEBPlusTree.file.writeInt(keyNumber());
        for(int i=0; i<keyNumber(); i++){
            TinySEBPlusTree.file.writeInt(keys.get(i));
            TinySEBPlusTree.file.writeInt(values.get(i));
        }
        return offset + TinySEBPlusTree.blocksize;
    }

    @Override
    Integer getValue(Integer key) {
        int loc = Collections.binarySearch(keys, key);
        return loc >= 0 ? values.get(loc) : null;
    }

    @Override
    void insertValue(Integer key, Integer value) throws IOException {
        int loc = Collections.binarySearch(keys, key);
        int valueIndex = loc >= 0 ? loc : -loc - 1;
        if (loc >= 0) {
            values.set(valueIndex, value);
        } else {
            keys.add(valueIndex, key);
            values.add(valueIndex, value);
        }
        if (TinySEBPlusTree.root.isOverflow()) {
            Node sibling = split();
            TinySEBPlusTree.num_nodes += 1;
            InternalNode newRoot = new InternalNode();
            TinySEBPlusTree.num_nodes += 1;
            newRoot.keys.add(sibling.getFirstLeafKey());
            newRoot.children.add(position);
            newRoot.children.add(sibling.position);
            newRoot.save(newRoot.position);
            TinySEBPlusTree.root = newRoot;
        }
    }

    @Override
    Integer getFirstLeafKey() {
        return keys.get(0);
    }

    @Override
    Node split() throws IOException {
        LeafNode sibling = new LeafNode();
        int from = (keyNumber() + 1) / 2, to = keyNumber();
        sibling.keys.addAll(keys.subList(from, to));
        sibling.values.addAll(values.subList(from, to));

        keys.subList(from, to).clear();
        values.subList(from, to).clear();

        sibling.next = next;
        next = sibling;
        sibling.writeToFileEnd();
        this.save(position);

        return sibling;
    }

    @Override
    boolean isOverflow() {
        return values.size() > TinySEBPlusTree.maxKeys - 1;
    }

    @Override
    boolean isUnderflow() {
        return values.size() < TinySEBPlusTree.maxKeys / 2;
    }

    void writeToFileEnd() throws IOException {
        save(((Integer)(TinySEBPlusTree.num_nodes * TinySEBPlusTree.blocksize)).longValue());
    }

    void print(){
        int i;
        for(i=0; i<keyNumber(); i++){
            System.out.print(keys.get(i) + " - ");
        }
        System.out.println();
    }
}