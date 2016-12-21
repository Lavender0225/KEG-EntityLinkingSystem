package edu.tsinghua.el.mention.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import edu.tsinghua.api.xlore.XloreGetEntity;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.Entity;
import edu.tsinghua.el.model.Mention;

import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



import edu.tsinghua.el.common.Constant;
import edu.tsinghua.el.common.FileManipulator;
import edu.tsinghua.el.index.AhoCorasickDoubleArrayTrie;
import edu.tsinghua.el.index.IndexBuilder;

public class MentionFilter {
	private List<String> extractResult = new ArrayList<String>();
	private List<String> midResult = new ArrayList<String>();
	private HashMap<String, Integer> timeMap = new HashMap<String, Integer>();  //label   score
	private HashMap<String, String> stringMap = new HashMap<String, String>();   //label   label::=value
	
	private List<Integer> PositionStart = new ArrayList<Integer>();
	private List<Integer> PositionEnd = new ArrayList<Integer>();
	private List<Integer> Ps_result = new ArrayList<Integer>();
	private List<Integer> Pe_result = new ArrayList<Integer>();
	
	private HashMap<Mention,CandidateSet> candidateSetMap = new HashMap<Mention,CandidateSet>();
	int count = 0;
	long total_query_time = 0;
	public static final Logger logger = LogManager.getLogger();
	
	private int selectByMaxLength(List<String> result)
	{
		int choice = 0,lengthMax = -1;
		for(int k=0; k<result.size(); k++)
		{
			String[] strsplit = result.get(k).split(";");
			int length = Integer.parseInt(strsplit[1])-Integer.parseInt(strsplit[0]);
			if (length>=lengthMax) {
				lengthMax=length;
				choice = k;
			}
		}
		return choice;
	}
	
	private void filterNumberAndVerb(List<String> str, String doc)  //去掉数字，日期，时间
	{
		midResult.clear();
		HashSet<String> verbList = getVerbOfString(doc);
		logger.info("verb list:" + verbList);
		for(int i=0; i<str.size(); i++)
		{
			String[] strsplit = str.get(i).split("::=");
			Pattern pattern = Pattern.compile("[[0-9]+[年|月|日|时|分|秒]*]+"); 
			if(!pattern.matcher(strsplit[0]).matches() && !verbList.contains(strsplit[0]) )//不是数字且不是动词
			{
				System.out.println(str.get(i));
				midResult.add(str.get(i));
				Ps_result.add(PositionStart.get(i));
				Pe_result.add(PositionEnd.get(i));
			}
		}

	}
	
	
	
	private void filterbyPosition(IndexBuilder ibd, String doc) 
	{
		midResult.clear();
		List<String> result = new ArrayList<String>();
		int endMax ;
		for (int i=0; i<Ps_result.size(); i++) 
		{
			if (result.contains(Ps_result.get(i)+";"+Pe_result.get(i))) 
			{
				continue;
			}
			/*for(String tmp : result)
			{
				System.out.println(tmp);
			}*/
			result.clear();
			endMax = Pe_result.get(i);
			result.add(Ps_result.get(i)+";"+Pe_result.get(i));
			//查找重叠的mention
			for (int j = i+1; j < i+20&&j<Ps_result.size(); j++) 
			{//向下最多找19次
				if (Ps_result.get(j)<endMax) 
				{
					result.add(Ps_result.get(j)+";"+Pe_result.get(j));
					if (Pe_result.get(j)>endMax) 
					{
						endMax = Pe_result.get(j);
					}
				}
			}
			
			//不应该找最长的，有特殊情况，应该找end值最大的
			for (int j = i+1; j < i+20&&j<Ps_result.size(); j++) 
			{//向下找19次
				if (Ps_result.get(j)<endMax) {
					if (result.contains(Ps_result.get(j)+";"+Pe_result.get(j))) 
					{
						continue;
					}
					result.add(Ps_result.get(j)+";"+Pe_result.get(j));
				}
			}
			List<Integer> scoreList = new ArrayList<Integer>();
			for(int s=0; s<result.size(); s++)
	        {
				scoreList.add(0);
	        }
			//选择最长的
			int choice = 0;
			choice = selectByMaxLength(result);
			String[] split = result.get(choice).split(";");
			int length = Integer.parseInt(split[1])-Integer.parseInt(split[0]);
			scoreList.set(choice, scoreList.get(choice)+length);  //+长度
			//System.out.println(choice+" "+scoreList.get(choice));

			for(int m=0; m<result.size(); m++)
			{
				String[] strsplit = result.get(m).split(";");
				
				int freq = timeMap.get(doc.substring(Integer.parseInt(strsplit[0]), Integer.parseInt(strsplit[1])));
				scoreList.set(m, scoreList.get(m)+freq);  //+出现次数
				
				if(extractResult.contains(doc.substring(Integer.parseInt(strsplit[0]), Integer.parseInt(strsplit[1]))))
				{
					scoreList.set(m, scoreList.get(m)+2);  //分词结果中含有则+2
				}
				//System.out.println(m+" "+scoreList.get(m));
			}		
			
			int max = 0;
			for(int m=0; m<result.size(); m++)
	        {
				if(max < scoreList.get(m))
				{
					max = scoreList.get(m);
					choice = m;
				}
	        }
			String[] strsplit = result.get(choice).split(";");
			String value = stringMap.get(doc.substring(Integer.parseInt(strsplit[0]), Integer.parseInt(strsplit[1])));
			//System.out.println(value);
			if(scoreList.get(choice) > 1){
				insertMention(Integer.parseInt(strsplit[0]), Integer.parseInt(strsplit[1]), value);
			}
		}
	}
	
	public HashMap<Mention,CandidateSet> disambiguating(IndexBuilder ibd, String doc) throws IOException
	{
		//System.out.println(NlpAnalysis.parse("奥巴马"));
		String extract = NlpAnalysis.parse(doc).toStringWithOutNature("&&");
		String[] extractList = extract.split("&&");
		for(String tmp: extractList)
		{
			extractResult.add(tmp);
			//System.out.println(tmp);
		}
    	List<String> str = new ArrayList<String>();
    	
    	long start = System.currentTimeMillis();
    	List<AhoCorasickDoubleArrayTrie<String>.Hit<String>> wordList = ibd.parseText(doc);
    	long end = System.currentTimeMillis(); 
        logger.info("Parsing doc finish! Time:" + (float)(end - start)/1000);
    	System.out.println("Parsing doc finish! Time:" + (float)(end - start)/1000);
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
        	PositionStart.add(tmp_hit.begin);
        	PositionEnd.add(tmp_hit.end);
        }
    	/*for (Map.Entry<String, Integer> entry : timeMap.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			System.out.println(key + " " + value);
    	}*/
    	//FileManipulator.outputStringList(str, System.getProperty("user.dir") + news_path+"_original.txt");

    	filterNumberAndVerb(str, doc);
		filterbyPosition(ibd, doc);
		
		//FileManipulator.outputStringList(midResult, System.getProperty("user.dir") + news_path+"_filter.txt");
		if(count != 0){
			logger.info("Query total time:" + (float)(total_query_time)/1000 + "s, #query times:" + count + ", average:" + (float)(total_query_time/count)/1000 + "s");
			System.out.println("Query total time:" + (float)(total_query_time)/1000 + "s, #query times:" + count + ", average:" + (float)(total_query_time/count)/1000 + "s");
		}
		
		//logger.info(candidateSetMap);
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
    	logger.info(item);
    	midResult.add(item);
    	Mention mention = new Mention();
    	mention.setLabel(label);
    	mention.setPos_start(begin);
    	mention.setPos_end(end);
    	mention.setPrev_context(prev_context);
    	mention.setAfter_context(after_context);
    	mention.setContext_entity(getNounOfString(prev_context + after_context));
    	CandidateSet cs = new CandidateSet();
    	String[] tmp_c = value.split("::=");
    	for(String ss : tmp_c){
    		String[] tmp_uri = ss.split("::;");
    		String id = tmp_uri[0];
    		// get entity details from XLore API
    		start1 = System.currentTimeMillis();
    		Entity tmp_e = XloreGetEntity.getEntityDetailByID(id);
    		end1 = System.currentTimeMillis();
    		total_query_time += end1 - start1;
    		count += 1;
    		if(tmp_uri.length > 1){
    			tmp_e.setDesc(tmp_uri[1]);
    		}
			cs.addElement(id, tmp_e);
    	}
    	candidateSetMap.put(mention, cs);
	}

	private HashSet<String> getNounOfString(String text){
		Result text_ansj = NlpAnalysis.parse(text);
		HashSet<String> noun_list = new HashSet<String>();
		for(Term item : text_ansj){
			if(item.getNatureStr().contains("n")){
				noun_list.add(item.getName());
			}
		}
		//System.out.println(text_ansj.toString());
		//System.out.println(noun_list);
		return noun_list;
	}
	
	private HashSet<String> getVerbOfString(String text){
		Result text_ansj = NlpAnalysis.parse(text);
		HashSet<String> verb_list = new HashSet<String>();
		for(Term item : text_ansj){
			if(item.getNatureStr().contentEquals("v")){
				verb_list.add(item.getName());
			}
		}
		//System.out.println(text_ansj.toString());
		//System.out.println(verb_list);
		return verb_list;
	}
	
	private HashSet<String> getAdjectiveOfString(String text){
		Result text_ansj = NlpAnalysis.parse(text);
		HashSet<String> adj_list = new HashSet<String>();
		for(Term item : text_ansj){
			if(item.getNatureStr().contentEquals("a")){
				adj_list.add(item.getName());
			}
		}
		//System.out.println(text_ansj.toString());
		//System.out.println(verb_list);
		return adj_list;
	}
	
	public static void main(String[] args){
		MentionFilter ms = new MentionFilter();
		ms.getVerbOfString("习近平对美国进行国事访问");
		ms.getVerbOfString("本月早些时候，外交部长王毅应约同美国国务卿克里通电话。王毅表示，中美元首即将在杭州举行的会晤是下阶段中美关系的最重要日程。克里表示，美方愿同中方合作，确保Ｇ２０杭州峰会取得圆满成功。");
	}
}
