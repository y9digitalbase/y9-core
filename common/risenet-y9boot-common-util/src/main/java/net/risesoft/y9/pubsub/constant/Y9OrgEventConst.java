package net.risesoft.y9.pubsub.constant;

/**
 * 组织事件类型常量类
 * 
 * @author dingzhaojun
 * @author qinman
 * @author mengjuhua
 * @date 2022/2/10
 */
public class Y9OrgEventConst {

    // 事件类型
    public static final String RISEORGEVENT_TYPE_ADD_ORGANIZATION = "RISEORGEVENT_TYPE_ADD_ORGANIZATION";
    public static final String RISEORGEVENT_TYPE_ADD_DEPARTMENT = "RISEORGEVENT_TYPE_ADD_DEPARTMENT";
    public static final String RISEORGEVENT_TYPE_ADD_GROUP = "RISEORGEVENT_TYPE_ADD_GROUP";

    public static final String RISEORGEVENT_TYPE_ADD_POSITION = "RISEORGEVENT_TYPE_ADD_POSITION";
    public static final String RISEORGEVENT_TYPE_ADD_JOB = "RISEORGEVENT_TYPE_ADD_JOB";
    public static final String RISEORGEVENT_TYPE_ADD_PERSON = "RISEORGEVENT_TYPE_ADD_PERSON";
    public static final String RISEORGEVENT_TYPE_ADD_MANAGER = "RISEORGEVENT_TYPE_ADD_MANAGER";

    public static final String RISEORGEVENT_TYPE_UPDATE_ORGANIZATION = "RISEORGEVENT_TYPE_UPDATE_ORGANIZATION";
    public static final String RISEORGEVENT_TYPE_UPDATE_DEPARTMENT = "RISEORGEVENT_TYPE_UPDATE_DEPARTMENT";
    public static final String RISEORGEVENT_TYPE_UPDATE_GROUP = "RISEORGEVENT_TYPE_UPDATE_GROUP";

    public static final String RISEORGEVENT_TYPE_UPDATE_POSITION = "RISEORGEVENT_TYPE_UPDATE_POSITION";
    public static final String RISEORGEVENT_TYPE_UPDATE_JOB = "RISEORGEVENT_TYPE_UPDATE_JOB";
    public static final String RISEORGEVENT_TYPE_UPDATE_PERSON = "RISEORGEVENT_TYPE_UPDATE_PERSON";
    public static final String RISEORGEVENT_TYPE_UPDATE_MANAGER = "RISEORGEVENT_TYPE_UPDATE_MANAGER";

    public static final String RISEORGEVENT_TYPE_UPDATE_USER = "RISEORGEVENT_TYPE_UPDATE_USER";

    public static final String RISEORGEVENT_TYPE_DELETE_ORGANIZATION = "RISEORGEVENT_TYPE_DELETE_ORGANIZATION";
    public static final String RISEORGEVENT_TYPE_DELETE_DEPARTMENT = "RISEORGEVENT_TYPE_DELETE_DEPARTMENT";
    public static final String RISEORGEVENT_TYPE_DELETE_GROUP = "RISEORGEVENT_TYPE_DELETE_GROUP";

    public static final String RISEORGEVENT_TYPE_DELETE_POSITION = "RISEORGEVENT_TYPE_DELETE_POSITION";
    public static final String RISEORGEVENT_TYPE_DELETE_PERSON = "RISEORGEVENT_TYPE_DELETE_PERSON";

    public static final String RISEORGEVENT_TYPE_DELETE_MANAGER = "RISEORGEVENT_TYPE_DELETE_MANAGER";
    public static final String RISEORGEVENT_TYPE_DELETE_USER = "RISEORGEVENT_TYPE_DELETE_USER";

    public static final String RISEORGEVENT_TYPE_GROUP_ADDPERSON = "RISEORGEVENT_TYPE_GROUP_ADDPERSON";
    public static final String RISEORGEVENT_TYPE_GROUP_REMOVEPERSON = "RISEORGEVENT_TYPE_GROUP_REMOVEPERSON";
    public static final String RISEORGEVENT_TYPE_GROUP_ORDER = "RISEORGEVENT_TYPE_GROUP_ORDER";

    public static final String RISEORGEVENT_TYPE_POSITION_ADDPERSON = "RISEORGEVENT_TYPE_POSITION_ADDPERSON";
    public static final String RISEORGEVENT_TYPE_POSITION_REMOVEPERSON = "RISEORGEVENT_TYPE_POSITION_REMOVEPERSON";
    public static final String RISEORGEVENT_TYPE_POSITION_ORDER = "RISEORGEVENT_TYPE_POSITION_ORDER";

    // 更新排序号
    public static final String RISEORGEVENT_TYPE_UPDATE_ORGANIZATION_TABINDEX =
        "RISEORGEVENT_TYPE_UPDATE_ORGANIZATION_TABINDEX";
    public static final String RISEORGEVENT_TYPE_UPDATE_DEPARTMENT_TABINDEX =
        "RISEORGEVENT_TYPE_UPDATE_DEPARTMENT_TABINDEX";
    public static final String RISEORGEVENT_TYPE_UPDATE_GROUP_TABINDEX = "RISEORGEVENT_TYPE_UPDATE_GROUP_TABINDEX";
    public static final String RISEORGEVENT_TYPE_UPDATE_POSITION_TABINDEX =
        "RISEORGEVENT_TYPE_UPDATE_POSITION_TABINDEX";
    public static final String RISEORGEVENT_TYPE_UPDATE_PERSON_TABINDEX = "RISEORGEVENT_TYPE_UPDATE_PERSON_TABINDEX";

    public static final String RISEORGEVENT_TYPE_SYNC = "RISEORGEVENT_TYPE_SYNC";
    public static final String RISEORGEVENT_TYPE_USER_SYNC = "RISEORGEVENT_TYPE_USER_SYNC";

    // 事件接收系统默认为所有系统
    public static final String EVENT_TARGET_ALL = "ALL";

    public static final String ORG_TYPE = "ORG_TYPE";
    public static final String SYNC_ID = "SYNC_ID";

    // 是否递归
    public static final String SYNC_RECURSION = "SYNC_RECURSION";
    public static final Integer SYNC_NEEDRECURSION = 1;
    public static final Integer SYNC_DONTRECURSION = 0;

    public static final String ORG_MAPPING_PERSONSGROUPS = "PersonsGroups";
    public static final String ORG_MAPPING_PERSONSPOSITIONS = "PersonsPositions";
    public static final String ORG_MAPPING_PERSONSROLES = "PersonsRoles";

}
