package edu.hanyang.submit;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import edu.hanyang.indexer.ExternalSort;
import edu.hanyang.utils.DiskIO;

import org.apache.commons.lang3.tuple.MutableTriple;

public class TinySEExternalSort implements ExternalSort {
	int memo_size;
	int n_elements;
	int buffer_size;
	int block_elements;
	int n_way_merge;
	int memo_elements;
	String tmpdir;
	
	public int getSize(String infile){
		File f = new File(infile);
		int len = (int)f.length();
		return len;
	}
	public static DataInputStream open_input_run(String filepath, int buffersize) throws FileNotFoundException {
		return new DataInputStream(new BufferedInputStream(new FileInputStream(filepath), buffersize));
	}
	
	public static DataOutputStream open_output_run(String filepath, int buffersize) throws IOException {
		File file = new File(filepath);
		File parentFile = file.getParentFile();
		if(!parentFile.exists()){
			parentFile.mkdir();
		}
		if(!file.exists()){
			file.createNewFile();
		}
		return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(filepath, true), buffersize));
	}
	
	public static int read_array(DataInputStream in, int nelements, ArrayList<MutableTriple<Integer, Integer, Integer>> arr) throws IOException {
		arr.clear();
		MutableTriple<Integer, Integer, Integer> mt;
		for(int cnt = 0; cnt < nelements; ++cnt) {
			mt = new MutableTriple<>();
			try {
				mt.setLeft(in.readInt());
			} catch (EOFException var5) {
				return cnt;
			}
			mt.setMiddle(in.readInt());
			mt.setRight(in.readInt());
			arr.add(mt);
		}
		return nelements;
	}
	
	public static void sort_arr (List<MutableTriple<Integer, Integer, Integer>> arr, int nelements) {
		Collections.sort(arr.subList(0, nelements));
	}
	
	public static void append_arr (DataOutputStream out, List<MutableTriple<Integer, Integer, Integer>> arr, int nelements) throws IOException {
		for (int i=0; i<nelements; i++) {
			out.writeInt(arr.get(i).getLeft());
			out.writeInt(arr.get(i).getMiddle());
			out.writeInt(arr.get(i).getRight());
		}
	}

	public void n_way_merge(ArrayList<DataInputStream> files, Integer run, Integer step) throws IOException {
		PriorityQueue<DataManager> queue = new PriorityQueue<>();
		ArrayList<MutableTriple<Integer, Integer, Integer>> result = new ArrayList<>();
		DataManager dm;
		String output_path = tmpdir + "/run_" + Integer.toString(run) + "/" + Integer.toString(step) + ".data";
		DataOutputStream os = open_output_run(output_path, buffer_size);
		for(DataInputStream file: files){
			queue.add(new DataManager(file));
		}
		while(queue.size() != 0){
			dm = queue.poll();
			result.add(dm.pop());
			if(result.size() == memo_elements){
				append_arr(os, result, result.size());
				os.flush();
				result.clear();
			}
			if(!dm.isEmpty()){
				queue.add(dm);
			}
		}
		if(result.size() > 0){
			append_arr(os, result, result.size());
			os.flush();
		}
		os.close();
	}
	public class DataManager implements Comparable<DataManager>{
	    DataInputStream is;
	    MutableTriple<Integer, Integer, Integer> mt;

	    public DataManager(DataInputStream is) throws IOException {
	        this.is = is;
	        this.mt = new MutableTriple<>();
	        if(this.is.available() > 0){
	            this.mt.setLeft(this.is.readInt());
	            this.mt.setMiddle(this.is.readInt());
	            this.mt.setRight(this.is.readInt());
	        } else {
	            this.mt = null;
	        }
	    }

	    public MutableTriple<Integer, Integer, Integer> getTuple() {
	        return mt;
	    }

	    public DataInputStream getIs() {
	        return is;
	    }

	    public MutableTriple<Integer, Integer, Integer> getTupleCopy(){
	        MutableTriple<Integer, Integer, Integer> copy = new MutableTriple<>(mt.getLeft(), mt.getMiddle(), mt.getRight());
	        return copy;
	    }

	    public MutableTriple<Integer, Integer, Integer> peek(){
	        return mt;
	    }

	    public MutableTriple<Integer, Integer, Integer> pop() throws IOException {
	        MutableTriple<Integer, Integer, Integer> temp = new MutableTriple<>(mt.getLeft(), mt.getMiddle(), mt.getRight());
	        reload();
	        return temp;
	    }

	    public void reload() throws IOException {
	        if(this.is.available() > 0){
	            this.mt.setLeft(this.is.readInt());
	            this.mt.setMiddle(this.is.readInt());
	            this.mt.setRight(this.is.readInt());
	        }else {
	            this.mt = null;
	        }
	    }

	    public boolean isEmpty(){
	        return this.mt == null;
	    }
	    @Override
	    public int compareTo(DataManager m2){
	        return this.mt.compareTo(m2.mt);
	    }
	}
	
	public void last_merge(ArrayList<DataInputStream> files, String path) throws IOException {
		PriorityQueue<DataManager> queue = new PriorityQueue<>();
		ArrayList<MutableTriple<Integer, Integer, Integer>> result = new ArrayList<>();
		
		DataManager dm;
		
		DataOutputStream os = open_output_run(path, buffer_size);
		for(DataInputStream file: files){
			queue.add(new DataManager(file));
		}
		
		while(queue.size() != 0){
			dm = queue.poll();
			result.add(dm.pop());
			if(result.size() == memo_elements){
				append_arr(os, result, result.size());
				os.flush();
				result.clear();
			}
			if(!dm.isEmpty()){
				queue.add(dm);
			}
		}
		
		if(result.size() > 0){
			append_arr(os, result, result.size());
			os.flush();
		}
		os.close();
	}
	public static void sort_arr(ArrayList<MutableTriple<Integer, Integer, Integer>> arr) {
		Collections.sort(arr);
	}
	
	// Data size 100MB
	// Heap size 16MB
	// Block size 4KB , 8KB
	// # of block 1000 , 1200 , 1400 ... 2000
	
	public void sort(String infile, String outfile, String tmpdir, int blocksize, int nblocks) throws IOException {
		memo_size = (int) (blocksize * nblocks);
		n_elements = (int) memo_size / 20;
		buffer_size = (int) 2048;
		block_elements = (int) blocksize / 20;
		memo_elements = (int) 1024 * 128 / 20;
		n_way_merge = nblocks > 64 ? 64 : nblocks;
		this.tmpdir = tmpdir;
		File file = new File(tmpdir);
		if (!file.exists()) {
			file.mkdir();
		}
		
		DataInputStream is = DiskIO.open_input_run(infile, blocksize);
		DataOutputStream os;
		ArrayList<MutableTriple<Integer, Integer, Integer>> dataArr = new ArrayList<>();
		
		// 1) initial phase
		
		int run = 1;
		int step = 1;
		String output_path;
		while (is.available() > 0) {
			if (is.available() < memo_elements) {
				read_array(is, (int) is.available() / 20, dataArr);
			} else {
				read_array(is, memo_elements, dataArr);
			}
			sort_arr(dataArr);
			step += 1;
			output_path = tmpdir + "/run_" + Integer.toString(run) + "/" + Integer.toString(step) + ".data";
			os = open_output_run(output_path, buffer_size);
			DiskIO.append_arr(os, dataArr, dataArr.size());
			os.flush();
		}

		// n-way-merge
		String prev_run = tmpdir + "/run_" + Integer.toString(run);
		File[] prev_files = new File(prev_run).listFiles();
		ArrayList<DataInputStream> file_to_merge = new ArrayList<>();
		while (prev_files.length > 1) {
			step = 1;
			run += 1;
			for (int i = 0; i < prev_files.length; i++) {
				file_to_merge.add(open_input_run(prev_files[i].getAbsolutePath(), buffer_size));
				if (file_to_merge.size() == n_way_merge) {
					n_way_merge(file_to_merge, run, step);
					file_to_merge.clear();
					step += 1;
				}
			}
			if(prev_files.length < n_way_merge){
				last_merge(file_to_merge, outfile);
				file_to_merge.clear();
				break;
			}
			if (file_to_merge.size() > 0) {
				n_way_merge(file_to_merge, run, step);
				file_to_merge.clear();
			}
			prev_run = tmpdir + "/run_" + Integer.toString(run);
			prev_files = new File(prev_run).listFiles();
		}
	}
}