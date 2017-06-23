package baike.entity.dao;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BaikeProbsHolder extends ProbHolder{
	private HashMap<String, Double> entityPriorMap = null;
	private HashMap<String, Double> linkProbMap = null;
	private HashMap<String, HashMap<String, Double>> popularityMap = null;	// P(e|m) key: mention, value:map<id, P(e|m)>
	private HashMap<String, HashMap<String, Double>> mentionEntityMap = null;  // P(m|e) key: entity_id value: map<menyion, P(m|e)>
	private double defaultLinkProb = 1;
	private int linkProbSize = 0;
	private int popularitySize = 0;
	private int mentionEntitySize = 0;
	private int priorSize = 0;
	
	private Logger logger = LogManager.getLogger(BaikeProbsHolder.class);
	
	public BaikeProbsHolder(String entityPriorPath, String MFivenEProbPath, String linkProbPath){
		entityPriorMap = loadEntityPrioreMap(entityPriorPath);
		priorSize = entityPriorMap.size();
		mentionEntityMap = loadMGivenEMap(MFivenEProbPath);
		mentionEntitySize = mentionEntityMap.size();
		loadLinkProbMapAndPopularity(linkProbPath);
		linkProbSize = linkProbMap.size();
	    popularitySize = popularityMap.size();
	}
	@Override
	public double getEntityPrior(String entityID) {
		if(entityPriorMap.containsKey(entityID))
			return entityPriorMap.get(entityID);
		return (double) 1/ (double) entityPriorMap.size();
	}

	@Override
	public double getEntityPopularity(String mention, String entityID) {
		if(popularityMap.containsKey(mention)){
			if(popularityMap.get(mention).containsKey(entityID))
				return popularityMap.get(mention).get(entityID);
		}
		return 0.0001;
	}

	@Override
	public double getMentionProbGivenEntity(String mention, String entityID) {
		if(mentionEntityMap.containsKey(entityID))
			if(mentionEntityMap.get(entityID).containsKey(mention))
				return mentionEntityMap.get(entityID).get(mention);
		return 0.0001;
	}
	
	private void loadLinkProbMapAndPopularity(String linkProbPath){
		linkProbMap = new HashMap<String, Double>();
		popularityMap = new HashMap<String, HashMap<String, Double>>();
		InputStream is = null;
		long start = System.currentTimeMillis();
		int count = 0;
		try {
		    is = new FileInputStream(linkProbPath);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] lineList = line.split("::;");
		        if(lineList.length == 6){
		
			        String mention = lineList[0];
			        double linkProb = Double.valueOf(lineList[4]);
			        String id = lineList[1];
			        double popularity = Double.valueOf(lineList[5]);
			        if(!popularityMap.containsKey(mention))
			        	popularityMap.put(mention, new HashMap<String,Double>());
			        popularityMap.get(mention).put(id, popularity);
			        if(count < 2)
			        	System.out.println("popularity demo item, key1:" +mention + ", key2:" + id + ", p:" + popularity);
			        if(!linkProbMap.containsKey(mention)){
			        	linkProbMap.put(mention, linkProb);
			        	if(count < 2)
			        		logger.info("link_prob demo item, key1:" +mention + ", p:" + linkProb);
			        	if(linkProb < defaultLinkProb) defaultLinkProb = linkProb;
			        }
			        count ++;
		        }
		    }
		    
		    long end = System.currentTimeMillis();
		    logger.info(linkProbPath + " loaded. Time:" + (double)(end-start)/1000);
		    logger.info("link prob map size:" + linkProbMap.size());
		    logger.info("popularity map size:" + popularityMap.size());
		    
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
	@Override
	public double getLinkProb(String mention) {
		if(linkProbMap.containsKey(mention)){
			return linkProbMap.get(mention);
		}
		return defaultLinkProb;
	}
	public int getLinkProbSize() {
		return linkProbSize;
	}

	public int getPopularitySize() {
		return popularitySize;
	}

	public int getMentionEntitySize() {
		return mentionEntitySize;
	}

	public int getPriorSize() {
		return priorSize;
	}
	
	
	public HashMap<String, Double> getEntityPriorMap() {
		return entityPriorMap;
	}
	public HashMap<String, Double> getLinkProbMap() {
		return linkProbMap;
	}
	public HashMap<String, HashMap<String, Double>> getPopularityMap() {
		return popularityMap;
	}
	public HashMap<String, HashMap<String, Double>> getMentionEntityMap() {
		return mentionEntityMap;
	}
	public void showDemo(HashMap<String, Double> map){
		int count = 0;
		for(String s : map.keySet()){
			if(count < 10)
				logger.info("key:" + s + ", value:" + map.get(s));
			else break;
		}
	}
	
	
}
