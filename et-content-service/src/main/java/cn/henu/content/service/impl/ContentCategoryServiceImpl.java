package cn.henu.content.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.tools.internal.xjc.model.SymbolSpace;

import cn.henu.common.pojo.EasyUITreeNode;
import cn.henu.common.utils.EtResult;
import cn.henu.content.service.ContentCategoryService;
import cn.henu.mapper.TbContentCategoryMapper;
import cn.henu.pojo.TbContentCategory;
import cn.henu.pojo.TbContentCategoryExample;
import cn.henu.pojo.TbContentCategoryExample.Criteria;

/**
 * 内容分类管理service
 * @author syw
 *
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

	@Autowired
	private TbContentCategoryMapper contentCategoryMapper;
	
	@Override
	public List<EasyUITreeNode> getContentCatList(Long parentId) {
		// 根据parentid查询子节点列表
		TbContentCategoryExample example = new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		//设置查询条件
		criteria.andParentIdEqualTo(parentId);
		//执行查询
		List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
		//转换成Easyuitreenode的列表
		List<EasyUITreeNode> nodeList = new ArrayList<EasyUITreeNode>();
		for (TbContentCategory contentCategory : list) {
			EasyUITreeNode node = new EasyUITreeNode();
			
			node.setId(contentCategory.getId());
			node.setText(contentCategory.getName());
			node.setState(contentCategory.getIsParent()?"closed":"open");
			//添加到列表
			nodeList.add(node);
		}
		return nodeList;
	}

	@Override
	public EtResult addContentCategory(long parentId, String name) {
		// 创建一个对应的pojo
		TbContentCategory contentCategory = new TbContentCategory();
		//设置pojo的属性,其中id不用设置
		contentCategory.setParentId(parentId);
		contentCategory.setName(name);
		//其中1表示正常,2表示删除
		contentCategory.setStatus(1);
		//默认排序是1
		contentCategory.setSortOrder(1);
		//因为新添加的节点一定是叶子节点
		contentCategory.setIsParent(false);
		contentCategory.setCreated(new Date());
		contentCategory.setUpdated(new Date());
		//插入到数据库，插完之后ID会自动补上，因为使用了selectKey
		contentCategoryMapper.insert(contentCategory);
		//判断父节点的isparent属性，如果不是true则改成true，
			//根据parentid查询父节点
			TbContentCategory parent = contentCategoryMapper.selectByPrimaryKey(parentId);
			if(!parent.getIsParent()) {
				parent.setIsParent(true);
				//更新到数据库中
				contentCategoryMapper.updateByPrimaryKey(parent);
			}
		//返回结果，返回EtResult，包含pojo
		
		return EtResult.ok(contentCategory);
	}

	@Override
	public EtResult updateNode(Long id, String name) {
		// 修改内容列表
		TbContentCategory tbContentCategory = contentCategoryMapper.selectByPrimaryKey(id);
		tbContentCategory.setName(name);
		tbContentCategory.setId(id);
		tbContentCategory.setUpdated(new Date());
		contentCategoryMapper.updateByPrimaryKey(tbContentCategory);
		return EtResult.ok(tbContentCategory);
	}

	@Override
	public int deleteNode(Long id) {
		//根据子节点的id查找该节点和其父节点
		TbContentCategory contentCategory = contentCategoryMapper.selectByPrimaryKey(id);//获取当前节点
		TbContentCategory parentcontent = contentCategoryMapper.selectByPrimaryKey(contentCategory.getParentId());//查询父节点
		//创建查询条件
		TbContentCategoryExample example = new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		//如果删除的节点是一个叶子结点
		if(contentCategory.getIsParent()==false) {
			//设置查询条件
			criteria.andParentIdEqualTo(parentcontent.getParentId());
			//该父节点是否还有其他的子节点
			int count = contentCategoryMapper.countByExample(example);
			if(count>1) {
				//如果还有其他的子节点，则表示删除的是一个叶子节点
				contentCategoryMapper.deleteByPrimaryKey(id);
			}else {
				//如果父节点只有这么一个子节点（通过isparent来判断），那么删除子节点之后，该父节点变成子节点
				contentCategoryMapper.deleteByPrimaryKey(id);
				parentcontent.setId(parentcontent.getId());
				parentcontent.setIsParent(false);
				contentCategoryMapper.updateByPrimaryKeySelective(parentcontent);
			}
		}else {
			//如果删除的节点是一个目录
			if(contentCategory.getParentId()==0||contentCategory.getParentId()==30) {
				return 0;//0代表的是根目录和二级目录,不让删除
			}else {	
				//设置查询条件
				criteria.andParentIdEqualTo(contentCategory.getId());
				List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
				for (TbContentCategory tb : list) {
					delAll(tb);
				}
			}
		}
		return 0;//表示删除成功
	}
	//递归删除节点
	private void delAll(TbContentCategory tb) {
		if(tb.getIsParent()) {
			delAll(tb);
			tb.setIsParent(false);
			contentCategoryMapper.updateByPrimaryKeySelective(tb);
		}else {
			contentCategoryMapper.deleteByPrimaryKey(tb.getId());
		}
	}

}
