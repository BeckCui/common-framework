package com.dhl.fin.api.controller.system;

import com.dhl.fin.api.common.controller.CommonController;
import com.dhl.fin.api.domain.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author becui
 * @date 6/17/2020
 */

@Slf4j
@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("log")
public class LogController extends CommonController<Log> {
}
