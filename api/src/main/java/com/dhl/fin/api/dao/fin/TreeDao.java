package com.dhl.fin.api.dao.fin;

import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.domain.Tree;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

public interface TreeDao {
    int deleteByPrimaryKey(Tree record);

    int insert(Tree record);

    int insertSelective(Tree record);

    List<Tree> selectPageQuery(QueryDto queryDto);

    List<Tree> selectSelective(QueryDto queryDto);

    int selectCount(QueryDto queryDto);

    Tree selectOne(QueryDto queryDto);

    int deleteTreeRoles(@Param("treeId")Long treeId, @Param("roleId")Long roleId);

    int insertTreeRoles(@Param("treeId")Long treeId, @Param("roleId")Long roleId);

    int deleteTreeRolesMiddle(@Param("treeId")Long treeId);

    Tree selectByPrimaryKey(Map data);

    int updateByPrimaryKeySelective(@Param("domain")Tree record, @Param("query")QueryDto queryDto);

    int updateByPrimaryKey(Tree record);
}