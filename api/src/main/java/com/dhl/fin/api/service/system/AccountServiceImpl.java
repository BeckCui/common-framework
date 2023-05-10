package com.dhl.fin.api.service.system;

import cn.hutool.core.collection.CollectionUtil;
import com.dhl.fin.api.common.dto.OptionDto;
import com.dhl.fin.api.common.dto.QueryDto;
import com.dhl.fin.api.common.enums.ActionEnum;
import com.dhl.fin.api.common.service.CommonService;
import com.dhl.fin.api.common.service.LdapService;
import com.dhl.fin.api.common.util.*;
import com.dhl.fin.api.dao.uam.UamDao;
import com.dhl.fin.api.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Created by CuiJianbo on 2020.02.23.
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccountServiceImpl extends CommonService<Account> {
    @Autowired
    private RoleServiceImpl roleService;

    @Autowired
    private TreeServiceImpl treeService;

    @Autowired
    private ProjectServiceImpl projectService;


    @Autowired
    private LdapService ldapService;

    @Autowired
    private PasswordServiceImpl passwordService;

    @Autowired
    private UamDao uamDao;

    @Override
    public void beforeSave(Account domain) throws Exception {
        if (ObjectUtil.isNull(domain.getUuid())) {
            domain.setIsSuperManager(false);
            domain.setAvailable(true);
        }

        String manageAreas = Arrays.stream(WebUtil.getStringArrayParam("manageArea")).collect(joining(","));

        domain.setManageAreas(manageAreas);

    }

    @Override
    public void afterSave(Account domain) throws Exception {
        domain.getRoles();
        super.afterSave(domain);
    }

    @Override
    public void afterGet(Account domain) throws Exception {


        String loginUuid = WebUtil.getLoginUser().getUuid();
        Account loginAccount = get(QueryDto.builder()
                .available()
                .addJoinDomain("roles")
                .addWhere(String.format("account.uuid='%s'", loginUuid))
                .build());

        if (CollectorUtil.isNoTEmpty(domain.getRoles())) {
            domain.setRoleId(domain.getRoles().stream().map(Role::getId).map(Object::toString).collect(Collectors.toList()));
        }


        //role list
        Long projectId = WebUtil.getLongParam("projectId");

        if (loginAccount.getIsSuperManager()) {
            QueryDto queryDto = QueryDto.builder().available().addWhere("project.id=" + projectId).build();
            List<OptionDto> optionDtos = roleService.select(queryDto).stream()
                    .map(p -> OptionDto.builder().name(p.getName()).value(p.getId().toString()).build())
                    .collect(Collectors.toList());
            if (CollectorUtil.isNoTEmpty(optionDtos)) {
                domain.setRoleOptions(optionDtos);
            }
        } else {
            if (CollectorUtil.isNoTEmpty(loginAccount.getRoles())) {

                String roleIds = loginAccount.getRoles().stream().map(p -> p.getId().toString()).collect(Collectors.joining(","));
                String mgRoleIds = null;
                if (StringUtil.isNotEmpty(roleIds)) {
                    List<OptionDto> optionDtos = roleService.select(QueryDto.builder()
                            .addJoinDomain("mgRoles")
                            .addWhere("project.id=" + projectId)
                            .addWhere(String.format("role.id in (%s)", roleIds)
                            ).build())
                            .stream()
                            .map(p -> p.getMgRoles())
                            .flatMap(List::stream)
                            .map(p -> OptionDto.builder()
                                    .name(p.getName())
                                    .value(p.getId().toString()
                                    ).build())
                            .collect(Collectors.toList());
                    if (CollectorUtil.isNoTEmpty(optionDtos)) {
                        domain.setRoleOptions(optionDtos);
                    }
                }
            }
        }


        //project list
        List<OptionDto> projectOptions = projectService
                .select(QueryDto.builder().available().build())
                .stream().map(p -> OptionDto.builder().name(p.getName()).value(p.getId().toString()).build())
                .collect(Collectors.toList());

        if (CollectorUtil.isNoTEmpty(projectOptions)) {
            domain.setProjectOptions(projectOptions);
        }

        if (ObjectUtil.notNull(domain.getId())) {
            List<Project> projects = getManageProjects(domain.getId());
            if (CollectorUtil.isNoTEmpty(projects)) {
                List<String> projectIds = projects
                        .stream()
                        .map(Project::getId)
                        .map(Object::toString)
                        .collect(Collectors.toList());
                domain.setManageProjectIds(projectIds);
            }
        }

        if (StringUtil.isNotEmpty(domain.getAreas())) {
            domain.setAreaCodes(ArrayUtil.arrayToList(domain.getAreas().split(",")));
        }

        String uuid = WebUtil.getLoginUser().getUuid();
        Account account = get(QueryDto.builder().addWhere(String.format("account.uuid='%s'", uuid)).build());
        if (ObjectUtil.notNull(account)) {
            if (StringUtil.isNotEmpty(account.getAreas())) {
                domain.setAreaOptions(ArrayUtil.arrayToList(account.getAreas().split(",")));
            }
        }

        Map<String, List<String>> areas = loadUsersOfAreas(loginAccount.getAreas());
        String uuids = areas.entrySet().stream().map(p -> p.getValue()).flatMap(List::stream).distinct().collect(Collectors.joining("','"));
        if (StringUtil.isNotEmpty(uuids)) {
            List<Account> accountList = select(QueryDto.builder().available().addWhere("account.uuid in ('" + uuids + "')").build());
            for (Map.Entry<String, List<String>> stringListEntry : areas.entrySet()) {
                List<String> newValues = new LinkedList<>();
                stringListEntry.getValue().forEach(p -> {
                    Account a = accountList.stream().filter(k -> k.getUuid().equals(p)).findFirst().orElse(null);
                    newValues.add(p + "-" + a.getName());
                });
                areas.put(stringListEntry.getKey(), newValues);
            }
        }
        domain.setAreaUsers(areas);


        if (StringUtil.isNotEmpty(domain.getManageAreas())) {
            domain.setManageAreaList(domain.getManageAreas().split(","));
        }

    }

    private List<String> errorUuid = new LinkedList<>();

    /**
     * 查找某个区域下的区域查询的人
     *
     * @return
     * @throws Exception
     */
    public Map loadUsersOfAreas(String areas) throws Exception {
        Map<String, List<String>> usersOfAreas = new HashMap<>();
        return usersOfAreas;
    }




    /**
     * 校验用户是否存在
     *
     * @param domain
     * @return
     * @throws Exception
     */
    @Override
    public String validate(Account domain) throws Exception {
        if (this.actionEnum.equals(ActionEnum.ADD)) {
            Account u = checkUnique(Account.class, "uuid", domain.getUuid());
            if (ObjectUtil.notNull(u) && !u.getRemove()) {
                return "已经存在uuid为" + domain.getUuid() + "的用户";
            }
        }
        return null;
    }

    /**
     * 通过用户工号查找用户
     *
     * @param userNo
     * @return
     * @throws Exception
     */
    public Account getUserByUserNo(String userNo) throws Exception {
        QueryDto queryDto = QueryDto.builder()
                .available()
                .addWhere(String.format("account.user_no = '%s'", userNo))
                .build();
        List<Account> accountList = select(queryDto);
        if (CollectorUtil.isNoTEmpty(accountList)) {
            return accountList.get(0);
        }
        return null;
    }


    /**
     * 通过uuid查找某个项目下的角色
     *
     * @param uuid
     * @param projectCode 应用的代码
     * @return
     * @throws Exception
     */
    public List<Role> getRolesByUuid(String uuid, String projectCode) throws Exception {
        QueryDto queryDto = QueryDto.builder()
                .available()
                .addWhere(String.format("accounts.uuid = '%s'", uuid))
                .addWhere(String.format("project.code= '%s'", projectCode))
                .build();
        return roleService.select(queryDto);
    }


    /**
     * 通过用户uuid查找某个应用下的权限操作有哪些
     *
     * @param uuid
     * @param projectCode
     * @return
     * @throws Exception
     */
    public List<Action> getActionsByUuid(String uuid, String projectCode) throws Exception {
        List<Tree> trees = getRoleTree(uuid, projectCode);
        if (CollectorUtil.isNoTEmpty(trees)) {
            List<Long> treeList = trees.stream().map(Tree::getId).collect(Collectors.toList());
            List<Action> actions = trees.stream().map(Tree::getAction).filter(ObjectUtil::notNull).collect(Collectors.toList());
            List<Action> childrenActions = treeService.getAllChildrenByRootIds(treeList, "action")
                    .stream()
                    .map(Tree::getAction)
                    .filter(ObjectUtil::notNull)
                    .collect(Collectors.toList());
            actions.addAll(childrenActions);
            return actions;
        }

        return null;
    }

    /**
     * 通过uui找所有角色下的权限树
     *
     * @param uuid
     * @param projectCode
     * @return
     * @throws Exception
     */
    private List<Tree> getRoleTree(String uuid, String projectCode) throws Exception {
        Account account = get(QueryDto.builder().available().addWhere(String.format("account.uuid = '%s'", uuid)).build());
        if (account.getIsSuperManager()) {
            Tree menuTree = projectService.get(QueryDto.builder()
                    .available()
                    .addJoinDomain("menuTree")
                    .addWhere(String.format("project.code = '%s'", projectCode)
                    ).build()).getMenuTree();
            return treeService.select(QueryDto.builder().available().addWhere(String.format("tree.parent_id = %s", menuTree.getId())).build());
        } else {
            List<Role> roles = getRolesByUuid(uuid, projectCode);
            if (CollectorUtil.isNoTEmpty(roles)) {
                String roleIds = roles.stream().map(Role::getId).map(Object::toString).collect(joining(","));
                QueryDto queryDto = QueryDto.builder()
                        .available()
                        .addJoinDomain("action")
                        .addWhere(String.format("roles.id in (%s)", roleIds))
                        .build();
                return treeService.select(queryDto);
            }
        }
        return null;
    }


    /**
     * 通过uuid查找某个项目下的菜单有哪些
     *
     * @param uuid
     * @param projectCode
     * @return
     * @throws Exception
     */
    public List<Menu> getMenusByUuid(String uuid, String projectCode) throws Exception {
        List<Tree> trees = getRoleTree(uuid, projectCode);
        if (CollectorUtil.isNoTEmpty(trees)) {
            String menuTreeCodes = trees.stream()
                    .map(Tree::getParentLine)
                    .filter(ObjectUtil::notNull)
                    .map(p -> p.split("->"))
                    .flatMap(Arrays::stream)
                    .distinct()
                    .collect(joining("','"));

            QueryDto queryDto = QueryDto.builder()
                    .available()
                    .addJoinDomain("menu")
                    .addWhere(String.format("menu.code in ('%s')", menuTreeCodes))
                    .addWhere(String.format("menu.code in ('%s')", menuTreeCodes))
                    .build();
            List<Menu> parentMenus = treeService.select(queryDto)
                    .stream()
                    .map(Tree::getMenu)
                    .filter(ObjectUtil::notNull)
                    .collect(Collectors.toList());

            List<Long> treeList = trees.stream().map(Tree::getId).collect(Collectors.toList());
            List<Menu> childrenMenus = treeService.getAllChildrenByRootIds(treeList, "menu")
                    .stream()
                    .map(Tree::getMenu)
                    .filter(ObjectUtil::notNull)
                    .collect(Collectors.toList());
            parentMenus.addAll(childrenMenus);
            return CollectorUtil.distinct(parentMenus);
        }
        return null;
    }


    /**
     * 通过uuid查找他能使用应用菜单树
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public Map<String, List<Tree>> getMenuTreePerProject(String uuid) throws Exception {
        List<Project> projects = getProjectsByUUID(uuid);
        MapBuilder<String, List<Tree>> mapBuilder = MapUtil.builder();
        if (CollectorUtil.isNoTEmpty(projects)) {
            for (Project project : projects) {
                List<Tree> roleTrees = getRoleTree(uuid, project.getCode());
                String ownerMenu = "";
                if (CollectorUtil.isNoTEmpty(roleTrees)) {
                    ownerMenu = "," + roleTrees.stream().map(p -> StringUtil.isEmpty(p.getParentLine()) ? "ALL" : p.getParentLine())
                            .filter(StringUtil::isNotEmpty)
                            .map(p -> p.split("->"))
                            .flatMap(Arrays::stream)
                            .distinct()
                            .filter(StringUtil::isNotEmpty)
                            .collect(Collectors.joining(",")) + ",";
                }
                Long menuTreeId = project.getMenuTree().getId();
                List<Tree> menuTrees = treeService.getChildrenByParentId(menuTreeId, "menu", "children");
                List<Tree> ownerMenuTrees = new LinkedList<>();
                if (CollectorUtil.isNoTEmpty(menuTrees)) {
                    for (Tree tree : menuTrees) {
                        if (ownerMenu.equals(",ALL,")) {
                            ownerMenuTrees.add(tree);
                        } else if (ownerMenu.contains("," + tree.getCode() + ",")) {
                            ownerMenuTrees.add(tree);
                        } else {
                            continue;
                        }
                        List<Tree> ownerChildMenuTrees = new LinkedList<>();
                        if (CollectorUtil.isNoTEmpty(tree.getChildren())) {
                            for (Tree subTree : tree.getChildren()) {

                                boolean flag = false;
                                if (ObjectUtil.notNull(subTree.getParentLine())) {
                                    String[] s = subTree.getParentLine().split("->");
                                    for (String s1 : s) {
                                        if (ownerMenu.contains("," + s1 + ",")) {
                                            flag = true;
                                            break;
                                        }
                                    }
                                } else if (ownerMenu.contains("," + subTree.getCode() + ",")) {
                                    flag = true;
                                }

                                if (ownerMenu.equals(",ALL,")) {
                                    ownerChildMenuTrees.add(subTree);
                                } else if (flag) {
                                    ownerChildMenuTrees.add(subTree);
                                } else {
                                    continue;
                                }

                                subTree.setMenu(treeService.get(subTree.getId(), "menu").getMenu());
                            }
                            if (ObjectUtil.isNull(tree.getChildren().get(0).getMenu())) {
                                tree.setChildren(null);
                            }
                        }
                    }
                }
                mapBuilder.add(project.getCode(), ownerMenuTrees);
            }
        }
        return mapBuilder.build();
    }

    /**
     * 通过uuid查找管理的项目有哪些
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public List<Project> getManageProjectsByUUID(String uuid) throws Exception {
        Account account = get(QueryDto.builder().available().addWhere(String.format("account.uuid = '%s'", uuid)).build());
        List<Project> projects = null;
        if (account.getIsSuperManager()) {
            projects = projectService.select(QueryDto.builder().available().build());
        } else {

            //查找系统管理员的角色
            String managerRoleIds = select(QueryDto.builder()
                    .available()
                    .addWhere("roles.code like '%_sys_manager'")
                    .addWhere("account.id = " + account.getId())
                    .build())
                    .stream()
                    .map(Account::getRoles)
                    .flatMap(List::stream)
                    .map(Role::getId)
                    .map(Object::toString)
                    .collect(Collectors.joining(","));

            //查找拥有配置用户权限的角色
            String actionRoleIds = roleService.select(QueryDto.builder()
                    .available()
                    .addWhere("accounts.uuid='" + uuid + "'")
                    .addWhere(" role.update_user_per = 1 or role.add_user_per = 1 or role.delete_user_per = 1").build()
            )
                    .stream()
                    .map(p -> p.getId().toString())
                    .collect(Collectors.joining(","));

            String ids = null;
            if (StringUtil.isNotEmpty(actionRoleIds)) {
                if (StringUtil.isEmpty(managerRoleIds)) {
                    ids = actionRoleIds;
                } else {
                    ids = StringUtil.join(managerRoleIds, ",", actionRoleIds);
                }
            } else if (StringUtil.isNotEmpty(managerRoleIds)) {
                ids = managerRoleIds;
            }

            projects = projectService.select(QueryDto.builder()
                    .available()
                    .addWhere(String.format("roles.id in  (%s)", ids))
                    .build());
            for (Project project : projects) {
                if (StringUtil.isNotEmpty(actionRoleIds) && CollectorUtil.isNoTEmpty(project.getRoles())) {
                    project.getRoles().stream().forEach(p -> {
                                if (p.getUpdateUserPer()) {
                                    project.setUpdateUserPer(true);
                                }
                                if (p.getDeleteUserPer()) {
                                    project.setDeleteUserPer(true);
                                }
                                if (p.getAddUserPer()) {
                                    project.setAddUserPer(true);
                                }
                            }
                    );
                }

                if (StringUtil.isNotEmpty(managerRoleIds)) {
                    String tempIds = "," + managerRoleIds + ",";
                    project.getRoles().stream().forEach(p -> {
                        if (tempIds.contains("," + p.getId() + ",")) {
                            project.setManager(true);
                        }
                    });
                }
            }
        }
        return projects;
    }

    /**
     * 通过uuid查找登录人信息
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public Account getUserByUuid(String uuid) throws Exception {
        Account loginAccount = null;

        loginAccount = get(QueryDto.builder().available().addWhere(String.format("account.uuid = '%s'", uuid)).build());

        return loginAccount;
    }


    /**
     * 通过uuid获取用户能使用的项目
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public List<Project> getProjectsByUUID(String uuid) throws Exception {
        Account account = get(QueryDto.builder().available().addWhere(String.format("account.uuid = '%s'", uuid)).build());
        List<Project> projects;
        if (ObjectUtil.notNull(account.getIsSuperManager()) && account.getIsSuperManager()) {
            projects = projectService.select(QueryDto.builder().available().build());
        } else {
            String[] projectIds = roleService.select
                    (QueryDto.builder()
                            .available()
                            .addJoinDomain("accounts")
                            .addJoinDomain("project")
                            .addWhere(String.format("accounts.uuid = '%s'", uuid))
                            .build()
                    ).stream()
                    .map(Role::getProject)
                    .map(Project::getId)
                    .map(Object::toString)
                    .distinct()
                    .toArray(String[]::new);

            projects = projectService.select(QueryDto.builder().available().addWhere(String.format("id in (%s)", StringUtil.join(projectIds, ","))).build());
        }
        return projects;
    }

    /**
     * 通过uuid查找每个项目下的菜单有哪些
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public Map<String, List<Menu>> getMenusPerProject(String uuid) throws Exception {
        List<Project> projects = projectService.select(QueryDto.builder().available().build());
        MapBuilder<String, List<Menu>> mapBuilder = MapUtil.builder();
        if (CollectorUtil.isNoTEmpty(projects)) {
            for (Project project : projects) {
                mapBuilder.add(project.getCode(), getMenusByUuid(uuid, project.getCode()));
            }
        }
        return mapBuilder.build();
    }

    /**
     * 通过uuid查找每个项目下的权限操作有哪些
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public Map<String, List<Action>> getActionsPerProject(String uuid) throws Exception {
        List<Project> projects = projectService.select(QueryDto.builder().available().build());
        MapBuilder<String, List<Action>> mapBuilder = MapUtil.builder();
        if (CollectorUtil.isNoTEmpty(projects)) {
            for (Project project : projects) {
                mapBuilder.add(project.getCode(), getActionsByUuid(uuid, project.getCode()));
            }
        }
        return mapBuilder.build();
    }


    /**
     * 通过account id查找能够使用的项目有哪些
     *
     * @param userId
     * @return
     * @throws Exception
     */
    private List<Project> getManageProjects(Long userId) throws Exception {
        List<Long> roleIds = roleService.select(QueryDto.builder()
                .available()
                .addWhere("accounts.id = " + userId)
                .build()).stream()
                .map(Role::getId).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(roleIds)) {
            return projectService.select(QueryDto
                    .builder()
                    .available()
                    .addWhere(String.format("roles.id in (%s)", CollectionUtil.join(roleIds, ",")))
                    .build());
        }
        return null;
    }


    /**
     * 通过uuid查找每个应用下的角色有哪些
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public Map<String, List<Role>> getRolesPerProject(String uuid) throws Exception {
        List<Project> projects = getProjectsByUUID(uuid);
        Map<String, List<Role>> roles = new HashMap<>();
        if (CollectionUtil.isNotEmpty(projects)) {
            for (Project p : projects) {
                List<Role> roleList = getRolesByUuid(uuid, p.getCode());
                roles.put(p.getCode(), roleList);
            }
        }
        return roles;
    }

    @Override
    protected void formatRowData(Account domain) throws Exception {
        List<Project> projects = getManageProjects(domain.getId());
        if (CollectorUtil.isNoTEmpty(projects)) {
            domain.setManageProjectStr(projects.stream().map(p -> p.getName()).collect(joining(",")));
        }

        if (CollectorUtil.isNoTEmpty(domain.getRoles())) {
            String roleStr = domain.getRoles().stream().map(Role::getName).collect(Collectors.joining(","));
            domain.setRoleStr(roleStr);
        }
    }

    @Override
    public void beforePageQuery(QueryDto pageQuery) {

        if (WebUtil.getRequest().getRequestURI().contains("projectuser")) {
            pageQuery.getWhereCondition().add("roles.project_id = projects.id");
        }

    }

    /**
     * 从系统获取某人，如果没有就从AD导入到系统中
     *
     * @param uuid
     * @return
     * @throws Exception
     */
    public Account getOrInsert(String uuid) throws Exception {
        Account account = get(QueryDto.builder()
                .available()
                .addWhere(String.format("account.uuid = '%s'", uuid))
                .build());

        if (ObjectUtil.isNull(account)) {

            account = getAccountFromAD(uuid);

            String country = account.getCountry();
            if (StringUtil.isNotEmpty(country) && country.equals("CN")) {
                try {
                    saveOrUpdate(account);
                } catch (Exception e) {
                    log.error("保存账号失败：" + uuid);
                }
            }
        }
        return account;
    }

    public List<Account> getOrInsert(List<String> uuids) throws Exception {
        List<Account> accounts = new LinkedList<>();
        for (String uuid : uuids) {
            accounts.add(getOrInsert(uuid));
        }
        return accounts;
    }


    /**
     * 从AD系统获取账号
     *
     * @param uuid
     * @return
     */
    public Account getAccountFromAD(String uuid) {
        Map data = null;
        try {
            PassWord fintpPwd = passwordService.get(QueryDto.builder().available().addWhere("account='srv_cnexp_fintp'").build());
            data = ldapService.queryAccountByUUID("srv_cnexp_fintp", SecUtil.decrypt(fintpPwd.getPwd()), uuid);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("AD获取失败:" + uuid);
        }

        if (ObjectUtil.notNull(data) && data.size() == 0) {
            Account acct = new Account();
            acct.setUuid(uuid);
            acct.setDescription("AD系统没有此账号");
            acct.setAdStatus("禁用");
            return acct;
        }

        String status = MapUtil.getString(data, "userAccountControl");
        String leader = MapUtil.getString(data, "manager");

        String adStatus = StringUtil.isEmpty(status) ? "其他" : (status.equals("512") ? "正常" : (status.equals("514") ? "禁用" : "其他"));

        if (StringUtil.isNotEmpty(leader)) {
            leader = leader.split(",")[0].split("=")[1];
        }

        Account acct = changeADtoAccount(data);

        acct.setUuid(uuid);
        acct.setLeader(leader);
        acct.setAdStatus(adStatus);

        return acct;

    }

    private void combineAccount(List<String> uuids) throws Exception {

        if (CollectorUtil.isEmpty(uuids)) {
            return;
        }

        for (String uuid : uuids) {
            if (StringUtil.isEmpty(uuid)) {
                continue;
            }

            Account fintpAccount = get(QueryDto.builder().addWhere("uuid = '" + uuid + "'").build());
            Account hrAccount = uamDao.queryHrInfo(uuid);
            Account adAccount = getAccountFromAD(uuid);

            if (ObjectUtil.isNull(fintpAccount)) {
                fintpAccount = new Account();
                fintpAccount.setAvailable(true);
                fintpAccount.setIsSuperManager(false);
            }

            if (ObjectUtil.notNull(hrAccount)) {
                fintpAccount.setCluster(hrAccount.getCluster());
                fintpAccount.setArea(hrAccount.getArea());
                if (ObjectUtil.notNull(fintpAccount.getIsSuperManager()) && !fintpAccount.getIsSuperManager()) {
                    fintpAccount.setName(hrAccount.getName());
                }
            }


            String description = adAccount.getDescription();
            if (StringUtil.isEmpty(description) || !description.equals("AD系统没有此账号")) {

                ObjectUtil.copyFieldValue(adAccount, fintpAccount);
                String employeeNum = fintpAccount.getEmployeeNum();
                if (StringUtil.isNotEmpty(employeeNum) && employeeNum.length() > 12) {
                    fintpAccount.setEmployeeNum(null);
                }
                save(fintpAccount);
            } else {
                errorUuid.add(uuid);
            }

        }
    }

    /**
     * 从UAM系统同步账号到 PDTP
     *
     * @throws Exception
     */
    public void synchronizeAccount() throws Exception {

        int length = 1000;

        errorUuid.clear();

        Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        Map params = MapUtil.builder()
                .add("startTime", year + "-01-01")
                .add("length", length)
                .build();

        Integer totalCount = uamDao.queryAcctCount(params);

        log.info("将要同步的账号数量：" + totalCount);


        for (int i = 0; i <= totalCount / length; i++) {
            params.put("startIndex", i * length);
            List<String> uuids = uamDao.pageQueryUAMAcct(params);

            combineAccount(uuids);

            if (CollectorUtil.isNoTEmpty(uuids)) {
                log.info("已完成：" + uuids.size());
            }

        }

        if (CollectorUtil.isNoTEmpty(errorUuid)) {

            log.info("还剩下" + errorUuid.size() + "个失败的账号，准备再次同步");
            LinkedList datas = new LinkedList<>();
            errorUuid.stream().forEach(p -> {
                datas.add(cn.hutool.core.util.ObjectUtil.clone(p));
            });
            errorUuid.clear();
            combineAccount(datas);

            log.info("最终还剩下" + errorUuid.size() + "个失败的账号");
        }

    }

    public Account changeADtoAccount(Map account) {
        String uuid = MapUtil.getString(account, "cn");
        String employeeNum = MapUtil.getString(account, "employeeid");
        String fullName = MapUtil.getString(account, "displayName");
        String title = MapUtil.getString(account, "title");
        String mail = MapUtil.getString(account, "mail");
        String department = MapUtil.getString(account, "department");
        String position = MapUtil.getString(account, "title");
        String country = MapUtil.getString(account, "c");
        String leader = MapUtil.getString(account, "manager");
        String adStatus = MapUtil.getString(account, "status");
        String expireDate = MapUtil.getString(account, "expireDate");
        String acctType = MapUtil.getString(account, "employeetype");
        String acctCreateDate = MapUtil.getString(account, "whencreated");
        String address = MapUtil.getString(account, "streetaddress");
        String description = MapUtil.getString(account, "description");


        Account acct = new Account();
        acct.setUuid(uuid);
        acct.setEmployeeNum(employeeNum);
        acct.setFullName(fullName);
        acct.setTitle(title);
        acct.setEmail(mail);
        acct.setExpireDate(expireDate);
        acct.setDepartment(department);
        acct.setPosition(position);
        acct.setLeader(leader);
        acct.setAdStatus(adStatus);
        acct.setAcctType(acctType);
        acct.setAcctCreateDate(acctCreateDate);
        acct.setAddress(address);
        acct.setDescription(description);
        acct.setCountry(country);


        Account hrAccount = uamDao.queryHrInfo(uuid);
        if (ObjectUtil.notNull(hrAccount)) {
            acct.setCluster(hrAccount.getCluster());
            acct.setArea(hrAccount.getArea());
            acct.setName(hrAccount.getName());
        }

        return acct;
    }


    public String getManageArea(String uuid) throws Exception {

        boolean isManager = getUserByUuid(uuid).getIsSuperManager();

        if (isManager) {
            return "area-cluster-root";
        } else {
            String areaIds = get(QueryDto.builder().available().addWhere("uuid='" + uuid + "'").build()).getManageAreas();
            if (StringUtil.isNotEmpty(areaIds)) {
                String allIds = findChildren(areaIds);

                allIds = StringUtil.isEmpty(allIds) ? areaIds : StringUtil.join(allIds, ",", areaIds);

                return treeService.select(QueryDto.builder()
                        .available()
                        .addWhere("id in (" + allIds + ")")
                        .build())
                        .stream()
                        .map(Tree::getCode)
                        .collect(Collectors.joining(","));
            }
        }

        return null;

    }

    private String findChildren(String ids) throws Exception {
        String childrenIds = treeService.select(QueryDto.builder()
                .available()
                .addWhere("parent.id in (" + ids + ")")
                .build())
                .stream()
                .map(Tree::getId)
                .map(Object::toString)
                .collect(Collectors.joining(","));

        if (StringUtil.isNotEmpty(childrenIds)) {
            String subIds = findChildren(childrenIds);
            if (StringUtil.isEmpty(subIds)) {
                return childrenIds;
            } else {
                return StringUtil.join(childrenIds, ",", subIds);
            }
        } else {
            return null;
        }
    }
}



