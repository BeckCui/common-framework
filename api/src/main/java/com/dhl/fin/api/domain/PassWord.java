package com.dhl.fin.api.domain;


import com.dhl.fin.api.common.annotation.BusinessId;
import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author becui
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_pass_word")
public class PassWord extends BasicDomain implements Serializable {


    /**
     * 账号
     */
    @BusinessId
    @ExcelTitle(name = "账号", sort = 1, width = 25)
    String account;


    /**
     * 账号的owner
     */
    @ExcelTitle(name = "owner", sort = 2, dictionary = "managers", width = 15)
    String owner;

    /**
     * 项目
     */
    @BusinessId
    @ExcelTitle(name = "project", sort = 3, dictionary = "appCodes", width = 15)
    String project;

    /**
     * 应用的环境，DEV，UAT ，PROD
     */
    @BusinessId
    @ExcelTitle(name = "应用环境", sort = 4, dictionary = "appEnv", width = 10)
    String appEnv;

    /**
     * owner邮箱
     */
    String email;

    /**
     * 当前密码
     */
    @ExcelTitle(name = "当前密码", sort = 5, width = 20)
    String pwd;

    /**
     * 密码前缀
     */
    @ExcelTitle(name = "密码前缀", sort = 5, width = 10)
    String passWordPrefix;


    /**
     * 周期（天）,为0就不自动更新
     */
    @ExcelTitle(name = "人为有效天数", sort = 6, width = 15)
    Integer cycle = 0;


    /**
     * 最后更新密码的时间
     */
    Date lastUpdateDate;

    /**
     * 是否到期提醒
     */
    @ExcelTitle(name = "是否到期提醒", sort = 7, dictionary = "passwordNotify", width = 20)
    String notify;

    /**
     * 到期通知的邮箱
     */
    @ExcelTitle(name = "到期通知的邮箱")
    String toEmail;

    /**
     * 到期抄送的邮箱
     */
    @ExcelTitle(name = "到期抄送的邮箱")
    String ccEmail;

    /**
     * 已经通知了的天数
     */
    Integer notifyDays;

    /**
     * AD系统到期日期
     */
    Date adExpireDate;


    /**
     * 人为到期日期
     */
    Date manualExpireDate;


    /**
     * 备注
     */
    @ExcelTitle(name = "备注", sort = 9, width = 50)
    String remark;

    /**
     * 历史密码
     */
    String historyPwd;


    /**
     * 账号类型：AD，普通
     */
    @BusinessId
    @ExcelTitle(name = "账号类型", sort = 8, width = 15, dictionary = "accountType")
    String acctType;

    @Transient
    @ExcelTitle(name = "密码到期时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss", disable = "isTemplateField")
    String adExpireDateStr;

    @Transient
    @ExcelTitle(name = "人为过期时间", width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss", disable = "isTemplateField")
    String manualExpireDateStr;

    /**
     * 应用编码
     */
    @Transient
    List<String> appCodes;

    /**
     * 账号owner候选项
     */
    @Transient
    List<String> ownerSelective;


}
