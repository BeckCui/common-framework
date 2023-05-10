package com.dhl.fin.api.dao.uam;

import java.util.List;
import java.util.Map;

public interface AssistantDao {


    List<Map> unRemoveView(Map params);

    List<Map> keepChangeView(Map params);

    List<Map> lastQuarterRemoveView(Map params);

    List<Map> genericAccountView(Map params);

    Map archiveTotal(Map params);

    List<Map> reviewOwner(Map params);

    List<Map> shouldRemoveByMe(Map params);

    List<Map> allRemoveAcct(Map params);

}




