package edu.tsinghua.el.common;

/**
 * 配置文件读取类
 * 
 * @author Jing Zhang
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesReader {
	private static Properties prop;
	private static HashMap<String, String> domainIndexMap =null;
	private static HashMap<String, String> vecPathMap = null;
	private PropertiesReader(){
	}
	private static void getProperties(){
        InputStream inStream;
		try {
			domainIndexMap = new HashMap<String, String>();
			vecPathMap = new HashMap<String, String>();
			inStream = new BufferedInputStream(new FileInputStream("/home/zj/EntityLinkingWeb/src/config.properties"));  //获取配置文件输入流
			//inStream = new BufferedInputStream(new FileInputStream("E://KEG//实体链接//EntityLinkingWeb//src//config.properties"));  //for local test
			prop.load(inStream);//载入输入流

            Enumeration<?> enumeration=prop.propertyNames();//取得配置文件里所有的key值
            
            while(enumeration.hasMoreElements()){
                String key=(String) enumeration.nextElement();
                String type = key.split("\\.")[0];
                String name = key.split("\\.")[1];
                
                if(type.contentEquals("domain")){
                	domainIndexMap.put(name, prop.getProperty(key));
                	//logger.info(name + ": " + prop.getProperty(key));
                }
                else if(type.contentEquals("vec_path")){
                	if(name.contentEquals("baidu_entity"))
                		vecPathMap.put("baidu_entity", prop.getProperty(key));
                	if(name.contentEquals("baidu_word"))
                		vecPathMap.put("baidu_word", prop.getProperty(key));
                	if(name.contentEquals("wiki_word"))
                		vecPathMap.put("wiki_word", prop.getProperty(key));
                	if(name.contentEquals("wiki_entity"))
                		vecPathMap.put("wiki_entity", prop.getProperty(key));
                }
                //System.out.println("配置文件里的key值："+key+"=====>配置文件里的value值："+prop.getProperty(key));//输出key值
            }
		} catch (IOException e1) {
			e1.printStackTrace();
		}

    }
	public static HashMap<String, String> getDomainIndexMap() {
		if (domainIndexMap == null){
			prop = new Properties();//获取Properties实例
			getProperties();
		}
		return domainIndexMap;
	}
	
	public static HashMap<String, String> getPathMap(){
		if(vecPathMap == null){
			prop = new Properties();
			getProperties();
		}
		return vecPathMap;
		
	}

}