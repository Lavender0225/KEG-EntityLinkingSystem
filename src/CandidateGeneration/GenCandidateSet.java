package CandidateGeneration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Common.Constant;
import EntityIndex.IndexBuilder;
import MentionFilter.MentionDisambiguation;
import Model.CandidateSet;
import Model.Mention;

public class GenCandidateSet {
	private static IndexBuilder ibd;
	
	private static HashMap<Mention,CandidateSet> candidateSetMap = new HashMap<Mention,CandidateSet>();
	public static final Logger logger = LogManager.getLogger();
	
	/**
	 * 抽取一篇文章的Mentions，并存储对应的candidateSet
	 * 
	 * @param news_path, news的存储路径
	 */
	private static void chooseIndex(){
		ibd = IndexBuilder.getInstance(Constant.entity_ready_file, Constant.entity_trie);
	}
	
	@SuppressWarnings("resource")
	public static HashMap<Mention, CandidateSet> extractMentionForNewsFromFile(String news_path){
    	BufferedReader reader = null;
    	chooseIndex();
		try {
			reader = new BufferedReader(new FileReader(System.getProperty("user.dir") + news_path));
			String line = null;
	    	StringBuffer sb = new StringBuffer();
	    	while((line = reader.readLine())!=null){
	    		sb.append(line).append("\n");
	    	}
	    	String doc = sb.toString().toLowerCase();
	    	
	    	MentionDisambiguation handle = new MentionDisambiguation();
	        //消歧后
	    	candidateSetMap = handle.disambiguating(ibd, doc, news_path);
	    	
	    	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return candidateSetMap;
	}
	public HashMap<Mention, CandidateSet> getCandidateSetMap() {
		return candidateSetMap;
	}
	public static HashMap<Mention, CandidateSet> extractMentionForNewsFromString(String doc){
		chooseIndex();
		doc = doc.toLowerCase();
		MentionDisambiguation handle = new MentionDisambiguation();
        //消歧后
    	try {
    		logger.info(doc);
			candidateSetMap = handle.disambiguating(ibd, doc, "/etc/news/streaming_news.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return candidateSetMap;
		
	}
	public static void main(String[] args) {
		//System.out.println(System.getProperty("user.dir"));
		GenCandidateSet.extractMentionForNewsFromFile(Constant.news_path);
	}
	

}
