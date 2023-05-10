package com.dhl.fin.api.controller.system;

import com.dhl.fin.api.common.controller.CommonController;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.service.LdapService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.domain.*;
import com.dhl.fin.api.service.system.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author CuiJianbo
 * @date 2020.02.25
 */
@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping({"account", "projectuser"})
public class AccountController extends CommonController<Account> {

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private ProjectServiceImpl projectService;

    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    SystemConfigServiceImpl systemConfigService;

    @Autowired
    private PasswordServiceImpl passwordService;

    @Autowired
    LdapService ldapService;

    /**
     * 新增和更新
     *
     * @param domain
     * @return
     * @throws Exception
     */
    @Override
    public ApiResponse saveOrUpdate(Account domain) throws Exception {


        PassWord fintpPwd = passwordService.get(QueryDto.builder().available().addWhere("account='srv_cnexp_fintp'").build());
        Map data = ldapService.queryAccountByUUID("srv_cnexp_fintp", SecUtil.decrypt(fintpPwd.getPwd()), domain.getUuid());
        if (data != null) {
            if (StringUtil.isEmpty(domain.getTitle())) {
                domain.setTitle(MapUtil.getString(data, "displayName"));
            }
            if (StringUtil.isEmpty(domain.getEmail())) {
                domain.setEmail(MapUtil.getString(data, "mail"));
            }
            if (StringUtil.isEmpty(domain.getUserNo())) {
                domain.setUserNo(MapUtil.getString(data, "employeeNumber"));
            }
        }


        if (WebUtil.getRequest().getRequestURI().contains("projectuser")) {
            Long projectId = WebUtil.getLongParam("projectId");
            Long[] roleIds = WebUtil.getLongArrayParam("roleId");
            Long[] userIds = WebUtil.getLongArrayParam("userIds");
            String[] areas = WebUtil.getStringArrayParam("areaCode");

            Project project = projectService.get(projectId);

            List<Long> rolesIdsInProject = roleService.select(QueryDto
                    .builder()
                    .available()
                    .addWhere("project.id = " + projectId).build()
            ).stream()
                    .map(p -> p.getId())
                    .collect(Collectors.toList());


            for (Long userId : userIds) {


                for (Long rid : rolesIdsInProject) {
                    commonService.deleteMiddleTable("roles", userId, rid);
                }
                commonService.deleteMiddleTable("projects", userId, projectId);

                if (ObjectUtil.notNull(roleIds)) {
                    List<Role> roles = roleService.select(QueryDto.builder().addWhere(String.format("id in (%s)", ArrayUtil.join(roleIds, ","))).build());
                    Optional optional = roles.stream().filter(p -> p.getName().equals("系统管理员")).findFirst();
                    if (optional.isPresent()) {
                        Account account = new Account();
                        account.setAreas("ALL");
                        commonService.updateBySelective(account, QueryDto.builder().addWhere(String.format("id = %s", userId)).build());
                    } else {
                        Account a = commonService.get(userId);
                        if ("ALL".equals(a.getAreas())) {
                            Account account = new Account();
                            account.setAreas("");
                            commonService.updateBySelective(account, QueryDto.builder().addWhere(String.format("id = %s", userId)).build());
                        }
                    }
                    for (Long roleId : roleIds) {
                        commonService.insertMiddleTable("roles", userId, roleId);
                    }


                }

                commonService.insertMiddleTable("projects", userId, projectId);
                Account account = new Account();
                account.setUpdateTime(DateUtil.getSysTime());
                account.setUpdateUser(WebUtil.getLoginUser().getUuid());
                if (StringUtil.isNotEmpty(domain.getEmail())) {
                    account.setEmail(domain.getEmail());
                }
                if (StringUtil.isNotEmpty(domain.getTitle())) {
                    account.setTitle(domain.getTitle());
                }
                account.setUserNo(domain.getUserNo());
                commonService.updateBySelective(account, QueryDto.builder().addWhere(String.format("id = %s", userId)).build());

            }

            if (ArrayUtil.isNotEmpty(areas)) {
                Account account = new Account();
                account.setAreas(ArrayUtil.join(areas, ","));
                commonService.updateBySelective(account, QueryDto.builder().addWhere("id=" + domain.getId()).build());
            }

            return ApiResponse.success();
        } else {
            Long[] projectIds = WebUtil.getLongArrayParam("projectIds");
            Long userId = WebUtil.getLongParam("id");
            if (ArrayUtil.isNotEmpty(projectIds)) {
                List<Long> projectIdList = projectService.select(QueryDto.builder()
                        .available()
                        .addWhere(String.format("project.id in (%s)", ArrayUtil.join(projectIds, ",")))
                        .addWhere(String.format("not exists(select id from t_account c where accounts.id = c.id)"))
                        .build()).stream().map(Project::getId).collect(Collectors.toList());

                for (Long projectId : projectIdList) {
                    projectService.insertMiddleTable("accounts", projectId, userId);
                }

                List<Long> roleIds = roleService.select(QueryDto.builder()
                        .available()
                        .addWhere("role.code like '%_sys_manager'")
                        .build()).stream()
                        .map(Role::getId).collect(Collectors.toList());

                for (Long roleId : roleIds) {
                    commonService.deleteMiddleTable("roles", userId, roleId);
                }

                roleIds = roleService.select(QueryDto.builder()
                        .available()
                        .addWhere(String.format("role.project_id in (%s)", ArrayUtil.join(projectIds, ",")))
                        .addWhere("role.code like '%_sys_manager'")
                        .build()).stream()
                        .map(Role::getId).collect(Collectors.toList());
                for (Long roleId : roleIds) {
                    commonService.insertMiddleTable("roles", userId, roleId);
                }
            }

            if (ObjectUtil.notNull(domain.getId())) {
                Account a = accountService.get(domain.getId());
                domain.setIsSuperManager(a.getIsSuperManager());
            } else {
                domain.setAvailable(true);
                domain.setIsSuperManager(false);
            }

            return super.saveOrUpdate(domain);
        }

    }

    /**
     * 删除
     *
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public ApiResponse delete(@PathVariable Long id) throws Exception {
        if (WebUtil.getRequest().getRequestURI().contains("projectuser")) {
            commonService.deleteMiddleTable("projects", id);
            return ApiResponse.success();
        } else {
            return super.delete(id);
        }
    }


    /**
     * 更新account状态
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("updatestatus/{id}/{status}")
    public ApiResponse updateAvailable(@PathVariable Long id, @PathVariable String status) throws Exception {
        Account account = new Account(id);
        account.setAvailable(Boolean.valueOf(status));
        commonService.updateBySelective(account);
        return ApiResponse.success();
    }

    /**
     * 更新account是否是超级管理员
     *
     * @param id
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("updatesuper/{id}/{status}")
    public ApiResponse updateSuperMG(@PathVariable Long id, @PathVariable String status) throws Exception {
        Account account = new Account(id);
        account.setIsSuperManager(Boolean.valueOf(status));
        commonService.updateBySelective(account);
        return ApiResponse.success();
    }



}






