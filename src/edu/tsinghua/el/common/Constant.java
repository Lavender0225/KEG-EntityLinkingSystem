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
    
    public static final String xloreEntityPrefix = "http://xlore.org/instance/";
    public static final String baiduEntityPrefix = "http://baike.baidu.com";
    public static final String wikiEntityPrefix = "https://en.wikipedia.org/wiki/";
    public static int mention_context_window_zh = 50;
    public static int mention_context_window_en = 250;
    
    //public static String popularity_path = "./etc/entity/popularity.txt";
    public static String commonness_path = "/home/zj/EntityLinkingWeb/etc/entity/popularity_.txt";
    public static String commonness_ready_path = "/home/zj/EntityLinkingWeb/etc/entity/commonnessReadyForInput";
    
    //baidu input files
    public static String baiduDataPrefix = "/home/zj/EntityLinkingWeb/data/baidu/";
    public static String baiduEntityPriorFile = baiduDataPrefix + "prob/baidu_entity_prior.dat";
    public static String baiduMGivenEProbFile = baiduDataPrefix + "prob/prob_mention_entity.dat";
    public static String baiduLinkProbFile = baiduDataPrefix + "prob/link_prob.dat";
    public static String baiduDictionaryFile = baiduDataPrefix + "dic/dictionary_baidu.dat";
    public static String baiduEntityVec = baiduDataPrefix + "vec_model/vectors_entity";
    public static String baiduWordVec = baiduDataPrefix + "vec_model/vectors_word";
    public static String baiduTrie = baiduDataPrefix + "trie/baidu.trie";
    
    //wiki input files
    public static String wikiDataPrefix = "/home/zj/EntityLinkingWeb/data/wiki/";
    public static String wikiEntityPriorFile = wikiDataPrefix + "prob/wiki_entity_prior.dat";
    public static String wikiMGivenEProbFile = wikiDataPrefix + "prob/prob_mention_entity.dat";
    public static String wikiLinkProbFile = wikiDataPrefix + "prob/link_prob.dat";
    public static String wikiDictionaryFile = wikiDataPrefix + "dic/dictionary_wiki.dat";
    public static String wikiEntityVec = wikiDataPrefix + "vec_model/vectors_entity";
    public static String wikiWordVec = wikiDataPrefix + "vec_model/vectors_word";
    public static String wikiTrie = wikiDataPrefix + "trie/wiki.trie";
    public static String wikiURLMap = wikiDataPrefix + "prob/wiki_url_map.dat";
    public static String wikiIdMap = wikiDataPrefix + "dic/vocab_entity.dat";
    // xlore linkprob file path
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
