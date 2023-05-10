package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.annotation.ExcelTitle;
import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 每日用户访问量
 *
 * @author becui
 * @date 7/29/2020
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_visit_count")
public class VisitCount extends BasicDomain implements Serializable {


    /**
     * 项目应用
     */
    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "null"))
    private Project project;

    /**
     * 总共用户访问量
     */
    private Integer totalVisit;

    /**
     * 总共用户访问量
     */
    @Column(columnDefinition = "varchar(800) ")
    private String users;


    /**
     * 日期
     */
    private Date date;

    @Transient
    @ExcelTitle(dateFormat = "yy-MM-dd")
    private String dateStr;


}
