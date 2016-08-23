package Model;

import java.util.ArrayList;
import java.util.HashMap;

import Common.Constant;

public class Entity extends AbstractEntity{
	private String uri;
	private ArrayList<String> alias;
	private String label_zh;
	private String label_en;
	private String abstract_en;
	private String abstract_zh;
	private String desc;
	private ArrayList<String> super_classes_en;
	private ArrayList<String> super_classes_zh;
	private ArrayList<String> related_entites_zh;
	private ArrayList<String> related_entites_en;
	private HashMap<String, String> infobox_zh;	// key: property, value: property-value
	private HashMap<String, String> infobox_en;
	
	public Entity(String id) {
		super(id);
		title = "";
		alias = new ArrayList<String>();
		uri = Constant.xlore_entity_prefix + id;
		label_zh = "";
		label_en = "";
		abstract_en = "";
		abstract_zh = "";
		super_classes_en = new ArrayList<String>();
		super_classes_zh = new ArrayList<String>();
		related_entites_zh = new ArrayList<String>();
		related_entites_en = new ArrayList<String>();
		infobox_zh = new HashMap<String, String>();
		setInfobox_en(new HashMap<String, String>());
		
	}
	


	public Entity(String id, String uri, String label_zh, String label_en, ArrayList<String> alias, 
			String abstract_en, String abstract_zh, ArrayList<String> super_classes_en, ArrayList<String> super_classes_zh,
			ArrayList<String> related_entites_zh, ArrayList<String> related_entites_en,
			HashMap<String, String> infobox_zh, HashMap<String, String> infobox_en) {
		super(id);
		this.uri = uri;
		this.label_zh = label_zh;
		this.label_en = label_en;
		this.alias = alias;
		if(!label_en.isEmpty()){
			this.title = label_en;
			this.alias.add(label_en);
		}
		if(!label_zh.isEmpty()){
			this.title = label_zh;
			this.alias.add(label_zh);
		}
		this.abstract_en = abstract_en;
		this.abstract_zh = abstract_zh;
		this.super_classes_en = super_classes_en;
		this.super_classes_zh = super_classes_zh;
		this.related_entites_zh = related_entites_zh;
		this.related_entites_en = related_entites_en;
		this.infobox_zh = infobox_zh;
		this.setInfobox_en(infobox_en);
	}



	public Entity(String label, String id) {
		super(id);
		title = label;
	}


	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public ArrayList<String> getAlias() {
		return alias;
	}

	public void setAlias(ArrayList<String> alias) {
		this.alias = alias;
	}
	
	public void addAlias(String a){
		this.alias.add(a);
	}

	public String getAbstract_en() {
		return abstract_en;
	}

	public void setAbstract_en(String abstract_en) {
		this.abstract_en = abstract_en;
	}

	public String getAbstract_zh() {
		return abstract_zh;
	}

	public void setAbstract_zh(String abstract_zh) {
		this.abstract_zh = abstract_zh;
	}

	public ArrayList<String> getsuper_classes_en() {
		return super_classes_en;
	}

	public void setsuper_classes_en(ArrayList<String> super_classes_en) {
		this.super_classes_en = super_classes_en;
	}

	public ArrayList<String> getsuper_classes_zh() {
		return super_classes_zh;
	}

	public void setsuper_classes_zh(ArrayList<String> super_classes_zh) {
		this.super_classes_zh = super_classes_zh;
	}


	public String getLabel_zh() {
		return label_zh;
	}


	public void setLabel_zh(String title_zh) {
		this.label_zh = title_zh;
	}


	public String getLabel_en() {
		return label_en;
	}


	public void setLabel_en(String title_en) {
		this.label_en = title_en;
	}
	
	

	public String getDesc() {
		return desc;
	}


	public void setDesc(String desc) {
		this.desc = desc;
	}


	public ArrayList<String> getRelated_entites_zh() {
		return related_entites_zh;
	}


	public void setRelated_entites_zh(ArrayList<String> related_entites_zh) {
		this.related_entites_zh = related_entites_zh;
	}


	public ArrayList<String> getRelated_entites_en() {
		return related_entites_en;
	}


	public void setRelated_entites_en(ArrayList<String> related_entites_en) {
		this.related_entites_en = related_entites_en;
	}
	public HashMap<String, String> getInfobox_en() {
		return infobox_en;
	}

	public void setInfobox_en(HashMap<String, String> infobox_en) {
		this.infobox_en = infobox_en;
	}

	public HashMap<String, String> getInfobox_zh() {
		return infobox_zh;
	}

	public void setInfobox_zh(HashMap<String, String> infobox_zh) {
		this.infobox_zh = infobox_zh;
	}
	

	@Override
	public String toString() {
		return "Entity [uri=" + uri + ", alias=" + alias + ", label_zh=" + label_zh + ", label_en=" + label_en
				+ ", abstract_en=" + abstract_en + ", abstract_zh=" + abstract_zh + ", desc=" + desc + ", super_classes_en="
				+ super_classes_en + ", super_classes_zh=" + super_classes_zh + ", related_entites_zh=" + related_entites_zh
				+ ", related_entites_en=" + related_entites_en + ", infobox_zh=" + infobox_zh + ", infobox_en=" + infobox_en
				+ ", title=" + title + ", id="
				+ id + "]";
	}



	
}
