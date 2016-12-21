package edu.tsinghua.el.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tsinghua.el.common.Constant;

/**
 * Created by ethan on 16/3/23.
 * 
 * @update by ZJ on 16/7/19, add comments
 */
public class IndexBuilder {
	private static final Logger logger = LogManager.getLogger(IndexBuilder.class.getName());
    private static AhoCorasickDoubleArrayTrie<String> acdat = null;
    private static HashMap<String, String> entities = null;
    private static HashMap<String, IndexBuilder> indexMap = new HashMap<String, IndexBuilder>();
    
 
    private IndexBuilder(String entity_path, String trie_path){
    	System.out.println("Indexbuilder constract function...I am being called...");
    	try {
    		File f = new File(trie_path);		// if entity_trie_index_path file exists, then load
    		if(f.exists() && !f.isDirectory()) { 
    			System.out.println("Index file exists, start loading index...");
    		    load(trie_path);
    		}
    		else{
    			build(entity_path, trie_path);
    		}
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
    }
    public static IndexBuilder getInstance(String entity_path, String trie_path){
    	if(indexMap.containsKey(trie_path)){
    		logger.info("The index " + trie_path + "is in memory, getting ...");
    		return indexMap.get(trie_path);
    	}
    	else{
    		logger.info("The index " + trie_path + "is not in memory, new ...");
    		IndexBuilder ibd= new IndexBuilder(entity_path, trie_path);
    		indexMap.put(trie_path, ibd);
    		return ibd;
    	}
    }
    

   /**
    * Load entity list form input_path
    * 
    * @param input_path
    * @return
    * @throws IOException
    */
    private int loadEntity(String input_path)throws IOException {
    	long start = System.currentTimeMillis();
        BufferedReader reader = new BufferedReader(new FileReader(input_path));
        String line;
        String[] items=null;
        entities = new HashMap<String, String>();
        while ((line = reader.readLine()) != null)
        {
            //add main entity
            items = line.split("::=",2);
            if(items.length<2)
                continue;
            entities.put(items[0], items[1]);
        }
        reader.close();
        
   	 	long end = System.currentTimeMillis();
   	 	logger.info("Loading entity list finish! Time:" + (float)(end - start)/1000);
   	 	System.out.println("Loading entity list finish! Time:" + (float)(end - start)/1000);
        return entities.size();
    }

    /**
     * Build the index of entitt list from inputpath
     * 
     * @param input_path
     * @throws IOException
     */
    private void build(String input_path, String trie_path)throws IOException{
    	long start = System.currentTimeMillis();
        if(entities==null){
            System.out.println("#Entity：" + this.loadEntity(input_path));
        }
        if(acdat == null){
	        acdat = new AhoCorasickDoubleArrayTrie<String>();
	        acdat.build(entities);
	        save(trie_path);
        }
        
		long end = System.currentTimeMillis();
		logger.info("Building index finish! Time:" + (float)(end - start)/1000);
		System.out.println("Building index finish! Time:" + (float)(end - start)/1000);
    }
    /**
     * Sava the trie index to the trie_path
     * 
     * @param trie_path
     * @throws Exception
     */
    private void save(String trie_path) throws IOException{
    	long start = System.currentTimeMillis();
    	 ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(trie_path));
    	 acdat.save(out);
    	 out.close();
    	 long end = System.currentTimeMillis();
    	 logger.info("Saving index finish! Time:" + (float)(end - start)/1000);
    	 System.out.println("Saving index finish! Time:" + (float)(end - start)/1000);
    }
    
    /**
     * Load index from trie_path
     * 
     * @param trie_path
     * @throws Exception
     */
    private void load(String trie_path) throws IOException{
    	long start = System.currentTimeMillis();
    	ObjectInputStream oin = new ObjectInputStream(new FileInputStream(trie_path));
    	if(acdat==null)
    		acdat = new AhoCorasickDoubleArrayTrie<String>();
    	try {
			acdat.load(oin);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
    	oin.close();
    	long end = System.currentTimeMillis();
    	logger.info("Loading index finish! Time:" + (float)(end - start)/1000);
    	System.out.println("Loading index finish! Time:" + (float)(end - start)/1000);
    }
    
   
    public List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> parseText(String doc){
    	return acdat.parseText(doc);
    }
    
    /**
     * get the mentions and their candidate entities in a news
     * @param news_path
     * @return
     */
    public HashMap<String, String> parseNews(String news_path){
    	long start = System.currentTimeMillis();
    	BufferedReader reader = null;
    	HashMap<String, String> resultMap = null;
		try {
			resultMap = new HashMap<String, String>();
			reader = new BufferedReader(new FileReader(news_path));
			String line = null;
	    	StringBuffer sb = new StringBuffer();
	    	while((line = reader.readLine())!=null){
	    		sb.append(line).append("\n");
	    	}
	    	String doc = sb.toString();
	    	List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> wordList = parseText(doc);
	        
	        for(AhoCorasickDoubleArrayTrie<String>.Hit<String> tmp_hit:wordList){
	        	logger.info(doc.substring(tmp_hit.begin, tmp_hit.end)+"::="+tmp_hit.value);
	        	resultMap.put(doc.substring(tmp_hit.begin, tmp_hit.end), tmp_hit.value);
	        }
	        long end = System.currentTimeMillis();
	        logger.info("time: " + (start - end));
	        //FileManipulator.outputStringHashMap(resultMap, news_path+"_result.txt", "::=");
	        logger.info("Parsing doc finish! Time:" + (float)(end - start)/1000);
	    	System.out.println("Parsing doc finish! Time:" + (float)(end - start)/1000);
	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return resultMap;
    }
    
    
    //利用sorted entity list, 调用doubleArrayTrie建立索引并保存
    //读入sorted entity list和索引, 使用findMention方法找到所有mention
    public static void main(String[] args) throws Exception
    {
    	IndexBuilder ibd = IndexBuilder.getInstance(Constant.entity_ready_file, Constant.entity_trie);
    	ibd.parseNews(System.getProperty("user.dir") + Constant.news_path);
    }
}
