package edu.tsinghua.el.candidate.ranking;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
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
import edu.tsinghua.api.xlore.XloreGetPopularity;
import edu.tsinghua.el.mention.parser.CandidateGeneration;

import edu.tsinghua.el.common.*;

public class TraditionalRanking {
	private static HashMap<Mention,CandidateSet> candidateSetMap = null;
	private static Word2VEC vec_model = Word2VEC.getInstance();
	private static HashMap<String, Float> coherenceMap = new HashMap<String, Float>();
	private static final Logger logger = LogManager.getLogger(TraditionalRanking.class);
	
	public static void processing(String doc){
		candidateSetMap = CandidateGeneration.extractMentionForNewsFromString(doc);
		//logger.info(candidateSetMap.toString());
		coherenceMap = calCoherence();
		getFeatures();
		logger.info(candidateSetMap.toString());
	}
	public void processing(String doc, String result_path){
		candidateSetMap = CandidateGeneration.extractMentionForNewsFromString(doc);
		//ogger.info(candidateSetMap.toString());
		coherenceMap = calCoherence();
		getFeatures();
		logger.info(candidateSetMap.toString());
		outputResult(doc, result_path);
		//outputDocWithLink(link_path, doc);
	}
	
	public static void getFeatures(){
		long start = System.currentTimeMillis();
		Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		while(entries.hasNext()){
			Entry<Mention, CandidateSet> entry = entries.next();
			Mention mention = entry.getKey();
			CandidateSet candidateSet = entry.getValue();
			Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
			double max_score = -1;
			// first circle, find the highest related score
			while(entries2.hasNext()){
				Entry<String, Candidate> entry2 = entries2.next();
				String entity_id = entry2.getKey();
				Candidate candidate = entry2.getValue();
				// calculate feature scores
				ArrayList<String> entity_alias = candidate.getEntity().getAlias();
				String mention_label = mention.getLabel();
				
				candidate.setPopularity(XloreGetPopularity.getPopularity(mention_label, entity_id));
				 if(candidate.getPopularity() < Constant.t){
					 // System.out.println("The commonness is too low: "+ mention_label + ", id:" + entity_id);
					 entries2.remove();
				 }
				/*
				candidate.setLabel_edit_distance(FeatureCal.editDistanceOfEntityAndMention(entity_alias, mention_label));
				candidate.setLabel_equals(FeatureCal.surfaceEqual(entity_alias, mention_label));
				candidate.setLabel_contains(FeatureCal.surfaceContains(entity_alias, mention_label));
				candidate.setLabel_startwith(FeatureCal.startwithOrEndwith(entity_alias, mention_label));
				*
				
				if(mention_label.contains("(") || mention_label.contains("（") || mention_label.contains("[")){
					String[] tmp = mention_label.split("[\\(（\\[]");
					String mention_desc = tmp[1].replaceAll("[\\)）\\]]", "");
					candidate.setSim_desc(FeatureCal.simOfDescAndType(candidate.getEntity(), mention_desc));
				}
				
				candidate.setSim_context_entity(FeatureCal.simContextEntity(candidate.getEntity(), mention));
				*/
				// get final label_sim score
				
				/*
				double  lable_sim_score = (0.4 * (1 - candidate.getLabel_edit_distance())
						+ 0.4 * candidate.getLabel_equals()
						+ 0.2 * candidate.getLabel_contains()
						+ 0.2 * candidate.getLabel_startwith()
						+ 0.2 * candidate.getSim_desc()
						+ candidate.getSim_context_entity())/2;		
				
				candidate.setScore(lable_sim_score);
				
				
				*/
				 
				double relatedness = coherenceMap.get(candidate.getEntity().getId());
				candidate.setCohenrence_score(relatedness);
				candidate.setScore(relatedness);
				// get highest score's entity id
				if(max_score < relatedness){
					max_score = relatedness;
				}
				//logger.info("Mention: "+ mention_label + "The most related entity score:" + max_score);
			}
			
			if(max_score < 0){	//如果最大相关度的分数是负数，直接认为没有合适的entity可以link
				mention.setResult_entity_id(null);
			}
			else{
			
				// second, delete the candidates that are not related to the mention
				entries2 = candidateSet.getSet().entrySet().iterator();
				while(entries2.hasNext()){
					Entry<String, Candidate> entry2 = entries2.next();
					Candidate candidate = entry2.getValue();
					if(candidate.getScore() < max_score - Constant.sigma){
						entries2.remove();
					}
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
				if(highest_score < 0.1){			//如果最高的popularity低于0.1，也认为没有合适的entity来link
					mention.setResult_entity_id(null);
				}
				else{
//					if(candidateSet.getSet().get(result_entity_id).getCohenrence_score() < 0.1) //如果最高popularity的实体对应的coherence_score低于0.1，也认为没有
//						mention.setResult_entity_id(null);
//					else
						mention.setResult_entity_id(result_entity_id);
				}
				logger.info(mention.getLabel() + ", the highest commonness: "+ highest_score + ", id: "+ result_entity_id);
				
				
				// finally, for the mentions with only one candidate, decide whether to keep it
				if(candidateSet.getSet().size() == 1){
					entries2 = candidateSet.getSet().entrySet().iterator();
					Entry<String, Candidate> entry2 = entries2.next();
					Candidate candidate = entry2.getValue();
					if((candidate.getScore() > 0.1 && candidate.getPopularity() > 0.1) || (candidate.getScore() < 0.1 && candidate.getPopularity() > 0.9)
							|| (candidate.getScore() > 0.9 && candidate.getPopularity() < 0.1)){
						
					}
					else{
						mention.setResult_entity_id(null);
					}
					
				}
			}
		}
		long end = System.currentTimeMillis();
		candidateSetMap = sortByMentionPos(candidateSetMap, false);
		System.out.println("Ranking finish!" + " Ranking time:"+ (double)(end-start)/1000);
		logger.info("Ranking time:"+ (double)(end-start)/1000);
		
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
			CandidateSet candidateSet = entry.getValue();
			Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
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
			score = (score - 1)/entitySet.size();
			resultMap.put(e.getId(), score);
		}
		
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
	public static void main(String[] args) {
		TraditionalRanking TRtest = new TraditionalRanking();
		Scanner sc = new Scanner(System.in); 
		while(true){
			System.out.println("Please input a news:");
			String news = sc.nextLine();
			TRtest.processing(news);
		}
		
	}

}
