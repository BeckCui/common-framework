package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.SpringContextUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.domain.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import javax.validation.constraints.NotNull;

/**
 * @author becui
 * @date 6/17/2020
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class LogServiceImpl extends CommonService<Log> {

    @Autowired
    private RedisService redisService;

    public void log(@NotNull String tableName, @NotNull String actionEnum, @NotNull String logStatus, String remark) throws Exception {

        String menu = WebUtil.getStringParam("menu");
        String ip = WebUtil.getClientIP();
        String browser = WebUtil.getClientBrowser();
        String os = WebUtil.getClientOs();
        String uuid = WebUtil.getRequest().getRequestURI().endsWith("login/login") ? WebUtil.getStringParam("userName") : WebUtil.getLoginUser().getUuid();
        String appCode = SpringContextUtil.getPropertiesValue("custom.projectCode");
        Map project = redisService.getProject(appCode);
        String appName = ObjectUtil.isNull(project) ? appCode.toUpperCase() : project.get("code").toString();

        log(StringUtil.isEmpty(tableName) ? "无" : tableName, actionEnum, logStatus, remark, StringUtil.isEmpty(menu) ? "无" : menu, ip, browser, os, uuid, appCode, appName);

    }

    public void log(@NotNull String tableName, @NotNull String actionEnum,
                    @NotNull String logStatus, String remark, String menu,
                    String ip, String browser, String os, String uuid, String appCode, String appName
    ) throws Exception {
        saveOrUpdate(new Log(appCode, appName, menu, tableName,
                actionEnum, logStatus, remark, uuid, ip, browser, os));

    }


}
