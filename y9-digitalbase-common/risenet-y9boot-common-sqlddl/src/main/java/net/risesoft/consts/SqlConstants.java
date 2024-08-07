package net.risesoft.consts;

/**
 *
 * @author mengjuhua
 *
 */
public class SqlConstants {

    /** microsoft数据库 */
    public static final String DBTYPE_MICROSOFT = "microsoft";
    /** microsoft数据库 */
    public static final String DBTYPE_MSSQL = "mssql";
    /** mysql数据库 */
    public static final String DBTYPE_MYSQL = "mysql";
    /** oracle数据库 */
    public static final String DBTYPE_ORACLE = "oracle";
    /** dm数据库 */
    public static final String DBTYPE_DM = "dm";
    /** dm数据库 */
    public static final String DBTYPE_DBMS = "dbms";
    /** 人大金仓 */
    public static final String DBTYPE_KINGBASE = "kingbase";

    /** CHAR */
    public static final String CHAR_TYPE = "CHAR";
    /** VARCHAR */
    public static final String VARCHAR_TYPE = "VARCHAR";
    /** NCHAR */
    public static final String NCHAR_TYPE = "NCHAR";
    /** VARCHAR2 */
    public static final String VARCHAR2_TYPE = "VARCHAR2";
    /** NVARCHAR2 */
    public static final String NVARCHAR2_TYPE = "NVARCHAR2";
    /** RAW */
    public static final String RAW_TYPE = "RAW";
    /** DECIMAL */
    public static final String DECIMAL_TYPE = "DECIMAL";
    /** NUMERIC */
    public static final String NUMERIC_TYPE = "NUMERIC";
    /** NUMBER */
    public static final String NUMBER_TYPE = "NUMBER";

    private SqlConstants() {
        throw new IllegalStateException("SqlConstants Utility class");
    }

}
