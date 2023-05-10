package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.annotation.BusinessId;
import com.dhl.fin.api.common.annotation.Excel;
import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.domain.BasicDomain;
import com.dhl.fin.api.common.dto.OptionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Excel("FINTP平台用户")
@Table(name = "t_account")
public class Account extends BasicDomain implements Serializable {

    /**
     * uuid
     */
    @BusinessId
    @ExcelTitle(name = "uuid", code = "uuid")
    @Column(columnDefinition = "varchar(50)")
    private String uuid;

    /**
     * 工号
     */
    @ExcelTitle(name = "userNo", code = "user_no")
    @Column(columnDefinition = "varchar(50)")
    private String userNo;

    /**
     * 名字
     */
    @ExcelTitle(name = "名字", code = "name")
    @Column(columnDefinition = "varchar(50) ")
    private String name;

    /**
     * title
     */
    @ExcelTitle(name = "title", code = "title")
    private String title;

    /**
     * 邮箱
     */
    @ExcelTitle(name = "邮箱", code = "email")
    private String email;

    /**
     * 工号
     */
    @ExcelTitle(name = "工号", code = "employeeNum")
    @Column(columnDefinition = "varchar(15) ")
    private String employeeNum;

    /**
     * 全名
     */
    private String fullName;

    /**
     * 分区
     */
    private String cluster;

    /**
     * 区域
     */
    private String area;


    /**
     * 部门
     */
    private String department;

    /**
     * 岗位
     */
    private String position;

    /**
     * 上级Leader
     */
    private String leader;

    /**
     * AD状态
     */
    private String adStatus;

    /**
     * 账号类型
     */
    private String acctType;

    /**
     * 账号创建时间
     */
    private String acctCreateDate;

    /**
     * 账号密码过期时间
     */
    private String expireDate;

    /**
     * 地址
     */
    private String address;

    /**
     * 描述
     */
    private String description;

    /**
     * 国家
     */
    private String country;

    /**
     * 登录时间
     */
    private Date loginDate;

    /**
     * 负责的区域
     */
    private String manageAreas;

    /**
     * 管理的区域
     */
    @ExcelTitle(name = "管理的区域", code = "areas")
    @Column(columnDefinition = "varchar(50) ")
    private String areas;

    /**
     * 是否可以用，用于是否离职在职
     */
    @ExcelTitle(name = "是否在职", code = "available", dictionary = "accountStatus")
    @Column(columnDefinition = "bit default 1 ")
    private Boolean available;

    /**
     * 是否是超级管理员
     */
    @Column(columnDefinition = "bit default 0 ")
    private Boolean isSuperManager;


    public Account() {
    }


    public Account(Long id) {
        this.id = id;
    }


    /**
     * 一个项目下有多少用户
     */
    @ManyToMany
    @JoinTable(name = "t_account_project",
            joinColumns = {@JoinColumn(name = "account_id")},
            inverseJoinColumns = {@JoinColumn(name = "project_id")})
    private List<Project> projects;

    @ManyToMany
    @JoinTable(name = "t_account_role",
            joinColumns = {@JoinColumn(name = "account_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private List<Role> roles;


    @Transient
    private List<OptionDto> roleOptions;

    @Transient
    private List<OptionDto> projectOptions;


    /**
     * 用户的角色id
     */
    @Transient
    private List<String> roleId;

    @Transient
    private List<Long> userIds;



    @Transient
    private String manageProjectStr;

    @Transient
    private String roleStr;

    @Transient
    private List<String> manageProjectIds;

    @Transient
    private List<String> areaCodes;

    @Transient
    private List<String> areaOptions;

    @Transient
    private String[] manageAreaList;

    @Transient
    private Map<String, List<String>> areaUsers;


}

