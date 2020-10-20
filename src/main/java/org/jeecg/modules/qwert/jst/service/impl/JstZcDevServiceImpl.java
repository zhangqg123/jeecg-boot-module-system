package org.jeecg.modules.qwert.jst.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.qwert.jst.entity.JstZcCat;
import org.jeecg.modules.qwert.jst.entity.JstZcDev;
import org.jeecg.modules.qwert.jst.mapper.JstZcCatMapper;
import org.jeecg.modules.qwert.jst.mapper.JstZcDevMapper;
import org.jeecg.modules.qwert.jst.service.IJstZcDevService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: jst_zc_dev
 * @Author: jeecg-boot
 * @Date:   2020-07-24
 * @Version: V1.0
 */
@Service
public class JstZcDevServiceImpl extends ServiceImpl<JstZcDevMapper, JstZcDev> implements IJstZcDevService {
	@Resource
	private JstZcDevMapper jstZcDevMapper;

//	@Cacheable(value = CacheConstant.JST_DEV_CACHE)
	@Override
	public List<JstZcDev> queryJzdList() {
//		QueryWrapper<JstZcDev> dqw = QueryGenerator.initQueryWrapper(new JstZcDev(), null);
//		dqw.eq("status", "0");
//		dqw.orderByAsc("dev_no");
		List<JstZcDev> jzdList = this.jstZcDevMapper.queryJzdList();
		return jzdList;
	}

}
