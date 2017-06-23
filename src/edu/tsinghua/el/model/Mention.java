package edu.tsinghua.el.model;

import java.util.ArrayList;

import org.apache.commons.lang.builder.HashCodeBuilder;

import baike.entity.dao.WikiIDMap;

public class Mention {
	
	private String label;
    private Position position;
    private String result_entity_id;
    private ArrayList<String> context_words;
    private double link_prob;
    private String truth_id;
    private boolean valid;
    private double belief_score = 0;

    public Mention(){
    	position = new Position(0, 0);
    	context_words = new ArrayList<String>();
    	result_entity_id = null;
    	link_prob = 0.000001;
    	truth_id = null;
    	valid = true;
    }

    
    public double getBelief_score() {
		return belief_score;
	}


	public void setBelief_score(double belief_score) {
		this.belief_score = belief_score;
	}


	public boolean isValid() {
		return valid;
	}


	public void setValid(boolean valid) {
		this.valid = valid;
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
	
	

	public String getTruth_id() {
		return truth_id;
	}

	public void setTruth_id(String truth_id) {
		this.truth_id = truth_id;
	}

	@Override
	public String toString() {
		return "Mention [label=" + label 
				+ ", result_entity_id=" + result_entity_id
				+ ", pos_start=" + position.begin + ", pos_end=" + position.end
				+ ", link_prob=" + link_prob
				+ ", context_entity=" + context_words
				+ ", trutj_id=" + truth_id 
				+ ", belief_score=" + belief_score
				
				+ "]\n";
	}
	
	@Override
    public boolean equals(Object obj){
		if(obj == null) return false;
		Mention o = (Mention) obj;
        return this.label.contentEquals(o.getLabel()) && this.position.equals(o.getPosition());
    }
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31) // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
        	.append(label)
        	.append(position)
            .toHashCode();
    }

	
    
}
