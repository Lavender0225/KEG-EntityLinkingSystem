package edu.tsinghua.el.mention.filter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import edu.tsinghua.el.common.Constant;

/**
 * @author Hujun
 * 在建立索引前过滤实体集，得到特定领域实体集freqEntities
 * 也可于commonEntities中建立一般实体集
 */
public class EntityFilter 
{
	//sets
	private Set<String> freqLabels ;
	private Set<String> freqConcepts ;
	private Set<String> freqEntities ;
	
	private Set<String> stopConcepts ;
	
	private Map<String,Set<String> > conceptTree ;//parent id : children ids
	private Map<String,Set<String> > concepts ;//id : label
	
	private HashMap<String, String> freqEntityClass ;   //entity所在的class <entityid, classid>
	private HashMap<String, String> freLableEntity;//标签 id
	
	Scanner input = new Scanner(System.in);
	
	
	/**
	 * Constructor
	 */
	public EntityFilter() 
	{
		
		this.conceptTree=new HashMap();
		this.concepts=new HashMap();
		this.freqLabels=new HashSet();
		this.freqConcepts=new HashSet();
		this.freqEntities=new HashSet();
		this.stopConcepts = new HashSet();
		
		this.freqEntityClass = new HashMap() ;
	}

	public static void filter(int fre) throws IOException
	{
		EntityFilter filter = new EntityFilter();
		filter.loadLabels();  //读取想要保留的类别的根节点存到freqConcepts和freqLabels
		
		filter.loadStopConcepts();   //读取想要去掉的类别的根节点存到stopConcepts和stopLabels
		
		filter.loadConceptTree();  //读取父类和它的所有子类的set到Map<String,Set<String> > conceptTree
		filter.filterConcepts(fre);  //将要保留的类别下的所有子类别存在freqConcepts中，去掉的存在stopConcepts
		filter.filterEntities();  //将要保留的instace存在freqEntities，去掉的存在stopEntities
		filter.saveEntities();
		filter.saveConcepts();
	}
	
	private void filterEntities() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(Constant.taxonomy_path));
		String line = null;
		String[] tmp = null ;
		String eId = null ;
		String cId = null ;
		System.out.println("filting entities ... ");
		int count = 0;
		while((line=reader.readLine())!=null)
		{
			if((line.indexOf('@')==0)||(line.length()==0))
				continue;
			tmp=line.split(" ",3);
			if(tmp[1].contains("InstanceOf"))
			{
				eId=tmp[0].substring(tmp[0].lastIndexOf('/')+1, tmp[0].lastIndexOf('>'));
				cId=tmp[2].substring(tmp[2].lastIndexOf('/')+1, tmp[2].lastIndexOf('>'));
				
				if(freqConcepts.contains(cId))
				{
					if(freqEntityClass.containsKey(eId))
					{
						freqEntityClass.put(eId, freqEntityClass.get(eId)+"::!"+cId);
					}
					else
					{
						freqEntityClass.put(eId, "::!"+cId);
					}
					if(!freqEntities.contains(eId))
					{
						freqEntities.add(eId);
					}
					
					//System.out.println(eId+"    "+freqEntityClass.get(eId));
				}
				++count;
			}
		}
		//System.out.println(count+"个instance");   //4918336个instance
		System.out.println("#frequency entities "+freqEntities.size());  //想留下的有1671060个
		reader.close();	
	}
	
	private void saveEntities() throws IOException
	{
		FileWriter w_freq = new FileWriter(Constant.freqEntityPath);
		FileWriter w_common = new FileWriter(Constant.commonEntityPath);
		
		BufferedReader reader = new BufferedReader(new FileReader(Constant.entityCountInput));
		String line = null;
		String[] tmp = null;
		System.out.println("writing ...");
		while((line=reader.readLine())!=null)
		{
			tmp = line.split("::;");
			if(tmp.length < 2)
			{
				//System.out.println(line);
			}
			//else if(freqEntities.contains(tmp[1]) || (Double.parseDouble(tmp[2]) > 0.8 && Double.parseDouble(tmp[2]) < 1.0))
			else if(freqEntities.contains(tmp[1]))
			{
				if(tmp[1].equals("36703"))
				{
					System.out.println(line);
				}
				w_freq.write(line+"\n");
				//System.out.println(line);
			} 
			else
			{
				w_common.write(line+"\n");
			}
		}
		System.out.println("write out finished!");
		
		w_freq.flush();
		w_freq.close();
		
		w_common.flush();
		w_common.close();
		
		reader.close();
	}
		
	private void filterConcepts(int fre_times)
	{
		//filter frequency concepts
		LinkedList<String> que = new LinkedList();
		
		String tmp = null ;
		
		System.out.println("filting frequency concepts ...");

		for(int i=0; i<fre_times; i++)
		{
			que.addAll(freqConcepts);
			for (String str : que) 
			{ 
				if(conceptTree.get(str) != null)
				{
					freqConcepts.addAll(conceptTree.get(str));  //找fre_times层
				}
			}
			System.out.println("第" + (i+1) +"层有" + freqConcepts.size()+"个节点");
		}

		//System.out.println("conceptTree现在是："+conceptTree.size());  //354211个中间节点
		freqConcepts.removeAll(stopConcepts);
		System.out.println("#frequency concepts "+freqConcepts.size());//
		
	}
	
	private void saveConcepts() throws IOException 
	{
		
		BufferedReader reader = new BufferedReader(new FileReader(Constant.concept_path));
		String[] tmp = null;
		String cId = null;
		String label = null;
		String line = null;
		
		FileWriter w_freq = new FileWriter(Constant.freqConceptPath);
		
		System.out.println("save concepts ...");
		while((line=reader.readLine())!=null)
		{
			if((line.indexOf('@')==0)||(line.length()==0))
				continue;
			tmp=line.split(" ",3);
			cId=tmp[0].substring(tmp[0].indexOf('<')+1, tmp[0].indexOf('>'));
			if(tmp[1].contains("label"))
			{
				label=tmp[2].substring(tmp[2].indexOf('\"')+1,tmp[2].lastIndexOf('\"'));
				if(freqConcepts.contains(cId))
				{
					w_freq.write(cId+"::="+label+"\n");
				}
			}
		}
		System.out.println("concepts saved!");
		w_freq.flush();
		w_freq.close();
	
		reader.close();
	}
	
	private void loadConceptTree() throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(Constant.taxonomy_path));
		String line = null;  //xlore.taxonomy.ttl，1.36GB
		String[] tmp = null;
		String supC = null;
		String subC = null;
		//int i=0;
		System.out.println("loading concept tree ... ");
		while((line=reader.readLine())!=null)
		{
			
			if((line.indexOf('@')==0)||(line.length()==0))
				continue;
			tmp=line.split(" ",3);
			if(tmp[1].contains("SubClassOf"))
			{
				subC=tmp[0].substring(tmp[0].lastIndexOf('/')+1, tmp[0].lastIndexOf('>'));
				supC=tmp[2].substring(tmp[2].lastIndexOf('/')+1, tmp[2].lastIndexOf('>'));
				//System.out.println(line);
				//System.out.println(subC+" is subClass of "+supC);
			
				if(!this.conceptTree.containsKey(supC))  //conceptTree中不含supC
					this.conceptTree.put(supC, new HashSet());  //将supC加入map，并创建set
				else if(!this.conceptTree.get(supC).contains(subC)) //conceptTree中存在supC且supC的set中不含subC
					this.conceptTree.get(supC).add(subC);//将subC加入supC的map
				//System.out.println(subC + "    " + supC);
			}
		}
		System.out.println("loading finished");
		System.out.println("#concept tree "+conceptTree.size());
	}

	private void loadStopConcepts() throws IOException{
		BufferedReader reader = new BufferedReader(new FileReader(Constant.stopConceptPath));
		String line = null;
		while((line=reader.readLine())!=null)
		{
			stopConcepts.add(line.split("::=")[0]);
		}
		//System.out.println(stopConcepts.size());
		reader.close();
	}
	
	private void loadLabels() throws IOException{
		//load frequency labels
		String line = null;
		BufferedReader reader1 = new BufferedReader(new FileReader(Constant.freqLabelInput));
		//System.out.println("loading frequency labels ... ");  //读labels_freq
		while((line=reader1.readLine())!=null)
			freqLabels.add(line);
		//System.out.println("loading finished!");
		System.out.println("#freqLabels "+freqLabels.size());  //想保留的
		reader1.close();
		
		//load concepts
		BufferedReader reader = new BufferedReader(new FileReader(Constant.concept_path));
		//xlore.concept.list   类别的列表
		String[] tmp = null;
		String cId = null;
		String label = null;
		
		System.out.println("loading concepts ...");
		while((line=reader.readLine())!=null){//&&this.concepts.size()<1000
			if((line.indexOf('@')==0)||(line.length()==0))
				continue;
			tmp=line.split(" ",3);   //最多分三段
			cId=tmp[0].substring(tmp[0].indexOf('<')+1, tmp[0].indexOf('>')); //编号
			if(tmp[1].contains("label")){
				label=tmp[2];
				label=tmp[2].substring(tmp[2].indexOf('\"')+1,tmp[2].lastIndexOf('\"'));//名称
				
				if(!freqConcepts.contains(cId)){
					for (String str : freqLabels) {  //找含有这个词的concept
					      //if(label.indexOf(str) != -1)
						  //if(label.endsWith(str) && !label.endsWith("国家"))
						  if(label.endsWith(str)){
					    	  System.out.println(cId + "    " + label);
					    	  freqConcepts.add(cId);
					      }
					} 
				}
				
			}
		}
		System.out.println("loading finished!");
		System.out.println("#freqConcepts "+freqConcepts.size());

		reader.close();
	}	
	
	public static void main(String[] args) {
		try {
			EntityFilter.filter(4);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
