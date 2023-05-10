package com.dhl.fin.api.controller;


import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.common.util.excel.ExcelSheet;
import com.dhl.fin.api.common.util.excel.ExcelTitleBean;
import com.dhl.fin.api.common.util.excel.ExcelUtil;
import com.dhl.fin.api.service.AssistantServiceImpl;
import com.dhl.fin.api.service.UamServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("assistant")
public class AssistantController {


    @Autowired
    private AssistantServiceImpl otherCaseService;

    @Autowired
    private UamServiceImpl uamService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AssistantServiceImpl assistantService;


    @ResponseBody
    @RequestMapping("uamfile")
    public ApiResponse genericUploadUamFile() throws Exception {

        return ApiResponse.success();

    }



}






