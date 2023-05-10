package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.domain.SystemConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author becui
 * @date 7/20/2020
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SystemConfigServiceImpl extends CommonService<SystemConfig> {

    public String getConfig(String key) throws Exception {
        SystemConfig systemConfig = get(QueryDto.builder().available().addWhere("code = '" + key + "'").build());
        return ObjectUtil.notNull(systemConfig) ? systemConfig.getValue() : null;
    }

    public List<SystemConfig> getAll() throws Exception {
        return select(QueryDto.builder().available().build());
    }


    public void updateConfig(String key, String value) throws Exception {
        SystemConfig config = new SystemConfig();
        config.setValue(value);
        updateBySelective(config, QueryDto.builder().available().addWhere("code='" + key + "'").build());
    }



}





