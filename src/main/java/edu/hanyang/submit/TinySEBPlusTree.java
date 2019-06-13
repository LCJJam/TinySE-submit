package edu.hanyang.submit;

import java.io.*;
import java.util.LinkedList;
import java.util.Queue;

import edu.hanyang.indexer.BPlusTree;

public class TinySEBPlusTree implements BPlusTree{
	static Node root;
	static Long root_position;
	static Integer maxKeys;
	static Integer blocksize;
	static RandomAccessFile file;
	static Integer num_nodes;
	String savepath;
	String metapath;
	
	public TinySEBPlusTree(){
	}
	
	
	@Override
	public void open(String metapath, String savepath, int blocksize, int nblocks)  {
		this.metapath = metapath;
		this.savepath = savepath;
		TinySEBPlusTree.blocksize = blocksize;
		maxKeys = (blocksize - 12) / 8;
		root_position = new Long(0);
		File meta = new File(metapath);
		if(meta.exists() && meta.length() > 0){
			DataInputStream is = null;
			try {
				is = new DataInputStream(new BufferedInputStream(new FileInputStream(metapath), 1024));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				root_position = is.readLong();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				maxKeys = is.readInt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				TinySEBPlusTree.blocksize = is.readInt();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			try {
				meta.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		File save = new File(savepath);
		if(!save.exists()){
			try {
				save.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			file = new RandomAccessFile(savepath, "rw");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TinySEBPlusTree.num_nodes = 0;
		try {
			if(file.length() > 0){
				TinySEBPlusTree.root = loadNode(root_position);
			}else{
				TinySEBPlusTree.root = new LeafNode();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("load error");
		}
	}
	
	@Override
	public void insert(int arg0, int arg1) {
		try {
			root.insertValue(arg0, arg1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("insert error");
		}
	}

	@Override
	public int search(int arg0) {
		try {
			return root.getValue(arg0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("get value error");
		}
		return -1;
	}

	public static Node loadNode(Long offset) throws IOException {
		file.seek(offset);
		long type = file.readLong();
		if(type == 1){
			return new LeafNode(offset);
		}else {
			return new InternalNode(offset);
		}
	}
	
	@Override
	public void close() {
		root_position = root.position;
		File meta = new File(metapath);
		if(!meta.exists()){
			try {
				meta.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			meta.delete();
			try {
				meta.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		DataOutputStream os = null;
		try {
			os = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(metapath, true), 1024));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			os.writeLong(root_position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.writeInt(maxKeys);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.writeInt(blocksize);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			root.save(root_position);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		try {
			((LeafNode)TinySEBPlusTree.loadNode(new Long(0))).print();
			((LeafNode)TinySEBPlusTree.loadNode(new Long(52))).print();
			((LeafNode)TinySEBPlusTree.loadNode(new Long(208))).print();
			((LeafNode)TinySEBPlusTree.loadNode(new Long(260))).print();
			((LeafNode)TinySEBPlusTree.loadNode(new Long(312))).print();
			((LeafNode)TinySEBPlusTree.loadNode(new Long(364))).print();
			((LeafNode)TinySEBPlusTree.loadNode(new Long(572))).print();
			System.out.println();
			((InternalNode)TinySEBPlusTree.loadNode(new Long(104))).print();
			((InternalNode)TinySEBPlusTree.loadNode(new Long(468))).print();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
}
