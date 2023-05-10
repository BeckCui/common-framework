package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Version;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface VersionDao {
    int deleteByPrimaryKey(Version record);

    int insert(Version record);

    int insertSelective(Version record);

    List<Version> selectPageQuery(QueryDto queryDto);

    List<Version> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Version selectOne(QueryDto queryDto);

    Version selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Version record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Version record);
}