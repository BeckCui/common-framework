package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Role;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface RoleDao {
    int deleteByPrimaryKey(Role record);

    int insert(Role record);

    int insertSelective(Role record);

    List<Role> selectPageQuery(QueryDto queryDto);

    List<Role> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Role selectOne(QueryDto queryDto);

    int deleteRoleTrees(@Param("roleId")Long roleId, @Param("treeId")Long treeId);

    int insertRoleTrees(@Param("roleId")Long roleId, @Param("treeId")Long treeId);

    int deleteRoleTreesMiddle(@Param("roleId")Long roleId);

    int deleteRoleAccounts(@Param("roleId")Long roleId, @Param("accountId")Long accountId);

    int insertRoleAccounts(@Param("roleId")Long roleId, @Param("accountId")Long accountId);

    int deleteRoleAccountsMiddle(@Param("roleId")Long roleId);

    int deleteRoleMgRoles(@Param("roleId")Long roleId, @Param("mgRoleId")Long mgRoleId);

    int insertRoleMgRoles(@Param("roleId")Long roleId, @Param("mgRoleId")Long mgRoleId);

    int deleteRoleMgRolesMiddle(@Param("roleId")Long roleId);

    int deleteRoleMgAccounts(@Param("roleId")Long roleId, @Param("mgAcctId")Long mgAcctId);

    int insertRoleMgAccounts(@Param("roleId")Long roleId, @Param("mgAcctId")Long mgAcctId);

    int deleteRoleMgAccountsMiddle(@Param("roleId")Long roleId);

    Role selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Role record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Role record);
}