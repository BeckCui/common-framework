package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.SystemConfig;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface SystemConfigDao {
    int deleteByPrimaryKey(SystemConfig record);

    int insert(SystemConfig record);

    int insertSelective(SystemConfig record);

    List<SystemConfig> selectPageQuery(QueryDto queryDto);

    List<SystemConfig> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    SystemConfig selectOne(QueryDto queryDto);

    SystemConfig selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")SystemConfig record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(SystemConfig record);
}