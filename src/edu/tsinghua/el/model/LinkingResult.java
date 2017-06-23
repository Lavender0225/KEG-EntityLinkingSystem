package edu.tsinghua.el.model;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class LinkingResult implements Comparable<LinkingResult>{
	private int start_index;
	private int end_index;
	private String label;
	private String entity_id;
	private String url;
	private double coherence_score;
	private double popularity_score;
	private double relatedness_score;
	private double contextSim;
	private double beliefScore;
	private double link_prob;
	
	public int getStart_index() {
		return start_index;
	}
	public void setStart_index(int start_index) {
		this.start_index = start_index;
	}
	public int getEnd_index() {
		return end_index;
	}
	public void setEnd_index(int end_index) {
		this.end_index = end_index;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	public double getContextSim() {
		return contextSim;
	}
	public void setContextSim(double contextSim) {
		this.contextSim = contextSim;
	}
	public double getBeliefScore() {
		return beliefScore;
	}
	public void setBeliefScore(double beliefScore) {
		this.beliefScore = beliefScore;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getEntity_id() {
		return entity_id;
	}
	public void setEntity_id(String entity_id) {
		this.entity_id = entity_id;
	}
	public double getCoherence_score() {
		return coherence_score;
	}
	public void setCoherence_score(double coherence_score) {
		this.coherence_score = coherence_score;
	}
	public double getPopularity_score() {
		return popularity_score;
	}
	public void setPopularity_score(double popularity_score) {
		this.popularity_score = popularity_score;
	}
	
	public double getRelatedness_score() {
		return relatedness_score;
	}
	public void setRelatedness_score(double relatedness_score) {
		this.relatedness_score = relatedness_score;
	}
	
	
	public double getLink_prob() {
		return link_prob;
	}
	public void setLink_prob(double link_prob) {
		this.link_prob = link_prob;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null || !LinkingResult.class.isAssignableFrom(obj.getClass()) )
			return false;
		LinkingResult o = (LinkingResult) obj;
	    if(this.label.contentEquals(o.getLabel()) && this.url.contentEquals(o.getUrl()))
	    	return false;
	    return true;
	}
	 @Override
	    public int hashCode() {
	        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
	            // if deriving: appendSuper(super.hashCode()).
	            append(label).
	            append(url).
	            toHashCode();
	    }
	
	@Override
	public String toString() {
		return "LinkingResult [start_index=" + start_index + ", end_index="
				+ end_index + ", label=" + label + ", entity_id=" + entity_id
				+ ", url=" + url + ", coherence_score=" + coherence_score
				+ ", popularity_score=" + popularity_score
				+ ", relatedness_score=" + relatedness_score + "]";
	}
	@Override
	public int compareTo(LinkingResult o) {
		if(this.beliefScore < o.getBeliefScore()){
			return 1;
		}
		else if(this.beliefScore == o.getBeliefScore())
			return 0;
			
		return -1;
	}
	
	
	
	
	
	
	

}
