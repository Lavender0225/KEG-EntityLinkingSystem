package edu.tsinghua.el.common;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesReader {
	private Properties prop;
	private static String triePath = "";
	public PropertiesReader(){
		prop = new Properties();//获取Properties实例
		getProperties();
	}
	private void getProperties(){
        InputStream inStream;
		try {
			inStream = new BufferedInputStream(new FileInputStream("/home/zj/EntityLinkingWeb/src/config.properties"));  //获取配置文件输入流
			 prop.load(inStream);//载入输入流
            Enumeration<?> enumeration=prop.propertyNames();//取得配置文件里所有的key值

            while(enumeration.hasMoreElements()){
                String key=(String) enumeration.nextElement();
                if(key.contentEquals("trie")){
                	triePath = prop.getProperty(key);
                }
                //System.out.println("配置文件里的key值："+key+"=====>配置文件里的value值："+prop.getProperty(key));//输出key值
            }
		} catch (IOException e1) {
			e1.printStackTrace();
		}

    }
	
	
	public static String getTriePath() {
		return triePath;
	}

}