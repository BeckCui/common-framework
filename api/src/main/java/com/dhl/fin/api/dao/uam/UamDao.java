package com.dhl.fin.api.dao.uam;

import com.dhl.fin.api.domain.Account;
import org.apache.ibatis.annotations.Param;

import java.util.*;

public interface UamDao {

    List<Map> waitReview(@Param("uuid") String uuid, @Param("today") String toDay);

    Account queryHrInfo(String uuids);

    List<Map> queryAppTotal(Map params);

    Integer totalProcess(Map params);

    Integer opsWaitTotal(Map params);

    Integer itWaitTotal(Map params);

    Integer saleWaitTotal(Map params);

    Integer finWaitTotal(Map params);

    Integer csdWaitTotal(Map params);

    Integer totalTaskAmount(Map params);

    Integer totalAppAmount(Map params);

    Map stepProcess(Map params);

    Integer archerProcess(Map params);

    List<Map> taskPerReviewer(Map params);

    List<Map> reviewerTasks(Map params);

    List<Map> allTeamProcess(Map params);

    List<Map> allTeamArcherProcess(Map params);

    List<String> queryItOwner(Map params);

    List<String> pageQueryUAMAcct(Map params);

    Integer queryAcctCount(Map params);

    List<Map> getAllReviewer(Map params);

    List<Map> getAllTask(Map params);

    List<String> getFinAllApp(Map params);

    List<String> getItOwner(@Param("startTime") String startTime);

    List<Map> selectAllRemoveAcct(Map params);

    List<Map> selectCountPerApp(Map params);






}






