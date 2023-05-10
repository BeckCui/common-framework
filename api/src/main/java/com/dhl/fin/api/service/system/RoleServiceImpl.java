package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.OptionDto;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.util.ArrayUtil;
import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.domain.Project;
import com.dhl.fin.api.domain.Role;
import com.dhl.fin.api.domain.Tree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by CuiJianbo on 2020.02.23.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleServiceImpl extends CommonService<Role> {

    @Autowired
    TreeServiceImpl treeService;


    @Autowired
    ActionServiceImpl actionService;


    /**
     * 在保存前
     *
     * @param domain
     */
    @Override
    public void beforeSave(Role domain) throws Exception {
        Long projectId = WebUtil.getLongParam("projectId");
        domain.setProject(new Project(projectId));

        if (domain.getCode().endsWith(" _sys_manager")) {
            domain.setUpdateUserPer(true);
            domain.setAddUserPer(true);
            domain.setDeleteUserPer(true);
        }

    }

    /**
     * 保存后
     *
     * @param domain
     */
    @Override
    public void afterSave(Role domain) throws Exception {
        Long[] treeIds = WebUtil.getLongArrayParam("roleTreeIds");
        if (ArrayUtil.isNotEmpty(treeIds)) {
            List<Tree> trees = findDomainListByIds(Arrays.asList(treeIds), Tree.class);
            if (ObjectUtil.notNull(trees)) {
                domain.setTrees(trees);
            }
        }

//        Long[] dataTreeIds = WebUtil.getLongArrayParam("dataTreeIds");
//        if (ArrayUtil.isNotEmpty(dataTreeIds)) {
//            List<Tree> trees = findDomainListByIds(ArrayUtil.arrayToList(dataTreeIds), Tree.class);
//            if (ObjectUtil.notNull(trees)) {
//                domain.setDataPerTrees(trees);
//            }
//        }

        Long[] mgRoleIds = WebUtil.getLongArrayParam("mgRoleIds");
        if (ArrayUtil.isNotEmpty(mgRoleIds)) {
            deleteMiddleTable("mgRoles", domain.getId());
            for (Long mgRoleId : mgRoleIds) {
                insertMiddleTable("mgRoles", domain.getId(), mgRoleId);
            }
        }

        Long[] mgAcctIds = WebUtil.getLongArrayParam("mgAcctIds");
        if (ArrayUtil.isNotEmpty(mgAcctIds)) {
            deleteMiddleTable("mgAccounts", domain.getId());
            for (Long mgRoleId : mgAcctIds) {
                insertMiddleTable("mgAccounts", domain.getId(), mgRoleId);
            }
        }


        freshMgRoles();

        saveOrUpdate(domain);
    }


    /**
     * 每次更新完角色后， 更新系统管理员的所有角色权限
     *
     * @throws Exception
     */
    private void freshMgRoles() throws Exception {
        Long projectId = WebUtil.getLongParam("projectId");

        Role managerRole = select(QueryDto.builder()
                .available()
                .addWhere("project.id=" + projectId)
                .addWhere("role.code like '%%_sys_manager'")
                .build())
                .stream()
                .findFirst()
                .orElse(null);

        deleteMiddleTable("mgRoles", managerRole.getId());
        deleteMiddleTable("mgAccounts", managerRole.getId());

        List<Long> allRoleIds = select(QueryDto.builder()
                .available()
                .addWhere("project.id=" + projectId)
                .build())
                .stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());

        for (Long roleId : allRoleIds) {
            insertMiddleTable("mgRoles", managerRole.getId(), roleId);
            insertMiddleTable("mgAccounts", managerRole.getId(), roleId);
        }


    }

    @Override
    public void afterGet(Role domain) throws Exception {
        if (ObjectUtil.notNull(domain.getTrees())) {
            List<Long> treeIds = domain.getTrees().stream().map(Tree::getId).collect(Collectors.toList());
            domain.setActionTreeIds(treeIds);
        }

        if (ObjectUtil.notNull(domain.getMgRoles())) {
            List<Long> mgRoleIds = domain.getMgRoles().stream().map(Role::getId).collect(Collectors.toList());
            domain.setMgRoleIds(mgRoleIds);
        }

        if (ObjectUtil.notNull(domain.getMgAccounts())) {
            List<Long> mgRoleIds = domain.getMgAccounts().stream().map(Role::getId).collect(Collectors.toList());
            domain.setMgAcctIds(mgRoleIds);
        }


        //role list
        Long projectId = WebUtil.getLongParam("projectId");
        QueryDto queryDto = QueryDto.builder().available().addWhere("project.id=" + projectId).build();
        List<OptionDto> optionDtos = select(queryDto).stream()
                .map(p -> OptionDto.builder()
                        .name(p.getName())
                        .value(p.getId().toString())
                        .build())
                .collect(Collectors.toList());
        if (CollectorUtil.isNoTEmpty(optionDtos)) {
            domain.setRoleOptions(optionDtos);
        }

    }


}




