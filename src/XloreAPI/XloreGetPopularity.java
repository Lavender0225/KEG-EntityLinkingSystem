package XloreAPI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Common.Constant;
import PreProcess.EntityPreprocess;

public class XloreGetPopularity {
	//private static HashMap<String, Integer> popularityMap = new HashMap<String, Integer>(); // P(e)
	private static HashMap<String, HashMap<String, Float>> commonnessMap = new HashMap<String, HashMap<String, Float>>();
	//private static HashMap<String, HashSet<String>> IdMentionMap = new HashMap<String, HashSet<String>>(); 
	
	private XloreGetPopularity(){
		
	}
	private static void loadPopularityMap(){
		File f = new File(Constant.commonness_path);		
		if(f.exists() && !f.isDirectory()) { 
			loadCommonness(Constant.commonness_path);
		}
		else{
			loadCommonnessMap();
		}
	}
	/**
	 * from file Constant.entityCountInput construct commonnessMap
	 */
	private static void loadCommonnessMap(){
		InputStream is = null;
		try {
			EntityPreprocess.formatter(Constant.entityCountInput, Constant.commonness_ready_path);
		    is = new FileInputStream(Constant.commonness_ready_path);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    int repeat = 0;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] tmp_list = line.split("::;");
		        String mention = tmp_list[0];
		        String id = tmp_list[1];
		        Float count = Float.valueOf(tmp_list[2]);
		        String desc =null;
		        if(tmp_list.length == 4){
		        	desc = tmp_list[3];
		        	id = id + "&&" + desc;
		        	if(mention.isEmpty()){
		        		mention = desc;
		        	}
		        }

		        if(!commonnessMap.containsKey(mention)){
		        	commonnessMap.put(mention, new HashMap<String, Float>());
		        	commonnessMap.get(mention).put(id, count);
		        	
		        }
		        else{
		        	String now_id = id.split("&&")[0];
		        	Iterator<Entry<String, Float>> entries = commonnessMap.get(mention).entrySet().iterator();  
		        	boolean flag = false;
		        	while (entries.hasNext()) {  
		        	    Map.Entry<String, Float> entry =  entries.next();  
		        	    String key = entry.getKey();  
		        	    Float value = entry.getValue();
		        	    String [] tmp = key.split("&&");
		        	    String id2 = tmp[0];
//		        	    String desc2 = null;
//		        	    if(tmp.length > 1){
//		        	    	desc2 = tmp[1];
//		        	    }
		        	    if(id2.compareTo(now_id) == 0){
		        	    	flag = true;
		        	    	entry.setValue(value + count);
		        	    	repeat ++;
		        	    	
		        	    }
		        	} 
		        	if(flag == false){
		        		commonnessMap.get(mention).put(id, count);
		        	}
		        }
		        
		    }
		    
		    Iterator<Entry<String, HashMap<String, Float>>> entries = commonnessMap.entrySet().iterator();
	        while(entries.hasNext()){
				Map.Entry<String, HashMap<String, Float>> entry =  entries.next();    
			    HashMap<String, Float> commonness = entry.getValue();
			    float total_freq = 0;
			    Iterator<Entry<String, Float>> entries2 = commonness.entrySet().iterator();
			    while(entries2.hasNext()){
			    	Map.Entry<String, Float> entry2 = entries2.next();
			    	total_freq += entry2.getValue();
			    }
			    entries2 = commonness.entrySet().iterator();
			    while(entries2.hasNext()){
			    	Map.Entry<String, Float> entry2 = entries2.next();
			    	entry2.setValue(entry2.getValue()/total_freq);
			    }
			}
		    System.out.println("loading commomnness map finished! Size:" + commonnessMap.size() + ", repeat:" + repeat );
//		    sortByValue(popularityMap, true);
//		    System.out.println("sorting finished!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	/**
	 * 
	 * @param path
	 */
	private static void loadCommonness(String path){
		InputStream is = null;
		try {
		    is = new FileInputStream(path);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] tmp_list = line.split("::;");
		        String mention = tmp_list[0];
		        try{
			        String id = tmp_list[1];
			        Float p = Float.valueOf(tmp_list[2]);
	
			        if(!commonnessMap.containsKey(mention)){
			        	commonnessMap.put(mention, new HashMap<String, Float>());
			        }
			        commonnessMap.get(mention).put(id, p);
			        
		        }catch(java.lang.ArrayIndexOutOfBoundsException e){
		        	System.out.println(line);
		        }
		        
		    }
		    System.out.println("loading popularity finished! Size:" + commonnessMap.size() );
//		    sortByValue(popularityMap, true);
//		    System.out.println("sorting finished!");
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
//	
//	public static void loadIdMentionMap(){
//		InputStream is = null;
//		try {
//		    is = new FileInputStream(Constant.entityCountInput);
//		    @SuppressWarnings("resource")
//		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
//		    String line = null;
//		    while((line = reader.readLine())!=null){
//		        line = line.trim();
//		        String[] tmp_list = line.split("::;");
//		        String mention = tmp_list[0];
//		        String id = tmp_list[1];
//		        if(!IdMentionMap.containsKey(id)){
//		        	IdMentionMap.put(id, new HashSet<String>());
//		        }
//		        IdMentionMap.get(id).add(mention);
//		    }
//		    System.out.println("loading id-mention map finished!");
////		    sortByValue(popularityMap, true);
////		    System.out.println("sorting finished!");
//		}catch (IOException ioe){
//		    ioe.printStackTrace();
//		} finally {
//		    try {
//		        if (is != null) {
//		            is.close();
//		            is = null;
//		        }
//		    } catch (IOException e) {
//		        e.printStackTrace();
//		    }
//		}  
//	}
	public static double getPopularity(String mention, String id){
		if(commonnessMap.size() == 0){
			loadPopularityMap();
		}
		if(commonnessMap.containsKey(mention)){
			if(commonnessMap.get(mention).containsKey(id)){
				System.out.println("get the commonness of " + mention + "&&" + id + ": "+ String.valueOf(commonnessMap.get(mention).get(id) + ", "+commonnessMap.get(mention)));
				return (double)commonnessMap.get(mention).get(id);
			}
		}
		return 0;
	}
	public static void outputCommonness(){
		OutputStream o = null;
	    try {
	        o = new FileOutputStream("./etc/entity/commonness.txt");
	        @SuppressWarnings("resource")
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"), 512);
	        Iterator<Entry<String, HashMap<String, Float>>> entries = commonnessMap.entrySet().iterator();
	        System.out.println("writing to file...size: " + commonnessMap.size());
	        while(entries.hasNext()){
				Map.Entry<String, HashMap<String, Float>> entry =  entries.next();  
			    String mention = entry.getKey();  
			    HashMap<String, Float> commonness = entry.getValue();
			    Iterator<Entry<String, Float>> entries2 = commonness.entrySet().iterator();
			    while(entries2.hasNext()){
			    	Map.Entry<String, Float> entry2 = entries2.next();
			    	String id = entry2.getKey();
			    	Float p = entry2.getValue();
			    	if(id.contains("&&")){
			    		String[] tmp = id.split("&&");
			    		writer.write(mention + "::;"+ tmp[0] + "::;" + String.valueOf(p) + "::;" + tmp[1] + "\n");
			    	}
			    	else{
			    		writer.write(mention + "::;"+ id + "::;" + String.valueOf(p) + "\n");
			    	}
			    }
			}System.out.println("writing to file finished. " );
	    }catch (IOException ioe){
	        ioe.printStackTrace();
	    } finally {
	        try {
	            if (o != null) {
	                o.close();
	                o = null;
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
//	
//	public static void outputMentions(){
//		OutputStream o = null;
//	    try {
//	        o = new FileOutputStream("./etc/entity/id-mention.txt");
//	        @SuppressWarnings("resource")
//	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"), 512);
//	        Iterator<Entry<String, HashSet<String>>> entries = IdMentionMap.entrySet().iterator();
//	        while(entries.hasNext()){
//				Map.Entry<String, HashSet<String>> entry =  entries.next();  
//			    String id = entry.getKey();  
//			    HashSet<String> mentions = entry.getValue();
//			    writer.write( id + "::=" + mentions + "\n");
//			}
//	    }catch (IOException ioe){
//	        ioe.printStackTrace();
//	    } finally {
//	        try {
//	            if (o != null) {
//	                o.close();
//	                o = null;
//	            }
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//	    }
//	}
//	
//	public void outPut(){
//		OutputStream o = null;
//	    try {
//	        o = new FileOutputStream("./etc/entity/popularity.txt");
//	        @SuppressWarnings("resource")
//	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"), 512);
//	        Iterator<Entry<String, Integer>> entries = popularityMap.entrySet().iterator();
//	        while(entries.hasNext()){
//				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) entries.next();  
//			    String id = entry.getKey();  
//			    Integer num = entry.getValue();
//			    writer.write( id + "::=" + String.valueOf(num) + "\n");
//			}
//	    }catch (IOException ioe){
//	        ioe.printStackTrace();
//	    } finally {
//	        try {
//	            if (o != null) {
//	                o.close();
//	                o = null;
//	            }
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
//	    }
//	}
	
	/**
	 * 排序函数，默认升序，reverse控制是否降序
	 * @param map
	 * @param reverse
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> HashMap<String, Integer> sortByValue(HashMap<String, Integer> map , final boolean reverse){
        List<Map.Entry<String, Integer>> list =new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare( Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2 )
            {
                if (reverse){
                	if(o1.getValue() < o2.getValue())
                		return 1;
                	else if(o1.getValue() >o2.getValue())
                		return -1;
                	return 0;
                }
                else{
                	if(o1.getValue() > o2.getValue())
                		return 1;
                	else if(o1.getValue() < o2.getValue())
                		return -1;
                	return 0;
                }
            }
        } );
        HashMap<String, Integer> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 

	public static void main(String[] args) {
		XloreGetPopularity.loadPopularityMap();
		XloreGetPopularity.outputCommonness();
		System.out.println(XloreGetPopularity.getPopularity("1", "12197909"));

	}

}
