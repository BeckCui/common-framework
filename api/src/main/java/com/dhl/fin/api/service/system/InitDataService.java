package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.JsonUtil;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.domain.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 4/6/2020
 */
@Service
public class InitDataService implements ApplicationRunner {


    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SystemConfigServiceImpl systemConfigService;

    @Autowired
    private ProjectServiceImpl projectService;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        redisService.put(CacheKeyEnum.SYSTEM_CONFIG, systemConfigService.getAll());
        cacheSuperManager();
        cacheApplication();
        projectService.cacheProjects();
    }

    /**
     * 缓存超级管理员信息
     */
    public void cacheSuperManager() throws Exception {
        List<Account> superManagerList = accountService.select(QueryDto.builder()
                .available()
                .addWhere("is_super_manager = 1")
                .build());
        redisService.put(CacheKeyEnum.SUPER_MANAGER, superManagerList);
    }

    /**
     * 缓存应用code
     */
    public void cacheApplication() throws Exception {

        String applications = systemConfigService.getConfig("projects");

        if (StringUtil.isEmpty(applications)) {
            return;
        }

        Map apps = new HashMap();
        Arrays.stream(applications.split(",")).forEach(p -> {
            apps.put(p, p);
        });

        Map dictionary = redisService.getMap(CacheKeyEnum.DICTIONARIES_PER_APP);
        if (ObjectUtil.isNull(dictionary)) {
            dictionary = new HashMap();
        }
        Map fintpMap = MapUtil.getMap(dictionary, "fintp");
        if (ObjectUtil.isNull(fintpMap)) {
            fintpMap = new HashMap();
        }
        fintpMap.put("appCodes", apps);
        dictionary.put("fintp", fintpMap);
        redisService.put(CacheKeyEnum.DICTIONARIES_PER_APP, JsonUtil.objectToString(dictionary));

    }


}





