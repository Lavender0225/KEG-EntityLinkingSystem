package edu.tsinghua.el.model.vec;

import edu.tsinghua.el.common.PropertiesReader;

public class BaiduWikiModels {
	private BaikeModel baiduModel = null;
	private BaikeModel wikiModel = null;
	
	private BaiduWikiModels(){
		if(baiduModel == null && PropertiesReader.getPathMap().containsKey("baidu_word") && PropertiesReader.getPathMap().containsKey("baidu_entity")){
			String wordPath = PropertiesReader.getPathMap().get("baidu_word");
			String entityPath = PropertiesReader.getPathMap().get("baidu_entity");
			baiduModel = new BaikeModel(wordPath, entityPath);
		}
		if(wikiModel == null && PropertiesReader.getPathMap().containsKey("wiki_word") && PropertiesReader.getPathMap().containsKey("wiki_entity")){
			String wordPath = PropertiesReader.getPathMap().get("wiki_word");
			String entityPath = PropertiesReader.getPathMap().get("wiki_entity");
			wikiModel = new BaikeModel(wordPath, entityPath);
		}
	}
	
	private static class InstanceHolder{
		private static final BaiduWikiModels instance = new BaiduWikiModels();
	}
	
	public static BaiduWikiModels getInstance(){
		return InstanceHolder.instance;
	}

	public BaikeModel getBaiduModel() {
		return baiduModel;
	}

	public BaikeModel getWikiModel() {
		return wikiModel;
	}
}
