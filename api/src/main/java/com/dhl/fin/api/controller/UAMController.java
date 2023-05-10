package com.dhl.fin.api.controller;

import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.service.UamServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("uam")
public class UAMController {

    @Autowired
    private UamServiceImpl uamService;

    @Autowired
    private RedisService redisService;


    @ResponseBody
    @RequestMapping("monitor")
    public ApiResponse queryAppTotal() throws Exception {

        return ApiResponse.success();
    }


}




