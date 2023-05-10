package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.util.ArrayUtil;
import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.domain.Action;
import com.dhl.fin.api.domain.Menu;
import com.dhl.fin.api.domain.Project;
import com.dhl.fin.api.domain.Tree;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author CuiJianbo
 * @date 2020.02.23
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class MenuServiceImpl extends CommonService<Menu> {

    @Autowired
    private TreeServiceImpl treeService;
    @Autowired
    private ActionServiceImpl actionService;
    @Autowired
    private ProjectServiceImpl projectService;
    @Autowired
    private AccountServiceImpl userService;

    @Override
    public void afterSave(Menu domain) throws Exception {
        //创建菜单对应的tree节点
        Long projectId = WebUtil.getLongParam("projectId");
        Long nodeId = WebUtil.getLongParam("nodeId");
        Long nodeParentId = WebUtil.getLongParam("nodeParentId");
        Tree tree = new Tree(nodeId, domain.getName(), domain.getCode(), null);
        Project project = projectService.get(projectId);

        if (ObjectUtil.isNull(nodeParentId)) {
            String rootCode = project.getCode() + "_menu_root";
            QueryDto queryDto = QueryDto.builder()
                    .available()
                    .addWhere("tree.code = '" + rootCode + "'")
                    .build();
            List<Tree> rootTreeList = treeService.select(queryDto);
            Tree menuRootTree;
            if (CollectorUtil.isNoTEmpty(rootTreeList)) {
                menuRootTree = rootTreeList.get(0);
            } else {
                menuRootTree = new Tree(null, project.getCode() + "_菜单", rootCode, null);
                project.setMenuTree(menuRootTree);
                projectService.saveOrUpdate(project);
            }
            tree.setParent(menuRootTree);
        } else {
            Tree parentTree = treeService.get(nodeParentId);
            tree.setParent(parentTree);
        }
        tree.setMenu(domain);
        treeService.saveDomain(tree, "menu");

        //删除以前勾选的权限
        QueryDto queryDto = QueryDto.builder()
                .addJoinDomain("parent")
                .addJoinDomain("action")
                .addWhere("parent.id = " + nodeId)
                .addWhere(String.format("tree.code like '%%_%s' or " +
                                "tree.code like '%%_%s' or " +
                                "tree.code like '%%_%s' or " +
                                "tree.code like '%%_%s' or " +
                                "tree.code like '%%_%s' or " +
                                "tree.code like '%%_%s' or " +
                                "tree.code like '%%_%s'",
                        ActionEnum.ADD.getCode(),
                        ActionEnum.UPDATE.getCode(),
                        ActionEnum.DELETE.getCode(),
                        ActionEnum.EXPORT.getCode(),
                        ActionEnum.BATCH_DELETE.getCode(),
                        ActionEnum.SELECT_DELETE.getCode(),
                        ActionEnum.BATCH_IMPORT.getCode()
                        )
                ).build();
        List<Tree> treeList = treeService.select(queryDto);
        for (Tree t : treeList) {
            Action action = t.getAction();
            action.setCode(null);
            t.setCode(null);
            actionService.saveOrUpdate(action);
            treeService.deleteDomain(t);
        }

        //创建勾选的权限
        if (WebUtil.containsKey("actions")) {
            String[] actions = WebUtil.getStringArrayParam("actions");
            if (ArrayUtil.isNotEmpty(actions)) {
                for (String item : actions) {
                    ActionEnum actionEnum = ActionEnum.getByCode(item);
                    if (ObjectUtil.isNull(actionEnum)) {
                        continue;
                    }
                    String title = actionEnum.getName();
                    String code = actionEnum.getCode();
                    Tree actionNode = new Tree(null, title, domain.getCode() + "_" + code, tree);
                    Action action = new Action(null, title, domain.getCode() + "_" + code, null);
                    actionService.saveDomain(action);
                    actionNode.setAction(action);
                    treeService.saveDomain(actionNode);
                }
            }
        }
    }


}




