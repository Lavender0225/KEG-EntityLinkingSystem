package baike.entity.dao;

import edu.tsinghua.el.common.Constant;


public class BaikeProbManager{
	private static ProbHolder baiduProbs = null;
	private static ProbHolder wikiProbs = null;
	
	private BaikeProbManager(){
		baiduProbs = new BaikeProbsHolder(Constant.baiduEntityPriorFile, Constant.baiduMGivenEProbFile, Constant.baiduLinkProbFile);
		wikiProbs = new BaikeProbsHolder(Constant.wikiEntityPriorFile, Constant.wikiMGivenEProbFile, Constant.wikiLinkProbFile);
		
	}
	
	private static class InstanceHolder{
		private static BaikeProbManager instance = new BaikeProbManager();
	}
	
	public static BaikeProbManager getInstance(){
		return InstanceHolder.instance;
	}
	
	

	public ProbHolder getBaiduProbs() {
		return baiduProbs;
	}

	public ProbHolder getWikiProbs() {
		return wikiProbs;
	}

}
