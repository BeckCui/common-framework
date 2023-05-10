package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.domain.Project;
import com.dhl.fin.api.domain.Role;
import com.dhl.fin.api.domain.Tree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by CuiJianbo on 2020.02.23.
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProjectServiceImpl extends CommonService<Project> {

    @Autowired
    private TreeServiceImpl treeService;

    @Autowired
    private RedisService redisService;

    /**
     * 保存后
     *
     * @param domain
     */
    @Override
    public void beforeSave(Project domain) {
        if (this.actionEnum.equals(ActionEnum.ADD)) {
            domain.setRoles(CollectorUtil.toList(new Role("系统管理员", domain.getCode() + "_sys_manager")));
        }
    }

    /**
     * 保存后
     *
     * @param domain
     */
    @Override
    public void afterSave(Project domain) throws Exception {
        if (this.actionEnum.equals(ActionEnum.ADD)) {

            Tree rootMenu = new Tree(null, domain.getName() + "-菜单", domain.getCode() + "_menu_root", null);
            rootMenu.setLeaf(true);

            treeService.saveOrUpdate(rootMenu);
            domain.setMenuTree(rootMenu);
            saveOrUpdate(domain);

            Long managerRoleId = domain.getRoles()
                    .stream()
                    .filter(role -> role.getCode().equalsIgnoreCase(domain.getCode() + "_sys_manager"))
                    .map(Role::getId)
                    .findFirst().orElse(null);
            if (ObjectUtil.notNull(managerRoleId)) {
                treeService.insertMiddleTable("roles", rootMenu.getId(), managerRoleId);
            }
        }

        cacheProjects();
    }

    public void cacheProjects() throws Exception {
        List<Project> projects = select(QueryDto.builder().available().build());
        redisService.put(CacheKeyEnum.ALL_PROJECTS, projects);
    }


}






