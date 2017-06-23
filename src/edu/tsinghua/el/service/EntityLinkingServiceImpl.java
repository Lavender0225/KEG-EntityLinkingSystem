package edu.tsinghua.el.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import baike.entity.dao.WikiURLMap;
import edu.tsinghua.el.model.Candidate;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.FormData;
import edu.tsinghua.el.model.Mention;
import edu.tsinghua.el.candidate.ranking.RepresentationRanking;
import edu.tsinghua.el.candidate.ranking.RepresentationRankingV2;
import edu.tsinghua.el.candidate.ranking.TraditionalRanking;
import edu.tsinghua.el.common.Constant;
import edu.tsinghua.el.model.LinkingResult;

public class EntityLinkingServiceImpl implements EntityLinkingService{

	@Override
	public ArrayList<LinkingResult> linking(String text, String index_choose) {
		ArrayList<LinkingResult> resultList = new ArrayList<LinkingResult> ();
		ArrayList<String> domainNameList = new ArrayList<String>();
		for(String s :index_choose.split(",")){
			if(!s.isEmpty())
				domainNameList.add(s);
		}
		String language = "zh";
		if(domainNameList.size() == 1)
			language = domainNameList.get(0);
		RepresentationRankingV2 ranker= new RepresentationRankingV2();
		ranker.processing(index_choose, text, language);
		HashMap<Mention,CandidateSet> candidateSetMap = ranker.getCandidateSetMap();
		Iterator<Entry<Mention,CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		
        while (entries.hasNext()) {  
        	Map.Entry<Mention,CandidateSet> entry =  entries.next();  
            Mention mention = entry.getKey();  
            if(mention.getResult_entity_id() != null){
            	Candidate candidate = entry.getValue().getSet().get(mention.getResult_entity_id());
            	LinkingResult result = new LinkingResult();
                result.setLabel(mention.getLabel());
                result.setStart_index(mention.getPosition().begin);
                result.setEnd_index(mention.getPosition().end);
            	result.setEntity_id(mention.getResult_entity_id());
                result.setCoherence_score(candidate.getContextEntitySim());
                result.setContextSim(candidate.getContextWordsSim());
                result.setBeliefScore(candidate.getBelifScore());
                result.setPopularity_score(candidate.getPopularity());
                //result.setRelatedness_score(candidate.getReletedness());
                if(language.contentEquals("zh"))
                	result.setUrl(Constant.baiduEntityPrefix + mention.getResult_entity_id());
                if(language.contentEquals("en"))
                	result.setUrl(WikiURLMap.getURL(mention.getResult_entity_id()));
                result.setLink_prob(mention.getLink_prob());
                resultList.add(result);
            }
        }
        //Collections.sort(resultList);
		return resultList;
	}
	
}
