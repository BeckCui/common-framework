package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.OptionDto;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.domain.Version;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 7/29/2020
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class VersionServiceImpl extends CommonService<Version> {

    @Autowired
    private ProjectServiceImpl projectService;

    @Override
    public void afterGet(Version domain) throws Exception {
        List<OptionDto> appOptions = projectService
                .select(QueryDto.builder().available().build())
                .stream()
                .map(p -> OptionDto.builder().name(p.getName()).value(p.getId().toString()).build())
                .collect(Collectors.toList());
        domain.setProjectOptions(appOptions);
    }
}
