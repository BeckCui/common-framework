package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Action;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface ActionDao {
    int deleteByPrimaryKey(Action record);

    int insert(Action record);

    int insertSelective(Action record);

    List<Action> selectPageQuery(QueryDto queryDto);

    List<Action> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Action selectOne(QueryDto queryDto);

    Action selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Action record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Action record);
}