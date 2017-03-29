package edu.tsinghua.el.model;

public class FormData {
	private String text = "";
	private String index_choose = "";
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getIndex_choose() {
		return index_choose;
	}
	public void setIndex_choose(String index_choose) {
		this.index_choose = index_choose;
	}
	@Override
	public String toString() {
		return "FormData [text=" + text + ", index_choose=" + index_choose
				+ "]";
	}
	

}
