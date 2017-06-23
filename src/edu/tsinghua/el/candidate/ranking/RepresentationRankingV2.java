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

import baike.entity.dao.BaikeProbManager;
import baike.entity.dao.ProbHolder;
import edu.tsinghua.el.mention.parser.CandidateGeneration;
import edu.tsinghua.el.model.Candidate;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.Mention;
import edu.tsinghua.el.model.vec.BaiduWikiModels;
import edu.tsinghua.el.model.vec.VecModel;

public class RepresentationRankingV2 {
	private HashMap<Mention,CandidateSet> candidateSetMap = null;
	private VecModel vecModel = null;
	private ProbHolder probModel = null;
	private double beliefScoreSum = 0;
	private Logger logger = LogManager.getLogger(RepresentationRankingV2.class);
	double a = 0.05;
	double b = 0.1;
	
	private int candidateAllCount = 0;
	
	public  void processing(String domainNameList, String doc, String language){
		if(language.contentEquals("zh")){
			vecModel = BaiduWikiModels.getInstance().getBaiduModel();
			probModel = BaikeProbManager.getInstance().getBaiduProbs();
			
			logger.info("choose baidu prob model, test:link prob of \"李克强\" is:" + probModel.getLinkProb("李克强") + ", size:" + probModel.getLinkProbSize());
			logger.info("popularity:" + probModel.getEntityPopularity("李克强", "/view/34021.htm") + ", size:" + probModel.getPopularitySize());
			logger.info("prior:" + probModel.getEntityPrior("/view/34021.htm") +  ", size:" + probModel.getPriorSize());
			logger.info("MGivenE:" + probModel.getMentionProbGivenEntity("李克强","/view/34021.htm") +  ", size:" + probModel.getMentionEntitySize());
			showDemo(probModel.getEntityPriorMap());
			showDemo(probModel.getLinkProbMap());
		}
		if(language.contentEquals("en")){
			vecModel = BaiduWikiModels.getInstance().getWikiModel();
			doc = doc.toLowerCase();
			probModel = BaikeProbManager.getInstance().getWikiProbs();
		}
		candidateSetMap = CandidateGeneration.extractMentionForNewsFromString(domainNameList, doc, language);
		//L2R order:sortByMentionPos()
		//S2R order:sortByCSetSize()
		candidateSetMap = sortByCSetSize(candidateSetMap, false); // size 1 -> size n
		String[] choose = {"prior", "context", "coherence"};
		ranking(choose);

		logger.info(candidateSetMap.toString());
	}
	
	public  void linkingWithParas(String[] choose, HashMap<Mention, CandidateSet> candidateMap, String language, double a, double b){
		this.a = a;
		this.b = b;
		if(language.contentEquals("zh")){
			vecModel = BaiduWikiModels.getInstance().getBaiduModel();
			probModel = BaikeProbManager.getInstance().getBaiduProbs();
		}
		if(language.contentEquals("en")){
			vecModel = BaiduWikiModels.getInstance().getWikiModel();
			probModel = BaikeProbManager.getInstance().getWikiProbs();
		}
		candidateSetMap = candidateMap;
		/**L2R order:sortByMentionPos()
			S2R order:sortByCSetSize()
			*/
		//logger.info("Before Ranking:" + candidateSetMap.toString());
		candidateSetMap = sortByCSetSize(candidateSetMap, false); // size 1 -> size n
		
		ranking(choose);
		candidateSetMap = sortByMentionPos(candidateSetMap, false);

		//logger.info(candidateSetMap.toString());
	}
	
	public void showDemo(HashMap<String, Double> map){
		int count = 0;
		for(String s : map.keySet()){
			if(count < 10)
				logger.info("key:" + s + ", value:" + map.get(s));
			else break;
			count ++;
			
		}
	}
	public void showDemo2(HashMap<String, HashMap<String, Double>> map){
		int count = 0;
		for(String s : map.keySet()){
				for(String s2: map.get(s).keySet()){
					if(count < 10)
						logger.info("key1:" + s + ", key2:" +s2 + ", value:"+ map.get(s));
					else break;
					count ++;
				}
			}
	}
	public  void ranking(String[] choose){
		long start = System.currentTimeMillis();
		ArrayList<String> unanbiguousEntitySet = new ArrayList<String>();
		int iter = 0;
		while(iter < 2){
			if(unanbiguousEntitySet.size() == 0){
				// find the unambiguous mentions at first
				Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
				while(entries.hasNext()){
					Entry<Mention, CandidateSet> entry = entries.next();
					Mention mention = entry.getKey();
					CandidateSet candidateSet = entry.getValue();
					Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
					mention.setLink_prob(probModel.getLinkProb(mention.getLabel()));	// set link prob P(m)
					if(entries2.hasNext()){
						Entry<String, Candidate> entry2 = entries2.next();
						String entity_id = entry2.getKey();
						Candidate candidate = entry2.getValue();
						candidate.setPopularity(probModel.getEntityPopularity(mention.getLabel(), entity_id)); // set P(e|m)
						//if the mention only have one candidate and the p(e|m) >= 0.95, then we think it's unambiguous
						if(candidate.getPopularity() >= 0.95 && vecModel.containsEntity(entity_id) && candidate != null){
							mention.setResult_entity_id(entity_id);
							candidate.setEntityPrior(probModel.getEntityPrior(entity_id));
							candidate.setMGivenEProb(probModel.getMentionProbGivenEntity(mention.getLabel(), entity_id));
							candidate.setContextWordsSim(calSimOfContextWords(entity_id, mention.getContext_words())); // get contextual cosine probability
							ArrayList<String> tmpSet = unanbiguousEntitySet;
							tmpSet.remove(entity_id);
							candidate.setContextEntitySim(calSimOfContextEntities(entity_id, tmpSet));	// get the coherence entity probability
							candidate.setNameSim(calSimOfNameAndEntity(mention.getLabel(), entity_id));
							candidate.setBelifScore(calBeliefScore(candidate));
							
							if(!unanbiguousEntitySet.contains(mention.getResult_entity_id())){
								unanbiguousEntitySet.add(mention.getResult_entity_id());
//								logger.info("unambiguous entity, mention:" + mention.getLabel() + ", [" + mention.getPosition().begin + "," + mention.getPosition().end + "]"
//										+ ", result_entity:" + entity_id);
							}
						}
						
					}
				}
			}
			
			Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
			while(entries.hasNext()){
				Entry<Mention, CandidateSet> entry = entries.next();
				Mention mention = entry.getKey();
				CandidateSet candidateSet = entry.getValue();
				Iterator<Entry<String, Candidate>> entries2 = candidateSet.getSet().entrySet().iterator();
					mention.setLink_prob(probModel.getLinkProb(mention.getLabel()));	// set link prob P(m)
					double maxScore = -1;
					while(entries2.hasNext()){
						Entry<String, Candidate> entry2 = entries2.next();
						String entity_id = entry2.getKey();
						Candidate candidate = entry2.getValue();
						if(vecModel.containsEntity(entity_id) && candidate != null){
							candidate.setEntityPrior(probModel.getEntityPrior(entity_id));
							candidate.setPopularity(probModel.getEntityPopularity(mention.getLabel(), entity_id)); // set P(e|m)
							candidate.setContextWordsSim(calSimOfContextWords(entity_id, mention.getContext_words())); // get contextual cosine probability
							ArrayList<String> tmpSet = unanbiguousEntitySet;
							tmpSet.remove(entity_id);
							candidate.setNameSim(calSimOfNameAndEntity(mention.getLabel(), entity_id));
							candidate.setContextEntitySim(calSimOfContextEntities(entity_id, tmpSet));	// get the coherence entity probability
							candidate.setMGivenEProb(probModel.getMentionProbGivenEntity(mention.getLabel(), entity_id));
							candidate.setBelifScore(calBeliefScore(candidate));
							if(candidate.getBelifScore() > maxScore){
								maxScore = candidate.getBelifScore();
								mention.setResult_entity_id(entity_id);
							}
						}
						else entries2.remove();
					}
					if(mention.getResult_entity_id() != null){
						if(!unanbiguousEntitySet.contains(mention.getResult_entity_id())){
							unanbiguousEntitySet.add(mention.getResult_entity_id());
//							logger.info("new unambiguous entity, mention:" + mention.getLabel() + ", [" + mention.getPosition().begin + "," + mention.getPosition().end + "]"
//									+ ", result_entity:" + mention.getResult_entity_id());
						}
					}
					candidateSet.setSet(sortByBeliefScore(candidateSet.getSet(), true));
//					if(candidateSet.getSet().size() > 1){
//						try{
//							candidateSet.setSet(sortByBeliefScore(candidateSet.getSet(), true));
//							entries2 = candidateSet.getSet().entrySet().iterator();
//							maxScore = 0;
//							double secondScore = 0;
//							String result_id = null;
//							Entry<String, Candidate> entry2;
//							if(entries2.hasNext()){
//								entry2 = entries2.next();
//								maxScore = entry2.getValue().getBelifScore();
//								result_id =  entry2.getKey();
//							}
//							if(entries2.hasNext())
//								secondScore = entries2.next().getValue().getBelifScore();
//							if(maxScore/secondScore  >= 1.2 ){
//								mention.setResult_entity_id(result_id);
//								unanbiguousEntitySet.add(result_id);
//								logger.info("unambiguous entity, mention:" + mention.getLabel() + ", [" + mention.getPosition().begin + "," + mention.getPosition().end + "]"
//										+ ", result_entity:" + result_id);
//							}else{
//								mention.setResult_entity_id(null);
//							}
//						}catch(Exception e){
//							logger.info(e.getMessage());
//							logger.info("error set:");
//							logger.info(candidateSet.getSet());
//						}
//						//logger.info(candidateSet.getSet());
//						
//					}
				}
//			logger.info("iter:" + iter);
//			logger.info(unanbiguousEntitySet);
			iter ++;
		}
		
		/**
		 * prune
		 */

		Iterator<Entry<Mention, CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		while(entries.hasNext()){
			Entry<Mention, CandidateSet> entry = entries.next();
			Mention mention = entry.getKey();
			CandidateSet candidateSet = entry.getValue();
			if(mention.getResult_entity_id() != null){
				candidateAllCount += candidateSet.getSet().size();
				if(candidateSet.getSet().containsKey(mention.getResult_entity_id())){
					double tmp = candidateSet.getSet().get(mention.getResult_entity_id()).getBelifScore();
					mention.setBelief_score((tmp + mention.getLink_prob())/2);
				}
			}
		}
		/**
		 *  prune
		 */
		
		//candidateSetMap = sortByMentionBeliefScore(candidateSetMap, false);
		
		//int deleteCount = (int) ((int) candidateSetMap.size() * b);
		int i = 0;
		entries = candidateSetMap.entrySet().iterator();
		while(entries.hasNext()){
			Entry<Mention, CandidateSet> entry = entries.next();
			Mention mention = entry.getKey();
			//CandidateSet canidateSet = entry.getValue();
			if(mention.getResult_entity_id() != null){
				
				if( mention.getLink_prob() < 0.02 )
					entries.remove();
			}
			else
				entries.remove();
			//i ++;
		}
		/***************/
		
		
		long end = System.currentTimeMillis();
		//candidateSetMap = sortByMentionPos(candidateSetMap, false);
		//System.out.println("Ranking finish!" + " Ranking time:"+ (double)(end-start)/1000);
		logger.info("Ranking time:"+ (double)(end-start)/1000);
		
	}
	
	
	public int getCandidateAllCount() {
		return candidateAllCount;
	}

	private HashMap<Mention, CandidateSet> sortByMentionBeliefScore( HashMap<Mention, CandidateSet> map, final boolean reverse) {
		List<Map.Entry<Mention, CandidateSet>> list =new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<Mention, CandidateSet>>()
        {
			@Override
			public int compare(Entry<Mention, CandidateSet> o1,
					Entry<Mention, CandidateSet> o2) {
				double k1 = o1.getKey().getBelief_score();
				double k2 = o2.getKey().getBelief_score();
				if (reverse){
					if(k1 < k2) return 1;
					else if(k1 > k2) return -1;
					return 0;
				}else{
					if(k1 < k2) return -1;
					if(k1 > k2) return 1;
					return 0;
				}
			}
        } );
        HashMap<Mention, CandidateSet> result = new LinkedHashMap<>();
        for (Entry<Mention, CandidateSet> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
	}

	public  <K, V extends Comparable<? super V>> HashMap<String, Candidate> sortByBeliefScore(HashMap<String, Candidate> map , final boolean reverse){
        List<Map.Entry<String, Candidate>> list =new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<String, Candidate>>()
        {
			@Override
			public int compare(Entry<String, Candidate> o1,
					Entry<String, Candidate> o2) {
				double k1 = o1.getValue().getBelifScore();
				double k2 = o2.getValue().getBelifScore();
				if (reverse){
					if(k1 < k2) return 1;
					else if(k1 > k2) return -1;
					return 0;
				}else{
					if(k1 < k2) return -1;
					if(k1 > k2) return 1;
					return 0;
				}
			}
        } );
        HashMap<String, Candidate> result = new LinkedHashMap<>();
        for (Entry<String, Candidate> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 
	
	public  <K, V extends Comparable<? super V>> HashMap<Mention,CandidateSet> sortByMentionPos(HashMap<Mention,CandidateSet> map , final boolean reverse){
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
	
	public  <K, V extends Comparable<? super V>> HashMap<Mention,CandidateSet> sortByCSetSize(HashMap<Mention,CandidateSet> map , final boolean reverse){
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
	
	private  float[] getContextWordsVec(ArrayList<String> context){
		if(context.size() == 0) return null;
		float[] result = new float[vecModel.getSize()];
		for(int i = 0; i < vecModel.getSize(); i ++) result[i] = 0;
		
		ArrayList<String> wordsWithVec = new ArrayList<String>();
		for(String s : context){
			if(!s.isEmpty() && vecModel.containsWord(s))
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
	
	private  float[] getContextEntityVec(ArrayList<String> entityList){
		if(entityList.size() == 0) return null;
		float[] result = new float[vecModel.getSize()];
		for(int i = 0; i < vecModel.getSize(); i ++) result[i] = 0;

		int rows = entityList.size();
		int cols = vecModel.getSize();
		
		
		float[][] entitiesVec = new float[rows][cols];
		for(int i = 0; i < rows; i ++){
			if(vecModel.containsEntity(entityList.get(i)))
				entitiesVec[i] = vecModel.getEntityVec(entityList.get(i));
			else{
				for(int j = 0; j < cols; j ++) entitiesVec[i][j] = 0;
			}
		}
		for(int j = 0; j < cols; j ++){
			for(int i = 0; i < rows; i++){
				result[j] += entitiesVec[i][j];
			}
			result[j] /= rows;			 
		}
		
		return result;
	}
	private  double cosineSimilarity(float[] a, float[] b){
		if(a == null || b == null)
			return 0.000001;
		float dist = 0;
		float modulo_a = 0;
		float modulo_b = 0;
		for (int i = 0; i < a.length; i ++){
			dist += a[i]*b[i];
			modulo_a += a[i]*a[i];
			modulo_b += b[i]*b[i];
		}
		return (1 + dist/java.lang.Math.sqrt(modulo_a*modulo_b))/2;
	}
	
	private  double calSimOfContextWords(String entity_id, ArrayList<String> context){
		float[] entity_vec = vecModel.getEntityVec(entity_id);
		return cosineSimilarity(entity_vec, getContextWordsVec(context));
	}
	
	private  double calSimOfContextEntities(String entity_id, ArrayList<String> context_entities){
		float[] entity_vec = vecModel.getEntityVec(entity_id);
		return cosineSimilarity(entity_vec, getContextEntityVec(context_entities));
	}
	
	private double calSimOfNameAndEntity(String name, String entity){
		float[] entity_vec = vecModel.getEntityVec(entity);
		float[] name_vec = null;
		if(vecModel.containsWord(name))
			name_vec = vecModel.getWordVec(name);
		else{
			name_vec = new float[vecModel.getSize()];
			String[] splitName = name.split("[, ]");
			float[][] name_matrix = new float[splitName.length][vecModel.getSize()];
			int count = 0;
			for(int i = 0; i < splitName.length; i ++){
				if(vecModel.containsWord(splitName[i])){
					name_matrix[i] = vecModel.getWordVec(splitName[i]);
					count ++;
				}
				else{
					//name_matrix[i] = vecModel.getWordVec("</s>");
					for(int j = 0; j < vecModel.getSize(); j ++)
						name_matrix[i][j] = 0;
				}
			}
			
			for(int j = 0; j < vecModel.getSize(); j ++){
				for(int i = 0; i < splitName.length; i++)
					name_vec[j] += name_matrix[i][j];
				name_vec[j] /=count;
			}
		}
		if(name_vec == null){
			name_vec = vecModel.getWordVec("</s>");
		}
		return cosineSimilarity(entity_vec, name_vec);
	}
	private  double calBeliefScore(Candidate candidate){
		double score = 1;
		score *= candidate.getContextEntitySim();
		score *= candidate.getContextWordsSim();
		//score *= candidate.getNameSim();
		score *= java.lang.Math.pow(candidate.getEntityPrior(), a);
		return score;
	}
	
	private double calBeliefScorePartly(String[] choose, Candidate candidate){
		double score = 1;
		for(String s : choose ){
			if(s.contentEquals("prior")){
				score *= java.lang.Math.pow(candidate.getEntityPrior(), a);
			}
			else if(s.contentEquals("context")){
				score *= candidate.getContextWordsSim();
			}else if(s.contentEquals("coherence")){
				score *= candidate.getContextEntitySim();
			}else if(s.contentEquals("name")){
				score *= candidate.getNameSim();
			}
		}
		return score;
		
	}


	public HashMap<Mention, CandidateSet> getCandidateSetMap() {
		return candidateSetMap;
	}

}
