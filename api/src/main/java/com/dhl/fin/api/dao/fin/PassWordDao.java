package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.PassWord;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface PassWordDao {
    int deleteByPrimaryKey(PassWord record);

    int insert(PassWord record);

    int insertSelective(PassWord record);

    List<PassWord> selectPageQuery(QueryDto queryDto);

    List<PassWord> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    PassWord selectOne(QueryDto queryDto);

    PassWord selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")PassWord record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(PassWord record);
}