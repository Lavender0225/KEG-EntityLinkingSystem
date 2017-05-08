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
	
	/**
	 * 抽取一篇文章的Mentions，并存储对应的candidateSet
	 * 
	 * @param news_path, news的存储路径
	 */
	
	@SuppressWarnings("resource")
	public static HashMap<Mention, CandidateSet> extractMentionForNewsFromFile(String domainName, String news_path){
    	BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + news_path));
			String line = null;
	    	StringBuffer sb = new StringBuffer();
	    	while((line = reader.readLine())!=null){
	    		sb.append(line).append("\n");
	    	}
	    	String doc = sb.toString().toLowerCase();
	    	
	    	MentionFilter handle = new MentionFilter(doc);
	        //消歧后
	    	candidateSetMap = handle.disambiguating(domainName);
	    	
	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return candidateSetMap;
	}
	public HashMap<Mention, CandidateSet> getCandidateSetMap() {
		return candidateSetMap;
	}
	public static HashMap<Mention, CandidateSet> extractMentionForNewsFromString(String domainName, String doc){
		doc = doc.toLowerCase();
		MentionFilter handle = new MentionFilter(doc);
        //消歧后
    	try {
    		//logger.info(doc);
			candidateSetMap = handle.disambiguating(domainName);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return candidateSetMap;
		
	}
}
