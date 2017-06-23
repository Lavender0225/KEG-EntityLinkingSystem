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

public class WikiURLMap {
	private  static HashMap<String, String>  wikiTitleURIMap = null;
	private  static HashMap<String, String>  wikiURITitleMap = null;
	private static Logger logger = LogManager.getLogger(WikiURLMap.class);
	
	private static void loadMap(){
		InputStream is = null;
		long start = System.currentTimeMillis();
		wikiTitleURIMap = new HashMap<String, String>();
		wikiURITitleMap = new HashMap<String, String>();
		try {
		    is = new FileInputStream(Constant.wikiURLMap);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] splitList = line.split("::;");
		        if(splitList.length == 2){
			        String entity = splitList[0];
			        String url = splitList[1];
			        wikiTitleURIMap.put(entity, url);
			        wikiURITitleMap.put(url, entity);
		        }
		    }
		    long end = System.currentTimeMillis();
		    logger.info("wikiURLmap loaded, size:" + wikiTitleURIMap.size() + ", time:"+ (double)(end-start)/1000);
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
	
	public static String getURL(String title){
		if(wikiTitleURIMap == null) 
			loadMap();
		if(wikiTitleURIMap.containsKey(title)){
			return wikiTitleURIMap.get(title);
		}
		return Constant.wikiEntityPrefix + title;
	}
	
	public  static String getTitle(String url){
		if(wikiURITitleMap == null) 
			loadMap();
		if(wikiURITitleMap.containsKey(url)){
			return wikiURITitleMap.get(url);
		}
		return null;
	}

}
