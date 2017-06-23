package edu.tsinghua.el.evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.tsinghua.el.model.CandidateSet;

public class CandidateGeneration {
	private HashMap<String, CandidateSet> candidateSetMap = new HashMap<String, CandidateSet>();
	private HashMap<String, ArrayList<String>> dict = new HashMap<String, ArrayList<String>>();
	private HashSet<String> entitySet = new HashSet<String>();
	private String dictPath = "/home/zj/EntityLinkingWeb/data/wiki/dic/dictionary_wiki.dat";
	private String groundTruthPath = "/home/zj/EntityLinkingWeb/data/evaluation/AIDA-YAGO2-annotations.tsv";
	
	public void loadDict(){
		InputStream is = null;
		try {
		    is = new FileInputStream(dictPath);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] list = line.split("::=");
		        String mention = list[0];
		        dict.put(mention, new ArrayList<String>());
		        for(int i = 1; i < list.length; i ++){
		        	dict.get(mention).add(list[i]);
		        	if(!entitySet.contains(list[i]))
		        		entitySet.add(list[i]);
		        }
		    }
		    System.out.println("load finished, dict size:" + dict.size() + ", entitySet size:" + entitySet.size());
		}catch (IOException ioe){
		    ioe.printStackTrace();
		} finally {
		    try {
		        if (is != null) {
		            is.close();
		            is = null;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}  
	}
	
}
