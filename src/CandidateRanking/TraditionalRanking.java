package CandidateRanking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import CandidateGeneration.GenCandidateSet;
import Common.Constant;
import Model.Candidate;
import Model.CandidateSet;
import Model.Mention;
import XloreAPI.XloreGetPopularity;

public class TraditionalRanking {
	private HashMap<Mention,CandidateSet> candidateSetMap = null;
	private static final Logger logger = LogManager.getLogger(TraditionalRanking.class);
	
	public void processing(String doc){
		candidateSetMap = GenCandidateSet.extractMentionForNewsFromString(doc);
		logger.info(candidateSetMap.toString());
		getFeatures();
		logger.info(candidateSetMap.toString());
	}
	
	public void getFeatures(){
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
					 System.out.println("The commonness is too low: "+ mention_label + ", id:" + entity_id);
					 entries2.remove();
				 }
				
				candidate.setLabel_edit_distance(FeatureCal.editDistanceOfEntityAndMention(entity_alias, mention_label));
				candidate.setLabel_equals(FeatureCal.surfaceEqual(entity_alias, mention_label));
				candidate.setLabel_contains(FeatureCal.surfaceContains(entity_alias, mention_label));
				candidate.setLabel_startwith(FeatureCal.startwithOrEndwith(entity_alias, mention_label));
				
				if(mention_label.contains("(") || mention_label.contains("（") || mention_label.contains("[")){
					String[] tmp = mention_label.split("[\\(（\\[]");
					String mention_desc = tmp[1].replaceAll("[\\)）\\]]", "");
					candidate.setSim_desc(FeatureCal.simOfDescAndType(candidate.getEntity(), mention_desc));
				}
				
				candidate.setSim_context_entity(FeatureCal.simContextEntity(candidate.getEntity(), mention));
				// get final score
				double  lable_sim_score = (0.4 * (1 - candidate.getLabel_edit_distance())
						+ 0.4 * candidate.getLabel_equals()
						+ 0.2 * candidate.getLabel_contains()
						+ 0.2 * candidate.getLabel_startwith()
						+ 0.2 * candidate.getSim_desc()
						+ candidate.getSim_context_entity())/2;
				candidate.setScore(lable_sim_score);
				// get highest score's entity id
				if(max_score < lable_sim_score){
					max_score = lable_sim_score;
				}
				
				//logger.info("Mention: "+ mention_label + "The most related entity score:" + max_score);
			}
			
			
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
			logger.info(mention.getLabel() + ", the highest commonness: "+ highest_score + ", id: "+ result_entity_id);
			mention.setResult_entity_id(result_entity_id);
			
			// finally, for the mentions with only one candidate, decide whether to keep it
			if(candidateSet.getSet().size() == 1){
				entries2 = candidateSet.getSet().entrySet().iterator();
				Entry<String, Candidate> entry2 = entries2.next();
				Candidate candidate = entry2.getValue();
				if(candidate.getScore() < 0.3){
					//entries2.remove();
					mention.setResult_entity_id(null);
				}
				
			}
		}
		long end = System.currentTimeMillis();
		System.out.println("Ranking finish!" + " Ranking time:"+ (double)(end-start)/1000);
		logger.info("Ranking time:"+ (double)(end-start)/1000);
		
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
