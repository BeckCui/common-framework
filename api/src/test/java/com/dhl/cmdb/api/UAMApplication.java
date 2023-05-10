package com.dhl.cmdb.api;

import com.dhl.fin.api.Application;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.service.LdapService;
//import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.common.util.excel.ExcelSheet;
import com.dhl.fin.api.common.util.excel.ExcelUtil;
import com.dhl.fin.api.domain.Account;
import com.dhl.fin.api.service.UamServiceImpl;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;


@Log4j2
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class UAMApplication {

    @Autowired
    private LdapService ldapService;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private UamServiceImpl uamService;


    private String localDir;
    private String uamWorkDir;


    /**
     * 检查导入到UAM后数据是一致
     *
     * @throws IOException
     */
    @Test
    public void checkUamAndFileData() throws IOException {


        List<Map> datas = uamService.selectCountPerApp("becui");

        FilesUtil.copy(uamWorkDir + "\\用户上传清单清理结果汇总.xlsx", localDir + "用户上传清单清理结果汇总.xlsx");

        String sheetName = "总数统计";
        Map<String, List> fileDatas = ExcelUtil.getExcelFromLocal(localDir + "用户上传清单清理结果汇总.xlsx", Map.class, sheetName);
        boolean flag = false;

        if (MapUtil.isNotEmpty(fileDatas)) {
            List<Map> fileDataCount = MapUtil.getList(fileDatas, sheetName);
            for (Map map : fileDataCount) {
                String fileApp = MapUtil.getString(map, "appName");
                Integer fileCount = MapUtil.getInteger(map, "清洗后总数量");

                Integer uamCount = datas.stream()
                        .filter(p -> MapUtil.getString(p, "Application_Name").contains(fileApp))
                        .map(p -> MapUtil.getInteger(p, "total"))
                        .findFirst().orElse(null);

                if (ObjectUtil.isNull(uamCount)) {
                    log.info("数据库里没有：" + fileApp);
                    continue;
                }

                if (!uamCount.equals(fileCount)) {
                    flag = true;
                    log.info(String.format("存在差异的APP：%s, 导入前：%s, 导入后：%s ", fileApp, fileCount, uamCount));
                }
            }
        } else {
            log.error("没有找到数据");
        }

        if (flag) {
            log.info("导入前前后的数据一致，没有问题。");
        }

    }

    /**
     * itconfirm文件转换成xlsx
     *
     * @throws Exception
     */
    public void copyITExcel() throws Exception {

        localDir = SpringContextUtil.getPropertiesValue("custom.uploadPath");
        uamWorkDir = SpringContextUtil.getPropertiesValue("custom.uamWorkDir");

        String ITTargetDir = this.uamWorkDir + "\\Temp\\xlsxFiles";
        String itConfirmDir = this.uamWorkDir + "\\User Confirmed List";

        FilesUtil.delete(ITTargetDir);

        FilesUtil.createDir(ITTargetDir);

        CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};

        Files.list(Paths.get(itConfirmDir))
                .map(p -> p.toFile().isDirectory() ? p.toFile().listFiles() : new File[]{p.toFile()})
                .map(p -> ArrayUtil.arrayToList(p))
                .flatMap(List::stream)
                .filter(p -> p.getName().contains("-ITConfirm"))
                .forEach(p -> {

                    String newName = p.getName().endsWith("xls") ? p.getName() + "x" : p.getName();

                    try {
                        Files.copy(new FileInputStream(p), Paths.get(ITTargetDir + "\\" + newName), options);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });


    }


    /**
     * UAM准备工作:
     * 1、生成uuid的Leader
     * 2、xls转换成xlsx
     *
     * @throws Exception
     */
    @Test
    public void prepareUAMFile() throws Exception {

//        copyITExcel();

        List<Account> datas = accountService.select(QueryDto.builder().available().build());
        String filePath = ExcelUtil.builder()
                .setFileName("Leaders")
                .addSheet(ExcelSheet.builder()
                        .addRowList(datas)
                        .addTitle("uuid", "UUID")
                        .addTitle("email", "Email")
                        .addTitle("leader", "上级Leader")
                        .addTitle("acctType", "acctType")
                        .addTitle("department", "Department")
                        .addTitle("adStatus", "AD状态")
                        .build())
                .build()
                .writeToLocal();

        FilesUtil.copy(filePath, uamWorkDir + "\\Temp\\Leaders.xlsx");
    }


}






