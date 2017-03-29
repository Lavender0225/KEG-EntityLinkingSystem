package edu.tsinghua.api.xlore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.tsinghua.el.model.Entity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.tsinghua.el.common.Constant;

/**
 * 根据entity的ID获取entity的详细内容
 * 服务地址：http://xlore.org/query?uri=xxxxx
 *
 * @author Jingzhang
 *
 */
public class XloreGetEntity {
	
	public static Entity getEntityDetailByID(String id){
		// get result fron http query
		String url = "http://10.1.1.66:8080/query?uri=" + Constant.xlore_entity_prefix + id;
        //System.out.println(url);
        StringBuilder json = new StringBuilder();
        try {
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                                        yc.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch ( IOException e) {
        	e.printStackTrace();
        }
        System.out.println(json);
        // json parse
		return parse(id, json.toString());
		
	}
	public static Entity parse(String id, String jsonLine) {
		if(jsonLine.isEmpty())
			return null;
	    JsonElement jelement = new JsonParser().parse(jsonLine);
	    JsonObject  jobject = jelement.getAsJsonObject();
	    try{
		    //String uri = jobject.get("uri").getAsString(); new interface dosn't have this attribute
		    
		    // get labels
		    String label_zh = "";
		    String label_en = "";
		    if(jobject.has("label")){
		    	JsonObject label = jobject.getAsJsonObject("label");
		    	if(label.has("_label_zh")){
		    		label_zh = label.get("_label_zh").getAsString();
		    	}
		    	if(label.has("_label_en")){
		    		label_en = label.get("_label_en").getAsString();
		    	}
		    }
		 
		    // get mentions
		    ArrayList<String> mention_list = new ArrayList<String>();;
		    if(jobject.has("mention")){
		    	for (JsonElement mention : jobject.getAsJsonArray("mention")){
		    		mention_list.add(mention.getAsString());
		    	}
		    }
		    // get abstract
		    
		    String abstract_zh = "";
		    String abstract_en = "";
		    if(jobject.has("abstracts")){
		    	JsonObject abstracts = jobject.getAsJsonObject("abstracts");
		    	if(abstracts.get("zhwiki") != null){
			    	abstract_zh += abstracts.get("zhwiki").getAsString();
			    }
			    if(abstracts.get("hudong") != null){
			    	abstract_zh += abstracts.get("hudong").getAsString();
			    }
			    if(abstracts.get("baidu") != null){
			    	abstract_zh += abstracts.get("baidu").getAsString();
			    }
			    if(abstracts.get("enwiki") != null){
			    	abstract_en += abstracts.get("enwiki").getAsString();
			    }
		    }
		    
		    // get superclass list
		    ArrayList<String> super_classes_en = new ArrayList<String>();;
		    ArrayList<String> super_classes_zh = new ArrayList<String>();;
		    if(jobject.has("superClasses")){
		    	for(Map.Entry<String, JsonElement> entry : jobject.getAsJsonObject("superClasses").entrySet()){
			    	String label_zh_2 = entry.getValue().getAsJsonObject().get("_label_zh").getAsString();
			    	String label_en_2 = entry.getValue().getAsJsonObject().get("_label_en").getAsString();
			    	if( !label_zh_2.isEmpty())
			    		super_classes_zh.add(label_zh_2);
			    	if( !label_en_2.isEmpty())
			    		super_classes_en.add(label_en_2);
			    }
		    }
		    
		    // get related entities, including relatedClasses and relatedInstances
		    ArrayList<String> related_items_en = new ArrayList<String>();;
		    ArrayList<String> related_items_zh = new ArrayList<String>();;
		    if(jobject.has("relatedClasses")){
			    for(Map.Entry<String, JsonElement> entry : jobject.getAsJsonObject("relatedClasses").entrySet()){
			    	String label_zh_2 = entry.getValue().getAsJsonObject().get("_label_zh").getAsString();
			    	String label_en_2 = entry.getValue().getAsJsonObject().get("_label_en").getAsString();
			    	if( !label_zh_2.isEmpty())
			    		related_items_zh.add(label_zh_2);
			    	if( !label_en_2.isEmpty())
			    		related_items_en.add(label_en_2);
			    }
		    }
		    if(jobject.has("relatedInstances")){
		    	for(Map.Entry<String, JsonElement> entry : jobject.getAsJsonObject("relatedInstances").entrySet()){
			    	String label_zh_2 = entry.getValue().getAsJsonObject().get("_label_zh").getAsString();
			    	String label_en_2 = entry.getValue().getAsJsonObject().get("_label_en").getAsString();
			    	if( !label_zh_2.isEmpty())
			    		related_items_zh.add(label_zh_2);
			    	if( !label_en_2.isEmpty())
			    		related_items_en.add(label_en_2);
			    }
		    }
		    
		    
		    // get infobox
		    HashMap<String, String> infobox_zh = new HashMap<String, String>();
		    HashMap<String, String> infobox_en = new HashMap<String, String>();
		    for(Map.Entry<String, JsonElement> entry : jobject.getAsJsonObject("properties").entrySet()){
		    	JsonObject obj = entry.getValue().getAsJsonObject();
		    	String label_zh_tmp = obj.getAsJsonObject("label").get("_label_zh").getAsString();
		    	String label_en_tmp = obj.getAsJsonObject("label").get("_label_en").getAsString();
		    	String value_zh = obj.get("baidu").getAsString()
		    			+ obj.get("hudong").getAsString()
		    			+ obj.get("zhWiki").getAsString();
		    	String value_en = obj.get("enWiki").getAsString();
		    	if( !label_zh_tmp.isEmpty() && !value_zh.isEmpty()){
		    		infobox_zh.put(label_zh_tmp, value_zh);
		    	}
		    	if( !label_en_tmp.isEmpty() && !value_en.isEmpty()){
		    		infobox_en.put(label_en_tmp, value_en);
		    	}
		    }
		    return new Entity(id, label_zh, label_en, mention_list, abstract_en, abstract_zh, super_classes_en, super_classes_zh,
					related_items_zh, related_items_en, infobox_zh, infobox_en);
	    }catch(Exception e){
	    	//e.printStackTrace();
	    	System.out.print("parse exception catch, id:" + id);
	    	return null;
	    }
	    // new entity
		
	}

	public static void main(String[] args) {
		System.out.println(XloreGetEntity.getEntityDetailByID("87320").toString());
		String url = "http://xlore.org/query?uri=" + Constant.xlore_entity_prefix + "87320";
        //System.out.println(url);
        StringBuilder json = new StringBuilder();
        try {
            URL oracle = new URL(url);
            URLConnection yc = oracle.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                                        yc.getInputStream()));
            String inputLine = null;
            while ( (inputLine = in.readLine()) != null) {
                json.append(inputLine);
            }
            in.close();
        } catch (MalformedURLException e) {
        	e.printStackTrace();
        } catch ( IOException e) {
        	e.printStackTrace();
        }
        System.out.println(json);
	}

}
