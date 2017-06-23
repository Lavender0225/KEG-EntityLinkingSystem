 package edu.tsinghua.el.model;

public class Score implements Comparable<Score>{
	int word_length = 0;
	double link_prob = 0;
	int freq = 0;
	public Score(int word_length, double link_prob, int freq) {
		super();
		this.word_length = word_length;
		this.link_prob = link_prob;
		this.freq = freq;
	}
	@Override
	public int compareTo(Score o) {
		int len_cmp = word_length - o.word_length;
		int link_cmp = link_prob > o.link_prob ? 1 : -1;
		if(link_prob == o.link_prob)
			link_cmp = 0;
		if(len_cmp == 0){
			if(link_cmp == 0)
				return freq - o.freq;
			return link_cmp;
		}
		return len_cmp;
	}

}
