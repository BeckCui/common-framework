package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.domain.Action;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by CuiJianbo on 2020.03.01.
 */

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ActionServiceImpl extends CommonService<Action> {

}
