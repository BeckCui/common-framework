package com.dhl.fin.api.dao.fin;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author becui
 * @date 6/23/2020
 */
public interface OtherDao {

    List<Map> queryRoleAction(@Param("projectCode") String projectCode);

    String queryLastUpdate();

    List<Map> queryUserRole(@Param("uuid") String uuid);

}
