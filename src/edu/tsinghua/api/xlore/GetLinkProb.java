package edu.tsinghua.api.xlore;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import edu.tsinghua.el.common.Constant;

public class GetLinkProb {
	private static HashMap<String, Float> linkProbMap = null;
	
	public static void loadLinkProb(){
		InputStream is = null;
		try {
		    is = new FileInputStream(Constant.linkProbPath);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    linkProbMap = new HashMap<String, Float>();
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] tmp_list = line.split("::=");
		        String label = tmp_list[0].trim().toLowerCase();
		        float value = Float.valueOf(tmp_list[1].trim());
		        linkProbMap.put(label, value);
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
	}
	
	public static float getLinkProbOfMention(String mention){
		if(linkProbMap == null){
			loadLinkProb();
		}
		if (linkProbMap.containsKey(mention.toLowerCase())){
			return linkProbMap.get(mention.toLowerCase());
		}
		return 0;
	}
	
//	public static void main(String[] args){
//		GetLinkProb linkProb = new GetLinkProb();
//		linkProb.loadLinkProb("./etc/link_prob.dat");
//		System.out.println(linkProb.getLinkProbOfMention("中国"));
//		
//	}

}
