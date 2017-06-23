package edu.tsinghua.el.candidate.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ansj.word2vec.Word2VEC;
import edu.tsinghua.api.xlore.GetLinkProb;
import edu.tsinghua.api.xlore.XloreGetPopularity;
import edu.tsinghua.el.mention.parser.CandidateGeneration;
import edu.tsinghua.el.model.Candidate;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.Mention;

public class RepresentationRanking {
	private static HashMap<Mention,CandidateSet> candidateSetMap = null;
	private static Word2VEC vecModel = Word2VEC.getInstance();
	private static final Logger logger = LogManager.getLogger(RepresentationRanking.class);
	
	public static void processing(String domainNameList, String doc, String language){
		candidateSetMap = CandidateGeneration.extractMentionForNewsFromString(domainNameList, doc, language);
		//L2R order:sortByMentionPos()
		//S2R order:sortByCSetSize()
		candidateSetMap = sortByCSetSize(candidateSetMap, false); // size 1 -> size n
		//logger.info("Before Ranking:" + candidateSetMap.toString());
		ranking();

		logger.info(candidateSetMap.toString());
	}
	
	public static void ranking(){
		long start = System.currentTimeMillis();
		ArrayList<String> unanbiguousEntitySet = new ArrayList<String>();
		int pre_size = 0, now_size = 0;
		do{
			pre_size = now_size;
			if(now_size == 0){
				// find the unambiguous mentions at first
				Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
				while(entries.hasNext()){
					Entry<Mention, CandidateSet> entry = entries.next();
					Mention mention = entry.getKey();
					CandidateSet candidateSet = entry.getValue();
					Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
					mention.setLink_prob(GetLinkProb.getLinkProbOfMention(mention.getLabel()));	// set link prob P(m)
					if(entries2.hasNext()){
						Entry<String, Candidate> entry2 = entries2.next();
						String entity_id = entry2.getKey();
						Candidate candidate = entry2.getValue();
						candidate.setPopularity(XloreGetPopularity.getPopularity(mention.getLabel(), entity_id)); // set P(e|m)
						//if the mention only have one candidate and the p(e|m) >= 0.95, then we think it's unambiguous
						if(candidate.getPopularity() >= 0.95 && vecModel.containsEntity(entity_id)){
							mention.setResult_entity_id(entity_id);
							unanbiguousEntitySet.add(entity_id);
							candidate.setContextWordsSim(calSimOfContextWords(entity_id, mention.getContext_words())); // get contextual cosine probability
							candidate.setContextEntitySim(calSimOfContextEntities(entity_id, unanbiguousEntitySet));	// get the coherence entity probability
							candidate.setBelifScore(calBeliefScore(candidate,mention.getLink_prob()));
							logger.info("unambiguous entities, mention:" + mention.getLabel() + ", [" + mention.getPosition().begin + "," + mention.getPosition().end + "]"
									+ ", result_entity:" + entity_id);
							now_size ++;		//change now_size
						}
						
					}
				}
			}
			else{
				Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
				while(entries.hasNext()){
					Entry<Mention, CandidateSet> entry = entries.next();
					Mention mention = entry.getKey();
					CandidateSet candidateSet = entry.getValue();
					Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
					// 对有歧义的mention进行排岐
					if(mention.getResult_entity_id() == null){
						mention.setLink_prob(GetLinkProb.getLinkProbOfMention(mention.getLabel()));	// set link prob P(m)
						double maxScore = -1;
						while(entries2.hasNext()){
							Entry<String, Candidate> entry2 = entries2.next();
							String entity_id = entry2.getKey();
							Candidate candidate = entry2.getValue();
							if(vecModel.containsEntity(entity_id)){
								candidate.setPopularity(XloreGetPopularity.getPopularity(mention.getLabel(), entity_id)); // set P(e|m)
								candidate.setContextWordsSim(calSimOfContextWords(entity_id, mention.getContext_words())); // get contextual cosine probability
								candidate.setContextEntitySim(calSimOfContextEntities(entity_id, unanbiguousEntitySet));	// get the coherence entity probability
								candidate.setBelifScore(calBeliefScore(candidate,mention.getLink_prob()));
								if(candidate.getBelifScore() > maxScore){
									maxScore = candidate.getBelifScore();
									mention.setResult_entity_id(entity_id);
								}
							}
							else entries2.remove();
						}
						if(mention.getResult_entity_id() != null){
							now_size ++;
							unanbiguousEntitySet.add(mention.getResult_entity_id());
						}
//						if(candidateSet.getSet().size() > 1){
//							candidateSet.setSet(sortByBeliefScore(candidateSet.getSet(), true));
//							entries2 = candidateSet.getSet().entrySet().iterator();
//							double maxScore = 0, secondScore = 0;
//							if(entries2.hasNext()){
//								maxScore = entries2.next().getValue().getBelifScore();
//							}
//							if(entries2.hasNext())
//								secondScore = entries2.next().getValue().getBelifScore();
//						}
					}
					
				}
			}
			
		}while(pre_size < now_size);
		long end = System.currentTimeMillis();
		candidateSetMap = sortByMentionPos(candidateSetMap, false);
		System.out.println("Ranking finish!" + " Ranking time:"+ (double)(end-start)/1000);
		logger.info("Ranking time:"+ (double)(end-start)/1000);
		
	}
	
	public static <K, V extends Comparable<? super V>> HashMap<String, Candidate> sortByBeliefScore(HashMap<String, Candidate> map , final boolean reverse){
        List<Map.Entry<String, Candidate>> list =new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<String, Candidate>>()
        {
			@Override
			public int compare(Entry<String, Candidate> o1,
					Entry<String, Candidate> o2) {
				if (reverse){
					if(o1.getValue().getBelifScore() < o2.getValue().getBelifScore()) return 1;
					else if(o1.getValue().getBelifScore() > o2.getValue().getBelifScore()) return -1;
					else return 0;
				}
				if(o1.getValue().getBelifScore() < o2.getValue().getBelifScore()) return -1;
				else if(o1.getValue().getBelifScore() > o2.getValue().getBelifScore()) return 1;
				else return 0;
			}
        } );
        HashMap<String, Candidate> result = new LinkedHashMap<>();
        for (Entry<String, Candidate> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 
	
	public static <K, V extends Comparable<? super V>> HashMap<Mention,CandidateSet> sortByMentionPos(HashMap<Mention,CandidateSet> map , final boolean reverse){
        List<Map.Entry<Mention,CandidateSet>> list =new LinkedList<Map.Entry<Mention,CandidateSet>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<Mention,CandidateSet>>()
        {
            @Override
            public int compare( Map.Entry<Mention,CandidateSet> o1, Map.Entry<Mention,CandidateSet> o2 )
            {
                if (reverse)
                    return o2.getKey().getPosition().compareTo(o1.getKey().getPosition());
                return o1.getKey().getPosition().compareTo(o2.getKey().getPosition());
            }
        } );
        HashMap<Mention,CandidateSet> result = new LinkedHashMap<Mention,CandidateSet>();
        for (Map.Entry<Mention,CandidateSet> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 
	
	public static <K, V extends Comparable<? super V>> HashMap<Mention,CandidateSet> sortByCSetSize(HashMap<Mention,CandidateSet> map , final boolean reverse){
        List<Map.Entry<Mention,CandidateSet>> list =new LinkedList<Map.Entry<Mention,CandidateSet>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<Mention,CandidateSet>>()
        {
            @Override
            public int compare( Map.Entry<Mention,CandidateSet> o1, Map.Entry<Mention,CandidateSet> o2 )
            {
                if (reverse)
                    return o2.getValue().getSet().size() - o1.getValue().getSet().size();
                return o1.getValue().getSet().size() - o2.getValue().getSet().size();
            }
        } );
        HashMap<Mention,CandidateSet> result = new LinkedHashMap<Mention,CandidateSet>();
        for (Map.Entry<Mention,CandidateSet> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 
	
	private static float[] getContextWordsVec(ArrayList<String> context){
		float[] result = new float[vecModel.getSize()];
		for(int i = 0; i < vecModel.getSize(); i ++) result[i] = 0;
		
		ArrayList<String> wordsWithVec = new ArrayList<String>();
		for(String s : context){
			if(!s.isEmpty() && vecModel.getWordMap().containsKey(s))
				wordsWithVec.add(s);
		}
		int rows = wordsWithVec.size();
		int cols = vecModel.getSize();
		
		if(rows == 0) return result;
		
		float[][] wordsVec = new float[rows][cols];
		for(int i = 0; i < rows; i ++){
			wordsVec[i] = vecModel.getWordVec(wordsWithVec.get(i));
		}
		for(int j = 0; j < cols; j ++){
			for(int i = 0; i < rows; i++){
				result[j] += wordsVec[i][j];
			}
			result[j] /= rows;			 
		}
		
		return result;
	}
	
	private static float[] getContextEntityVec(ArrayList<String> entityList){
		float[] result = new float[vecModel.getSize()];
		for(int i = 0; i < vecModel.getSize(); i ++) result[i] = 0;

		int rows = entityList.size();
		int cols = vecModel.getSize();
		
		if(rows == 0) return result;
		
		float[][] entitiesVec = new float[rows][cols];
		for(int i = 0; i < rows; i ++){
			entitiesVec[i] = vecModel.getEntityVec(entityList.get(i));
		}
		for(int j = 0; j < cols; j ++){
			for(int i = 0; i < rows; i++){
				result[j] += entitiesVec[i][j];
			}
			result[j] /= rows;			 
		}
		
		return result;
	}
	private static double cosineSimilarity(float[] a, float[] b){
		float dist = 0;
		float modulo_a = 0;
		float modulo_b = 0;
		for (int i = 0; i < a.length; i ++){
			dist += a[i]*b[i];
			modulo_a += a[i]*a[i];
			modulo_b += b[i]*b[i];
		}
		return dist/java.lang.Math.sqrt(modulo_a*modulo_b);
	}
	
	private static double calSimOfContextWords(String entity_id, ArrayList<String> context){
		float[] entity_vec = vecModel.getEntityVec(entity_id);
		return cosineSimilarity(entity_vec, getContextWordsVec(context));
	}
	
	private static double calSimOfContextEntities(String entity_id, ArrayList<String> context_entities){
		float[] entity_vec = vecModel.getEntityVec(entity_id);
		return cosineSimilarity(entity_vec, getContextEntityVec(context_entities));
	}
	
	private static double calBeliefScore(Candidate candidate, double link_prob){
		double score = 0;
		score += java.lang.Math.log(candidate.getContextEntitySim());
		score += java.lang.Math.log(candidate.getContextWordsSim());
		score += java.lang.Math.log(candidate.getPopularity());
		score += java.lang.Math.log(link_prob);
		return java.lang.Math.exp(score);
	}


	public static HashMap<Mention, CandidateSet> getCandidateSetMap() {
		return candidateSetMap;
	}

}
