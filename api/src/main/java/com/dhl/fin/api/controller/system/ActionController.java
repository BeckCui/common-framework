package com.dhl.fin.api.controller.system;

import cn.hutool.core.bean.BeanUtil;
import com.dhl.fin.api.common.controller.CommonController;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.domain.Action;
import com.dhl.fin.api.domain.Tree;
import com.dhl.fin.api.service.system.TreeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author CuiJianbo
 * @date 2020.03.01
 */
@RestController
@RequestMapping("action")
public class ActionController extends CommonController<Action> {

    @Autowired
    private TreeServiceImpl treeService;

    @ResponseBody
    @RequestMapping("saveAction")
    public ApiResponse saveAction(HttpServletRequest request, Action domain) throws Exception {
        Map params = request.getParameterMap();

        //保存action对应的tree节点
        Tree tree = new Tree();
        Long treeId = MapUtil.getLong(params, "nodeId");
        if (ObjectUtil.notNull(treeId)) {
            Tree dbTree = treeService.get(treeId, "parent");
            tree.setParent(dbTree.getParent());
            BeanUtil.copyProperties(dbTree, tree);
        } else {
            if (MapUtil.hasKey(params, "checkedMenuId")) {
                Tree dbTree = treeService.get(MapUtil.getLong(params, "checkedMenuId"));
                tree.setParent(dbTree);
            }
        }

        if (ObjectUtil.isNull(domain.getId())) {
            String pcode = tree.getParent().getCode();
            domain.setCode(StringUtil.join(pcode, "_", domain.getCode()));
        }
        tree.setTitle(domain.getName());
        tree.setCode(domain.getCode());
        tree.setLeaf(true);
        tree.setAction(domain);
        treeService.saveDomain(tree, "action");
        //将之前的url删除掉
        if (ObjectUtil.notNull(tree.getId())) {
            for (Tree t : treeService.getChildrenByParentId(tree.getId())) {
                treeService.deleteTreeAndJoinDomain(t);
            }
        }

        //创建新的url
        if (ObjectUtil.notNull(domain.getUrl())) {
            String[] urls = domain.getUrl().split(",");
            for (int i = 0; i < urls.length; i++) {
                String childCode = domain.getCode() + "_" + i;
                Action action = new Action(null, null, childCode, urls[i]);

                Tree childTree = new Tree(null, urls[i], childCode, tree);
                childTree.setAction(action);
                action.setTree(childTree);

                commonService.saveOrUpdate(action);
            }
        }
        return ApiResponse.success();
    }

}






