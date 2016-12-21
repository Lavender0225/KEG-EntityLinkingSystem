package edu.tsinghua.el.model;

public abstract class AbstractEntity implements Comparable<AbstractEntity>{
	protected String title;
	protected String id;
	
	
	public AbstractEntity(String uri){
		this.id = uri;
	}
	
	public String getTitle() {
        return this.title;
    }
    public void setTitle(String label) {
        this.title = label;
    }
    
    public String getId() {
        return this.id;
    }
    public void setId(String uri) {
        this.id = uri;
    }
    
    public int compareTo(AbstractEntity arg0) {
        return this.getTitle().compareTo(arg0.getTitle());
    }
}
