package org.jeecg.modules.qwert.jst.service;

import java.util.List;

import org.jeecg.modules.qwert.jst.entity.JstZcTarget;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @Description: jst_zc_target
 * @Author: jeecg-boot
 * @Date:   2020-07-24
 * @Version: V1.0
 */
public interface IJstZcTargetService extends IService<JstZcTarget> {
	public List<JstZcTarget> queryJztList();
	public boolean edit(JstZcTarget jstZcTarget);
}
