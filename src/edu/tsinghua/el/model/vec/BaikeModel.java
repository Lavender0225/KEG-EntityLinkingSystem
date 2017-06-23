package edu.tsinghua.el.model.vec;

import java.util.HashMap;

import edu.tsinghua.el.common.PropertiesReader;

public class BaikeModel extends VecModel{
	
	private HashMap<String, float[]> wordMap = null;
	private HashMap<String, float[]> entityMap = null;
	
	public BaikeModel(String wordPath, String entityPath){
		wordMap = loadBinaryVecModel(wordPath);
		entityMap = loadBinaryVecModel(entityPath);
	}

	@Override
	public float[] getWordVec(String word) {
		return wordMap.get(word);
	}

	@Override
	public float[] getEntityVec(String entity) {
		return entityMap.get(entity);
	}

	@Override
	public boolean containsWord(String word) {
		return wordMap.containsKey(word);
	}

	@Override
	public boolean containsEntity(String entity) {
		return entityMap.containsKey(entity);
	}
}
