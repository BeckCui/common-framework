package com.dhl.fin.api.dao.fin;

import org.apache.ibatis.annotations.Param;

/**
 * Created by CuiJianbo on 2020.02.28.
 */
public interface TreeDaoOwner {


    /**
     * 更新 targetCode 节点同级别node的排序值
     *
     * @param targetCode
     */
    void updateSort(@Param("targetCode") String targetCode, @Param("startOrderNum") int startOrderNum);


    /**
     * 拖拽node到兄弟节点旁边，更改parent Id
     *
     * @param targetCode
     * @param dragCode
     */
    void updateParentIdDrag(@Param("targetCode") String targetCode, @Param("dragCode") String dragCode);

    /**
     * 拖拽node到父节点下，更改parent Id
     *
     * @param targetCode
     * @param dragCode
     */
    void updateParentIdDragInner(@Param("targetCode") String targetCode, @Param("dragCode") String dragCode);


}
