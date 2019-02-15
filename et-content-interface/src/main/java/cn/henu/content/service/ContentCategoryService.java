package cn.henu.content.service;

import java.util.List;

import cn.henu.common.pojo.EasyUITreeNode;
import cn.henu.common.utils.EtResult;

public interface ContentCategoryService {

	//内容分类列表
	List<EasyUITreeNode> getContentCatList(Long parentId);
	
	//新增分类
	EtResult addContentCategory(long parentId,String name);
	
	//编辑分类的内容
	EtResult updateNode(Long id, String name);
	
	//删除节点
	int deleteNode(Long id);
}
