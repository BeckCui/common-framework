package com.dhl.fin.api.domain;

import com.dhl.fin.api.common.domain.BasicDomain;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;


@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "t_line_chart_data")
public class LineChartData extends BasicDomain {

    //时间点
    private Date pointDate;

    //数据
    private Double amount;

    //数据简写code
    private String dataCode;

    //数据中文描述
    private String dataDesc;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private LineChartData parent;






}






