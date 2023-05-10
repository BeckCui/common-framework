package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * 权限
 *
 * Created by CuiJianbo on 2020.02.16.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_action")
public class Action extends BasicDomain implements Serializable {

    public Action() {
    }

    public Action(Long id, String name, String code, String url) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.url = url;
    }

    public Action(Long id) {
        this.id = id;
    }

    @Column(columnDefinition = "varchar(50)")
    private String name;

    @Column(columnDefinition = "varchar(50) ")
    private String code;

    @Column(columnDefinition = "varchar(250) ")
    private String url;

    @OneToOne
    @JoinColumn(name = "tree_id", foreignKey = @ForeignKey(name = "null"))
    private Tree tree;

}
