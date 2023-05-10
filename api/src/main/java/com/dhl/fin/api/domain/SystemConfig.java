package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

/**
 *
 * 系统配置
 *
 * @author becui
 * @date 7/20/2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_system_config")
public class SystemConfig extends BasicDomain implements Serializable {


    /**
     * 参数代码
     */
    private String code;


    /**
     * 参数值
     */
    private String value;


    /**
     * 功能介绍
     */
    private String remark;

}














