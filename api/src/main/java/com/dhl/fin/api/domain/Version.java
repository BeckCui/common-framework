package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.domain.BasicDomain;
import com.dhl.fin.api.common.dto.OptionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 版本
 *
 * @author becui
 * @date 7/29/2020
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_version")
public class Version extends BasicDomain implements Serializable {


    /**
     * 项目
     */
    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "null"))
    private Project project;

    /**
     * 版本号 0.0.0
     * <p>
     * 第一位：项目功能升级开发
     * 第二位：小模块功能修复
     * 第三位：配置文件修改
     */
    private String version;

    /**
     * change编号
     */
    private String gsnNumber;


    /**
     * 实施的人
     */
    private String implementPerson;

    /**
     * 实施的时间
     */
    private Date implementDate;

    /**
     * 变更的内容
     */
    private String remark;

    @Transient
    @ExcelTitle(dateFormat = "yyyy-MM-dd")
    private String implementDateStr;

    @Transient
    private List<OptionDto> projectOptions;

}







