package cn.henu.content.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.henu.common.jedis.JedisClient;
import cn.henu.common.pojo.EasyUIDataGridResult;
import cn.henu.common.utils.EtResult;
import cn.henu.common.utils.JsonUtils;
import cn.henu.content.service.ContentService;
import cn.henu.mapper.TbContentMapper;
import cn.henu.pojo.TbContent;
import cn.henu.pojo.TbContentExample;
import cn.henu.pojo.TbContentExample.Criteria;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private JedisClient jedisClient;
	
	@Value("${CONTENT_LIST}")
	private String CONTENT_LIST;
	
	//注意在对内容进行增删改的时候需要进行同步缓存，最简单的做法就是吧缓存删除了，让她查询数据库然后再加上
	@Override
	public EtResult addContent(TbContent content) {
		// 将内容数据插入到内容表
		content.setCreated(new Date());
		content.setUpdated(new Date());
		//插入到数据库
		contentMapper.insert(content);
		//缓存同步，删除缓存中对应的数据,注意不要删除hash
		jedisClient.hdel(CONTENT_LIST, content.getCategoryId().toString());
		return EtResult.ok();
	}

	@Override
	public EasyUIDataGridResult getContentList(int page, int rows, long categoryId) {
		// 设置分页的条件
		PageHelper.startPage(page, rows);
		// 设置查询的条件
		TbContentExample example = new TbContentExample(); //  查询所有
		 Criteria criteria = example.createCriteria();
		criteria.andCategoryIdEqualTo(categoryId);
		// 按分页的条件查出对应的数据
		List<TbContent> list = contentMapper.selectByExample(example);
		//封装pageInfo对象，应为在pageInfo中可以获取所有的条目数，和每一个也展示的数据
		PageInfo<TbContent> pageInfo = new PageInfo<>(list);
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setTotal(pageInfo.getTotal());
		result.setRows(list);
		return result;
	}

	//根据内容分类id查询内容列表(引入缓存减轻数据库的压力)
	@Override
	public List<TbContent> getContentByCid(long cid) {
		//查询缓存
		try {
			//如果缓存中有直接响应
			String json = jedisClient.hget(CONTENT_LIST, cid+"");
			//因为下面添加缓存的时候用的是json，所以这里
			if(StringUtils.isNotBlank(json)) {
				List<TbContent> list = JsonUtils.jsonToList(json, TbContent.class);
				return list;
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		//如果没有再去查询数据库
		TbContentExample example = new TbContentExample();
		Criteria criteria = example.createCriteria();
		//设置查询条件，也就是分类id
		criteria.andCategoryIdEqualTo(cid);
		//执行查询
		List<TbContent> list = contentMapper.selectByExampleWithBLOBs(example);
		//把查询的结构添加到缓存
		try {
			jedisClient.hset("CONTENT_LIST", cid+"", JsonUtils.objectToJson(list));//使用json工具吧list转化成字符串
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return list;
	}

}
