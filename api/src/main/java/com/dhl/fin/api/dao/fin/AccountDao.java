package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Account;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface AccountDao {
    int deleteByPrimaryKey(Account record);

    int insert(Account record);

    int insertSelective(Account record);

    List<Account> selectPageQuery(QueryDto queryDto);

    List<Account> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Account selectOne(QueryDto queryDto);

    int deleteAccountProjects(@Param("accountId")Long accountId, @Param("projectId")Long projectId);

    int insertAccountProjects(@Param("accountId")Long accountId, @Param("projectId")Long projectId);

    int deleteAccountProjectsMiddle(@Param("accountId")Long accountId);

    int deleteAccountRoles(@Param("accountId")Long accountId, @Param("roleId")Long roleId);

    int insertAccountRoles(@Param("accountId")Long accountId, @Param("roleId")Long roleId);

    int deleteAccountRolesMiddle(@Param("accountId")Long accountId);

    Account selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Account record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Account record);
}