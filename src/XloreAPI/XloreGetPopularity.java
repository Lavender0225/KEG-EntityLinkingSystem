package XloreAPI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Common.Constant;

public class XloreGetPopularity {
	private static HashMap<String, Integer> popularityMap = new HashMap<String, Integer>(); // P(e)
	private static HashMap<String, HashSet<String>> IdMentionMap = new HashMap<String, HashSet<String>>(); 
	
	private XloreGetPopularity(){
		
	}
	
	private static void loadPopularityMap(){
		InputStream is = null;
		try {
		    is = new FileInputStream(Constant.popularity_path);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] tmp_list = line.split("::=");
		        String id = tmp_list[0];
		        String count = tmp_list[1];

		        popularityMap.put(id, Integer.valueOf(count));
		        
		    }
		    System.out.println("loading popularity finished!");
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
	public static void loadIdMentionMap(){
		InputStream is = null;
		try {
		    is = new FileInputStream(Constant.entityCountInput);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] tmp_list = line.split("::;");
		        String mention = tmp_list[0];
		        String id = tmp_list[1];
		        if(!IdMentionMap.containsKey(id)){
		        	IdMentionMap.put(id, new HashSet<String>());
		        }
		        IdMentionMap.get(id).add(mention);
		    }
		    System.out.println("loading id-mention map finished!");
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
	public static int getPopularity(String id){
		if(popularityMap.size() == 0){
			loadPopularityMap();
		}
		if(popularityMap.containsKey(id)){
			return popularityMap.get(id);
		}
		return 0;
	}
	
	public static void outputMentions(){
		OutputStream o = null;
	    try {
	        o = new FileOutputStream("./etc/entity/id-mention.txt");
	        @SuppressWarnings("resource")
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"), 512);
	        Iterator<Entry<String, HashSet<String>>> entries = IdMentionMap.entrySet().iterator();
	        while(entries.hasNext()){
				Map.Entry<String, HashSet<String>> entry =  entries.next();  
			    String id = entry.getKey();  
			    HashSet<String> mentions = entry.getValue();
			    writer.write( id + "::=" + mentions + "\n");
			}
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
	
	public void outPut(){
		OutputStream o = null;
	    try {
	        o = new FileOutputStream("./etc/entity/popularity.txt");
	        @SuppressWarnings("resource")
	        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o, "UTF-8"), 512);
	        Iterator<Entry<String, Integer>> entries = popularityMap.entrySet().iterator();
	        while(entries.hasNext()){
				Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) entries.next();  
			    String id = entry.getKey();  
			    Integer num = entry.getValue();
			    writer.write( id + "::=" + String.valueOf(num) + "\n");
			}
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
		XloreGetPopularity.loadIdMentionMap();
		XloreGetPopularity.outputMentions();

	}

}
