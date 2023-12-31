--创建数据库
CREATE
DATABASE IF NOT EXISTS y9log;

--创建表
-- 访问日志实体表
CREATE TABLE IF NOT EXISTS y9log.Y9LOG_ACCESSLOG
(
    `ID`
    String,
    `LOGTIME`
    TIMESTAMP,
    `LOGLEVEL`
    String,
    `OPERATETYPE`
    String,
    `OPERATENAME`
    String,
    `MODULARNAME`
    String,
    `METHODNAME`
    String,
    `ELAPSEDTIME`
    UInt64,
    `USERID`
    String,
    `USERNAME`
    String,
    `USERHOSTIP`
    String,
    `TENANTID`
    String,
    `TENANTNAME`
    String,
    `SERVERIP`
    String,
    `SUCCESS`
    String,
    `REQUESTURL`
    String,
    `ERRORMESSAGE`
    String,
    `LOGMESSAGE`
    String,
    `THROWABLE`
    String,
    `DN`
    String,
    `GUIDPATH`
    String,
    `SYSTEMNAME`
    String,
    `USERAGENT`
    String,
    `MACADDRESS`
    String,
    `LOGINNAME`
    String
) ENGINE = MergeTree
(
)
    PARTITION BY toYYYYMM
(
    LOGTIME
)
    ORDER BY
(
    ID,
    LOGTIME
) SETTINGS index_granularity=8192

-- IP与部门对照实体表
CREATE TABLE IF NOT EXISTS y9log.Y9LOG_IPDEPTMAPPING
(
    `ID`
    String,
    `CLIENTIPSECTION`
    String,
    `OPERATOR`
    String,
    `DEPTNAME`
    String,
    `SAVETIME`
    String,
    `UPDATETIME`
    String,
    `TABINDEX`
    Int32,
    `STATUS`
    Int32
) ENGINE = MergeTree
(
)
    ORDER BY
(
    ID
) SETTINGS index_granularity=8192

-- 日志字段映射表
CREATE TABLE IF NOT EXISTS y9log.Y9LOG_LOGMAPPING
(
    `ID`
    String,
    `MODULARNAME`
    String,
    `MODULARCNNAME`
    String
) ENGINE = MergeTree
(
)
    ORDER BY
(
    ID
) SETTINGS index_granularity=8192

-- 用户IP与区间段的对照实体表
CREATE TABLE IF NOT EXISTS y9log.Y9LOG_USERHOSTIPINFO
(
    `ID`
    String,
    `USERHOSTIP`
    String,
    `CLIENTIPSECTION`
    String
) ENGINE = MergeTree
(
)
    ORDER BY
(
    ID
) SETTINGS index_granularity=8192

-- 用户登录信息实体表
CREATE TABLE IF NOT EXISTS y9log.Y9LOG_USERLOGININFO
(
    `ID`
    String,
    `LOGINTIME`
    TIMESTAMP,
    `LOGINTYPE`
    String,
    `USERID`
    String,
    `USERNAME`
    String,
    `USERHOSTIP`
    String,
    `USERHOSTMAC`
    String,
    `USERHOSTNAME`
    String,
    `USERHOSTDISKID`
    String,
    `TENANTID`
    String,
    `TENANTNAME`
    String,
    `SERVERIP`
    String,
    `SUCCESS`
    String,
    `LOGMESSAGE`
    String,
    `BROWSERNAME`
    String,
    `BROWSERVERSION`
    String,
    `OSNAME`
    String,
    `SCREENRESOLUTION`
    String,
    `CLIENTIPSECTION`
    String
) ENGINE = MergeTree
(
)
    PARTITION BY toYYYYMM
(
    LOGINTIME
)
    ORDER BY
(
    ID,
    LOGINTIME
) SETTINGS index_granularity=8192