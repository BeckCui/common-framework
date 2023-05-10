package com.dhl.fin.api.service;


import com.dhl.fin.api.dao.uam.UamDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class UamServiceImpl {

    @Autowired
    private UamDao uamDao;

}













