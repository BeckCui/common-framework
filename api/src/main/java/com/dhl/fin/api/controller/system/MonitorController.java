package com.dhl.fin.api.controller.system;

import com.dhl.fin.api.common.dto.ApiResponse;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.enums.CacheKeyEnum;
import com.dhl.fin.api.common.service.LdapService;
import com.dhl.fin.api.common.service.RedisService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.domain.VisitCount;
import com.dhl.fin.api.service.system.AccountServiceImpl;
import com.dhl.fin.api.service.system.SystemConfigServiceImpl;
import com.dhl.fin.api.service.system.VisitCountServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author becui
 * @date 7/18/2020
 */
@RestController
@RequestMapping("monitor")
@Transactional(rollbackFor = Exception.class)
public class MonitorController {

    @Autowired
    private RedisService redisService;

    @Autowired
    private VisitCountServiceImpl visitCountService;

    @Autowired
    private SystemConfigServiceImpl systemConfigService;


    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    LdapService ldapService;

    @ResponseBody
    @RequestMapping("activeacct")
    public ApiResponse getAccountInfo() throws Exception {

        String activeUserTime = systemConfigService.getConfig("activeUserTime");
        Map<String, Object> appActiveUsers = redisService.getMap(CacheKeyEnum.ACTIVE_USER);
        Map<String, Map> appCount = new HashMap<>();
        if (ObjectUtil.notNull(appActiveUsers)) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, StringUtil.isNotEmpty(activeUserTime) ? 0 - Integer.valueOf(activeUserTime) : -30);
            String nowDate = DateUtil.getFullTime(calendar.getTime());
            for (Map.Entry<String, Object> item : appActiveUsers.entrySet()) {
                String appCode = item.getKey();
                Map<String, Object> appKV = (Map) item.getValue();
                Map<String, Object> users = MapUtil.getMap(appKV, "users");
                Map<String, Object> loginUsers = MapUtil.getMap(appKV, "loginUsers");
                Map appData = new HashMap();
                String appEnName = MapUtil.getString(appKV, "appEnName");
                int n = 0;
                Map newUsers = new HashMap();
                List<String> userList = new LinkedList<>();
                for (Map.Entry<String, Object> user : users.entrySet()) {
                    if (user.getValue().toString().compareTo(nowDate) > 0) {
                        newUsers.put(user.getKey(), user.getValue());
                        userList.add(user.getKey() + "|" + user.getValue().toString());
                        n++;
                    }
                }


                Collections.sort(userList, new Comparator<String>() {

                    @Override
                    public int compare(String o1, String o2) {
                        String date1 = o1.split("\\|")[1];
                        String date2 = o2.split("\\|")[1];

                        return date1.compareTo(date2);
                    }
                });

                userList = userList.stream().map(p -> {
                    String[] ss = p.split("\\|");
                    String uuid = ss[0];
                    String time = ss[1].split(" ")[1];
                    return StringUtil.join(uuid, " ", time);
                }).collect(Collectors.toList());


                appData.put("appCode", appCode);
                appData.put("enName", appEnName);
                appData.put("count", n);
                appData.put("userList", userList);


                appCount.put(appCode, appData);
                appActiveUsers.put(appCode, MapUtil.builder().add("users", newUsers).add("loginUsers", loginUsers).add("appCode", appCode).add("appEnName", appEnName).build());
            }
            redisService.put(CacheKeyEnum.ACTIVE_USER, appActiveUsers);
        }

        return ApiResponse.success(appCount);
    }


    @ResponseBody
    @RequestMapping("dailyuser")
    public ApiResponse queryDailyUser() throws Exception {
        List<VisitCount> dailyUser = visitCountService.dailyUser();
        List<String> dates = new LinkedList<>();
        List<String> counts = new LinkedList<>();
        for (VisitCount data : dailyUser) {
            dates.add(data.getDateStr());
            counts.add(data.getTotalVisit().toString());
        }

        Map result = MapUtil.builder().add("dates", dates).add("counts", counts).build();

        return ApiResponse.success(result);

    }

    @ResponseBody
    @RequestMapping("dailyuser/{appcode}/{date}")
    public ApiResponse queryDailyUser(@PathVariable String date, @PathVariable String appcode) throws Exception {

        VisitCount visitCount = visitCountService.get(QueryDto
                .builder()
                .addWhere(String.format("project.id = %s", appcode))
                .addWhere(String.format("FORMAT(date, 'yyyy-MM-dd') = '%s'", date))
                .build()
        );

        if (ObjectUtil.notNull(visitCount) && StringUtil.isNotEmpty(visitCount.getUsers())) {
            List<Map> visitUsers = new LinkedList<>();
            Map<String, Object> userMap = JsonUtil.parseToMap(visitCount.getUsers());
            for (Map.Entry<String, Object> o : userMap.entrySet()) {
                visitUsers.add(MapUtil.builder()
                        .add("name", o.getKey())
                        .add("date", o.getValue().toString().split(" ")[1])
                        .build());
            }
            return ApiResponse.success(visitUsers);
        }

        return ApiResponse.success();
    }

    @ResponseBody
    @RequestMapping("cachecode/{code}")
    public ApiResponse queryDailyUser(@PathVariable String code) {

        if (code.equals("loginUsers")) {
            return ApiResponse.success("不展示登录人信息");
        }

        Object u = redisService.get(CacheKeyEnum.getByCode(code));
        if (ObjectUtil.notNull(u)) {
            return ApiResponse.success(JsonUtil.objectToString(u));
        } else {
            return ApiResponse.success("没有数据");
        }
    }

    @ResponseBody
    @RequestMapping("cachekeys")
    public ApiResponse cacheKeys() {
        List<Map> datas = Arrays.stream(CacheKeyEnum.values()).map(p -> MapUtil.builder()
                .add("code", p.getCode())
                .add("remark", p.getRemark())
                .build()).collect(Collectors.toList());
        return ApiResponse.success(datas);
    }

}



