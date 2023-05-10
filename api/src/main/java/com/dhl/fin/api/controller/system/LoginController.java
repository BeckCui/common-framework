package com.dhl.fin.api.controller.system;

import cn.hutool.core.collection.CollectionUtil;
import com.dhl.fin.api.common.authentication.TokenService;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.dto.UserInfo;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.enums.LogStatus;
import com.dhl.fin.api.common.enums.LoginStatus;
import com.dhl.fin.api.common.exception.BusinessException;
import com.dhl.fin.api.common.exception.LoginException;
import com.dhl.fin.api.common.service.LdapService;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.domain.Account;
import com.dhl.fin.api.domain.Project;
import com.dhl.fin.api.domain.SystemConfig;
import com.dhl.fin.api.domain.Tree;
import com.dhl.fin.api.dto.LoginResponse;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import com.dhl.fin.api.service.system.LogServiceImpl;
import com.dhl.fin.api.service.system.LoginServiceImpl;
import com.dhl.fin.api.service.system.SystemConfigServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 4/1/2020
 */
@Slf4j
@RestController
@RequestMapping("login")
public class LoginController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private LoginServiceImpl loginService;


    @Autowired
    private RedisService redisService;

    @Autowired
    private LogServiceImpl logService;

    @Autowired
    private SystemConfigServiceImpl systemConfigService;

    @Autowired
    private LdapService ldapService;


    @Value("${spring.profiles.active}")
    private String profiles;


    @CrossOrigin
    @RequestMapping("/login")
    public ApiResponse loginUser(String password, String userName) throws Exception {
        String active = SpringContextUtil.getPropertiesValue("spring.profiles.active");
        if (active.equalsIgnoreCase("dev")) {
            loginService.checkSqlInject();
        }

        if (StringUtil.isEmpty(userName) || userName.equals("system")) {
            return ApiResponse.success();
        }

        LoginResponse loginResponse = userLogin(password, userName);
        logService.log(null, ActionEnum.LOGIN.getCode(), LogStatus.SUCCESS.getCode(), ActionEnum.LOGIN.getName());
        return ApiResponse.success(loginResponse);
    }

    @RequestMapping("/loginout")
    public ApiResponse loginOut() throws Exception {


        String uuid = WebUtil.getLoginUser().getUuid();
        Map loginUsers = redisService.getMap(CacheKeyEnum.LOGIN_USERS);
        if (ObjectUtil.isNull(loginUsers)) {
            return ApiResponse.success();
        }

        loginUsers.remove(uuid);
        redisService.put(CacheKeyEnum.LOGIN_USERS, loginUsers);

        Map data = redisService.getMap(CacheKeyEnum.ACTIVE_USER);

        log.info(JsonUtil.objectToString(data));

        //active user 减一
        Map<String, Object> activeUsersPerApp = redisService.getMap(CacheKeyEnum.ACTIVE_USER);
        for (Map.Entry<String, Object> item : activeUsersPerApp.entrySet()) {
            String appCode = item.getKey();
            Map<String, Object> appKV = (Map) item.getValue();
            Map<String, Object> users = MapUtil.getMap(appKV, "users");
            Map<String, Object> newUsers = new HashMap<>();
            for (String key : users.keySet()) {
                if (!uuid.equalsIgnoreCase(key)) {
                    newUsers.put(key, users.get(key));
                }
            }
            appKV.put("users", newUsers);
        }

        redisService.put(CacheKeyEnum.ACTIVE_USER, activeUsersPerApp);

        logService.log(null, ActionEnum.LOGIN_OUT.getCode(), LogStatus.SUCCESS.getCode(), ActionEnum.LOGIN_OUT.getName());

        return ApiResponse.success();
    }


    public void setDefaultUrl(LoginResponse loginResponse, Account account) throws Exception {
        String userName = account.getUuid();
        List<Project> projects = accountService.getProjectsByUUID(userName);
        Map<String, List<Tree>> menus = accountService.getMenuTreePerProject(userName);
        String defaultRouteUrl = null;
        Map<String, String> urlParams = new HashMap<>();
        if (CollectionUtil.isNotEmpty(menus) && CollectionUtil.isNotEmpty(projects)) {
            String projectCode = projects.get(0).getCode();
            String routePath = "";
            List<Tree> projectMenus = menus.get(projectCode);
            if (CollectionUtil.isNotEmpty(projectMenus)) {
                List<Tree> childrenMenu = projectMenus.get(0).getChildren();
                if (CollectionUtil.isNotEmpty(childrenMenu)) {
                    routePath = childrenMenu.get(0).getMenu().getClickUrl();
                } else {
                    routePath = projectMenus.get(0).getMenu().getClickUrl();
                }
            }
            defaultRouteUrl = StringUtil.join(projectCode.toLowerCase(), "/", routePath);
            urlParams.put("m_c", projectCode);
        }

        if (StringUtil.isEmpty(defaultRouteUrl) && account.getIsSuperManager()) {
            defaultRouteUrl = "menu";
        }


        if (StringUtil.isEmpty(defaultRouteUrl)) {
            throw new BusinessException("此账号没有配置可使用的菜单");
        }

        loginResponse.setRouteUrl(defaultRouteUrl);
        loginResponse.setUrlParams(urlParams);

    }


    public LoginResponse userLogin(String password, String userName) throws Exception {

        LoginResponse loginResponse = new LoginResponse();
        boolean isLogin = true;
        boolean loginUsePwd = true;
        SystemConfig systemConfig = systemConfigService.get(QueryDto.builder().available().addWhere("code='loginUsePwd'").build());
        loginUsePwd = ObjectUtil.isNull(systemConfig) ? true : Boolean.valueOf(systemConfig.getValue());
        Account loginAccount = accountService.get(
                QueryDto.builder()
                        .available()
                        .addJoinDomain("roles")
                        .addWhere(String.format("account.uuid='%s'", userName.trim()))
                        .addWhere(String.format("account.available=1"))
                        .build()
        );

        if (ObjectUtil.isNull(loginAccount)) {
            String logInfo = "FINTP没有添加此账号：" + userName;
            log.info(logInfo);
            throw new LoginException(logInfo);
        }

        Map userData = new HashMap();
        if (loginUsePwd) {
            if (StringUtil.isEmpty(password)) {
                loginResponse.setStatus(LoginStatus.VERIFICATIONFAILED.getStatus());
                String msg = "密码不能为空";
                throw new LoginException(msg);
            } else {
                userData = ldapService.auth(userName, password);
                if (!userData.isEmpty()) {
                    isLogin = true;
                } else {
                    String errorInf = String.format("用户名或密码错误（%s）", userName);
                    log.info(errorInf);
                    throw new LoginException(errorInf);
                }
            }
        } else {
            isLogin = true;
        }


        if (isLogin) {
            Account account = accountService.get(QueryDto.builder().available().addWhere("account.uuid='" + userName + "'").build());
            setDefaultUrl(loginResponse, loginAccount);
            UserInfo userInfo = UserInfo.builder()
                    .uuid(loginAccount.getUuid())
                    .email(account.getEmail())
                    .userName(loginAccount.getName())
                    .adminType(account.getIsSuperManager() ? "superManager" : "")
                    .defaultRoute(loginResponse.getRouteUrl())
                    .build();
            loginResponse.setStatus(LoginStatus.SUCCESS.getStatus());
            loginResponse.setToken(tokenService.generateToken(userInfo));
            loginResponse.setUuid(userInfo.getUuid());
            loginResponse.setUserId(loginAccount.getId().toString());
            loginResponse.setUserName(userInfo.getUserName());

            account.setLoginDate(DateUtil.getSysTime());

            accountService.updateBySelective(account);

        }


        log.info("登录成功：" + userName);
        return loginResponse;
    }


    @ResponseBody
    @RequestMapping("acctdata")
    public ApiResponse getAccountInfo() {
        try {
            String uuid = WebUtil.getLoginUser().getUuid();
            Object permission = loginService.getUserPermissionFromCache(uuid);
            return ApiResponse.success(permission);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException("", ActionEnum.GET_DOMAIN, LogStatus.FAILED, "获取人员权限信息失败");
        }

    }

}


