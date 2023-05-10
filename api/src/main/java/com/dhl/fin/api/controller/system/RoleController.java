package com.dhl.fin.api.controller.system;


import com.dhl.fin.api.common.controller.CommonController;
import com.dhl.fin.api.domain.Role;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 *
 * @author CuiJianbo
 * @date 2020.02.25
 */
@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("role")
public class RoleController extends CommonController<Role> {


}