package com.dhl.cmdb.api;

import com.dhl.fin.api.Application;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.common.util.excel.ExcelUtil;
import com.dhl.fin.api.service.AssistantServiceImpl;
import com.dhl.fin.api.service.UamServiceImpl;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import com.dhl.fin.api.service.system.RoleServiceImpl;
import com.dhl.fin.api.service.system.TreeServiceImpl;
import com.dhl.fin.api.timer.AccountTimer;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @author becui
 * @date 5/28/2020
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class SpringbootTest {

    @Autowired
    private TreeServiceImpl treeService;

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private UamServiceImpl uamService;

    @Autowired
    private AssistantServiceImpl assistantService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AccountTimer accountTimer;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Test
    public void monitorUAMStatus() throws Exception {
        accountTimer.synchronizeAccountFromUAM();
    }

    @Test
    public void productUserList() throws Exception {

//        uamService.uamFile();

        uamService.setItOwner("FIN");

        assistantService.sentToDoList();


    }

    @Test
    public void copyITExcel() throws Exception {

        String localDir = SpringContextUtil.getPropertiesValue("custom.uploadPath");
        String uamWorkDir = SpringContextUtil.getPropertiesValue("custom.uamWorkDir");

        String ITTargetDir = uamWorkDir + "\\Temp\\xlsxFiles";
        String itConfirmDir = uamWorkDir + "\\User Confirmed List";

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

    @Test
    public void importMapper() throws Exception {

        assistantService.waitToDeal();

      /*  List<Map> datas = ExcelUtil.getExcelFromLocal("C:\\Users\\becui\\Desktop\\mapper.xlsx");
        for (Map data : datas) {
            String UAMCode = MapUtil.getString(data, "UAMCode");
            String SystemBCAName = MapUtil.getString(data, "SystemBCAName");
            String sql = String.format("insert into t_bca_uam_map(bca_name ,uam_name) values('%s','%s')", SystemBCAName, UAMCode);
            jdbcTemplate.execute(sql);

        }
*/

    }

}







