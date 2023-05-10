package com.dhl.fin.api.controller.system;

import com.dhl.fin.api.common.annotation.Excel;
import com.dhl.fin.api.common.controller.CommonController;
import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.service.LdapService;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.common.util.excel.ExcelSheet;
import com.dhl.fin.api.common.util.excel.ExcelUtil;
import com.dhl.fin.api.domain.Account;
import com.dhl.fin.api.domain.PassWord;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import com.dhl.fin.api.service.system.PasswordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;


@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("password")
public class PasswordController extends CommonController<PassWord> {


    @Autowired
    private PasswordServiceImpl passwordService;


    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private LdapService ldapService;

    @RequestMapping("getpwd")
    public ApiResponse getPassword(String account) throws Exception {

        PassWord passWord = passwordService.get(QueryDto.builder().available().addWhere("account = '" + account + "'").build());

        if (ObjectUtil.isNull(passWord)) {
            return ApiResponse.success();
        } else {
            String pwd = SecUtil.decrypt(passWord.getPwd());
            passWord.setPwd(pwd);
            return ApiResponse.success(passWord);
        }
    }


    public void downloadTemplate(HttpServletResponse response) throws Exception {
        ExcelSheet sheet = ExcelSheet.builder().addProperty("isTemplateField", true).setTitle(PassWord.class).build();
        String fileName = "导入模板";
        Excel excel = PassWord.class.getDeclaredAnnotation(Excel.class);
        if (ObjectUtil.notNull(excel)) {
            fileName = excel.value();
        }

        ExcelUtil.builder().addSheet(sheet).setFileName(fileName).build().writeToClient(response);

    }

    @RequestMapping("managers")
    public ApiResponse getSupperManager() {

        List<Map> managers = redisService.getList(CacheKeyEnum.SUPER_MANAGER, Map.class);
        Map data = new HashMap();
        if (CollectorUtil.isNoTEmpty(managers)) {
            for (Map manager : managers) {
                String name = MapUtil.getString(manager, "name");
                data.put(name, name);
            }
        }

        List<Map> options = managers.stream().map(p -> MapUtil.builder()
                .add("name", MapUtil.getString(p, "name"))
                .add("value", MapUtil.getString(p, "name"))
                .build()).collect(Collectors.toList());

        Map dictionariesAapp = redisService.getMap(CacheKeyEnum.DICTIONARIES_PER_APP);
        Map fintpMap = MapUtil.getMap(dictionariesAapp, "pdtp");

        fintpMap.put("managers", data);

        redisService.put(CacheKeyEnum.DICTIONARIES_PER_APP, dictionariesAapp);

        return ApiResponse.success(options);

    }


    /**
     * 随机生成密码
     *
     * @param length
     * @return
     */
    @RequestMapping("randompwd")
    public ApiResponse randomPassword(int length) {

        String[] passwordCombine = WebUtil.getStringArrayParam("passwordCombine");
        String tempstr = Arrays.stream(passwordCombine).collect(Collectors.joining(","));
        boolean bigChart = tempstr.contains("bigChart");
        boolean smallChart = tempstr.contains("smallChart");
        boolean numberChart = tempstr.contains("number");
        return ApiResponse.success(SecUtil.alphaNumeric(length, bigChart, smallChart, numberChart));
    }


    /**
     * 查询AD账号信息
     *
     * @param
     * @return
     */
    @RequestMapping("getadaccount")
    public ApiResponse getADAccount(String field, String fieldValue, String byAd) throws Exception {
        List<String> vlist = new LinkedList<>();
        vlist.add(fieldValue);

        String key = field.equals("cn") ? "uuid" : (field.equals("mail") ? "email" : "employee_num");

        Account account = accountService.get(QueryDto.builder()
                .available()
                .addWhere(String.format("%s = '%s'", key, fieldValue))
                .build());

        if (StringUtil.isNotEmpty(byAd) || ObjectUtil.isNull(account)) {
            List<Map> accountDatas = passwordService.findADInformation(vlist, field);
            if (CollectorUtil.isNoTEmpty(accountDatas)) {
                account = accountService.changeADtoAccount(accountDatas.get(0));
                return ApiResponse.success(account);
            } else {
                return ApiResponse.error("没有此账号");
            }
        } else {
            return ApiResponse.success(account);
        }
    }


    /**
     * 校验账号密码是否正确
     *
     * @param
     * @return
     */
    @RequestMapping("checkpwd")
    public ApiResponse checkPassword(String uuid, String password) {
        ldapService.auth(uuid, password);
        return ApiResponse.success();
    }

    /**
     * 批量查询AD账号
     *
     * @return
     */
    @RequestMapping("batchqueryad")
    public ApiResponse batchQueryAD(MultipartHttpServletRequest request) throws Exception {
        List<Map> datas = ExcelUtil.getExcelFromRequest(request);
        List<String> vlist = datas.stream().map(p -> p.get("数据").toString()).collect(Collectors.toList());
        String filed = WebUtil.getStringParam("field");
        List<Map> accounts = passwordService.findADInformation(vlist, filed);
        String filePath = ExcelUtil.builder()
                .setFileName("AD账号")
                .addSheet(ExcelSheet
                        .builder()
                        .addTitle("cn", "UUID", 15)
                        .addTitle("employeeid", "工号", 15)
                        .addTitle("mail", "邮箱", 25)
                        .addTitle("displayName", "全名", 25)
                        .addTitle("c", "国家")
                        .addTitle("department", "部门")
                        .addTitle("title", "岗位", 25)
                        .addTitle("manager", "上级Leader", 20)
                        .addTitle("managerMail", "Leader邮箱", 25)
                        .addTitle("status", "状态")
                        .addTitle("whencreated", "创建时间", 20)
                        .addTitle("employeetype", "账号类型", 15)
                        .addTitle("streetaddress", "地址", 25)
                        .addTitle("description", "描述", 30)
                        .addRowList(accounts)
                        .build())
                .build()
                .writeToLocal();
        return ApiResponse.success(filePath);
    }

    @RequestMapping("adtemplate")
    public ApiResponse template(HttpServletResponse response) throws Exception {
        String filed = WebUtil.getStringParam("field");
        ExcelUtil.builder()
                .setFileName("AD账号模板")
                .addSheet(ExcelSheet
                        .builder()
                        .addTitle("filed", "数据")
                        .build())
                .build()
                .writeToClient(response);
        return ApiResponse.success();
    }

}




