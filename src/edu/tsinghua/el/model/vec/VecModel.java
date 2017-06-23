package edu.tsinghua.el.model.vec;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class VecModel {
	private int words;
	private int size;
	private static final int MAX_SIZE = 50;
	private static final Logger logger = LogManager.getLogger(VecModel.class);
	
	protected String wordVecpath;
	protected String entityVecPath;

	
	//************ define some common operations *********************//
	/**
	 * calculate cosine similarity of two vectors
	 * @param a
	 * @param b
	 * @return
	 */
	public double cosineSimilarity(float[] a, float[] b){
		float dist = 0;
		float modulo_a = 0;
		float modulo_b = 0;
		for (int i = 0; i < a.length; i ++){
			dist += a[i]*b[i];
			modulo_a += a[i]*a[i];
			modulo_b += b[i]*b[i];
		}
		return dist/java.lang.Math.sqrt(modulo_a*modulo_b);
	}
	
	public abstract float[] getWordVec(String word);
	public abstract float[] getEntityVec(String entity);
	public abstract boolean containsWord(String word);
	public abstract boolean containsEntity(String entity);
	
	public int getSize(){
		return size;
	}
	
	
	/**
	 * Load Binary Vector Model
	 * @return
	 */
	public HashMap<String, float[]> loadBinaryVecModel(String path){
		HashMap<String, float[]> wordMap = new HashMap<String, float[]>();
		DataInputStream dis = null;
		BufferedInputStream bis = null;
		//BufferedReader bis = null;
		double len = 0;
		float vector = 0;
		try {
			long start = System.currentTimeMillis();
			bis = new BufferedInputStream(new FileInputStream(path));
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
				long end = System.currentTimeMillis();
				logger.info("Loading model " + path +  " success...size: "+wordMap.size());
				
				//System.out.println("Loading success...size: "+wordMap.size()+", time: " + (double)(end - start)/1000);
			}
		}
		catch(IOException e){
			e.printStackTrace();
			logger.info("IOEception, path:"+path);
		}
		finally {
			if(bis != null && dis != null){
				try {
					bis.close();
					dis.close();
				} catch (IOException e) {
					logger.info(e.getMessage());
				}
				
			}
		}
		return wordMap;
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
}
