package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 日志
 *
 * @author becui
 * @date 6/17/2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_log")
public class Log extends BasicDomain implements Serializable {

    /**
     * 项目代码
     */
    private String projectCode;

    /**
     * 项目名字
     */
    private String projectName;

    /**
     * 当前使用菜单
     */
    private String menu;

    /**
     * 影响的表
     */
    private String domain;

    /**
     * 操作
     */
    @ExcelTitle(dictionary = "actionType")
    private String action;

    /**
     * 操作是否成功
     */
    @ExcelTitle(dictionary = "logStatus")
    private String status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 操作人的uuid
     */
    private String uuid;

    /**
     * 操作人的ip
     */
    private String ip;

    /**
     * 操作人使用的浏览器
     */
    private String browser;


    /**
     * 操作人使用的终端
     */
    private String client;


    public Log() {
    }

    public Log(String projectCode, String projectName, String menu, String domain,
               String action, String status, String remark, String uuid, String ip, String browser, String client) {
        this.projectCode = projectCode;
        this.projectName = projectName;
        this.menu = menu;
        this.domain = domain;
        this.action = action;
        this.status = status;
        this.remark = remark;
        this.uuid = uuid;
        this.ip = ip;
        this.browser = browser;
        this.client = client;
    }

}
