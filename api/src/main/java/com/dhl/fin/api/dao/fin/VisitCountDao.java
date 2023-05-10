package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.VisitCount;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface VisitCountDao {
    int deleteByPrimaryKey(VisitCount record);

    int insert(VisitCount record);

    int insertSelective(VisitCount record);

    List<VisitCount> selectPageQuery(QueryDto queryDto);

    List<VisitCount> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    VisitCount selectOne(QueryDto queryDto);

    VisitCount selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")VisitCount record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(VisitCount record);
}