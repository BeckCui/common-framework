package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.domain.BasicDomain;
import com.dhl.fin.api.common.dto.OptionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 *
 * 角色
 *
 *
 * @author CuiJianbo
 * @date 2020.02.16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_role")
public class Role extends BasicDomain implements Serializable {

    /**
     * 角色名字
     */
    @Column(columnDefinition = "varchar(50)")
    private String name;


    /**
     * 角色代码
     */
    @Column(columnDefinition = "varchar(50)")
    private String code;


    /**
     * 新增用户权限
     */
    @Column(columnDefinition = "bit default 0")
    private Boolean addUserPer;

    /**
     * 更新用户权限
     */
    @Column(columnDefinition = "bit default 0")
    private Boolean updateUserPer;

    /**
     * 删除用户权限
     */
    @Column(columnDefinition = "bit default 0")
    private Boolean deleteUserPer;

    public Role() {

    }

    public Role(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /**
     * 关联tree树, 用于存放已选择的菜单权限
     */
    @ManyToMany
    @JoinTable(name = "t_role_tree",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "tree_id")})
    private List<Tree> trees;


    @ManyToMany
    @JoinTable(name = "t_account_role",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "account_id")})
    private List<Account> accounts;

    /**
     *
     * 管理的角色，控制下拉框的候选项
     *
     */
    @ManyToMany
    @JoinTable(name = "t_role_mg_role",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "mg_role_id")})
    private List<Role> mgRoles;


    /**
     *
     * 管理的角色下的账号
     *
     */
    @ManyToMany
    @JoinTable(name = "t_role_mg_acct",
            joinColumns = {@JoinColumn(name = "role_id")},
            inverseJoinColumns = {@JoinColumn(name = "mg_acct_id")})
    private List<Role> mgAccounts;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "null"))
    private Project project;

    @Transient
    private List<Long> actionTreeIds;


    @Transient
    private List<Long> dataPerTreeIds;

    @Transient
    private List<Long> mgRoleIds;

    @Transient
    private List<OptionDto> roleOptions;

    @Transient
    private List<Long> mgAcctIds;



}
