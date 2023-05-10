package com.dhl.fin.api.controller.system;

import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.util.CollectorUtil;
import com.dhl.fin.api.common.util.SecUtil;
import com.dhl.fin.api.common.util.WebUtil;
import com.dhl.fin.api.common.util.excel.ExcelSheet;
import com.dhl.fin.api.common.util.excel.ExcelSheetBuilder;
import com.dhl.fin.api.common.util.excel.ExcelTitleBean;
import com.dhl.fin.api.common.util.excel.ExcelUtil;
import com.dhl.fin.api.service.system.OtherServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 6/23/2020
 */
@Slf4j
@Transactional(rollbackFor = Exception.class)
@RestController
@RequestMapping("other")
public class OtherController {

    @Autowired
    private OtherServiceImpl otherService;


    /**
     * 导出UAM
     *
     * @param response
     * @param projectCode
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping("actionroles")
    public void queryActionRoles(HttpServletResponse response, String projectCode) throws Exception {
        Map queryResult = otherService.roleActions(projectCode);
        List datas = (List) queryResult.get("datas");
        List<String> menus = (List) queryResult.get("menus");

        ExcelSheetBuilder excelSheetBuilder = ExcelSheet.builder().addTitle(ExcelTitleBean.builder()
                .key("roleName")
                .name("角色")
                .sort(1)
                .width(20)
                .build());
        if (CollectorUtil.isNoTEmpty(datas)) {
            for (String menu : menus) {
                String name = menu;
                String key = menu;
                excelSheetBuilder.addTitle(ExcelTitleBean.builder()
                        .name(name)
                        .sort(2)
                        .width(30)
                        .key(key)
                        .build());
            }
        }
        ExcelUtil.builder()
                .setFileName("UAM")
                .addSheet(excelSheetBuilder
                        .addRowList(datas)
                        .build())
                .build().writeToClient(response);

    }


    /**
     * 加密
     */
    @ResponseBody
    @RequestMapping("encrypt")
    public ApiResponse encrypt() throws DecoderException {
        String text = WebUtil.getStringParam("text");
        String key = WebUtil.getStringParam("key");
        String algorithm = WebUtil.getStringParam("algorithm");
        String poolSize = WebUtil.getStringParam("poolSize");
        String iterations = WebUtil.getStringParam("iterations");
        String encryptStr = SecUtil.pbeEncrypt(text, key, algorithm, poolSize, iterations);

        return ApiResponse.success(encryptStr);
    }

    /**
     * 解密
     */
    @ResponseBody
    @RequestMapping("decrypt")
    public ApiResponse deEncrypt() throws DecoderException {
        String text = WebUtil.getStringParam("text");
        String key = WebUtil.getStringParam("key");
        String algorithm = WebUtil.getStringParam("algorithm");
        String poolSize = WebUtil.getStringParam("poolSize");
        String iterations = WebUtil.getStringParam("iterations");

        String decryptStr = SecUtil.pbeDecrypt(text, key, algorithm, poolSize, iterations);

        return ApiResponse.success(decryptStr);

    }


}





