 package edu.tsinghua.el.model;

public class Score implements Comparable<Score>{
	int word_length = 0;
	int token_score = 0;
	int freq = 0;
	public Score(int word_length, int token_score, int freq) {
		super();
		this.word_length = word_length;
		this.token_score = token_score;
		this.freq = freq;
	}
	@Override
	public int compareTo(Score o) {
		int len_cmp = word_length - o.word_length;
		int token_cmp = token_score - o.token_score;
		if(len_cmp == 0){
			if(token_cmp == 0)
				return freq - o.freq;
			return token_cmp;
		}
		return len_cmp;
	}

}
