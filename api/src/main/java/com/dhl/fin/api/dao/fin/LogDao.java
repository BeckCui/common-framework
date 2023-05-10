package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Log;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface LogDao {
    int deleteByPrimaryKey(Log record);

    int insert(Log record);

    int insertSelective(Log record);

    List<Log> selectPageQuery(QueryDto queryDto);

    List<Log> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Log selectOne(QueryDto queryDto);

    Log selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Log record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Log record);
}