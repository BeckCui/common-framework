package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Menu;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface MenuDao {
    int deleteByPrimaryKey(Menu record);

    int insert(Menu record);

    int insertSelective(Menu record);

    List<Menu> selectPageQuery(QueryDto queryDto);

    List<Menu> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Menu selectOne(QueryDto queryDto);

    Menu selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Menu record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Menu record);
}