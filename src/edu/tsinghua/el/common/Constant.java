package edu.tsinghua.el.common;

public class Constant {
	public static String entity_original_path = "/home/zj/EntityLinkingWeb/etc/entity/Mention_Entity_Count.dat";	// xlore's entity list
	//public static String entity_original_path = "./etc/entity_filter//entities_freq";	// xlore's entity list
	public static String entity_formatted_path = "/home/zj/EntityLinkingWeb/etc/entity/xlore.formatted.entities";  	// formatted entity list
    public static String entity_formatted_sorted = "/home/zj/EntityLinkingWeb/etc/entity/xlore.sorted.entities";	// sorted entity after formatting					
    public static String entity_ready_file = "/home/zj/EntityLinkingWeb/etc/entity/ready.forInput.entities";		// filtered entity after sorting, is ready to input to adbc
    public static String entity_trie =  "/home/zj/EntityLinkingWeb/etc/trie/entities_freq_203w_9_6_16.trie";
    //public static String entity_trie =  "./etc/entity/entities_freq_trie";
    
    public static String news_path = "/home/zj/EntityLinkingWeb/etc/news/news1";		
    
    public static String concept_path = "/home/zj/EntityLinkingWeb/etc/entity/xlore.concept.list.ttl";//concept list file
  	public static String taxonomy_path = "/home/zj/EntityLinkingWeb/etc/entity/xlore.taxonomy.ttl";//taxonomy file
  	public static String entityCountInput = "/home/zj/EntityLinkingWeb/etc/entity/Mention_Entity_Count.dat";//Count file
  	
  	public static String freqConceptPath = "/home/zj/EntityLinkingWeb/etc/concept/concepts_freq";//freq_concepts
  	public static String freqLabelInput = "/home/zj/EntityLinkingWeb/etc/label/labels_freq";//freq_labels
  	public static String stopConceptPath = "/home/zj/EntityLinkingWeb/etc/concept/concepts_stop";//freq_concepts
  	
  	public static String freqEntityPath = "/home/zj/EntityLinkingWeb/etc/entity_filter/entities_freq";   //freq_entities
  	public static String commonEntityPath = "/home/zj/EntityLinkingWeb/etc/entity_filter/entities_common";//common_entities
    
    public static int entity_min_length = 2;
    
    public static final String xlore_entity_prefix = "http://xlore.org/instance/";
    public static final String baidu_entity_prefix = "http://baike.baidu.com"; 
    public static int mention_context_window = 50;
    
    //public static String popularity_path = "./etc/entity/popularity.txt";
    public static String commonness_path = "/home/zj/EntityLinkingWeb/etc/entity/popularity_.txt";
    public static String commonness_ready_path = "/home/zj/EntityLinkingWeb/etc/entity/commonnessReadyForInput";
    
    // linkprob file path
    public static String linkProbPath = "/home/zj/EntityLinkingWeb/etc/entity/link_prob.dat";
    
    // constants of traditional ranking algorithm
    public static double sigma = 0.4;
    public static double t = 0.02;
    
    
    // path of vector models
    public static String words_model_path = "/home/zj/EntityLinkingWeb/etc/vec_model/vectors_word_baidu";
    public static String entity_model_path = "/home/zj/EntityLinkingWeb/etc/vec_model/vectors_entity_baidu";
    public static String baidu_xlore_map_path = "/home/zj/EntityLinkingWeb/etc/vec_model/baidu-xlore.map";
    
    //wp regex
    public static String wpRegex = "[——\\-_<>《》?？/、，,。\\.‘\'；;：:“\"\\{\\}\\*&#\\^@\\$\\(\\)（）【】\\[\\]\\+!！\n\r]+";
}
