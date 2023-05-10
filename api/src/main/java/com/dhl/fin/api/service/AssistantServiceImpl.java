package com.dhl.fin.api.service;

import com.dhl.fin.api.dao.uam.AssistantDao;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class AssistantServiceImpl {

    @Autowired
    private UamServiceImpl uamService;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private AssistantDao assistantDao;

}











