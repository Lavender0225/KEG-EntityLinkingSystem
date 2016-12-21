package edu.tsinghua.el.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import edu.tsinghua.el.model.Candidate;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.Mention;
import edu.tsinghua.el.candidate.ranking.TraditionalRanking;
import edu.tsinghua.el.common.Constant;
import edu.tsinghua.el.model.LinkingResult;

public class EntityLinkingServiceImpl implements EntityLinkingService{

	@Override
	public ArrayList<LinkingResult> linking(String text) {
		ArrayList<LinkingResult> resultList = new ArrayList<LinkingResult> ();
		TraditionalRanking.processing(text);
		HashMap<Mention,CandidateSet> candidateSetMap = TraditionalRanking.getCandidateSetMap();
		Iterator<Entry<Mention,CandidateSet>> entries = candidateSetMap.entrySet().iterator();
		
        while (entries.hasNext()) {  
        	Map.Entry<Mention,CandidateSet> entry =  entries.next();  
            Mention mention = entry.getKey();  
            if(mention.getResult_entity_id() != null){
            	Candidate candidate = entry.getValue().getSet().get(mention.getResult_entity_id());
            	LinkingResult result = new LinkingResult();
                result.setLabel(mention.getLabel());
                result.setStart_index(mention.getPos_start());
                result.setEnd_index(mention.getPos_end());
                result.setEntity_id(mention.getResult_entity_id());
                result.setCohenrence_score(candidate.getCohenrence_score());
                result.setPopularity_score(candidate.getPopularity());
                result.setUrl(Constant.xlore_entity_prefix + mention.getResult_entity_id());
                resultList.add(result);
            }
            
        }
		return resultList;
	}
	
}
