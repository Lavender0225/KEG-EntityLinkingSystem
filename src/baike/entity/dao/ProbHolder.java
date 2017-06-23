package baike.entity.dao;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ProbHolder {
	
	public abstract double getEntityPrior(String entityID);
	public abstract double getEntityPopularity(String mention, String entityID);
	public abstract double getMentionProbGivenEntity(String mention, String entityID);
	public abstract double getLinkProb(String mention);
	public abstract int getLinkProbSize();
	public abstract int getMentionEntitySize();
	public abstract int getPriorSize();
	public abstract int getPopularitySize();
	public abstract HashMap<String, Double> getEntityPriorMap();
	public abstract HashMap<String, Double> getLinkProbMap();
	public abstract HashMap<String, HashMap<String, Double>> getPopularityMap();
	public abstract HashMap<String, HashMap<String, Double>> getMentionEntityMap();
	private static Logger logger = LogManager.getLogger(ProbHolder.class);
	
	public HashMap<String, Double> loadEntityPrioreMap(String path){
		InputStream is = null;
		HashMap<String, Double> entityPriorMap = new HashMap<String, Double>();
		long start = System.currentTimeMillis();
		int count = 0;
		try {
		    is = new FileInputStream(path);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] splitList = line.split("::;");
		        if(splitList.length == 2){
			        String entity = line.split("::;")[0];
			        double prob = Double.valueOf(line.split("::;")[1]);
			        entityPriorMap.put(entity, prob);
			        if(count < 2){
			        	logger.info("prior demo item, key1:" +entity + ", p:" + prob);
			        }
			        count ++;
		        }
		    }
		    long end = System.currentTimeMillis();
		    logger.info("prior map loaded. Time:" + (double)(end - start)/1000 + ", size:" + entityPriorMap.size());
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
		return entityPriorMap;  
	}
	
	public HashMap<String, HashMap<String, Double>> loadMGivenEMap(String path){
		InputStream is = null;
		HashMap<String, HashMap<String, Double>> map = new HashMap<String, HashMap<String, Double>>();
		try {
		    is = new FileInputStream(path);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    int count = 0;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] splitList = line.split("::;");
		        if(splitList.length == 3){
		        	String entity = splitList[0];
			        String mention = splitList[1];
			        double prob = Double.valueOf(splitList[2]);
			        if(!map.containsKey(entity)){
			        	map.put(entity, new HashMap<String, Double>());
			        }
			        map.get(entity).put(mention, prob);
			        if(count < 2)
			        	logger.info("MGivenEMap demo item, key1:" +entity + ", key2:" + mention + ", p:" + prob);
			        count ++;
		        }
		    }
		    logger.info("MGivenE Map loaded, size:" + map.size());
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
		return map;  
	}
}
