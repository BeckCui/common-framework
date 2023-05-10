package com.dhl.fin.api.controller.system;

import com.dhl.fin.api.common.controller.CommonController;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.domain.Tree;
import com.dhl.fin.api.service.system.TreeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author CuiJianbo
 * @date 2020.02.27
 */
@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("tree")
public class TreeController extends CommonController<Tree> {

    @Autowired
    private TreeServiceImpl treeService;

    @ResponseBody
    @RequestMapping("children")
    public ApiResponse children(Long[] treeIds, @RequestParam String pcode, @RequestParam String joinDomain) throws Exception {
        Tree tree = treeService.getByCode(pcode);
        List<Tree> children;
        children = treeService.getChildrenByTreeIds(tree.getId(), treeIds, joinDomain.split(","));
        return ApiResponse.success(children);
    }

    @ResponseBody
    @RequestMapping("root/{rootCode}")
    public ApiResponse getRoot(@PathVariable String rootCode, @RequestParam String joinDomain) throws Exception {
        Tree tree = treeService.getOrCreatTree(rootCode, joinDomain);
        return ApiResponse.success(tree);
    }

    @ResponseBody
    @RequestMapping("changesort")
    public ApiResponse changeSort(int pos, String dragCode, String targetCode) throws Exception {
        treeService.changeSort(pos, dragCode, targetCode);
        return ApiResponse.success();
    }


}
