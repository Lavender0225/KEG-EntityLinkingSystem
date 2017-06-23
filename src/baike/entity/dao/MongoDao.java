package baike.entity.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.ServerAddress;

import edu.tsinghua.el.common.Constant;

public class MongoDao {
	private static final Logger logger = LogManager.getLogger(MongoDao.class);
	private static MongoClient mongoClient = null;
	private static MongoDatabase mongoDatabase = null;
	private static MongoCollection<Document> baiduCollection = null;
	public MongoDao(){
		connectDB();
		baiduCollection = getCollection("baidu");
	}
	public void connectDB(){
		try{
			ServerAddress serverAddress = new ServerAddress("10.1.1.68",27017);
			List<ServerAddress> addrs = new ArrayList<ServerAddress>();
			addrs.add(serverAddress);
			MongoCredential credential = MongoCredential.createScramSha1Credential("zj", "EntityLinking", "zhangjing".toCharArray());  
            List<MongoCredential> credentials = new ArrayList<MongoCredential>();  
            credentials.add(credential);  
            
            mongoClient = new MongoClient(addrs,credentials);  
            mongoDatabase = mongoClient.getDatabase("EntityLinking");
            
			System.out.println("database EntityLinking connected.");
		}catch(Exception e){
			logger.info(e.getClass() + ":" + e.getMessage());
		}
	}
	
	public MongoCollection<Document> getCollection(String collectionName){
		try{
			return mongoDatabase.getCollection(collectionName);
		}catch(Exception e){
			logger.info(e.getClass() + ":" + e.getMessage());
		}
		return null;
	}
	
	public String getTitleByID(String baidu_id){
		String title = "";
		baidu_id = Constant.baiduEntityPrefix + baidu_id;
		BasicDBObject query = new BasicDBObject("url", baidu_id);

		MongoCursor<Document> cursor = baiduCollection.find(query).iterator();

		while(cursor.hasNext())
		{
		   System.out.println(cursor.next());
		}
		cursor.close();
		return title;
	}
	
	public static void main(String[] args) {
		MongoDao m = new MongoDao();
		m.getTitleByID("/view/39784.htm");

	}

}
