package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.domain.BasicDomain;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.dao.fin.TreeDaoOwner;
import com.dhl.fin.api.domain.Action;
import com.dhl.fin.api.domain.Menu;
import com.dhl.fin.api.domain.Tree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * @author CuiJianbo
 * @date 2020.02.27
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class TreeServiceImpl extends CommonService<Tree> {

    @Autowired
    private TreeDaoOwner treeDaoOwner;

    @Override
    public void beforePageQuery(QueryDto queryDto) throws Exception {
        addRequestJoin(queryDto);
    }

    /**
     * list查询前
     *
     * @param queryDto
     */
    @Override
    public void beforeList(QueryDto queryDto) throws Exception {
        addRequestJoin(queryDto);
    }

    private void addRequestJoin(QueryDto queryDto) throws Exception {
        if (WebUtil.containsKey("joinDomain")) {
            queryDto.getJoinDomain().add(WebUtil.getStringParam("joinDomain"));
        }
    }

    public List<Tree> getChildrenByParentId(Long parentId, String... joinDomain) throws Exception {
        QueryDto queryDto = QueryDto.builder()
                .available()
                .addWhere(String.format("tree.parent_id = %s", parentId))
                .addAllJoinDomain(Arrays.stream(joinDomain).collect(Collectors.toList()))
                .addOrder("tree.order_num")
                .build();
        return select(queryDto);
    }

    public List<Tree> getChildrenByTreeIds(Long parentId, Long[] treeIds, String... joinDomain) throws Exception {
        String TreeCodes = null;
        if (ArrayUtil.isNotEmpty(treeIds)) {
            List<Tree> list = findDomainListByIds(Arrays.asList(treeIds), Tree.class);
            String actionCodes = list.stream()
                    .map(Tree::getParentLine)
                    .filter(StringUtil::isNotEmpty)
                    .map(p -> p.split("->"))
                    .flatMap(Arrays::stream)
                    .filter(ObjectUtil::notNull)
                    .distinct().collect(joining(","));
            TreeCodes = "," + actionCodes + ",";
        }
        return getChildren(parentId, TreeCodes, joinDomain);
    }

    private List<Tree> getChildren(Long parentId, String treeCodes, String... joinDomain) throws Exception {
        List<Tree> children = getChildrenByParentId(parentId, joinDomain);
        if (CollectorUtil.isNoTEmpty(children)) {
            for (Tree tree : children) {
                if (ObjectUtil.notNull(treeCodes) && treeCodes.contains("," + tree.getCode() + ",")) {
                    List<Tree> childrenList = getChildren(tree.getId(), treeCodes, joinDomain);
                    if (CollectorUtil.isNoTEmpty(childrenList)) {
                        tree.setChildren(childrenList);
                    }
                } else {
                    tree.setChildrenData(getChildrenByParentId(tree.getId(), joinDomain));
                }
            }
            return children;
        }
        return null;
    }


    public List<Tree> getAllChildrenByRootIds(List<Long> rootIds, String... joinDomain) throws Exception {
        List<Tree> allTree = new LinkedList<>();
        for (Long id : rootIds) {
            allTree.addAll(getAllChildrenByRootIds(id, joinDomain));
        }
        return allTree;
    }

    /**
     * 获取某个节点下的所有子节点
     *
     * @param rootId
     * @param joinDomain
     * @return
     * @throws Exception
     */
    public List<Tree> getAllChildrenByRootIds(Long rootId, String... joinDomain) throws Exception {
        List<Tree> children = getChildrenByParentId(rootId, joinDomain);
        List<List<Tree>> treeList = new LinkedList<>();
        if (CollectorUtil.isNoTEmpty(children)) {
            for (Tree tree : children) {
                List<Tree> trees = getAllChildrenByRootIds(tree.getId(), joinDomain);
                if (CollectorUtil.isNoTEmpty(trees)) {
                    treeList.add(trees);
                }
            }
        }
        for (List<Tree> trees : treeList) {
            children.addAll(trees);
        }
//        children.add(get(rootId, joinDomain));
        return children;
    }

    /**
     * 通过code获取tree
     *
     * @param code
     * @param joinDomain
     * @return
     * @throws Exception
     */
    public Tree getByCode(String code, String... joinDomain) throws Exception {
        String jDomain = ArrayUtil.isNotEmpty(joinDomain) ? joinDomain[0] : null;
        QueryDto queryDto = QueryDto.builder()
                .available()
                .addWhere(String.format("tree.code = '%s'", code))
                .addJoinDomain("parent")
                .addJoinDomainWhen(jDomain, ObjectUtil.notNull(jDomain))
                .build();
        return get(queryDto);
    }

    public Tree getOrCreatTree(String code, String... joinDomain) throws Exception {
        Tree t = getByCode(code, joinDomain);

        if (ObjectUtil.isNull(t)) {
            Tree tree = new Tree();
            tree.setCode(code);
            tree.setTitle("Root");
            tree.setRemove(false);
            saveDomain(tree);
            return tree;
        }

        return t;
    }

    /**
     * 在保存前
     *
     * @param domain
     */
    @Override
    public void beforeSave(Tree domain) throws Exception {
        if (ObjectUtil.isNull(domain.getId())) {
            if (ObjectUtil.notNull(domain.getParent()) && ObjectUtil.notNull(domain.getParent().getId())) {
                Long parentId = domain.getParent().getId();
                Tree pdomain = get(parentId);
                String parentName = StringUtil.isEmpty(pdomain.getParentLine()) ? pdomain.getCode() : pdomain.getParentLine();
                List<Tree> children = getChildrenByParentId(parentId);
                int newSort;
                if (CollectorUtil.isEmpty(children)) {
                    newSort = 1;
                } else {
                    Tree lastNode = children.get(children.size() - 1);
                    newSort = lastNode.getOrderNum() + 1;
                }
                domain.setParentLine(parentName + "->" + domain.getCode());
                domain.setOrderNum(newSort);
            } else {
                domain.setOrderNum(1);
            }
        }
    }

    /**
     * 在保存后
     *
     * @param domain
     */
    @Override
    public void afterSave(Tree domain) throws Exception {
        if (ObjectUtil.notNull(domain.getId())) {
            validateIsLeaf(domain.getId());
        }
        if (ObjectUtil.notNull(domain.getParent()) && ObjectUtil.notNull(domain.getParent().getId())) {
            validateIsLeaf(domain.getParent().getId());
        }
    }

    @Override
    public int delete(Tree domain) throws Exception {
        deleteAllChildren(domain);
        validateIsLeaf(domain.getParent().getId());
        return 1;
    }

    /**
     * 递归删除所有子节点
     *
     * @param tree
     * @throws Exception
     */
    public void deleteAllChildren(Tree tree) throws Exception {
        List<Tree> children = getChildrenByParentId(tree.getId());
        if (CollectorUtil.isNoTEmpty(children)) {
            for (Tree child : children) {
                deleteAllChildren(child);
            }
        }
        deleteTreeAndJoinDomain(tree);
    }

    /**
     * 删除节点和节点对应的domain
     *
     * @param tree
     * @throws Exception
     */
    public void deleteTreeAndJoinDomain(Tree tree) throws Exception {
        tree.setCode(null);
        tree.setRemove(true);
        saveOrUpdate(tree);
        List<Method> methods = Arrays.stream(tree.getClass().getMethods())
                .filter(p -> p.getReturnType().getSuperclass() != null
                        && !p.getReturnType().equals(Tree.class)
                        && p.getReturnType().getSuperclass().equals(BasicDomain.class))
                .collect(Collectors.toList());
        for (Method m : methods) {
            BasicDomain basicDomain = (BasicDomain) m.invoke(tree);
            if (ObjectUtil.notNull(basicDomain)) {
                CommonService joinDomainService = SpringContextUtil.getServiceImplByDomain(m.getReturnType());
                BasicDomain domain = joinDomainService.get(basicDomain.getId());
                if (ObjectUtil.isNull(domain)) {
                    continue;
                }
                domain.setRemove(true);
                if (domain instanceof Action) {
                    ((Action) domain).setCode(null);
                }
                if (domain instanceof Menu) {
                    ((Menu) domain).setCode(null);
                }
                if (domain instanceof Tree) {
                    ((Tree) domain).setCode(null);
                }
                joinDomainService.saveOrUpdate(domain);
            }
        }
    }

    public void validateIsLeaf(Long id) throws Exception {
        if (ObjectUtil.isNull(id)) {
            return;
        }
        Tree parentTree = new Tree(id);
        int n = count(QueryDto.builder().available().addWhere("tree.parent_id = " + id).build());
        if (n == 0) {
            parentTree.setLeaf(true);
        } else {
            parentTree.setLeaf(false);
        }
        updateBySelective(parentTree);
    }

    /**
     * 提交数据前的数据校验
     * <p>
     * 返回数据为前端显示的提示信息
     *
     * @param domain
     */
    @Override
    public String validate(Tree domain) throws Exception {
        int n = count(QueryDto.builder()
                .addWhere(String.format("tree.code = '%s'", domain.getCode()))
                .addWhereWhen(String.format("tree.id != %s", domain.getId()), ObjectUtil.notNull(domain.getId()))
                .build());
        return n > 0 ? String.format("code为%s的节点已经存在", domain.getCode()) : null;
    }

    public void changeSort(int pos, String dragCode, String targetCode) throws Exception {
        Tree targetTree = getByCode(targetCode);
        List<Tree> children = ObjectUtil.isNull(targetTree.getParent()) ? null : getChildrenByParentId(targetTree.getParent().getId());
        Tree startTree;
        if (pos == 1) {
            startTree = ObjectUtil.isNull(children) ? targetTree : children.stream().filter(p -> p.getOrderNum() > targetTree.getOrderNum()).findFirst().orElse(null);
        } else {
            startTree = targetTree;
        }

        String pcode = targetTree.getParent().getCode();
        treeDaoOwner.updateSort(pcode, ObjectUtil.isNull(startTree) ? Integer.MAX_VALUE : startTree.getOrderNum());

        Tree dragNode = getByCode(dragCode);
        Long dragNodePid = dragNode.getParent().getId();
        dragNode.setOrderNum(ObjectUtil.isNull(startTree) ? targetTree.getOrderNum() + 1 : startTree.getOrderNum());
        saveOrUpdate(dragNode);

        if (pos == 0) {
            treeDaoOwner.updateParentIdDragInner(targetCode, dragCode);
        } else {
            treeDaoOwner.updateParentIdDrag(targetCode, dragCode);
        }
        validateIsLeaf(dragNodePid);

    }


}




