package com.dhl.fin.api.controller.system;

import com.dhl.fin.api.common.controller.CommonController;
import com.dhl.fin.api.domain.Version;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author becui
 * @date 7/29/2020
 */

@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("version")
public class VersionController extends CommonController<Version> {




}


