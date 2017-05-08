package edu.tsinghua.el.mention.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import edu.tsinghua.api.xlore.XloreGetEntity;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.Entity;
import edu.tsinghua.el.model.Mention;
import edu.tsinghua.el.model.Position;
import edu.tsinghua.el.model.Score;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.tsinghua.el.common.Constant;
import edu.tsinghua.el.index.AhoCorasickDoubleArrayTrie;
import edu.tsinghua.el.index.IndexBuilder;

public class MentionFilter {
	private String doc = "";
	private Result ansj_result = null;
	private List<String> extractResult = new ArrayList<String>();
	private List<String> midResult = new ArrayList<String>();
	private HashMap<String, Integer> timeMap = new HashMap<String, Integer>();  //label   score
	private HashMap<String, String> stringMap = new HashMap<String, String>();   //label   label::=value
	
	private List<Position> positionList = new ArrayList<Position>();
	private List<Position> positionResultList = new ArrayList<Position>();
	
	
	private HashMap<Mention,CandidateSet> candidateSetMap = new HashMap<Mention,CandidateSet>();
	int count = 0;
	long total_query_time = 0;
	public static final Logger logger = LogManager.getLogger();
	
	public MentionFilter(String doc){
		this.doc = doc;
		this.ansj_result = NlpAnalysis.parse(doc);
		filterWp();
	}
	
	private Position selectByMaxLength(HashSet<Position> result)
	{
		int lengthMax = -1;
		Position choice = null;
		for(Position p : result)
		{
			int length =  p.end - p.begin;
			if (length>=lengthMax) {
				lengthMax=length;
				choice = p;
			}
		}
		return choice;
	}
	
	private void filterNumberAndVerb(List<String> str, String doc)  //去掉数字，日期，时间
	{
		midResult.clear();
		HashSet<String> verbList = getVerbOfDoc();
		logger.info("verb list:" + verbList);
		for(int i=0; i<str.size(); i++)
		{
			String[] strsplit = str.get(i).split("::=");
			Pattern pattern = Pattern.compile("[[0-9今昨明后前本上去]+[年|月|日|时|分|秒]*]+"); 
			if(!pattern.matcher(strsplit[0]).matches() && !verbList.contains(strsplit[0]) )//不是数字、日期且不是动词
			{
				//System.out.println(str.get(i));
				midResult.add(str.get(i));
				positionResultList.add(positionList.get(i));
			}
		}
		positionList.clear();

	}
	private void filterbyPosition_new(String doc){
		Collections.sort(positionResultList);
//		for(Position p : positionResultList){
//			logger.info(p);
//			//System.out.println(p);
//		}
		HashSet<Position> result = new HashSet<Position>();
		boolean overlapFlag = false;
		do{
			overlapFlag = false;
			int i = 0;
			while(i < positionResultList.size())
			{
				Position pos = positionResultList.get(i);
				logger.info( i + ": "+ pos + ", doc:" + doc.substring(pos.begin, pos.end));
				int j = i + 1;
				result.add(pos);
				//找到重叠的position并加入result
				while( j < positionResultList.size() && positionResultList.get(j).begin < pos.end){
					result.add(positionResultList.get(j));
					overlapFlag = true;
					j ++;
				}
				if(result.size() > 1){
					Position choice = null;
					Score max_score = new Score(-1,-1,-1);
					//找出重叠的position中得分最大的position
					for(Position p : result)
					{
						int freq = timeMap.get(doc.substring(p.begin, p.end));
						int token_score = extractResult.contains(doc.substring(p.begin, p.end)) ? 2 : 0;
						Score score = new Score(p.end - p.begin, token_score, freq);
						logger.info(p +", doc:" + doc.substring(p.begin, p.end)+", len:" + (p.end - p.begin));
						//System.out.println(p +", doc:" + doc.substring(p.begin, p.end)+", len:" + (p.end - p.begin) +", freq:" + freq + ", token:" + token_score + ", totel:" + score);
						if(score.compareTo(max_score) > 0){
							max_score = score;
							choice = p;
						}
					}
					logger.info("choice:" + choice + ", doc:" + doc.substring(choice.begin, choice.end));
					//去掉不留的
					j = i;
					while( j < positionResultList.size() && positionResultList.get(j).begin < pos.end){
						if(!positionResultList.get(j).equals(choice)){
							logger.info("remove:" + positionResultList.get(j) + ", doc:" + doc.substring(positionResultList.get(j).begin, positionResultList.get(j).end));
							positionResultList.remove(j);
						}
						j ++;
					}
				}
				result.clear();
				i ++;
			}
		}while(overlapFlag);
		
		//将剩余在positionResultList中的插入mentionList
		for(Position p : positionResultList){
			logger.info("insert mention:" + p + ", doc:" + doc.substring(p.begin, p.end));
			//System.out.println(p + ", doc:" + doc.substring(p.begin, p.end));
			insertMention(p.begin, p.end, stringMap.get(doc.substring(p.begin, p.end)));
		}
		
		
	}
	
	public static <K, V extends Comparable<? super V>> HashMap<K, V> sortByValue(HashMap<K, V> map , final boolean reverse){
        List<Map.Entry<K, V>> list =new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                if (reverse)
                    return - (o1.getValue()).compareTo(o2.getValue());
                return (o1.getValue()).compareTo(o2.getValue());
            }
        } );
        HashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 
	public HashMap<Mention,CandidateSet> disambiguating(String domainNameList) throws IOException
	{
		//System.out.println(NlpAnalysis.parse("奥巴马"));
		String extract = NlpAnalysis.parse(doc).toStringWithOutNature("&&");
		String[] extractList = extract.split("&&");
		for(String tmp: extractList)
		{
			extractResult.add(tmp);
			//System.out.println("token:" + tmp);
		}
    	List<String> str = new ArrayList<String>();
    	
    	long start = System.currentTimeMillis();
    	List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> wordList = IndexBuilder.parseTextFromMultiIndex(domainNameList, doc);
    	long end = System.currentTimeMillis(); 
        logger.info("Parsing doc finish! Time:" + (float)(end - start)/1000);
    	//System.out.println("Parsing doc finish! Time:" + (float)(end - start)/1000);
    	//logger.info(candidateSetMap.toString());
    	
    	for(AhoCorasickDoubleArrayTrie<String>.Hit<String> tmp_hit:wordList){
    		String label = doc.substring(tmp_hit.begin, tmp_hit.end);
    		/***************** This is the part of getting context info of a mention *********************/
    		String prev_context = null;
    		String after_context = null;
    		int prev_context_start = tmp_hit.begin - Constant.mention_context_window;
    		int after_context_end = tmp_hit.end + Constant.mention_context_window;
    		if(prev_context_start > -1){
    			prev_context = doc.substring(prev_context_start, tmp_hit.begin);
    		}
    		else{
    			prev_context = doc.substring(0, tmp_hit.begin);
    		}
    		if(after_context_end < doc.length()){
    			after_context = doc.substring(tmp_hit.end, after_context_end);
    		}
    		else{
    			after_context = doc.substring(tmp_hit.end, doc.length());
    		}
    		
    		/******************************** end ******************************************/
    		String text = label + "::=" + tmp_hit.value + ":::" + prev_context + ":::" + after_context;
    		logger.info(label +"[" + tmp_hit.begin + ", " + tmp_hit.end +  "]::=" + tmp_hit.value);
        	str.add(text);
        	stringMap.put(label, text);

        	if(timeMap.containsKey(doc.substring(tmp_hit.begin, tmp_hit.end)))
        	{
        		timeMap.put(doc.substring(tmp_hit.begin, tmp_hit.end), timeMap.get(doc.substring(tmp_hit.begin, tmp_hit.end))+1);
        	}
        	else 
        	{
        		timeMap.put(doc.substring(tmp_hit.begin, tmp_hit.end), 1);
        	}
        	positionList.add(new Position(tmp_hit.begin, tmp_hit.end));
        }
    	
    	/*for (Map.Entry<String, Integer> entry : timeMap.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			System.out.println(key + " " + value);
    	}*/
    	//FileManipulator.outputStringList(str, System.getProperty("user.dir") + news_path+"_original.txt");

    	filterNumberAndVerb(str, doc);
    	filterbyPosition_new(doc);
		
		//FileManipulator.outputStringList(midResult, System.getProperty("user.dir") + news_path+"_filter.txt");
		if(count != 0){
			logger.info("Query total time:" + (float)(total_query_time)/1000 + "s, #query times:" + count + ", average:" + (float)(total_query_time/count)/1000 + "s");
			//System.out.println("Query total time:" + (float)(total_query_time)/1000 + "s, #query times:" + count + ", average:" + (float)(total_query_time/count)/1000 + "s");
		}
		return candidateSetMap;
	}
	
	private void insertMention(int begin, int end, String item)
	{
		String[] tmp = item.split(":::", 3);
		String label = tmp[0].split("::=", 2)[0];
		String value = tmp[0].split("::=", 2)[1];
		String prev_context = tmp[1];
		String after_context = tmp[2];

    	long start1 = 0;
    	long end1 = 0;
    	//logger.info(item);
    	midResult.add(item);
    	Mention mention = new Mention();
    	mention.setLabel(label);
    	mention.setPosition(begin, end);
    	//mention.setPrev_context(prev_context);
    	//mention.setAfter_context(after_context);
    	mention.setContext_words(getNounOfString(prev_context + after_context));
    	CandidateSet cs = new CandidateSet();
    	String[] tmp_c = value.split("::=");
    	for(String ss : tmp_c){
    		String[] tmp_uri = ss.split("::;");
    		String id = tmp_uri[0];
    		// get entity details from XLore API
    		start1 = System.currentTimeMillis();
    		Entity tmp_e = XloreGetEntity.getEntityDetailByID(id);
    		if(tmp_e != null){
    			end1 = System.currentTimeMillis();
        		total_query_time += end1 - start1;
        		count += 1;
        		if(tmp_uri.length > 1){
        			tmp_e.setDesc(tmp_uri[1]);
        		}
    			cs.addElement(id, tmp_e);
    		}
    		
    		
    	}
    	candidateSetMap.put(mention, cs);
	}

	private ArrayList<String> getNounOfString(String text){
		Result text_ansj = NlpAnalysis.parse(text);
		ArrayList<String> noun_list = new ArrayList<String>();
		for(Term item : text_ansj){
			if(item.getNatureStr().contains("n") && !item.getName().isEmpty()){
				noun_list.add(item.getName());
			}
		}
		//System.out.println(text_ansj.toString());
		//System.out.println(noun_list);
		return noun_list;
	}
	
	private HashSet<String> getVerbOfDoc(){
		HashSet<String> verb_list = new HashSet<String>();
		for(Term item : ansj_result){
			if(item.getNatureStr().contentEquals("v")){
				verb_list.add(item.getName());
			}
		}
		//System.out.println(text_ansj.toString());
		//System.out.println(verb_list);
		return verb_list;
	}
	
	private HashSet<String> getAdjectiveOfDoc(){
		HashSet<String> adj_list = new HashSet<String>();
		for(Term item : ansj_result){
			if(item.getNatureStr().contentEquals("a") && !item.getName().isEmpty()){
				adj_list.add(item.getName());
			}
		}
		//System.out.println(text_ansj.toString());
		//System.out.println(verb_list);
		return adj_list;
	}
	
	public void filterWp(){
		doc = doc.replaceAll(Constant.wpRegex, " ");
	}
	
	public String getDoc() {
		return doc;
	}

	public void setDoc(String doc) {
		this.doc = doc;
	}

	public static void main(String[] args){
		String doc = "本月早些时候，外交部长王毅应约同美国国务卿克里通电话。王毅表示，中美元首即将在杭州举行的会晤是下阶段中美关系的最重要日程。克里表示，美方愿同中方合作，确保Ｇ２０杭州峰会取得圆满成功。——《》}}}}}}"
				+ ""
				+ ""
				+ "&*3#，？：";
		MentionFilter ms = new MentionFilter(doc);
		ms.getVerbOfDoc();
		ms.getVerbOfDoc();
		ms.filterWp();
		//System.out.println(ms.getDoc());
	}
}
