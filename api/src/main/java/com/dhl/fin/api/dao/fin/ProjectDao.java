package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Project;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface ProjectDao {
    int deleteByPrimaryKey(Project record);

    int insert(Project record);

    int insertSelective(Project record);

    List<Project> selectPageQuery(QueryDto queryDto);

    List<Project> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Project selectOne(QueryDto queryDto);

    int deleteProjectAccounts(@Param("projectId")Long projectId, @Param("accountId")Long accountId);

    int insertProjectAccounts(@Param("projectId")Long projectId, @Param("accountId")Long accountId);

    int deleteProjectAccountsMiddle(@Param("projectId")Long projectId);

    Project selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Project record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Project record);
}