package edu.hanyang.submit;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public abstract class Node {
    List<Integer> keys;
    Integer maxKeys;
    Long position;

    int keyNumber() {
        return keys.size();
    }

    abstract Integer getValue(Integer key) throws IOException;

    abstract void insertValue(Integer key, Integer value) throws IOException;

    abstract Integer getFirstLeafKey() throws IOException;

    abstract Node split() throws IOException;

    abstract boolean isOverflow();

    abstract boolean isUnderflow();

    abstract Long save(Long offset) throws IOException;

    public String toString() {
        return keys.toString();
    }
}