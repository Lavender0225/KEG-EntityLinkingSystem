package edu.tsinghua.el.initialization;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ansj.word2vec.Word2VEC;
import edu.tsinghua.api.xlore.GetLinkProb;
import edu.tsinghua.api.xlore.XloreGetPopularity;
import edu.tsinghua.el.common.Constant;
import edu.tsinghua.el.common.PropertiesReader;
import edu.tsinghua.el.index.IndexBuilder;

public class Initialize  extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = LogManager.getLogger(Initialize.class);
	
	public void init() throws ServletException{
		logger.info("Initialization starts...");
		long start = System.currentTimeMillis();
		// load properties
		PropertiesReader prop = new PropertiesReader();
		String trie_path = prop.getTriePath();
		// load index
		IndexBuilder ibd = IndexBuilder.getInstance(Constant.entity_ready_file,trie_path);
		
		// load entity vector
		Word2VEC.getInstance();
		
		// load popularity file
		XloreGetPopularity.getPopularity("1", "12197909");		// if popularity map is not in mem, it will be loaded
		
		// load link probability file
		GetLinkProb.getLinkProbOfMention("中国");
		long end = System.currentTimeMillis();
		logger.info("Initialzation finished. Time: " + (float)(end - start)/1000 + "s.");
		
	}

}
