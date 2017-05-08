package edu.tsinghua.el.model;

import java.util.ArrayList;
import java.util.HashSet;

public class Mention {
	
	private String label;
    private Position position;
    private String result_entity_id;
    private ArrayList<String> context_words;
    private double link_prob;

    public Mention(){
    	position = new Position(0, 0);
    	context_words = new ArrayList<String>();
    	result_entity_id = null;
    	link_prob = 0.000001;
    }

    public String getLabel(){
        return this.label;
    }

	
	public Position getPosition() {
		return position;
	}

	public void setPosition(int begin, int end) {
		this.position.begin = begin;
		this.position.end = end;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getResult_entity_id() {
		return result_entity_id;
	}

	public void setResult_entity_id(String result_entity_id) {
		this.result_entity_id = result_entity_id;
	}

	public ArrayList<String> getContext_words() {
		return context_words;
	}

	public void setContext_words(ArrayList<String> context_entity) {
		this.context_words = context_entity;
	}
	
	

	public double getLink_prob() {
		return link_prob;
	}

	public void setLink_prob(double link_prob) {
		this.link_prob = link_prob;
	}

	@Override
	public String toString() {
		return "Mention [label=" + label 
				+ ", result_entity_id=" + result_entity_id
				+ ", pos_start=" + position.begin + ", pos_end=" + position.end
				+ ", context_entity=" + context_words
				+ ", result_entity_id=" + result_entity_id 
				
				+ "]\n";
	}

	
    
}
