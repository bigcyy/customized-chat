package com.cyy.chat.controller;

import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.model.Application;
import com.cyy.chat.service.IApplicationService;
import com.cyy.common.converter.BeanConverter;
import com.cyy.common.utils.R;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author CYY
 * @since 2025-02-25
 */
@Controller
@RestController
@RequestMapping("/api/v1/application")
public class ApplicationController {

    @Resource
    private IApplicationService applicationService;

    @PostMapping
    public R add(@RequestBody ApplicationDto applicationDto) {
        Application app = BeanConverter.source(applicationDto).target(Application.class)
                .convert();
        applicationService.save(app);
        return R.ok().data("applicationId",app.getId());
    }
}
