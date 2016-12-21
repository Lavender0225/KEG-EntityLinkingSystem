package edu.tsinghua.el.model;

import java.util.HashSet;

public class Mention {
	
	private String label;
    private int pos_start;
    private int pos_end;
    private String prev_context;
    private String after_context;
    private String uris;
    private String result_entity_id;
    private HashSet<String> context_entity;

    public Mention(){
    	context_entity = new HashSet<String>();
    }

    public String getLabel(){
        return this.label;
    }

	public int getPos_start() {
		return pos_start;
	}

	public void setPos_start(int pos_start) {
		this.pos_start = pos_start;
	}

	public int getPos_end() {
		return pos_end;
	}

	public void setPos_end(int pos_end) {
		this.pos_end = pos_end;
	}

	public String getPrev_context() {
		return prev_context;
	}

	public void setPrev_context(String prev_context) {
		this.prev_context = prev_context;
	}

	public String getAfter_context() {
		return after_context;
	}

	public void setAfter_context(String after_context) {
		this.after_context = after_context;
	}

	public String getUris() {
		return uris;
	}

	public void setUris(String uris) {
		this.uris = uris;
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

	public HashSet<String> getContext_entity() {
		return context_entity;
	}

	public void setContext_entity(HashSet<String> context_entity) {
		this.context_entity = context_entity;
	}

	@Override
	public String toString() {
		return "Mention [label=" + label 
				+ ", result_entity_id=" + result_entity_id
				+ ", pos_start=" + pos_start + ", pos_end=" + pos_end
				+ ", context_entity=" + context_entity
				+ ", prev_context=" + prev_context + ", after_context=" + after_context 
				+ ", uris=" + uris 
				+ ", result_entity_id=" + result_entity_id 
				
				+ "]";
	}

	
    
}
