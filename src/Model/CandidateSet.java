package Model;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 候选实体集合的Model
 * @author Jingzhang
 *
 */
public class CandidateSet {
	private HashMap<String, Candidate> set;
	
	public CandidateSet(){
		set = new HashMap<String, Candidate>();
	}
	
	public void addElement(String id, Entity entity){
		if(!set.containsKey(id) && entity != null){
			Candidate c = new Candidate();
			c.setEntity(entity);
			set.put(id, c);
		}
	}

	public HashMap<String, Candidate> getSet() {
		return set;
	}

	public void setSet(HashMap<String, Candidate> set) {
		this.set = set;
	}
	
	public boolean contains(String key){
		return set.containsKey(key);
	}
	/**
	 * 排序函数，默认升序，reverse控制是否降序
	 * @param map
	 * @param reverse
	 * @return
	 */
	public static <K, V extends Comparable<? super V>> HashMap<String, Candidate> sortByValue(HashMap<String, Candidate> map , final boolean reverse){
        List<Map.Entry<String, Candidate>> list =new LinkedList<>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<String, Candidate>>()
        {
            public int compare( Map.Entry<String, Candidate> o1, Map.Entry<String, Candidate> o2 )
            {
                if (reverse){
                	if(o1.getValue().getScore() < o2.getValue().getScore())
                		return 1;
                	else if(o1.getValue().getScore() >o2.getValue().getScore())
                		return -1;
                	return 0;
                }
                else{
                	if(o1.getValue().getScore() > o2.getValue().getScore())
                		return 1;
                	else if(o1.getValue().getScore() < o2.getValue().getScore())
                		return -1;
                	return 0;
                }
            }
        } );
        HashMap<String, Candidate> result = new LinkedHashMap<>();
        for (Map.Entry<String, Candidate> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    } 
	@Override
	public String toString() {
		String str =  "CandidateSet, size:"+set.size()+", [\n ";
		for(Candidate e : set.values()){
			str += "\t"+ e.toString() + "\n";
		}
		str += "\n";
		return str;
	}
	
	
}
