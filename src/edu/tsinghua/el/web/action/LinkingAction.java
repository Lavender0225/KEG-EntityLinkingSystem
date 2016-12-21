package edu.tsinghua.el.web.action;

import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.json.annotations.JSON;

import com.opensymphony.xwork2.ActionSupport;

import edu.tsinghua.el.model.LinkingResult;
import edu.tsinghua.el.service.EntityLinkingServiceImpl;

public class LinkingAction extends ActionSupport{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LogManager.getLogger(ActionSupport.class);
	
	private String text;
	
	private ArrayList<LinkingResult> resultList;

	@JSON(name="Text")
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	@JSON(name="ResultList")
	public ArrayList<LinkingResult> getResultList() {
		return resultList;
	}

	public void setResultList(ArrayList<LinkingResult> resultList) {
		this.resultList = resultList;
	}

	public String execute() throws Exception{
		logger.info("Text Acceptted: " + text);
		System.out.println("Get Text:" + text);
		
		EntityLinkingServiceImpl linkingIns = new EntityLinkingServiceImpl();
		resultList = linkingIns.linking(text);
		
		logger.info("Linking Result List:" + resultList);
		System.out.println("Linking Result List:" + resultList);
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/xml;charset=UTF-8");
		response.setHeader("Cache-Control", "no-cache");
		
		return SUCCESS;
		
	}
}
