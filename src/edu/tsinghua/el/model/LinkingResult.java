package edu.tsinghua.el.model;

public class LinkingResult {
	private int start_index;
	private int end_index;
	private String label;
	private String entity_id;
	private String url;
	private double cohenrence_score;
	private double popularity_score;
	
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
	public double getCohenrence_score() {
		return cohenrence_score;
	}
	public void setCohenrence_score(double cohenrence_score) {
		this.cohenrence_score = cohenrence_score;
	}
	public double getPopularity_score() {
		return popularity_score;
	}
	public void setPopularity_score(double popularity_score) {
		this.popularity_score = popularity_score;
	}
	@Override
	public String toString() {
		return "LinkingResult [start_index=" + start_index + ", end_index="
				+ end_index + ", label=" + label + ", entity_id=" + entity_id
				+ ", url=" + url + ", cohenrence_score=" + cohenrence_score
				+ ", popularity_score=" + popularity_score + "]";
	}
	
	
	
	
	

}
