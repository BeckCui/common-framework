package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.service.LdapService;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.common.util.excel.ExcelSheet;
import com.dhl.fin.api.common.util.mail.MailUtil;
import com.dhl.fin.api.domain.Account;
import com.dhl.fin.api.domain.PassWord;
import com.dhl.fin.api.enums.AccountType;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Getter
@Setter
@Transactional(rollbackFor = Exception.class)
public class PasswordServiceImpl extends CommonService<PassWord> {

    private String newPassword;

    @Autowired
    LdapService ldapService;

    @Autowired
    RedisService redisService;

    @Autowired
    private AccountServiceImpl accountService;


    @Override
    public void afterGet(PassWord domain) throws Exception {

        Map dic = redisService.getMap(CacheKeyEnum.DICTIONARIES_PER_APP);
        Map fintpMap = MapUtil.getMap(dic, "fintp");
        Map<String, String> appcodes = MapUtil.getMap(fintpMap, "appCodes");
        if (ObjectUtil.notNull(appcodes)) {
            List<String> appCodes = appcodes.entrySet().stream().map(p -> p.getKey()).collect(Collectors.toList());
            domain.setAppCodes(appCodes);
        }

        if (StringUtil.isNotEmpty(domain.getPwd())) {
            String pwd = SecUtil.decrypt(domain.getPwd());
            domain.setPwd(pwd);
        }

        List<Map> superManager = redisService.getList(CacheKeyEnum.SUPER_MANAGER);
        if (CollectorUtil.isNoTEmpty(superManager)) {
            List<String> managers = superManager
                    .stream()
                    .map(p -> MapUtil.getString(p, "name") + "-" + MapUtil.getString(p, "email"))
                    .collect(Collectors.toList());
            domain.setOwnerSelective(managers);
        }

    }

    @Override
    public String validate(PassWord domain) throws Exception {
        String result = null;
        String account = domain.getAccount();
        String oldPassword = domain.getPwd();
        String acctType = domain.getAcctType();
        String project = domain.getProject();

        if (ObjectUtil.notNull(WebUtil.getRequest())) {
            newPassword = WebUtil.getStringParam("newPassword");
            String passWordMiddle = WebUtil.getStringParam("passWordMiddle");
            String passWordPrefix = WebUtil.getStringParam("passWordPrefix");
            if (StringUtil.isNotEmpty(newPassword)) {
                newPassword = StringUtil.join(passWordPrefix, passWordMiddle, newPassword);
            }
        }

        result = super.validate(domain);

        if (StringUtil.isNotEmpty(result)) {
            return result;
        }

        if (StringUtil.isNotEmpty(project) && project.equals("RPA") && StringUtil.isNotEmpty(newPassword) && newPassword.length() < 26) {
            return "密码长度不能少于26位";
        } else if (StringUtil.isNotEmpty(newPassword) && newPassword.length() < 16) {
            return "密码长度不能少于16位";
        }

        if (StringUtil.isNotEmpty(oldPassword)
                && StringUtil.isNotEmpty(newPassword)
                && StringUtil.isNotEmpty(acctType)
                && acctType.equals(AccountType.AD.getCode())) {
            result = WindowsUtil.setADUserNewPWD(account, oldPassword, newPassword);

            String historyPwd = domain.getHistoryPwd();
            if (StringUtil.isEmpty(historyPwd)) {
                domain.setHistoryPwd(newPassword);
            } else {
                if (historyPwd.split(",").length > 5) {
                    int start = historyPwd.indexOf(",") + 1;
                    historyPwd = historyPwd.substring(start);
                }
                domain.setHistoryPwd(StringUtil.join(historyPwd, ",", newPassword));
            }
            log.info("update password result:" + result);
        }
        return result;
    }

    @Override
    public void beforeSave(PassWord passWordData) throws Exception {

        String account = passWordData.getAccount();
        String acctType = passWordData.getAcctType();
        String oldPassword = passWordData.getPwd();

        if (StringUtil.isNotEmpty(oldPassword)) {
            String encryptPassword = SecUtil.encrypt(oldPassword);
            passWordData.setPwd(encryptPassword);

            if (StringUtil.isNotEmpty(newPassword)) {
                encryptPassword = SecUtil.encrypt(newPassword);
                passWordData.setPwd(encryptPassword);
                passWordData.setLastUpdateDate(DateUtil.getSysDate());

                if (passWordData.getCycle() > 0) {
                    Calendar nowDate = Calendar.getInstance();
                    nowDate.add(Calendar.DAY_OF_MONTH, passWordData.getCycle());
                    passWordData.setManualExpireDate(nowDate.getTime());
                } else {
                    passWordData.setManualExpireDate(null);
                }

            }

            if (StringUtil.isNotEmpty(acctType) && passWordData.getAcctType().equals(AccountType.AD.getCode())) {
                if (StringUtil.isNotEmpty(newPassword) || ObjectUtil.isNull(passWordData.getAdExpireDate())) {
                    String expireDate = WindowsUtil.getADUserExpireDate(account);
                    passWordData.setAdExpireDate(DateUtil.getFullDate(expireDate));
                }
            }

        }


        if (ObjectUtil.isNull(passWordData.getCycle()) || passWordData.getCycle() == 0) {
            passWordData.setManualExpireDate(null);
        } else if (ObjectUtil.notNull(passWordData.getLastUpdateDate())) {
            int cycle = passWordData.getCycle();
            Date lastUpdate = passWordData.getLastUpdateDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastUpdate);
            calendar.add(Calendar.DAY_OF_MONTH, cycle);
            passWordData.setManualExpireDate(calendar.getTime());
        }


    }

    @Override
    public void afterSave(PassWord passWordData) throws Exception {

        Object request = WebUtil.getRequest();

        if (ObjectUtil.isNull(request)) {
            if (StringUtil.isNotEmpty(newPassword)) {
                String account = passWordData.getAccount();
                String ownerName = passWordData.getOwner();
                Account owner = accountService.get(QueryDto.builder().available().addWhere("name='" + ownerName + "'").build());

                String content = String.format("Hi %s<br/>&nbsp;&nbsp;&nbsp;&nbsp;%s的密码到期自动被重置，它的新密码是：%s，请知悉。", ownerName, account, newPassword);
                MailUtil.builder()
                        .setFrom("FINTP@dhl.com")
                        .addTo(owner.getEmail())
                        .setContent(content)
                        .setTitle(account + "的新密码")
                        .build()
                        .send();
            }
        }

        setNewPassword(null);
    }


    @Override
    public List<ExcelSheet> validateExcelData(List<PassWord> domainList, Map<String, Object> excelContext) throws Exception {
        for (PassWord passWord : domainList) {
            String oldPassword = passWord.getPwd();

            if (StringUtil.isNotEmpty(oldPassword)) {
                String encryptPassword;
                encryptPassword = SecUtil.encrypt(oldPassword);
                passWord.setPwd(encryptPassword);
            }

            String expireDate = WindowsUtil.getADUserExpireDate(passWord.getAccount());
            passWord.setAdExpireDate(DateUtil.getFullDate(expireDate));

            log.info(passWord.getAccount() + ":" + DateUtil.getTime(passWord.getAdExpireDate()));
        }
        return null;
    }

    public List<Map> findADInformation(List<String> valueList, String field) throws Exception {
        PassWord fintpAcct = get(QueryDto.builder().available().addWhere("account='srv_cnexp_fintp'").build());
        String pwd = SecUtil.decrypt(fintpAcct.getPwd());
        List<Map> accounts = new LinkedList<>();
        int n = 0;
        for (String value : valueList) {

            if (StringUtil.isEmpty(value)) continue;

            value = value.trim();
            Map accountData = new HashMap();
            accountData.put("cn", value);
            try {
                accountData = ldapService.queryAccount("srv_cnexp_fintp", pwd, field, value);
            } catch (Exception e) {
                e.printStackTrace();
                accountData.put("description", ObjectUtil.isNull(e.getCause()) ? "异常" : e.getCause().getMessage());
                accountData.put("cn", value);
                accounts.add(accountData);
                continue;
            }

            if (accountData.size() == 0) {
                accountData.put("description", "AD系统没有此账号");
                accountData.put("cn", value);
                accountData.put("status", "禁用");
                accounts.add(accountData);
                continue;
            }

            String account = MapUtil.getString(accountData, "cn");
            String manager = MapUtil.getString(accountData, "manager");
            String status = MapUtil.getString(accountData, "userAccountControl");


            if (StringUtil.isNotEmpty(manager)) {
                manager = manager.split(",")[0].split("=")[1];
                accountData.put("manager", manager);

               /* if (StringUtil.isNotEmpty(manager)) {
                    Map managerAccount = null;
                    try {
                        managerAccount = ldapService.queryAccountByUUID("srv_cnexp_fintp", pwd, manager);
                        if (managerAccount.size() > 0) {
                            String managerMail = MapUtil.getString(managerAccount, "mail");
                            accountData.put("managerMail", managerMail);
                        }
                    } catch (CommunicationException e) {
                        e.printStackTrace();
                        accountData.put("description", ObjectUtil.isNull(e.getCause()) ? "异常" : e.getCause().getMessage());
                        accounts.add(accountData);
                        continue;
                    }
                }*/

            }


            if (StringUtil.isNotEmpty(status)) {
                status = status.equals("512") ? "正常" : (status.equals("514") ? "禁用" : "其他");
                accountData.put("status", status);
            }


            if (valueList.size() == 1) {
                String lastLoginDate = null;
                try {
                    lastLoginDate = WindowsUtil.getADUserLastLoginDate(account);

                    accountData.put("lastlogon", lastLoginDate);

                    String expireDate = WindowsUtil.getADUserExpireDate(account);

                    accountData.put("expireDate", expireDate);

                } catch (Exception e) {
                    e.printStackTrace();
                    StackTraceElement[] elements = e.getStackTrace();
                    if (e.getCause() == null) {
                        for (StackTraceElement element : elements) {
                            log.info(element.toString());
                        }
                        accountData.put("description", "出现异常");
                    } else {
                        accountData.put("description", e.getCause());
                    }

                }
            }

            accounts.add(accountData);


            Thread.sleep(100);
            n++;

            log.info("完成了: " + n * 1.0 / valueList.size());

        }
        return accounts;
    }


}







