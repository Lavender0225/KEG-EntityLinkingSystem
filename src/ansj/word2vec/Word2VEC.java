package ansj.word2vec;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import edu.tsinghua.el.common.Constant;


/**
 * word2vec模型读取及相似度计算
 * 
 * @author ANSJ, Jing Zhang
 * @update 2016/3/29
 */
public class Word2VEC {
	private static final Logger logger = LogManager.getLogger("Word2VEC");
	public static String words_model_path = Constant.words_model_path;
	public static String entity_model_path = Constant.entity_model_path;
	public static String baidu_xlore_map_path = Constant.baidu_xlore_map_path;
	public static HashMap<String, String> baiduXloreMap = new HashMap<String, String>();	//key:xlore_instance_id, value:baidu_id
	private HashMap<String, float[]> wordMap = null;
	private HashMap<String, float[]> entityMap = null;
	private int words;
	private int size;
	private int topNSize = 40;
	
	//使用内部静态类实现单例模式
	private static class Word2VECHolder{
		private static final Word2VEC instance = new Word2VEC();
	}
	
	private Word2VEC(){
		try {
			//wordMap = this.loadGoogleModel(words_model_path);
			loadBaiduXloreMap();
			entityMap = this.loadGoogleModel(entity_model_path);
			//logger.info(this.similarityOfWords("Harry Potter", "Hogwarts"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static Word2VEC getInstance(){
		return Word2VECHolder.instance;
	}

	
	/**
	 * 判断文件的格式
	 * @param path
	 * @return
	 */
	public static String getFileEncode(String path) {
        String charset ="asci";
        byte[] first3Bytes = new byte[3];
        BufferedInputStream bis = null;
        try {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(path));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "Unicode";//UTF-16LE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE && first3Bytes[1] == (byte) 0xFF) {
                charset = "Unicode";//UTF-16BE
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF && first3Bytes[1] == (byte) 0xBB && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF8";
                logger.info(charset);
                checked = true;
            }
            bis.reset();
            if (!checked) {
                int len = 0;
                int loc = 0;
                while ((read = bis.read()) != -1) {
                    loc++;
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) //单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) 
                        //双字节 (0xC0 - 0xDF) (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) { //也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
                //TextLogger.getLogger().info(loc + " " + Integer.toHexString(read));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException ex) {
                }
            }
        }
        return charset;
    }
 
    /**
	 * 加载模型, txt格式， 编码：utf-8
	 * 
	 * @param path
	 *            模型的路径
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	public HashMap<String, float[]> loadGoogleModelTXT(String path) throws IOException {
		HashMap<String, float[]> wordMap = new HashMap<String, float[]>();
		logger.info("file encode:"+getFileEncode(path));
		BufferedReader bisr = null;
		double len = 0;
		try{
			bisr = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
			String line = null;
			String[] firstline = bisr.readLine().split(" ");
			words = Integer.parseInt(firstline[0]);
			size = Integer.parseInt(firstline[1]);
			while((line = bisr.readLine()) != null){
				String [] tempList = line.split(" ");
				String word = tempList[0];
				float[] floatList = new float[size];
				len = 0;
				for(int i = 1; i <= size; i ++){
					float vector = Float.parseFloat(tempList[i]);
					floatList[i-1] = vector;
					len += vector*vector;
				}
				len = Math.sqrt(len);
				for (int i = 0; i < size; i ++){
					floatList[i] /= len;
				}
				wordMap.put(word, floatList);
			}
			if(wordMap.isEmpty()){
				logger.info("Loading fail...");
			}
			else
				logger.info("Loading file "+path+" success...size: "+wordMap.size());
			
		}catch(IOException e){
			e.printStackTrace();
		}
		return wordMap;
	}
	
	/**
	 * 加载二进制模型
	 * 
	 * @param path
	 * @throws IOException
	 */
	public HashMap<String, float[]> loadGoogleModel(String path) throws IOException {
		HashMap<String, float[]> wordMap = new HashMap<String, float[]>();
		DataInputStream dis = null;
		BufferedInputStream bis = null;
		//BufferedReader bis = null;
		double len = 0;
		float vector = 0;
		try {
			long start = System.currentTimeMillis();
			bis = new BufferedInputStream(new FileInputStream(path));
			logger.info("file encode:"+getFileEncode(path));
			dis = new DataInputStream(bis);
			//dis.read(new byte[3], 0, 3);
			// //读取词数
			System.out.println("File:"+path);
			words = Integer.parseInt(new String(readStringWithoutBlank(dis).getBytes(), "ASCII"));
			logger.info("#words:"+words);
			//words = Integer.parseInt(readString(dis));
			// //大小
			size = Integer.parseInt(new String(readStringWithoutBlank(dis).getBytes(), "ASCII"));
			logger.info("#size:"+size);
			//size = Integer.parseInt(readString(dis));
			String word;
			float[] vectors = null;
			for (int i = 0; i < words; i++) {
				word = new String(readStringWithBlank(dis).getBytes(), "UTF8");
				//word = new String(readString(dis));
				
				vectors = new float[size];
				len = 0;
				for (int j = 0; j < size; j++) {
					vector = readFloat(dis);
					len += vector * vector;
					vectors[j] = (float) vector;
				}
				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					vectors[j] /= len;
				}

				wordMap.put(word, vectors);
				dis.read();
			}
			if(wordMap.isEmpty()){
				logger.info("Loading fail...");
			}
			else{
				logger.info("Loading vec model success...size: "+wordMap.size());
				long end = System.currentTimeMillis();
				System.out.println("Loading success...size: "+wordMap.size()+", time: " + (double)(end - start)/1000);
			}
		}
		catch(IOException e){
			e.printStackTrace();
			logger.info("IOEception, sysdir:"+System.getProperty("user.dir"));
		}
		finally {
			if(bis != null && dis != null){
				bis.close();
				dis.close();
			}
		}
		return wordMap;
	}

	/**
	 * 加载模型
	 * 
	 * @param path
	 *            模型的路径
	 * @throws IOException
	 */
	public void loadJavaModel(String path) throws IOException {
		try {
			@SuppressWarnings("resource")
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)));
			words = dis.readInt();
			size = dis.readInt();

			float vector = 0;

			String key = null;
			float[] value = null;
			for (int i = 0; i < words; i++) {
				double len = 0;
				key = dis.readUTF();
				value = new float[size];
				for (int j = 0; j < size; j++) {
					vector = dis.readFloat();
					len += vector * vector;
					value[j] = vector;
				}

				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					value[j] /= len;
				}
				wordMap.put(key, value);
			}

		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private static final int MAX_SIZE = 50;

	/**
	 * 近义词
	 * 
	 * @return
	 */
	public TreeSet<WordEntry> analogy(String word0, String word1, String word2) {
		float[] wv0 = getWordVector(word0);
		float[] wv1 = getWordVector(word1);
		float[] wv2 = getWordVector(word2);

		if (wv1 == null || wv2 == null || wv0 == null) {
			return null;
		}
		float[] wordVector = new float[size];
		for (int i = 0; i < size; i++) {
			wordVector[i] = wv1[i] - wv0[i] + wv2[i];
		}
		float[] tempVector;
		String name;
		List<WordEntry> wordEntrys = new ArrayList<WordEntry>(topNSize);
		for (Entry<String, float[]> entry : wordMap.entrySet()) {
			name = entry.getKey();
			if (name.equals(word0) || name.equals(word1) || name.equals(word2)) {
				continue;
			}
			float dist = 0;
			tempVector = entry.getValue();
			for (int i = 0; i < wordVector.length; i++) {
				dist += wordVector[i] * tempVector[i];
			}
			insertTopN(name, dist, wordEntrys);
		}
		return new TreeSet<WordEntry>(wordEntrys);
	}

	private void insertTopN(String name, float score, List<WordEntry> wordsEntrys) {
		// TODO Auto-generated method stub
		if (wordsEntrys.size() < topNSize) {
			wordsEntrys.add(new WordEntry(name, score));
			return;
		}
		float min = Float.MAX_VALUE;
		int minOffe = 0;
		for (int i = 0; i < topNSize; i++) {
			WordEntry wordEntry = wordsEntrys.get(i);
			if (min > wordEntry.score) {
				min = wordEntry.score;
				minOffe = i;
			}
		}

		if (score > min) {
			wordsEntrys.set(minOffe, new WordEntry(name, score));
		}

	}

	public Set<WordEntry> distance(String queryWord) {

		float[] center = wordMap.get(queryWord);
		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}
	
	public float similarity(String s1, String s2){
		float score = 0;
		float avg = 0;
		float tmp = 0;
		tmp = similarityOfWords(s1, s2);
		if ( tmp != 0){
			score += tmp;
			avg += 1;
		}
		tmp = similarityOfEntity(s1, s2);
		if ( tmp != 0){
			score += tmp;
			avg += 1;
		}
		tmp = similarityOfEntityAndWord(s1, s2);
		if ( tmp != 0){
			score += tmp;
			avg += 1;
		}
		tmp = similarityOfEntityAndWord(s2, s1);
		if ( tmp != 0){
			score += tmp;
			avg += 1;
		}
		return score/avg;
	}
	/**
	 * get the similarity of two words
	 * @param word1
	 * @param word2
	 * @return
	 */
	public float similarityOfWords(String word1, String word2){
		if(wordMap.containsKey(word1) && wordMap.containsKey(word2)){
			float[] s1 = this.wordMap.get(word1);
			float[] s2 = this.wordMap.get(word2);
			
			float dist = 0;
			for (int i = 0; i < s1.length; i ++)
				dist += s1[i]*s2[i];
			System.out.println("Get the similarity betweent two words: "+word1+" "+word2+" "+dist);
			return dist;
		}
		System.out.println("One of the words or both is not in the trained model"+word1+" "+word2);
		return 0;
	}
	
	/**
	 * get the similarity of two entities
	 * @param entity1
	 * @param entity2
	 * @return
	 */
	
	public float similarityOfEntity(String entity1, String entity2){
		
		if(entityMap.containsKey(entity1) && entityMap.containsKey(entity2)){
			float[] s1 = this.entityMap.get(entity1);
			float[] s2 = this.entityMap.get(entity2);
			
			float dist = 0;
			for (int i = 0; i < s1.length; i ++)
				dist += s1[i]*s2[i];
			System.out.println("Get the similarity betweent two entities: "+entity1+" "+entity2+" "+dist);
			return dist;
		}
		System.out.println("One of the words or both is not in the trained model"+entity1+" "+entity2);
		return 0;
	}
	
	/**
	 * get the similarity of a entity and a word
	 * @param entity
	 * @param word
	 * @return
	 */
	public float similarityOfEntityAndWord(String entity, String word){
		if(entityMap.containsKey(entity) && wordMap.containsKey(word)){
			float[] s1 = this.entityMap.get(entity);
			float[] s2 = this.wordMap.get(word);
			
			float dist = 0;
			for (int i = 0; i < s1.length; i ++)
				dist += s1[i]*s2[i];
			System.out.println("Get the similarity betweent entity and word: "+entity+" "+word+" "+dist);
			return dist;
		}
		System.out.println("One of the words or both is not in the trained model"+entity+" "+word);
		return 0;
	}
	
	/**
	 * get similarity of two baidu entity
	 * @param two entity id
	 * @return
	 */
	public float similarityOfBaiduEntity(String id1, String id2){
		if(baiduXloreMap.containsKey(id2) && baiduXloreMap.containsKey(id1)){
			String baidu_id1 = baiduXloreMap.get(id1);
			baidu_id1 = (String) baidu_id1.subSequence(Constant.baidu_entity_prefix.length(), baidu_id1.length());
			String baidu_id2 = baiduXloreMap.get(id2);
			baidu_id2 = (String) baidu_id2.subSequence(Constant.baidu_entity_prefix.length(), baidu_id2.length());
			if(entityMap.containsKey(baidu_id1) && entityMap.containsKey(baidu_id2)){
				float[] s1 = this.entityMap.get(baidu_id1);
				float[] s2 = this.entityMap.get(baidu_id2);
				float dist = 0;
				for (int i = 0; i < s1.length; i ++)
					dist += s1[i]*s2[i];
				//logger.info("similarity of " + id1 + " and " + id2 + " is: " + dist);
				return dist;
			}
//			else{
//				if(!entityMap.containsKey(baidu_id1)){
//					logger.info("entity " + baidu_id1 + " is not in entityMap.");
//				}
//				if(!entityMap.containsKey(baidu_id2)){
//					logger.info("entity " + baidu_id2 + " is not in entityMap.");
//				}
//			}
		}
		return 0;
	}
	
	public Set<WordEntry> distance(List<String> words) {

		float[] center = null;
		for (String word : words) {
			center = sum(center, wordMap.get(word));
		}

		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Map.Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}

	private float[] sum(float[] center, float[] fs) {

		if (center == null && fs == null) {
			return null;
		}

		if (fs == null) {
			return center;
		}

		if (center == null) {
			return fs;
		}

		for (int i = 0; i < fs.length; i++) {
			center[i] += fs[i];
		}

		return center;
	}

	/**
	 * 得到词向量
	 * 
	 * @param word
	 * @return
	 */
	public float[] getWordVector(String word) {
		return wordMap.get(word);
	}

	public static float readFloat(InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		is.read(bytes);
		return getFloat(bytes);
	}

	/**
	 * 读取一个float
	 * 
	 * @param b
	 * @return
	 */
	public static float getFloat(byte[] b) {
		int accum = 0;
		accum = accum | (b[0] & 0xff) << 0;
		accum = accum | (b[1] & 0xff) << 8;
		accum = accum | (b[2] & 0xff) << 16;
		accum = accum | (b[3] & 0xff) << 24;
		return Float.intBitsToFloat(accum);
	}

	/**
	 * 读取一个带空格的字符串
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 */
	private static String readStringWithBlank(DataInputStream dis) throws IOException {
		// TODO Auto-generated method stub
		byte[] bytes = new byte[MAX_SIZE];
		byte b = dis.readByte();
		int i = -1;
		StringBuilder sb = new StringBuilder();
		while (b != '\t' && b != '\n') {
			i++;
			bytes[i] = b;
			b = dis.readByte();
			if (i == 49) {
				sb.append(new String(bytes));
				i = -1;
				bytes = new byte[MAX_SIZE];
			}
		}
		sb.append(new String(bytes, 0, i + 1));
		return sb.toString();
	}
	/**
	 * 读取没有空格的字符串
	 * 
	 * @param dis
	 * @return
	 * @throws IOException
	 */
	private static String readStringWithoutBlank(DataInputStream dis) throws IOException {
		// TODO Auto-generated method stub
		byte[] bytes = new byte[MAX_SIZE];
		byte b = dis.readByte();
		int i = -1;
		StringBuilder sb = new StringBuilder();
		while (b != ' ' && b != '\n') {
			i++;
			bytes[i] = b;
			b = dis.readByte();
			if (i == 49) {
				sb.append(new String(bytes));
				i = -1;
				bytes = new byte[MAX_SIZE];
			}
		}
		sb.append(new String(bytes, 0, i + 1));
		return sb.toString();
	}
	/**
	 * 载入baidu-xlore的entity id的对应关系
	 * 存储在baiduXloreMap中，key:xlore_instance_id, value:baidu_id
	 */
	private void loadBaiduXloreMap(){
		InputStream is = null;
		try {
		    is = new FileInputStream(baidu_xlore_map_path);
		    @SuppressWarnings("resource")
		    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 512);
		    String line = null;
		    while((line = reader.readLine())!=null){
		        line = line.trim();
		        String[] tmp = line.split("\t");
		        String xlore_id = tmp[0];
		        String entity_id = tmp[1];
		        baiduXloreMap.put(xlore_id, entity_id);
		    }
		    logger.info("xlore-baidu map has been loaded.");
		}catch (IOException ioe){
		    ioe.printStackTrace();
		} finally {
		    try {
		        if (is != null) {
		            is.close();
		            is = null;
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}  
	}

	public int getTopNSize() {
		return topNSize;
	}

	public void setTopNSize(int topNSize) {
		this.topNSize = topNSize;
	}

	public HashMap<String, float[]> getWordMap() {
		return wordMap;
	}

	public int getWords() {
		return words;
	}

	public int getSize() {
		return size;
	}
	
	public static void main(String[] args){
		Word2VEC w2vec = Word2VEC.getInstance();
		Scanner sc = new Scanner(System.in); 
		while(true){
			System.out.println("Please two words:");
			String line = sc.nextLine();
			String[] word_list = line.split(" ");
			System.out.println(w2vec.similarityOfBaiduEntity(word_list[0], word_list[1]));
		}
	}
}
