package com.dhl.fin.api.service.system;

import com.dhl.fin.api.common.util.MapUtil;
import com.dhl.fin.api.common.util.ObjectUtil;
import com.dhl.fin.api.common.util.StringUtil;
import com.dhl.fin.api.dao.fin.OtherDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 6/23/2020
 */
@Transactional(rollbackFor = Exception.class)
@Service
public class OtherServiceImpl {

    @Autowired
    private OtherDao otherDao;

    public Map<String, List<Map>> roleActions(String projectCode) {
        List<Map> datas = otherDao.queryRoleAction(projectCode);

        List<String> menus = datas.stream()
                .map(p -> ObjectUtil.isNull(p.get("menuName")) ? "" : p.get("menuName").toString())
                .filter(StringUtil::isNotEmpty)
                .distinct().collect(Collectors.toList());
        List<Map> roleActions = new LinkedList<>();
        Map item = null;
        String roleName = null;
        for (Map data : datas) {
            String rn = MapUtil.getString(data, "roleName");
            if (!rn.equals(roleName)) {
                item = MapUtil.builder().build();
                roleName = rn;
                item.put("roleName", rn);
                roleActions.add(item);
            }

            String mn = MapUtil.getString(data, "menuName", "");
            if (StringUtil.isEmpty(mn)) {
                continue;
            }

            String action = MapUtil.getString(data, "nn");
            item.put(mn, StringUtil.isEmpty(action) ? "ALL" : action);
        }

        return MapUtil.builder().add("menus", menus).add("datas", roleActions).build();
    }

    public String queryLastUpdate() {
        return otherDao.queryLastUpdate();
    }


    public List<Map> getRoles(String uuid) {
        return otherDao.queryUserRole(uuid);
    }


}
