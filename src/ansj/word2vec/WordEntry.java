package ansj.word2vec;

public class WordEntry {
	public String name;
    public float score;

    public WordEntry(String name, float score) {
        this.name = name;
        this.score = score;
    }

    public String toString() {
        return this.name + "\t" + score;
    }

    public int compareTo(WordEntry o) {
        if (this.score < o.score) {
            return 1;
        } else {
            return -1;
        }
    }
}
