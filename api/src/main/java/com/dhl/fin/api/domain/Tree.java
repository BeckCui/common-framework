package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 *
 * 树形结构存储
 *
 *
 * @author CuiJianbo
 * @date 2020.02.27
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_tree")
public class Tree extends BasicDomain implements Serializable {

    public Tree() {
    }

    public Tree(Long id, String title, String code, Tree parent) {
        this.id = id;
        this.title = title;
        this.code = code;
        this.parent = parent;
    }

    public Tree(Long id) {
        super.setId(id);
    }


    /**
     * 节点名字
     */
    @Column(columnDefinition = "varchar(50) ")
    private String title;


    /**
     * 节点代码
     */
    @Column(columnDefinition = "varchar(50)")
    private String code;

    /**
     * 节点顺序
     */
    @Column(columnDefinition = "int default 1 ")
    private Integer orderNum;

    /**
     * 是否是叶子节点
     */
    @Column(columnDefinition = "bit default 1 ")
    private Boolean leaf;


    /**
     * 父节点路径
     */
    @Column(columnDefinition = "varchar(300)")
    private String parentLine;

    @ManyToOne
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "null"))
    private Tree parent;

    @OneToMany(mappedBy = "parent")
    private List<Tree> children;

    @Transient
    private List<Tree> childrenData;

    /**
     * 关联角色
     */
    @ManyToMany
    @JoinTable(name = "t_role_tree",
            joinColumns = {@JoinColumn(name = "tree_id")},
            inverseJoinColumns = {@JoinColumn(name = "role_id")})
    private List<Role> roles;

    @OneToOne
    @JoinColumn(name = "menu_id", foreignKey = @ForeignKey(name = "null"))
    private Menu menu;

    @OneToOne
    @JoinColumn(name = "action_id", foreignKey = @ForeignKey(name = "null"))
    private Action action;


}



