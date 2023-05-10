package com.dhl.fin.api.controller.system;


import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.domain.Account;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import com.dhl.fin.api.service.system.AccountTreeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("acctree")
public class AccountTreeController {

    @Autowired
    private AccountTreeServiceImpl accountTreeService;

    @Autowired
    private AccountServiceImpl accountService;


    @ResponseBody
    @RequestMapping("add")
    public ApiResponse addUserLevel() throws Exception {

        accountTreeService.createUserLevel();

        return ApiResponse.success();
    }

    @ResponseBody
    @RequestMapping("delete/{nodeId}")
    public ApiResponse deleteUserLevel(@PathVariable("nodeId") Long nodeId) throws Exception {

        accountTreeService.deleteUserLevel(nodeId);

        return ApiResponse.success();
    }

    @ResponseBody
    @RequestMapping("users")
    public ApiResponse getFilterUser() throws Exception {
        String uuid = WebUtil.getStringParam("uuid");
        List<Account> accounts = null;
        if (StringUtil.isNotEmpty(uuid)) {
            accounts = accountService.select(QueryDto.builder()
                    .available()
                    .addWhere(String.format("uuid like '%%%s%%' or name like '%%%s%%'", uuid, uuid))
                    .build());
        }

        if (CollectorUtil.isNoTEmpty(accounts)) {
            List<Map> accts = accounts.stream().map(p -> MapUtil.builder()
                    .add("name", StringUtil.isEmpty(p.getName()) ? "" : p.getName())
                    .add("uuid", p.getUuid())
                    .add("id", p.getId())
                    .build())
                    .collect(Collectors.toList());
            return ApiResponse.success(accts);
        } else {
            return ApiResponse.success();
        }

    }


}
