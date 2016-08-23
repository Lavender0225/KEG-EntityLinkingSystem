package Common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Constant {
	//public static String entity_original_path = "./etc/entity/xlore.instance.mention.ttl";	// xlore's entity list
	public static String entity_original_path = "./etc/entity_filter//entities_freq";	// xlore's entity list
	public static String entity_formatted_path = "./etc/entity/xlore.formatted.entities";  	// formatted entity list
    public static String entity_formatted_sorted = "./etc/entity/xlore.sorted.entities";	// sorted entity after formatting					
    public static String entity_ready_file = "./etc/entity/ready.forInput.entities";		// filtered entity after sorting, is ready to input to adbc
    public static String entity_trie =  "./etc/trie/entities_freq.trie";
    //public static String entity_trie =  "./etc/entity/entities_freq_trie";
    
    public static String news_path = "/etc/news/news1";		
    
    public static String concept_path = "./etc/entity/xlore.concept.list.ttl";//concept list file
  	public static String taxonomy_path = "./etc/entity/xlore.taxonomy.ttl";//taxonomy file
  	public static String entityCountInput = "./etc/entity/Mention_Entity_Count.dat";//Count file
  	
  	public static String freqConceptPath = "./etc/concept/concepts_freq";//freq_concepts
  	public static String freqLabelInput = "./etc/label/labels_freq";//freq_labels
  	
  	public static String freqEntityPath = "./etc/entity_filter/entities_freq";   //freq_entities
  	public static String commonEntityPath = "./etc/entity_filter/entities_common";//common_entities
    
    public static int entity_min_length = 2;
    
    public static final String xlore_entity_prefix = "http://xlore.org/instance/";
    
    
    
    
}
