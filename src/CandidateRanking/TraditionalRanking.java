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
			String result_entity_id = null;
			double tmp_max_score = -1;
			while(entries2.hasNext()){
				Entry<String, Candidate> entry2 = entries2.next();
				String entity_id = entry2.getKey();
				Candidate candidate = entry2.getValue();
				// calculate feature scores
				ArrayList<String> entity_alias = candidate.getEntity().getAlias();
				String mention_label = mention.getLabel();
				
				candidate.setPopularity(Math.log(XloreGetPopularity.getPopularity(entity_id)));
				
				candidate.setLabel_edit_distance(FeatureCal.editDistanceOfEntityAndMention(entity_alias, mention_label));
				candidate.setLabel_equals(FeatureCal.surfaceEqual(entity_alias, mention_label));
				candidate.setLabel_contains(FeatureCal.surfaceContains(entity_alias, mention_label));
				candidate.setLabel_startwith(FeatureCal.startwithOrEndwith(entity_alias, mention_label));
				
				if(mention_label.contains("(") || mention_label.contains("（") || mention_label.contains("[")){
					String[] tmp = mention_label.split("[\\(（\\[]");
					String mention_desc = tmp[1].replaceAll("[\\)）\\]]", "");
					candidate.setSim_desc(FeatureCal.simOfDescAndType(candidate.getEntity(), mention_desc));
				}
				// get final score
				double score = candidate.getPopularity()
						+ 2 * (1 - candidate.getLabel_edit_distance())
						+ 2 * candidate.getLabel_equals()
						+ candidate.getLabel_contains()
						+ candidate.getLabel_startwith()
						+ candidate.getSim_desc();
				candidate.setScore(score);
				// get highest score's entity id
				if(tmp_max_score < score){
					tmp_max_score = score;
					result_entity_id = entity_id;
				}
				// ranking candidates according to final score
				candidateSet.setSet(CandidateSet.sortByValue(candidateSet.getSet(), true));
				// set ranking result in mention
				mention.setResult_entity_id(result_entity_id);
			}
			
		}
		long end = System.currentTimeMillis();
		System.out.println("Ranking finish!" + " Ranking time:"+ (double)(end-start)/1000);
		logger.info("Ranking time:"+ (double)(end-start)/1000);
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TraditionalRanking TRtest = new TraditionalRanking();
		Scanner sc = new Scanner(System.in); 
		while(true){
			System.out.println("Please input a news:");
			String news = sc.nextLine();
			TRtest.processing(news);
		}
		
	}

}
