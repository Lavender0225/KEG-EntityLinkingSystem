package edu.tsinghua.el.web.action;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

import edu.tsinghua.el.common.PropertiesReader;

public class GetIndexAction extends ActionSupport{

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(GetIndexAction.class);
	
	private ArrayList<String> indexArray = new ArrayList<String>();

	@JSON(name="IndexArray")
	public ArrayList<String> getIndexArray() {
		return indexArray;
	}
	
	public String execute() throws Exception{
		HashMap<String, String> indexMap = PropertiesReader.getDomainIndexMap();
		for(String i : indexMap.keySet()){
			indexArray.add(i);
		}
		logger.info(indexArray);
		ActionContext context = ActionContext.getContext();
		context.put("indexArr", indexArray);
		
		return SUCCESS;
		
	}
	
	
	
	

}
