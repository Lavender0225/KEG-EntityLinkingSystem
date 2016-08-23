package CandidateRanking;

import java.util.ArrayList;
import java.util.HashSet;

import Model.Entity;
import Model.Mention;

/**
 * Calculate string similarities
 * 
 * @author Jingzhang
 *
 */
public class FeatureCal {
	/**
	 * calculate the minimun edit distance of entity's aliases and mention'slabel
	 * @param alias
	 * @param mention_label
	 * @return
	 */
	public static double editDistanceOfEntityAndMention(ArrayList<String> alias, String mention_label){
		double min_score = 1;
		double tmp_score = 0;
		for(String a : alias){
			tmp_score = editDistanceOfTwoString(a, mention_label);
			if(tmp_score < min_score){
				min_score = tmp_score;
			}
		}
		return min_score;
	}
	
	/**
	 * return the edit distence of two strings
	 * 
	 * @param word1
	 * @param word2
	 * @return
	 */
	public static double editDistanceOfTwoString(String word1, String word2){
		int len1 = word1.length();
		int len2 = word2.length();
	 
		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];
	 
		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}
	 
		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}
	 
		//iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = word1.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = word2.charAt(j);
	 
				//if last two chars equal
				if (c1 == c2) {
					//update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;
	 
					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}
		if(len1 > len2)
			return (double)dp[len1][len2]/len1;	
		return (double)dp[len1][len2]/len2;
	}
	/**
	 * return ture if the one of the entity's aliases exactly equals the surface of mention 
	 * 
	 * @param entity_title
	 * @param mention
	 * @return
	 */
	public static double surfaceEqual(ArrayList<String> alias, String mention){
		for(String entity_title : alias){
			if(entity_title.contentEquals(mention))
				return 1.0;
		}
		return 0.0;
	}
	/**
	 * return ture if the one of the entity's aliases contains the surface of mention 
	 * 
	 * @param alias
	 * @param mention
	 * @return
	 */
	public static double surfaceContains(ArrayList<String> alias, String mention){
		for(String entity_title : alias){
			if(entity_title.contains(mention))
				return 1.0;
		}
		return 0.0;
	}
	
	/**
	 * return true if the one of the entity's aliases starts with mention 
	 * or ends with mention
	 * @param entity_title
	 * @param mention
	 * @return
	 */
	public static double startwithOrEndwith(ArrayList<String> alias, String mention){
		for(String entity_title : alias){
			if(entity_title.startsWith(mention) || entity_title.endsWith(mention))
				return 1.0;
		}
		return 0.0;
	}
	
	/**
	 * return the max Jaccard Similarity between desc and superclasses
	 * @param superclasses
	 * @param desc, the description of a mention, assuming the form is mention_label(desc)
	 * @return
	 */
	public static double simOfDescAndType(Entity entity, String desc){
		double max_score = 0;
		double tmp_score = 0;
		if(isEnglish(desc)){
			for(String superclass : entity.getsuper_classes_en()){
				tmp_score = JaccardSim(superclass, desc);
				if(tmp_score > max_score){
					max_score = tmp_score;
				}
			}
		}
		else{
			for(String superclass : entity.getsuper_classes_zh()){
				tmp_score = JaccardSim(superclass, desc);
				if(tmp_score > max_score){
					max_score = tmp_score;
				}
			}
		}
		
		tmp_score = JaccardSim(entity.getDesc(), desc);
		if(tmp_score > max_score){
			max_score = tmp_score;
		}
		return max_score;
	}
	
	private static boolean isEnglish(String word){
		if(word.matches("[a-zA-Z0-9 ·\t,\\(\\)\\[\\]!.]+")){
			return true;
		}
		return false;
	}
	
	
	public static double JaccardSim(String a, String b){
		float sim = 0;
		a = a.toLowerCase();
		b = b.toLowerCase();
		HashSet<Character> h1 = new HashSet<Character>(), h2 = new HashSet<Character>();
		for(int i = 0; i < a.length(); i++)                                            
		{
		  h1.add(a.charAt(i));
		}
		for(int i = 0; i < b.length(); i++)
		{
		  h2.add(b.charAt(i));
		}
		h1.retainAll(h2);
		//System.out.println(h1+",len:"+h1.size());
		Character[] intersection = h1.toArray(new Character[0]);
		sim = (float)intersection.length/(a.length() + b.length() - intersection.length);
		//System.out.println(sim);
		return sim;
	}
	/**
	 * calculate the context entity's similarity between entity and mention
	 * @param entity
	 * @param mention
	 * @return
	 */
	public static double simContextEntity(Entity entity, Mention mention){
		HashSet<String> entity_context = new HashSet<String>();
		entity_context.addAll(entity.getRelated_entites_en());
		entity_context.addAll(entity.getsuper_classes_en());
		int len_entity = entity_context.size();
		entity_context.retainAll(mention.getContext_entity());
		double score_en =  (double)entity_context.size()/(len_entity + mention.getContext_entity().size() - entity_context.size());
		entity_context.clear();
		
		entity_context.addAll(entity.getRelated_entites_zh());
		entity_context.addAll(entity.getsuper_classes_zh());
		int len_entity_2 = entity_context.size();
		entity_context.retainAll(mention.getContext_entity());
		double score_zh = (double)entity_context.size()/(len_entity_2 + mention.getContext_entity().size() - entity_context.size());
		if(score_zh > score_en)
			return score_zh;
		return score_en;
	}

	
	public static void main(String[] args) {
		System.out.println(FeatureCal.editDistanceOfTwoString("中华人民共和国", "中华人民共和国"));
		System.out.println(FeatureCal.editDistanceOfTwoString("中华人民共和国", "中华人民"));
		System.out.println(FeatureCal.editDistanceOfTwoString("中华人民共和国", "美国"));
		//System.out.println(FeatureCal.matchesOrContains("中华人民共和国", "中华人民共和国"));
		//System.out.println(FeatureCal.startwithOrEndwith("中华人民共和国", "共和国"));
	}
	

}
