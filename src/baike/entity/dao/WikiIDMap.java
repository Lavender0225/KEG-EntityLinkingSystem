package baike.entity.dao;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tsinghua.el.common.Constant;

public class WikiIDMap {
	private static HashMap<String, String>  wikiIDTitleMap = null;
	private static HashMap<String, String> wikiTitleIdMap = null;
	private static Logger logger = LogManager.getLogger(WikiURLMap.class);
	
	private static void loadMap(){
		InputStream is = null;
		long start = System.currentTimeMillis();
		wikiIDTitleMap = new HashMap<String, String>();
		wikiTitleIdMap = new HashMap<String, String>();
		try {
		    is = new FileInputStream(Constant.wikiIdMap);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    int count = 0;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] splitList = line.split("\t");
		        if(splitList.length == 2){
			        String id = splitList[0];
			        String name = splitList[1];
			        wikiIDTitleMap.put(id, name);
			        wikiTitleIdMap.put(name, id);
			        if(count < 5){
			        	System.out.println("wiki id title map, id:" + id + ", title:" + name );
			        }
			        count ++;
		        }
		    }
		    long end = System.currentTimeMillis();
		    logger.info("wikiIDmap loaded, time:"+ (double)(end-start)/1000 + ", size:" + wikiIDTitleMap.size());
		    logger.info("wikiIDmap loaded, time:"+ (double)(end-start)/1000 + ", size:" + wikiTitleIdMap.size());
		}catch (IOException ioe){
		    ioe.printStackTrace();
		    logger.info(ioe.getMessage());
		} finally {
		    try {
		        if (is != null) {
		            is.close();
		            is = null;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		        logger.info(e.getMessage());
		    }
		} 
	}
	
	public static String getTitle(String id){
		if(wikiIDTitleMap == null) 
			loadMap();
		if(wikiIDTitleMap.containsKey(id)){
			return wikiIDTitleMap.get(id);
		}
		return null;
	}
	public static String getId(String title){
		if(wikiTitleIdMap == null) 
			loadMap();
		if(wikiTitleIdMap.containsKey(title)){
			return wikiTitleIdMap.get(title);
		}
		return null;
	}
}
