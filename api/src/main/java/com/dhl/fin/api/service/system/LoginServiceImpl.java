package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.LoginUserPermissionDto;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.exception.SqlInjectionException;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author becui
 * @date 5/1/2020
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@Service
public class LoginServiceImpl {


    @Autowired
    private AccountServiceImpl accountService;


    @Autowired
    private RedisService redisService;

    public LoginUserPermissionDto getUserPermissionFromCache(String uuid) throws Exception {
        LoginUserPermissionDto o = redisService.getUserPermission(uuid);

        if (ObjectUtil.notNull(o)) {
            return o;
        } else {
            LoginUserPermissionDto loginUserPermission = getLoginUserPermission();
            Map loginUsers = redisService.getMap(CacheKeyEnum.LOGIN_USERS);
            if (ObjectUtil.isNull(loginUsers)) {
                loginUsers = MapUtil.builder().add(uuid, loginUserPermission).build();
            } else {
                loginUsers.put(uuid, loginUserPermission);
            }

            redisService.put(CacheKeyEnum.LOGIN_USERS, loginUsers);
            return loginUserPermission;
        }
    }

    public LoginUserPermissionDto getLoginUserPermission() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        String uuid = WebUtil.getLoginUser().getUuid();
        Map dictionaries = redisService.getMap(CacheKeyEnum.DICTIONARIES_PER_APP);
        Account account = accountService.getUserByUuid(uuid);
        List<Integer> count = new LinkedList<>();

        LoginUserPermissionDto userPermissionDto = new LoginUserPermissionDto();
        userPermissionDto.setDictionaries(dictionaries);
        userPermissionDto.setLoginUser(MapUtil.beanToMap(account));

        executorService.execute(new projectsThread(userPermissionDto, count, uuid));
        executorService.execute(new manageProjectsThread(userPermissionDto, count, uuid));
        executorService.execute(new menuTreesThread(userPermissionDto, count, uuid));
        executorService.execute(new actionsThread(userPermissionDto, count, uuid));
        executorService.execute(new rolesThread(userPermissionDto, count, uuid));

        while (count.size() < 5) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(p -> log.error(p.toString()));
            }
        }
        executorService.shutdown();
        return userPermissionDto;
    }

    private class projectsThread implements Runnable {

        private String uuid;
        private LoginUserPermissionDto userPermissionDto;
        private List<Integer> count;

        projectsThread(LoginUserPermissionDto userPermissionDto, List<Integer> count, String uuid) {
            this.uuid = uuid;
            this.userPermissionDto = userPermissionDto;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                List<Project> projects = accountService.getProjectsByUUID(uuid);
                userPermissionDto.setProjects(projects);
            } catch (Exception e) {
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(p -> log.error(p.toString()));
            }

            this.count.add(1);
        }
    }

    private class manageProjectsThread implements Runnable {

        private String uuid;
        private LoginUserPermissionDto userPermissionDto;
        private List<Integer> count;

        manageProjectsThread(LoginUserPermissionDto userPermissionDto, List<Integer> count, String uuid) {
            this.uuid = uuid;
            this.userPermissionDto = userPermissionDto;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                List<Project> manageProjects = accountService.getManageProjectsByUUID(uuid);
                userPermissionDto.setManageProjects(manageProjects);
            } catch (Exception e) {
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(p -> log.error(p.toString()));
            }
            this.count.add(1);
        }
    }

    private class menuTreesThread implements Runnable {

        private String uuid;
        private LoginUserPermissionDto userPermissionDto;
        private List<Integer> count;

        menuTreesThread(LoginUserPermissionDto userPermissionDto, List<Integer> count, String uuid) {
            this.uuid = uuid;
            this.userPermissionDto = userPermissionDto;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                Map<String, List<Tree>> menuTrees = accountService.getMenuTreePerProject(uuid);
                userPermissionDto.setMenus(menuTrees);
            } catch (Exception e) {
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(p -> log.error(p.toString()));
            }
            this.count.add(1);
        }
    }

    private class actionsThread implements Runnable {

        private String uuid;
        private LoginUserPermissionDto userPermissionDto;
        private List<Integer> count;

        actionsThread(LoginUserPermissionDto userPermissionDto, List<Integer> count, String uuid) {
            this.uuid = uuid;
            this.userPermissionDto = userPermissionDto;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                Map<String, List<Action>> actions = accountService.getActionsPerProject(uuid);
                userPermissionDto.setActions(actions);
            } catch (Exception e) {
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(p -> log.error(p.toString()));
            }
            this.count.add(1);
        }
    }

    private class rolesThread implements Runnable {

        private String uuid;
        private LoginUserPermissionDto userPermissionDto;
        private List<Integer> count;

        rolesThread(LoginUserPermissionDto userPermissionDto, List<Integer> count, String uuid) {
            this.uuid = uuid;
            this.userPermissionDto = userPermissionDto;
            this.count = count;
        }

        @Override
        public void run() {
            try {
                Map<String, List<Role>> roles = accountService.getRolesPerProject(uuid);
                userPermissionDto.setRoles(roles);
            } catch (Exception e) {
                e.printStackTrace();
                Arrays.stream(e.getStackTrace()).forEach(p -> log.error(p.toString()));
            }
            this.count.add(1);
        }
    }


    public void checkSqlInject() {
        String inj_str = "'|\"|and|*|#|/| - |+";

        String inj_stra[] = inj_str.split("\\|");

        Map<String, String[]> params = WebUtil.getRequestParams();
        for (Map.Entry<String, String[]> o : params.entrySet()) {
            for (int i = 0; i < inj_stra.length; i++) {
                if (ArrayUtil.isNotEmpty(o.getValue()) && StringUtil.isNotEmpty(o.getValue()[0])) {
                    String v = o.getValue()[0];
                    if (v.indexOf(inj_stra[i]) >= 0) {
                        throw new SqlInjectionException();
                    }
                }
            }
        }
    }
}
