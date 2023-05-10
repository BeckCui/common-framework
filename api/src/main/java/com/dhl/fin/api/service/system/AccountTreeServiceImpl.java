package com.dhl.fin.api.service.system;


import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.domain.Account;
import com.dhl.fin.api.domain.Tree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountTreeServiceImpl {

    @Autowired
    private TreeServiceImpl treeService;

    @Autowired
    private AccountServiceImpl accountService;


    public void deleteUserLevel(Long nodeId) throws Exception {
        Tree tree = treeService.get(nodeId);
        treeService.delete(tree);
    }

    public void createUserLevel() throws Exception {

        Long acctId = WebUtil.getLongParam("acctId");
        Long nodeParentId = WebUtil.getLongParam("nodeParentId");

        Account account = accountService.get(acctId);

        String acctName = account.getName();
        String acctUuid = account.getUuid();

        Tree parentTree = treeService.get(nodeParentId);

        Tree tree = new Tree(null, StringUtil.join(acctName, "(", acctUuid, ")"), acctUuid, parentTree);

        treeService.saveDomain(tree);


    }

    /**
     * 获取下级一层的人员
     */
    public List<String> getChildren(String uuid) throws Exception {

        List<String> uuids = treeService.select(QueryDto.builder()
                .available()
                .addJoinDomain("parent")
                .addWhere("parent.code = '" + uuid + "'")
                .build()).stream()
                .map(p -> p.getCode().trim())
                .collect(Collectors.toList());

        return uuids;
    }


    /**
     * 获取当前人下挂着的所有层的人员
     */
    public List<String> getAllChildren(String uuid) throws Exception {

        Tree parent = treeService.get(QueryDto.builder().addWhere("code = '" + uuid + "'").build());
        if (ObjectUtil.notNull(parent)) {
            List<String> uuids = treeService.getAllChildrenByRootIds(parent.getId())
                    .stream()
                    .map(p -> p.getCode())
                    .collect(Collectors.toList());
            return uuids;
        }

        return null;
    }


}



