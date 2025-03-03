package com.cyy.chat.service.impl;

import com.cyy.chat.dao.ApplicationMapper;
import com.cyy.chat.model.Application;
import com.cyy.chat.service.IApplicationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CYY
 * @since 2025-02-25
 */
@Service
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application> implements IApplicationService {

}
