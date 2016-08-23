package XloreAPI;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

public class XloreGetPopularity {
	private static HashMap<String, Integer> popularityMap = new HashMap<String, Integer>(); // P(e)
	
	private XloreGetPopularity(){
		
	}
	
	private static void loadPopularityMap(){
		InputStream is = null;
		try {
			long start = System.currentTimeMillis();
		    is = new FileInputStream(Constant.entityCountInput);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] tmp_list = line.split("::;");
		        String id = tmp_list[1];
		        String count = tmp_list[2];
		        if(popularityMap.containsKey(id)){
		        	popularityMap.put(id, popularityMap.get(id) + Integer.valueOf(count));
		        }
		        else{
		        	popularityMap.put(id, Integer.valueOf(count));
		        }
		    }
		    long end = System.currentTimeMillis();
		    System.out.println("loading popularity finished!" + " Time:" + (double)(end - start)/1000);
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
	
	public void outPutTopK(int K){
		int count = 0;
		Iterator<Entry<String, Integer>> entries = popularityMap.entrySet().iterator();
		while(count < K){
			Map.Entry<String, Integer> entry = (Map.Entry<String, Integer>) entries.next();  
		    String id = entry.getKey();  
		    Integer num = entry.getValue();
		    System.out.println("entity:" + GetEntityAPI.getEntityDetailByID(id).toString());
		    System.out.println("count:"+num);
		    count ++;
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
		XloreGetPopularity test = new XloreGetPopularity();
		test.loadPopularityMap();
		test.outPutTopK(30);

	}

}
