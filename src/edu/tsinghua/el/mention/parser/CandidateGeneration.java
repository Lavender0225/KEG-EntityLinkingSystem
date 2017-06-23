package edu.tsinghua.el.mention.parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import edu.tsinghua.el.mention.filter.MentionFilter;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.Mention;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.tsinghua.el.common.Constant;
import edu.tsinghua.el.common.PropertiesReader;
import edu.tsinghua.el.index.IndexBuilder;

public class CandidateGeneration {
	
	private static HashMap<Mention,CandidateSet> candidateSetMap = new HashMap<Mention,CandidateSet>();
	public static final Logger logger = LogManager.getLogger();
	
	public HashMap<Mention, CandidateSet> getCandidateSetMap() {
		return candidateSetMap;
	}
	public static HashMap<Mention, CandidateSet> extractMentionForNewsFromString(String domainName, String doc, String language){
		MentionFilter handle = new MentionFilter(doc);
        //消歧后
    	try {
    		//logger.info(doc);
			candidateSetMap = handle.disambiguating(domainName, language);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return candidateSetMap;
		
	}
}
