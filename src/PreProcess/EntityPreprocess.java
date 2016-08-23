package PreProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import Common.Constant;
import Model.AbstractEntity;
import Model.Entity;

/**
 * Created by ethan on 16/3/23.
 * Sort the entity-uri list,
 * @update by ZJ on 16/7/19, add logger, comment
 */
public class EntityPreprocess {
	private static final Logger logger = LogManager.getLogger();
	
	/**
	 * format:  <13> property:hasMention "Salavan (city)" .
	 * to:		salavan::=13::;city
	 * And lowercase all the String
	 * 
	 * @param input_path, file: Constant.entity_original_path
	 * @param output_path, file: entity_dic_path
	 * @throws Exception
	 */

    public static void formatter(String input_path, String output_path) throws Exception{
        BufferedReader reader = new BufferedReader(new FileReader(input_path));
        BufferedWriter writer = new BufferedWriter(new FileWriter(output_path, false));
        String line=null;
        while ((line = reader.readLine()) != null) {
        	if(line.contains("(") && line.contains(")") && line.lastIndexOf(')') > line.indexOf('(')){  //去小括号
        		if(line.indexOf('(') == 0)      //(作为开头
        		{
        			//System.out.println(line);
        			String label = line.substring(line.lastIndexOf(')')+1, line.indexOf("::;"));
            		String desc = line.substring(line.indexOf('(')+1, line.lastIndexOf(')'));
            		String value = line.substring(line.indexOf("::;"));
            		writer.write(label.toLowerCase().trim()+value.trim()+"::;"+desc.trim()+"\n");
            		//System.out.println(label.trim()+value+"::;"+desc);
        		}
        		else if(line.contains("tunny)*****HELIX*****ISAAC 作为伪随机数发生器使用*****Leviathan (cipher)*****LILI-128*****MUG1 (CRYPTREC 推荐使用****"))
        		{
        			continue;
        		}
        		else
        		{
        			//System.out.println(line);
        			String label = line.substring(0, line.indexOf('('));
            		String desc = line.substring(line.indexOf('(')+1, line.lastIndexOf(')'));
            		String value = line.substring(line.lastIndexOf(')')+1);
            		writer.write(label.toLowerCase().trim()+value.trim()+"::;"+desc.trim()+"\n");
            		//System.out.println(label.trim()+value+"::;"+desc);
        		}
        	}
        	else if(line.contains("[") && line.contains("]") && line.lastIndexOf(']') > line.indexOf('[')){  //去中括号
        		//System.out.println(line);
        		String label = line.substring(0, line.indexOf('['));
        		String desc = line.substring(line.indexOf('[')+1, line.lastIndexOf(']'));
        		String value = line.substring(line.lastIndexOf(']')+1);
        		writer.write(label.toLowerCase().trim()+value.trim()+"::;"+desc.trim()+"\n");
        		//System.out.println(label.trim()+value.trim()+"::;"+desc.trim());
        	}
        	else if(line.contains("（") && line.contains("）") && line.lastIndexOf('）') > line.indexOf('（')){  //去中文括号
        		//System.out.println(line);
        		String label = line.substring(0, line.indexOf('（'));
        		String desc = line.substring(line.indexOf('（')+1, line.lastIndexOf('）'));
        		String value = line.substring(line.lastIndexOf('）')+1);
        		writer.write(label.toLowerCase().trim()+value.trim()+"::;"+desc.trim()+"\n");
        		//System.out.println(label.trim()+value.trim()+"::;"+desc.trim());
        	}
        	else{
        		writer.write(line+"\n");
        	}
        }
        
        reader.close();
        writer.close();
        
    }


    /**
     * Sort the formatted entity list, 不能有重复
     * 将相同label的entity合并为一行
     * 
     * @param input_path, file: Constant.entity_formatted_path
     * @param output_path, file: Constant.entity_formatted_sorted
     * @return
     * @throws IOException
     */
    public static int EntitySort(String input_path, String output_path)throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input_path));
        String line;
        HashMap<String, String> entityMap = new HashMap<String, String>();
        String[] items;
        int count = 0;
        while ((line = reader.readLine()) != null) {
        	if(count%5000000==0 && count > 0){
                System.out.println("has read:"+count);
                //break;
        	}
            items = line.split("::;", 2);			// format: label::;uri::;desc1::;desc2
            if(items.length<2)
            	continue;
            if(!entityMap.containsKey(items[0])){
            	entityMap.put(items[0], items[1]);   //如：小黑屋软件::;7820431::;2

            }
            	
            else{
            	String tmp_uris = entityMap.get(items[0]).split("::;")[0];//7820431
            	String[] uri_des = items[1].split("::;");
            	if(tmp_uris.compareTo(uri_des[0]) != 0){   //编号不同则合并
            		//logger.info("get repeat iterm:"+items[0]+",uri and desc:"+entityMap.get(items[0])+"::="+items[1]);
            		entityMap.put(items[0], entityMap.get(items[0])+"::="+items[1]);
            	}
            }
            count++;
        }
        System.out.println(count);
        reader.close();
        System.out.println("read entities finish! sorting...");
        
        // sort the entity list
        List<AbstractEntity> l_entities = new ArrayList<AbstractEntity>();
        for(Map.Entry<String, String> entry:entityMap.entrySet()){

        	l_entities.add(new Entity(entry.getKey(), entry.getValue()));
        }
        Collections.sort(l_entities);
        System.out.println("sort entities finish! outputting...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(output_path, false));
        for (AbstractEntity en : l_entities)
        {
            writer.write(en.getTitle() + "::=" + en.getId());
            writer.newLine();
        }
        writer.close();
        return l_entities.size();
    }
    
    public static int newEntitySort(String input_path, String output_path)throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(input_path));
        String line;
        HashMap<String, String> entityMap = new HashMap<String, String>();
        String[] items;
        int count = 0;
        while ((line = reader.readLine()) != null) {
        	if(count%5000000==0 && count > 0){
                System.out.println("has read:"+count);
                //break;
        	}
            items = line.split("::=", 2);			// format: label::=uri::;desc1::;desc2
            if(items.length<2)
            	continue;
            if(!entityMap.containsKey(items[0])){
            	entityMap.put(items[0], items[1]);   //如：梅奥::=6160344::;3::=261317::;1

            }
            	
            else{
            	String tmp_uris = entityMap.get(items[0]).split("::;")[0];//7820431
            	String[] uri_des = items[1].split("::;");
            	if(tmp_uris.compareTo(uri_des[0]) != 0){   //编号不同则合并
            		//logger.info("get repeat iterm:"+items[0]+",uri and desc:"+entityMap.get(items[0])+"::="+items[1]);
            		entityMap.put(items[0], entityMap.get(items[0])+"::="+items[1]);
            	}
            }
            count++;
        }
        System.out.println(count);
        reader.close();
        System.out.println("read entities finish! sorting...");
        
        // sort the entity list
        List<AbstractEntity> l_entities = new ArrayList<AbstractEntity>();
        for(Map.Entry<String, String> entry:entityMap.entrySet()){

        	l_entities.add(new Entity(entry.getKey(), entry.getValue()));
        }
        Collections.sort(l_entities);
        System.out.println("sort entities finish! outputting...");
        BufferedWriter writer = new BufferedWriter(new FileWriter(output_path, false));
        for (AbstractEntity en : l_entities)
        {
            writer.write(en.getTitle() + "::=" + en.getId());
            writer.newLine();
        }
        writer.close();
        return l_entities.size();
    }
    

    /**
     * filter entity labels, filter out chars that are not numbers or letters or Chinese
     * pattern: non-[0-9a-zA-Z\u4e00-\u9fa5]
     * 
     * @param input_path
     * @param output_path
     * @throws Exception
     */
    public static void filter(String input_path, String output_path) throws Exception{
    	BufferedReader reader = new BufferedReader(new FileReader(input_path));
        BufferedWriter writer = new BufferedWriter(new FileWriter(output_path, false));
        String line = null;
        String[] items = null;
        char[] char_line = null;
        HashSet<Character> chars = new HashSet<>();
        int line_count = 0;
        while((line = reader.readLine())!=null){
        	line = line.replaceAll("[\\[\\]]|'|\"|<|>|（|）|《|》|\\{|\\}|\\(|\\)", "");
        	line = line.toLowerCase();
        	items = line.split("::=",2);
        	//if(!items[0].matches("[0-9a-zA-Z\u4e00-\u9fa5]+")||items[0].length()<Constant.entity_min_length)
        	if(items[0].length()<Constant.entity_min_length)
        	{
        		continue;    
        	}
        	else 
        	{
        		line_count++;
        		writer.write(line+"\n");
        	}
        	char_line = items[0].toCharArray();
        	for(char tmp:char_line){
        		if(!chars.contains(tmp))
        			chars.add(tmp);
        	}
        }
        System.out.println(line_count);
        reader.close();
        writer.close();
    }
    
    public static void partition(int num, String input_path, String output_path) throws Exception{
    	BufferedReader reader = new BufferedReader(new FileReader(input_path));
        BufferedWriter writer = new BufferedWriter(new FileWriter(output_path, false));
        int line_count = 0;
        String line=null;
        while((line = reader.readLine()) != null){
        	writer.write(line);
        	writer.newLine();
        	if(line_count>=num)
        		break;
        	line_count++;
        }
        reader.close();
        writer.close();
    }
    
    public static void main(String[] args) throws Exception {
    	//EntityFilter.filter(5);
    	//formatter(Constant.entity_original_path, Constant.entity_formatted_path);
    	//System.out.println("#sorted entities: "+EntitySort(Constant.entity_formatted_path, Constant.entity_formatted_sorted));
    	//filter(Constant.entity_formatted_sorted, Constant.entity_ready_file);
    	
    	
    	//将多个领域的实体集放到formatted_path中，然后去掉重复的
    	System.out.println("#sorted entities: "+newEntitySort(Constant.entity_formatted_path, Constant.entity_formatted_sorted));
    	filter(Constant.entity_formatted_sorted, Constant.entity_ready_file);
    	
//    	partition(4000000, Constant.entity_file, Constant.entity_file+"_1");
//    	System.out.println("partition finished!");
    }
}
