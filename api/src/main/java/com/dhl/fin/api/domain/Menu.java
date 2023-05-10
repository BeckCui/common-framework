package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * 菜单
 *
 * Created by CuiJianbo on 2020.02.16.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_menu")
public class Menu extends BasicDomain implements Serializable {

    @Column(columnDefinition = "varchar(50) ")
    private String name;

    @Column(columnDefinition = "varchar(50) ")
    private String code;

    @Column(columnDefinition = "varchar(50)")
    private String icon;

    @Column(columnDefinition = "varchar(50) ")
    private String clickUrl;

}



