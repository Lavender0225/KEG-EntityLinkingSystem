package edu.tsinghua.el.model;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class Position implements Comparable<Position> {
	public int begin = 0;
	public int end = 0;
	
	public Position(int b, int e){
		this.begin = b;
		this.end = e;
	}

	@Override
	public int compareTo(Position o) {
		int b_cmp = begin - o.begin;
		return b_cmp == 0 ? end - o.end : b_cmp;
	}

	@Override
	public String toString() {
		return "Position [begin=" + begin + ", end=" + end + "]";
	}
	
	@Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            // if deriving: appendSuper(super.hashCode()).
            append(begin).
            append(end).
            toHashCode();
    }
	
	@Override
    public boolean equals(Object obj){
        if(obj == null || !Position.class.isAssignableFrom(obj.getClass()))
            return false;
        Position o = (Position) obj;
        return this.begin == o.begin && this.end == o.end;
    }

}
