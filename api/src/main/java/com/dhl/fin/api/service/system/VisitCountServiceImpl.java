package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.domain.VisitCount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author becui
 * @date 7/29/2020
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class VisitCountServiceImpl extends CommonService<VisitCount> {

    public List<VisitCount> dailyUser() throws Exception {
        Long projectId = WebUtil.getLongParam("projectId");
        List<VisitCount> visitCounts = select(QueryDto.builder().available().addWhere("project.id=" + projectId).build());
        formatPageData(visitCounts);
        return visitCounts;
    }

}
