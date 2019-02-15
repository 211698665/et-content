package cn.henu.content.service;

import java.util.List;

import cn.henu.common.pojo.EasyUIDataGridResult;
import cn.henu.common.utils.EtResult;
import cn.henu.pojo.TbContent;

public interface ContentService {

	
	EtResult addContent(TbContent content);
	EasyUIDataGridResult getContentList(int page,int rows,long categoryId);
	
	List<TbContent> getContentByCid(long cid);
}
