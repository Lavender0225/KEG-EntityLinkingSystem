package edu.tsinghua.el.candidate.ranking;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import edu.tsinghua.el.model.Candidate;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.Entity;
import edu.tsinghua.el.model.Mention;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ansj.word2vec.Word2VEC;
import edu.tsinghua.api.xlore.GetLinkProb;
import edu.tsinghua.api.xlore.XloreGetPopularity;
import edu.tsinghua.el.mention.parser.CandidateGeneration;
import edu.tsinghua.el.common.*;

public class TraditionalRanking {
	private static HashMap<Mention,CandidateSet> candidateSetMap = null;
	private static Word2VEC vec_model = Word2VEC.getInstance();
	private static HashMap<String, Float> coherenceMap = new HashMap<String, Float>();
	private static double avg_coherence = 0;		// calculated in calCoherence()
	private static double avg_link_prob = 0;		// calculated in calCoherence()
	private static final Logger logger = LogManager.getLogger(TraditionalRanking.class);
	
	public static void processing(String domainName, String doc){
		candidateSetMap = CandidateGeneration.extractMentionForNewsFromString(domainName, doc);
		//logger.info(candidateSetMap.toString());
		ranking();
		coherenceMap = calCoherence();
		logger.info("The average score of coherence: " + avg_coherence);
		prune();
		logger.info(candidateSetMap.toString());
	}
	public void processing(String domainName, String doc, String result_path){
		candidateSetMap = CandidateGeneration.extractMentionForNewsFromString(domainName, doc);
		//ogger.info(candidateSetMap.toString());
		ranking();
		prune();
		logger.info(candidateSetMap.toString());
		outputResult(doc, result_path);
		//outputDocWithLink(link_path, doc);
	}
	
	public static void ranking(){
		long start = System.currentTimeMillis();
		Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		while(entries.hasNext()){
			Entry<Mention, CandidateSet> entry = entries.next();
			Mention mention = entry.getKey();
			CandidateSet candidateSet = entry.getValue();
			Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
			mention.setLink_prob(GetLinkProb.getLinkProbOfMention(mention.getLabel()));
			// first circle, remove the entities with the popularity lower than Constant.t, to reduce the calculation
			while(entries2.hasNext()){
				Entry<String, Candidate> entry2 = entries2.next();
				String entity_id = entry2.getKey();
				Candidate candidate = entry2.getValue();
				//ArrayList<String> entity_alias = candidate.getEntity().getAlias();
				String mention_label = mention.getLabel();
				
				// get popularity
				candidate.setPopularity(XloreGetPopularity.getPopularity(mention_label, entity_id));
				
				if(candidate.getPopularity() < Constant.t){
					// System.out.println("The commonness is too low: "+ mention_label + ", id:" + entity_id);
					entries2.remove();
				}
			}
			
			// second circle, find the highest related score
			entries2 = candidateSet.getSet().entrySet().iterator();
			double max_score = -1;
			while(entries2.hasNext()){	
				Entry<String, Candidate> entry2 = entries2.next();	
				String entity_id = entry2.getKey();
				Candidate candidate = entry2.getValue();
				
				// get the relatedness of an entity
				double relatedness = calRelatednessOfEntity(mention.getLabel(), entity_id);
				
				candidate.setReletedness(relatedness);
				// get highest score's entity id
				if(max_score < relatedness){
					max_score = relatedness;
				}
				//logger.info("Mention: "+ mention_label + "The most related entity score:" + max_score);
			}
			
			if(max_score <= 0){	//如果最大相关度的分数是负数，直接认为没有合适的entity可以link
				mention.setResult_entity_id(null);
			}
			else{
			
				CandidateSet.sortByValue(candidateSet.getSet(), true);
				//logger.info(candidateSet.getSet());
				int maintain_amount = (int) (candidateSet.getSet().size() * Constant.sigma + 1);
				// second, delete the candidates that are not related to the mention
				entries2 = candidateSet.getSet().entrySet().iterator();
				int i = 1;
				while(entries2.hasNext()){
					Entry<String, Candidate> entry2 = entries2.next();
					if(i > maintain_amount){
						logger.info("remove item:" + entry2.getKey());
						entries2.remove();
					}
					i ++;
				}
				
				// third, choose the one with highest popularity as the final result
				String result_entity_id = null;
				double highest_score = 0;
				entries2 = candidateSet.getSet().entrySet().iterator();
				while(entries2.hasNext()){
					Entry<String, Candidate> entry2 = entries2.next();
					Candidate candidate = entry2.getValue();
					if(candidate.getPopularity() > highest_score){
						highest_score = candidate.getPopularity();
						result_entity_id = candidate.getEntity().getId();
					}
				}
				mention.setResult_entity_id(result_entity_id);
			}

		}
		long end = System.currentTimeMillis();
		candidateSetMap = sortByMentionPos(candidateSetMap, false);
		System.out.println("Ranking finish!" + " Ranking time:"+ (double)(end-start)/1000);
		logger.info("Ranking time:"+ (double)(end-start)/1000);
	}
	
	/**
	 * prune
	 * strategy: 1） if the coherence score is lower than average_coherence, then remove it.
	 * 				2） if the link probability is lower than average_cohenrence/10, then remove it
	 */
	public static void prune(){
		Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		while(entries.hasNext()){
			Entry<Mention, CandidateSet> entry = entries.next();
			Mention mention = entry.getKey();
			if(mention.getLink_prob() < avg_link_prob/10){
				entries.remove();
			}
			else{
				CandidateSet candidateSet = entry.getValue();
				Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
				while(entries2.hasNext()){
					Entry<String, Candidate> entry2 = entries2.next();
					String entity_id = entry2.getKey();
					Candidate candidate = entry2.getValue();
					// get coherence
					double coherence_score = coherenceMap.get(entity_id);
					if(coherence_score < avg_coherence){
						if(mention.getResult_entity_id() != null && entity_id.contentEquals(mention.getResult_entity_id())){
							mention.setResult_entity_id(null);
						}
						entries2.remove();
						logger.info("The coherence of entity:" + entity_id + " of mention: " + mention.getLabel() + " is lower than avg: " + coherence_score);
					}
					else{
						candidate.setCohenrence_score(coherence_score);
					}
				}
			}
		}
		
		
	}
	/**
	 * 计算一个实体的relatedness值
	 */
	private static double calRelatednessOfEntity(String mention_label, String entity_id){
		//logger.info("Calculating relatedness");

		Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		double candidateSetSum = 0;
		double entitySimScore = 0;
		double prob = 0;
		double result_score = 0;
		while(entries.hasNext()){
			Entry<Mention, CandidateSet> entry = entries.next();
			Mention mention = entry.getKey();
			if(!mention.getLabel().contentEquals(mention_label)){
				CandidateSet candidateSet = entry.getValue();
				Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
				candidateSetSum = 0;
				while(entries2.hasNext()){
					Entry<String, Candidate> entry2 = entries2.next();
					String entity_id_candidate = entry2.getKey();
					entitySimScore = vec_model.similarityOfBaiduEntity(entity_id, entity_id_candidate);
					prob = XloreGetPopularity.getPopularity(mention.getLabel(), entity_id_candidate);
					candidateSetSum += entitySimScore * prob;
				}
				candidateSetSum /= (candidateSet.getSet().size() + 1);
				result_score += candidateSetSum;
			}
		}
		//result_score /= (candidateSetMap.size() + 1);
		return result_score;
	}
	
	/**
	 * 计算实体的cohenrence值
	 * 
	 * @return
	 */
	private static HashMap<String, Float> calCoherence(){
		logger.info("Calculating cohenrence...");
		HashMap<String, Float> resultMap = new HashMap<String, Float>();
		HashSet<Entity> entitySet = new HashSet<Entity>();
		Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		while(entries.hasNext()){
			Entry<Mention, CandidateSet> entry = entries.next();
			Mention mention = entry.getKey();
			CandidateSet candidateSet = entry.getValue();
			Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
			avg_link_prob += mention.getLink_prob();
			while(entries2.hasNext()){
				Entry<String, Candidate> entry2 = entries2.next();
				Candidate candidate = entry2.getValue();
				entitySet.add(candidate.getEntity());
			}
		}
		for(Entity e : entitySet){
			float score = 0;
			for (Entity e2 : entitySet){
				
				score += vec_model.similarityOfBaiduEntity(e.getId(), e2.getId());
			}
			score = score/(entitySet.size() + 1);		// 平滑，防止分母为0
			avg_coherence += score;
			resultMap.put(e.getId(), score);
		}
		avg_coherence /= (entitySet.size() + 1);		// 平滑，防止分母为0  
		avg_link_prob /= (entitySet.size() + 1);		// 平滑，防止分母为0
		
		System.out.println("Size of coherence map: " + resultMap.size());
		return resultMap;
	}
	
	
	public void outputResult(String doc, String filepath){
		OutputStream o = null;
	    try {
	        o = new FileOutputStream(filepath, true);
	        @SuppressWarnings("resource")
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"), 512);
	        Iterator<Entry<Mention,CandidateSet>> entries = candidateSetMap.entrySet().iterator();  
	        writer.write("\n**************************************************************\n");
	        writer.write("news content:" + doc + "\n\n");
	        String doc_with_link = "";
	        int last_end = 0;
	        while (entries.hasNext()) {  
	            Map.Entry<Mention,CandidateSet> entry =  entries.next();  
	            Mention mention = entry.getKey();  
	            CandidateSet candidateSet = entry.getValue(); 
	            Candidate c = candidateSet.getSet().get(mention.getResult_entity_id());
	            
	            if (c != null){
	            	writer.write(mention.getLabel()+ "----" + candidateSet.getSet().get(mention.getResult_entity_id()).getEntity() + "\n");
	            	doc_with_link += doc.substring(last_end, mention.getPos_end()) + "[http://xlore.org/sigInfo.json?uri=http%3A%2F%2Fxlore.org%2Finstance%2F" + mention.getResult_entity_id() + "]";
	            	last_end = mention.getPos_end();
	            } 
	        }
	        doc_with_link += doc.substring(last_end, doc.length());
	        writer.write(doc_with_link + "\n");
	        logger.info("doc with link:" + doc_with_link + "\n");
	        
	    }catch (IOException ioe){
	        ioe.printStackTrace();
	    } finally {
	        try {
	            if (o != null) {
	                o.close();
	                o = null;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public void outputDocWithLink(String filepath, String doc){
		OutputStream o = null;
	    try {
	        o = new FileOutputStream(filepath, true);
	        @SuppressWarnings("resource")
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"), 512);
	        Iterator<Entry<Mention,CandidateSet>> entries = candidateSetMap.entrySet().iterator();  
	        String doc_with_link = "";
	        int last_end = 0;
	        while (entries.hasNext()) {  
	            Map.Entry<Mention,CandidateSet> entry =  entries.next();  
	            Mention mention = entry.getKey();  
	            CandidateSet candidateSet = entry.getValue(); 
	            Candidate c = candidateSet.getSet().get(mention.getResult_entity_id());
	            if (c != null){
	            	doc_with_link += doc.substring(last_end, mention.getPos_end()) + "[http://xlore.org/sigInfo.json?uri=http%3A%2F%2Fxlore.org%2Finstance%2F" + mention.getResult_entity_id() + "]";
	            	last_end = mention.getPos_end() + 1;
	            }
	            
	        }
	        writer.write(doc_with_link + "\n");
	        logger.info("doc with link:" + doc_with_link + "\n");
	        //writer.write(strbuffer.toString() + "\n");
	        
	    }catch (IOException ioe){
	        ioe.printStackTrace();
	    } finally {
	        try {
	            if (o != null) {
	                o.close();
	                o = null;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
	
	public static <K, V extends Comparable<? super V>> HashMap<Mention,CandidateSet> sortByMentionPos(HashMap<Mention,CandidateSet> map , final boolean reverse){
        List<Map.Entry<Mention,CandidateSet>> list =new LinkedList<Map.Entry<Mention,CandidateSet>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<Mention,CandidateSet>>()
        {
            @Override
            public int compare( Map.Entry<Mention,CandidateSet> o1, Map.Entry<Mention,CandidateSet> o2 )
            {
                if (reverse)
                    return o2.getKey().getPos_end() - o1.getKey().getPos_end();
                return o1.getKey().getPos_end() - o2.getKey().getPos_end();
            }
        } );
        HashMap<Mention,CandidateSet> result = new LinkedHashMap<Mention,CandidateSet>();
        for (Map.Entry<Mention,CandidateSet> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 
	
	
	
	public static HashMap<Mention, CandidateSet> getCandidateSetMap() {
		return candidateSetMap;
	}

}
