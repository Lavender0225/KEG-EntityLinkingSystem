package edu.tsinghua.el.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tsinghua.el.common.Constant;
import edu.tsinghua.el.common.PropertiesReader;
import edu.tsinghua.el.index.AhoCorasickDoubleArrayTrie.Hit;

/**
 * Created by ethan on 16/3/23.
 * 
 * @update by ZJ on 16/7/19, add comments
 * @update by ZJ on 17/2/13, add indexMap
 */
public class IndexBuilder {
	private static final Logger logger = LogManager.getLogger(IndexBuilder.class.getName());
    private static HashMap<String, String> indexDomainPathMap = null;
    private static HashMap<String, AhoCorasickDoubleArrayTrie<String>> indexInstanceMap = new HashMap<String, AhoCorasickDoubleArrayTrie<String>>();
    
    private static class IndexBuilderHolder{
    	public static IndexBuilder instance = new IndexBuilder();
    }
    private IndexBuilder(){
    	System.out.println("Indexbuilder constract function...I am being called...");
    	indexDomainPathMap = PropertiesReader.getDomainIndexMap();
    	Iterator<Entry<String, String>> entries = indexDomainPathMap.entrySet().iterator();  
    	while (entries.hasNext()) {  
    	    Map.Entry<String, String> entry =  entries.next();  
    	    String domainName = entry.getKey();  
    	    String path = entry.getValue();  
    	    try {
				loadTrie(domainName, path);			// load index into memory and add it to indexInstanceMap
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	
    }
    public static IndexBuilder getInstance(){
    	return IndexBuilderHolder.instance;
    }
    public static AhoCorasickDoubleArrayTrie<String> getIndex(String domainName){
    	if(!indexInstanceMap.containsKey(domainName)){
    		logger.info("The index " + domainName + " is not in memory, new ...");
    		try {
				loadTrie(domainName, indexDomainPathMap.get(domainName));
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	return indexInstanceMap.get(domainName);
    }
    
    
    /**
     * Load index from trie_path
     * 
     * @param domainName, triePath
     * @throws IOException
     */
    private static boolean loadTrie(String domainName, String triePath) throws IOException{
    	long start = System.currentTimeMillis();
    	if(indexInstanceMap.containsKey(domainName))
    		return true;
    	ObjectInputStream oin = new ObjectInputStream(new FileInputStream(triePath));
    	AhoCorasickDoubleArrayTrie<String>	acdat = new AhoCorasickDoubleArrayTrie<String>();
    	try {
			acdat.load(oin);
			indexInstanceMap.put(domainName, acdat);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
    	oin.close();
    	long end = System.currentTimeMillis();
    	logger.info("Loading "+ domainName + " finish! Time:" + (float)(end - start)/1000);
    	System.out.println("Loading "+ domainName + " finish! Time:" + (float)(end - start)/1000);
    	return true;
    }
    
   
    public static List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> parseText(String domainName, String doc){
    	return getIndex(domainName).parseText(doc);
    }
	public static List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> parseTextFromMultiIndex(String domainNameList,
			String doc) {
		String[] nameList = domainNameList.split(",");
		HashSet<AhoCorasickDoubleArrayTrie<String>.Hit<String>> collectedEmits = new HashSet<AhoCorasickDoubleArrayTrie<String>.Hit<String>>();
		for (String s : nameList){
			logger.info("parsing domain:" + s);
			for(AhoCorasickDoubleArrayTrie<String>.Hit<String> tmp : parseText(s,doc)){
				if(!collectedEmits.contains(tmp)){
					collectedEmits.add(tmp);
				}
			}
		}
		//logger.info("no duplicated parsed collections:");
		//logger.info(collectedEmits);
		List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> result = new ArrayList<AhoCorasickDoubleArrayTrie<String>.Hit<String>>();
		result.addAll(collectedEmits);
		return result;
	}
    
}
