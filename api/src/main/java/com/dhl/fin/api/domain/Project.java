package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * 项目
 * <p>
 *
 * @author CuiJianbo
 * @date 2020.02.16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_project")
public class Project extends BasicDomain implements Serializable {


    public Project() {
    }

    public Project(Long id) {
        this.id = id;
    }

    /**
     * 项目名
     */
    @Column(columnDefinition = "varchar(50) ")
    private String name;

    /**
     * 项目英文名
     */
    @Column(columnDefinition = "varchar(50) ")
    private String enName;

    /**
     * 项目代码
     */
    @Column(columnDefinition = "varchar(50) ")
    private String code;


    /**
     * 业务owner
     */
    @Column(columnDefinition = "varchar(50) ")
    private String owner;


    /**
     * 业务onwer邮箱
     */
    @Column(columnDefinition = "varchar(200) ")
    private String ownerEmail;


    /**
     * 项目介绍
     */
    @Column(columnDefinition = "text ")
    private String introduction;


    @ManyToMany
    @JoinTable(name = "t_account_project",
            joinColumns = {@JoinColumn(name = "project_id")},
            inverseJoinColumns = {@JoinColumn(name = "account_id")})
    private List<Account> accounts;


    @OneToMany(mappedBy = "project")
    private List<Role> roles;

    /**
     * 菜单权限
     */
    @OneToOne
    @JoinColumn(name = "menu_tree_id", foreignKey = @ForeignKey(name = "null"))
    private Tree menuTree;

    @Transient
    private boolean isManager = false;

    @Transient
    private boolean addUserPer = false;

    @Transient
    private boolean updateUserPer = false;

    @Transient
    private boolean DeleteUserPer = false;

}
