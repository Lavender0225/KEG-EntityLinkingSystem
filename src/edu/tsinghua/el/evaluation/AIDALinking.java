package edu.tsinghua.el.evaluation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import baike.entity.dao.BaikeProbManager;
import baike.entity.dao.ProbHolder;
import baike.entity.dao.WikiURLMap;
import edu.tsinghua.el.candidate.ranking.RepresentationRankingV2;
import edu.tsinghua.el.common.PropertiesReader;
import edu.tsinghua.el.index.IndexBuilder;
import edu.tsinghua.el.mention.parser.CandidateGeneration;
import edu.tsinghua.el.model.BaikeEntity;
import edu.tsinghua.el.model.CandidateSet;
import edu.tsinghua.el.model.LinkingResult;
import edu.tsinghua.el.model.Mention;
import edu.tsinghua.el.model.vec.BaiduWikiModels;

public class AIDALinking {
	private String sentPath = "/home/zj/EntityLinkingWeb/data/evaluation/AIDA-sent.dat";
	private String anoDataPath = "/home/zj/EntityLinkingWeb/data/evaluation/AIDA-YAGO2-dataset.tsv";
	private String dictPath = "/home/zj/EntityLinkingWeb/data/wiki/dic/dictionary_wiki.dat";
	private String candidatePath = "/home/zj/EntityLinkingWeb/data/evaluation/candidate.json";
	private RepresentationRankingV2 ranker = new RepresentationRankingV2();
	
	private int mentionCount = 0;
	private int allMentionCount = 0;
	private int validMentionCount = 0;
	
	private ProbHolder probModel= BaikeProbManager.getInstance().getWikiProbs();
	
	private static HashSet<String> noMentionDoc = new HashSet<String>();
	private static Logger logger = LogManager.getLogger(RepresentationRankingV2.class);
	
	private HashMap<String, ArrayList<String>> dict = new HashMap<String, ArrayList<String>>();
	private HashSet<String> entitySet = new HashSet<String>();
	
	private HashMap<String,String[]> trainSet = new HashMap<String,String[]>();
	private HashMap<String, ArrayList<Mention>>  trainSetGroundTruth = new HashMap<String, ArrayList<Mention>> ();
	private HashMap<String, ArrayList<Mention>>  trainSetResult = new HashMap<String, ArrayList<Mention>> ();
	
	private HashMap<String,String[]> testA = new HashMap<String,String[]>();
	private HashMap<String, ArrayList<Mention>>  testAGroundTruth = new HashMap<String, ArrayList<Mention>> ();
	private HashMap<String, ArrayList<Mention>>  testAResult = new HashMap<String,ArrayList<Mention>> ();
	
	private HashMap<String,String[]> testB = new HashMap<String,String[]>();
	private HashMap<String, ArrayList<Mention>>  testBGroundTruth = new HashMap<String, ArrayList<Mention>> ();
	private HashMap<String, ArrayList<Mention>>  testBResult = new HashMap<String, ArrayList<Mention>> ();
	
	public void loadDict(){
		InputStream is = null;
		try {
		    is = new FileInputStream(dictPath);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] list = line.split("::=");
		        String mention = list[0];
		        dict.put(mention, new ArrayList<String>());
		        for(int i = 1; i < list.length; i ++){
		        	dict.get(mention).add(list[i]);
		        	if(!entitySet.contains(list[i]))
		        		entitySet.add(list[i]);
		        }
		    }
		    System.out.println("load finished, dict size:" + dict.size() + ", entitySet size:" + entitySet.size());
		}catch (IOException ioe){
		    ioe.printStackTrace();
		} finally {
		    try {
		        if (is != null) {
		            is.close();
		            is = null;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}  
	}
	
	public void loadDocs(){
		InputStream is = null;
		try {
		    is = new FileInputStream(sentPath);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] lineList = line.split("\\*\\*\\*\\*");
		        String title = lineList[0];
		        String[] doc = lineList[1].toLowerCase().split(" ");
		        String id = title.split("\\(")[1].split(" ")[0];
//		        if(id.contains("testa")){
//		        	testA.put(id, doc);
//		        }else if(id.contains("testb"))
//		        	testB.put(id, doc);
//		        else{
//		        	trainSet.put(id, doc);
//		        }
		        trainSet.put(id, doc);
		        
		    }
		    System.out.println("train doc size:" + trainSet.size());
		}catch (IOException ioe){
		    ioe.printStackTrace();
		} finally {
		    try {
		        if (is != null) {
		            is.close();
		            is = null;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}  
	}
	
	public HashMap<String, HashMap<Mention, CandidateSet>> loadCandidate(HashMap<String, String[]> corpus, HashMap<String, ArrayList<Mention>> truthMap){
		
		HashMap<String, HashMap<Mention, CandidateSet>> corpusCandidate = new HashMap<String, HashMap<Mention, CandidateSet>>();
		InputStream is = null;
		try {
		    is = new FileInputStream(candidatePath);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    String text = "";
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        text += line;
		    }
		    
		    JsonElement jelement = new JsonParser().parse(text);
		    JsonObject  jobject = jelement.getAsJsonObject();
		    for(int i = 1; i <= 1393; i++){
		    	String id = String.valueOf(i);
		    	
		    	if(jobject.has(id)){
		    		
		    		JsonObject docJson = jobject.getAsJsonObject(id);
		    		if(i > 946 && i < 1163){
			    		id += "testa";
			    	}else if(i > 1162){
			    		id += "testb";
			    	}
		    		
		    		if(truthMap.containsKey(id)){
		    			HashMap<Mention, CandidateSet> cMap = new HashMap<Mention, CandidateSet>();
		    			ArrayList<Mention> truthSet = truthMap.get(id);
			    		for(int j = 0; j < truthSet.size(); j ++){
			    			Mention result = truthSet.get(j);
			    			if(docJson.has(result.getLabel())){
			    			
				    			Mention mention = new Mention();
				    			mention.setLabel(result.getLabel());
				    			mention.setPosition(result.getPosition().begin, result.getPosition().end);
				    			
				    			int window = 25;
				    			ArrayList<String> context = new ArrayList<String>();
				    			if(!corpus.containsKey(id)){
				    				System.err.println("**************"+id);
				    				System.exit(-1);
				    			}
				    			String[] doc = corpus.get(id);
				    			for(int i1 = max(0,mention.getPosition().begin - window); i1 < min(doc.length, mention.getPosition().begin + window); i1 ++ ){
				    				context.add(doc[i1]);
				    			}
				    			mention.setContext_words(context);
				    			
				    			String result_url = docJson.getAsJsonObject(result.getLabel()).get("result_url").toString();
				    			result_url = result_url.substring(1,result_url.length()-1);
				    			
				    			if(!result_url.contentEquals("NIL")){
					    			mention.setTruth_id(result_url);
				//	    			logger.info("mention:"+ mention.getLabel() + "truth_url:" + result_url);
					    			cMap.put(mention, new CandidateSet());
				    			
				    				JsonArray candiadateJson = docJson.getAsJsonObject(result.getLabel()).getAsJsonArray("candidate");
	//			    				logger.info("candidate Array in Json:");
	//			    				logger.info(candiadateJson.toString());
				    				for(JsonElement entity : candiadateJson){
				    					String entity_url = entity.getAsJsonObject().get("url").toString();
				    					entity_url = entity_url.substring(1, entity_url.length()-1);
				    					String entity_id = WikiURLMap.getTitle(entity_url);
				    					if(entity_id != null)
				    						cMap.get(mention).addElement(entity_id, new BaikeEntity(entity_id));
	//			    					String entity_id= WikiIDMap.getId(WikiURLMap.getTitle(entity_url));
	//			    					logger.info("entity_url:" + entity_url + ", entity_title:" + WikiURLMap.getTitle(entity_url) +  ", entity_id:" + entity_id);
				    					
				    				}
				    				//cMap.get(mention).addElement("NIL", new BaikeEntity("NIL"));
				    				validMentionCount ++;
				    			}
			    			}
			    			else{
			    				logger.info(result.getLabel() + " not in candidate. doc id:" + id);
			    			}
			    			
			    		}
			    		if(cMap.size() == 0)
			    			logger.info("doc id:" + id + " have no mentions");
			    		
			    		corpusCandidate.put(id, cMap);
		    		}
		    		
		    		
		    		
//		    		else{
//		    			System.out.println("doc id:" + id + "not in truthMap");
//		    		}
		    	}
		    }
		}catch (IOException ioe){
		    ioe.printStackTrace();
		} finally {
		    try {
		        if (is != null) {
		            is.close();
		            is = null;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		return corpusCandidate;  
	}
	
	public void loadGroundTruth(){
		InputStream is = null;
		try {
		    is = new FileInputStream(anoDataPath);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    ArrayList<Mention> tmpResult = new ArrayList<Mention>();
		    String docID = "";
		    String type = "";
		    int count = 0;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        if(line.contains("DOCSTART")){
		        	count = 0;
		        	docID = line.split("\\(")[1].split(" ")[0];
		        	tmpResult = new ArrayList<Mention>();
//		        	if(line.contains("testa")){
//		        		testAGroundTruth.put(docID, tmpResult);
//		        		type = "testa";
//		        	}
//		        	else if(line.contains("testb")){
//		        		testBGroundTruth.put(docID, tmpResult);
//		        		type = "testb";
//		        	}else{
//		        		trainSetGroundTruth.put(docID, tmpResult);
//		        		type = "train";
//		        	}
		        	trainSetGroundTruth.put(docID, tmpResult);
		        }
		        else{
		        	String[] list = line.split("\t");
		        	if(list.length == 7){
		        		if(list[1].contentEquals("B")){
		        			String mention = list[2].toLowerCase();
		        			String wikiURL = list[4];
		        			Mention r = new Mention();
		        			r.setLabel(mention);
		        			r.setTruth_id(wikiURL);
		        			r.setPosition(count++, 0);
//		        			if(type.contentEquals("testa")){
//		        				testAGroundTruth.get(docID).add(r);
//				        	}
//		        			else if(type.contentEquals("testb")){
//		        				testBGroundTruth.get(docID).add(r);
//		        			}
//		        			else{
//		        				trainSetGroundTruth.get(docID).add(r);
//		        			}
		        			trainSetGroundTruth.get(docID).add(r);
		        			allMentionCount ++;
		        		}
		        	}
//		        	if(list.length == 4){
//		        		if(list[1].contentEquals("B")){
//		        			String mention = list[2].toLowerCase();;
//		        			LinkingResult r = new LinkingResult();
//		        			r.setLabel(mention);
//		        			r.setUrl(null);
//		        			r.setStart_index(count++);
//		        			r.setEnd_index(0);
//		        			if(type.contentEquals("testa")){
//		        				testAGroundTruth.get(docID).add(r);
//				        	}
//		        			else if(type.contentEquals("testb")){
//		        				testBGroundTruth.get(docID).add(r);
//		        			}
//		        			else{
//		        				trainSetGroundTruth.get(docID).add(r);
//		        			}
//		        			allMentionCount ++;
//		        		}
//		        	}
		        	
		        }
		        
		    }
		    System.out.println("ground truth size, A:" + testAGroundTruth.size() + ", B:" + testBGroundTruth.size() + ", train:" + trainSetGroundTruth.size());
		   // System.out.println("demo of testa truth:" + testAGroundTruth.get("1000testa"));
		}catch (IOException ioe){
		    ioe.printStackTrace();
		} finally {
		    try {
		        if (is != null) {
		            is.close();
		            is = null;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}  
	}
	public HashMap<String, HashMap<Mention, CandidateSet>> corpusCandidateGeneration(
			HashMap<String, String[]> corpus, HashMap<String, ArrayList<Mention>> trainSetGroundTruth2){
		
		HashMap<String, HashMap<Mention, CandidateSet>> result = new HashMap<String, HashMap<Mention, CandidateSet>>();
		String data = "";
		for(String id : corpus.keySet()){
			//result.put(id, candidateGeneration(trainSetGroundTruth2.get(id), corpus.get(id)));
			String text = "";
			for(String s: corpus.get(id))
				text += s + " ";
			long start = System.currentTimeMillis();
			HashMap<Mention, CandidateSet> candidates = CandidateGeneration.extractMentionForNewsFromString("en", text, "en");
			long end = System.currentTimeMillis();
			data += text.length() + "\t" + candidates.size() + "\t" + (end - start) + "\n";
			result.put(id, candidates);
		}
		try{
			File file = new  File("/home/zj/EntityLinkingWeb/data/evaluation/computation_evaluation_ner.txt");
			FileUtils.write(file, data);
		}catch(IOException e){
			logger.info(e.getMessage());
		}
		System.out.println("candidate generate done.");
		return result;
		
	}
	public HashMap<Mention, CandidateSet> candidateGeneration(ArrayList<Mention> resultSet, String[] doc){
		HashMap<Mention, CandidateSet> candidateMap = new HashMap<Mention, CandidateSet>();
		for(Mention result : resultSet){
			Mention mention = new Mention();
			mention.setLabel(result.getLabel());
			mention.setPosition(result.getPosition().begin, result.getPosition().end);
			
			int window = 10;
			ArrayList<String> context = new ArrayList<String>();
			for(int i = max(0,mention.getPosition().begin - window); i < min(doc.length, mention.getPosition().begin + window); i ++ ){
				context.add(doc[i]);
			}
			mention.setContext_words(context);
			mention.setTruth_id(result.getTruth_id());
			candidateMap.put(mention, new CandidateSet());
			if(dict.containsKey(result.getLabel())){
				ArrayList<String> entities = dict.get(result.getLabel());
				for(String entityId : entities){
					candidateMap.get(mention).addElement(entityId, new BaikeEntity(entityId));
				}
			}
			else{
				System.out.println(result.getLabel() + " not in dict.");
			}
		}
		return candidateMap;
	}
	private int min(int length, int end) {
		return length <= end? length : end;
	}

	private int max(int i, int begin) {
		return i >= begin ? i : begin;
	}
	
	private  HashMap<String, ArrayList<Mention>> linkingForACorpus( String[] choose,
			HashMap<String, String[]> corpus, HashMap<String, HashMap<Mention, CandidateSet>> corpusCandidateMap, double a, double bList) {
		String data = "";
		HashMap<String, ArrayList<Mention>> result = new HashMap<String, ArrayList<Mention>>();
		int lastCount = 0;
		for(String id : corpusCandidateMap.keySet()){
			HashMap<Mention, CandidateSet> candidateMap = corpusCandidateMap.get(id);
			logger.info("id:" + id);
			long start = System.currentTimeMillis();
			ranker.linkingWithParas(choose, candidateMap, "en", a, bList);
			long end = System.currentTimeMillis();
			candidateMap = ranker.getCandidateSetMap();
			if(candidateMap.size() == 0) {
				noMentionDoc.add(id);
			}else{
				data += ranker.getCandidateAllCount() - lastCount + "\t" + candidateMap.size() + "\t" + (end - start) + "\n";
				lastCount = ranker.getCandidateAllCount();
			}
			
			ArrayList<Mention> tmp = new ArrayList<Mention>();
			for(Mention m : candidateMap.keySet()){
				String url = WikiURLMap.getURL(m.getResult_entity_id());
				m.setResult_entity_id(url);
				tmp.add(m);
			}
			result.put(id, tmp);
			
		}
		try{
			File file = new  File("/home/zj/EntityLinkingWeb/data/evaluation/computation_evaluation.txt");
			FileUtils.write(file, data);
		}catch(IOException e){
			logger.info(e.getMessage());
		}
		
		return result;
	}
	
	public double calMicroP(HashMap<String, ArrayList<Mention>> result, String choose){
		int TPsum = 0;
		int FPsum = 0;
		
		for(String id : result.keySet()){
			int TP = 0;
			int FP = 0;
			
			
			if(choose.contentEquals("NIL")){
				for(Mention m : result.get(id)){
					if(m.getResult_entity_id() == null || m.getResult_entity_id().contentEquals("NIL")){
						if(m.getTruth_id().contentEquals("NIL")){
							TP += 1;
						}
						else{
							FP += 1;
							logger.info("id: " +id + "truth:" + m.getTruth_id() + ", result:" + m.getResult_entity_id());
						}
					}
				}
				
			}else{
				for(Mention m : result.get(id)){
					if(m.getResult_entity_id() != null &&  m.getResult_entity_id().contentEquals(m.getTruth_id())){
						TP += 1;
					}
					else{
						FP += 1;
						logger.info("id: " +id + "truth:" + m.getTruth_id() + ", result:" + m.getResult_entity_id());
					}
				}
			}
			
			TPsum += TP;
			FPsum += FP;
			logger.info("id:" + id + ", TP:" + TP + ", FP:" + FP + ",total:" + (TP + FP) + ", precision:" + (double)(TP)/(TP + FP));
		
		}
		return (double)TPsum / (double)(TPsum + FPsum);
	}
	
	public double calMacroP(HashMap<String, ArrayList<Mention>> result, String choose){
		double precisionSum = 0;
		int count = 0;
		if(choose.contentEquals("NIL")){
			for(String id : result.keySet()){
				int TP = 0;
				int FP = 0;
				for(Mention m : result.get(id)){
					if(m.getResult_entity_id() == null || m.getResult_entity_id().contentEquals("NIL")){
						if(m.getTruth_id().contentEquals("NIL")){
							TP += 1;
						}
						else{
							FP += 1;
						}
						precisionSum += (double)TP/(TP + FP);
						if(TP + FP != 0)
							count ++;
					}
					
				}
				//logger.info("id:" + id + ", precision:" + (double)TP/(TP + FP));
			}
		}
		else{
			for(String id : result.keySet()){
				int TP = 0;
				int FP = 0;
				for(Mention m : result.get(id)){
						if(m.getResult_entity_id() != null && m.getResult_entity_id().contentEquals(m.getTruth_id())){
							TP += 1;
						}
						else{
							FP += 1;
						}
						precisionSum += (double)TP/(TP + FP);
						if(TP + FP != 0)
							count ++;
					
				}
				//logger.info("id:" + id + ", precision:" + (double)TP/(TP + FP));
			}
		}
		return precisionSum/count;
		
	}
	
	public void entityRecognition(){
		
	}
	
	
	public void process(){

        File file = new File("/home/zj/EntityLinkingWeb/data/evaluation/result.txt");
        
       
        //loadDict();
        loadDocs();
		loadGroundTruth();
		float aList[] = new float[30];
		double bList[] = new double[11];
		
		for(int i = 0; i < 11; i ++){
			bList[i] = (double)(50 + 5*i);
		}
		for(int i = 0; i < 20; i++){
			aList[i] = (float) (0.005*((float)i));
//			bList[i-1] = (float) (0.05*((float)i));
		}
		for(int i = 20; i < 30; i ++){
			aList[i] = (float) (0.1*((float)(i-19)));
		}

		String data = "";

//		trainSet.putAll(testA);
//		trainSet.putAll(testB);
//		HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapA =  loadCandidate(testA, testAGroundTruth);
//		HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapB =  loadCandidate(testB, testBGroundTruth);
		
		HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapTrain =  loadCandidate(trainSet, trainSetGroundTruth);
//		logger.info("candidate map:");
//		logger.info(corpusCMapTrain);
		

		
//		trainSetGroundTruth.putAll(testAGroundTruth);
//		trainSetGroundTruth.putAll(testBGroundTruth);
		System.out.println("evaluate doc size:" + trainSet.size());
		System.out.println("evaluate truth set size:" + trainSetGroundTruth.size());
		
		String[] choose = {"prior", "context", "coherence"};
		for(int i = 0; i < 30; i++){
				System.out.println("a:" + aList[i]);

				//trainSetResult = linkingForACorpus(trainSet, aList[i], bList[i]);
				
//				HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapA =  loadCandidate(testA, testAGroundTruth);
//				HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapB =  loadCandidate(testB, testBGroundTruth);
				
//				testAResult = linkingForACorpus("name", testA, corpusCMapA, aList[i],  0);
//				testBResult = linkingForACorpus("name", testB, corpusCMapB, aList[i],   0);
				
				trainSetResult = linkingForACorpus(choose, trainSet, corpusCMapTrain,aList[i],0);
				
				System.out.println("result size, A:" + testAResult.size() + ", B:" + testBResult.size() + ", train:" + trainSetResult.size());
				//logger.info("demo of train result:" + trainSetResult.get("1"));
//				logger.info("demo of testa result:" + testAResult.get("1000testa"));
//				logger.info("demo of testa truth:" + testAGroundTruth.get("1000testa"));
				//logger.info("demo of testb result:" + testBResult.get("1200testb"));
				
				double microTrain = calMicroP(trainSetResult, "");
				double nilMicroTrain = calMicroP(trainSetResult, "NIL");
//				double microTestA = calMicroP(testAResult);
//				double microTestB = calMicroP(testBResult);
				double macroTrain = calMacroP(trainSetResult, "");
				double nilMacroTrain = calMacroP(trainSetResult, "NIL");
//				double macroTestA = calMacroP(testAResult);
//				double macroTestB = calMacroP(testBResult);
				
//				for(String id: testAResult.keySet()){
//					if(!trainSetResult.containsKey(id) || trainSetResult.get(id).size() == 0)
//						trainSetResult.put(id, testAResult.get(id));
//				}
//				for(String id: testBResult.keySet()){
//					if(!trainSetResult.containsKey(id) || trainSetResult.get(id).size() == 0)
//						trainSetResult.put(id, testBResult.get(id));
//				}
				System.out.println("all size:" + trainSetResult.size());
				mentionCount = countMentions(trainSetResult);
//				double microAll = calMicroP(trainSetResult);
//				double macroAll = calMacroP(trainSetResult);
//				logger.info("testA micro precisions:" + microTestA);
//				logger.info("train: " + microTrain + ", testA: " + microTestA + ", testB: " + microTestB);
//				logger.info("testB macro precisions:" + macroTestA);
//				logger.info("train: " + macroTrain + ", testA: " + macroTestA + ", testB: " + macroTestB);
//				String r = aList[i] + "\t" +microTrain + "\t" + microTestA +"\t" + microTestB + "\t"  + microAll + "\t"+ macroTrain + "\t" + macroTestA +"\t" + macroTestB + "\t" + macroAll + "\n";
				String r = aList[i] + "\t"  + microTrain + "\t" + macroTrain + "\n";
				
				System.out.println(r);
				data += r;
				System.out.println("evaluated mentions:" + mentionCount);
				System.out.println("valid mentions:" + validMentionCount);
				System.out.println("total mentions:" + allMentionCount);
				trainSetResult.clear();
			}
		logger.info(data);
		logger.info("total evaluated mentions:" + mentionCount);
		logger.info("total valid mentions:" + validMentionCount);
		logger.info("total mentions:" + allMentionCount);
		try {
			FileUtils.write(file, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void testParts(){
		File file = new File("/home/zj/EntityLinkingWeb/data/evaluation/result_part.txt");
		File file2 = new File("/home/zj/EntityLinkingWeb/data/evaluation/belief_score.txt");
		String data = "";
		String data2 = "";
        loadDocs();
		loadGroundTruth();
//		HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapA =  loadCandidate(testA, testAGroundTruth);
//		HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapB =  loadCandidate(testB, testBGroundTruth);
		HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapTrain =  loadCandidate(trainSet, trainSetGroundTruth);
		String[][] chooseList = {{"prior", "context", "coherence"}};
				//{"prior","context"}, {"prior","coherence"}, {"coherence","context"}, {"prior"}};
	
		for(String[] s : chooseList){
			for(String x : s)
				System.out.println("choose:" + x);
//			testAResult = linkingForACorpus(s, testA, corpusCMapA, 0.06,  0);
//			testBResult = linkingForACorpus(s, testB, corpusCMapB, 0.06,   0);
			trainSetResult = linkingForACorpus(s, trainSet, corpusCMapTrain, 0.05,  0);
//			for(String id: testAResult.keySet()){
//				if(!trainSetResult.containsKey(id) || trainSetResult.get(id).size() == 0)
//					trainSetResult.put(id, testAResult.get(id));
//			}
//			for(String id: testBResult.keySet()){
//				if(!trainSetResult.containsKey(id) || trainSetResult.get(id).size() == 0)
//					trainSetResult.put(id, testBResult.get(id));
//			}
			double microAll = calMicroP(trainSetResult, "");
			double macroAll = calMacroP(trainSetResult, "");
			mentionCount = countMentions(trainSetResult);
			System.out.println("evaluated mentions:" + mentionCount);
			System.out.println("valid mentions:" + validMentionCount);
			System.out.println("total mentions:" + allMentionCount);
			System.out.println("micro:" + microAll +"，　macro:" + macroAll + "\n");
			data += microAll + "\t" + macroAll + "\t" + "\n";
			for(String id : trainSetResult.keySet())
				for(Mention m : trainSetResult.get(id)){
					data2 += m.getBelief_score() + "\n";
				}
				

		}
		try {
			FileUtils.write(file, data);
			FileUtils.write(file2, data2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int countMentions(HashMap<String, ArrayList<Mention>> map){
		int countMention = 0;
		System.out.println("#doc:" + map.size());
		for(String id : map.keySet()){
			countMention += map.get(id).size();
			if(map.get(id).size() == 0)
				System.out.println("id: "+id + " result is empty");
			
		}
		return countMention;
		
	}
	
	public void evaluateNER(){
		loadDict();
        loadDocs();
		loadGroundTruth();
		HashMap<String, HashMap<Mention, CandidateSet>> corpusCMapTrain = corpusCandidateGeneration(trainSet, trainSetGroundTruth);
		
		String[] choose = {"prior", "context", "coherence"};
		double[] bList = new double[20];
		for(int i = 0; i < 20; i ++){
			bList[i] = 0.005 *(i + 1);
		}
		
		String data = "";
		for(int i = 19; i >= 0; i --){
			int allmentions = 0;
			//trainSetResult = linkingForACorpus(choose, trainSet, corpusCMapTrain, 0.05,  bList[i]);
			
			int FPsum = 0;
			int TPsum = 0;
			double f1sum = 0;
			double pSum = 0;
			int count = 0;
			double recall = 0;
			for(String id : corpusCMapTrain.keySet()){
				ArrayList<Mention> mentions = new ArrayList<Mention>();
				mentions.addAll(corpusCMapTrain.get(id).keySet());
				int j = 0;
				while(j < mentions.size()){
					mentions.get(j).setLink_prob(probModel.getLinkProb(mentions.get(j).getLabel()));
					if( mentions.get(j).getLink_prob() < bList[i])
						mentions.remove(j);
					else
						j ++;
				}
				int tp = 0;
				int fp = 0;
				ArrayList<Mention> truth = trainSetGroundTruth.get(id);
				for(Mention m : mentions){
					if(checkContains(m, truth)){
						tp ++;
					}
					else{
						fp ++;
						logger.info("id:" + id + ", mention:" + m);
					}
				}
				
				FPsum += fp;
				TPsum += tp;
				double precision = (double) tp/(double)(tp + fp);
				double r = (double) tp /(double)(truth.size());
				double f1 = 2*precision*r/(precision + r);
				if(fp + tp != 0){
					count ++;
					pSum += precision;
					if(truth.size() != 0){
						recall += r;
						f1sum += f1;
					}
				}
				
				logger.info("b:" + bList[i] + ",result size:" + mentions.size() + ", truth size:" + truth.size() + ", fp:" +  fp + ", tp" + tp
						+ ", precision:" + precision + ", recall:" + r + ", f1 score:" + f1);
				 
			}
			allmentions += count;
			System.out.println("b:" + bList[i] + ", micro:" + (double)TPsum/(double)(TPsum + FPsum) + ", macro:" + pSum/count + ", recall:" + recall/count + ", f1:" + f1sum/count + "mention count:" + allmentions);
			data += bList[i] + "\t" + (double)TPsum/(double)(TPsum + FPsum) + "\t" + pSum/count + "\t" + recall/count + "\t" + f1sum/count + "\n";
		}
		try {
			File file = new File("/home/zj/EntityLinkingWeb/data/evaluation/result_ner.txt");
			FileUtils.write(file, data);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
	}
	
	boolean checkContains(Mention m, ArrayList<Mention> truth){
		for(Mention s : truth){
			if(s.getLabel().contentEquals(m.getLabel()))
				return true;
		}
		return false;
		
	}
	 

	public static void main(String[] args){

		logger.info("Initialization starts...");
		long start = System.currentTimeMillis();
		// load properties
		logger.info(PropertiesReader.getDomainIndexMap());
		IndexBuilder ibd = IndexBuilder.getInstance();
		BaiduWikiModels model = BaiduWikiModels.getInstance();
		BaikeProbManager pModel = BaikeProbManager.getInstance();
		WikiURLMap.getURL("1");
		long end = System.currentTimeMillis();
		logger.info("Initialzation finished. Time: " + (float)(end - start)/1000 + "s.");
		AIDALinking linker = new AIDALinking();
		//linker.process();
		//linker.testParts();
		linker.evaluateNER();
		for(String id: noMentionDoc){
			logger.info(id);
		}
		
	}
}
