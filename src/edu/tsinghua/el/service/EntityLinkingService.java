package edu.tsinghua.el.service;

import java.util.ArrayList;

import edu.tsinghua.el.model.FormData;
import edu.tsinghua.el.model.LinkingResult;

public interface EntityLinkingService {
	public ArrayList<LinkingResult> linking(String text, String index_choose);
}
