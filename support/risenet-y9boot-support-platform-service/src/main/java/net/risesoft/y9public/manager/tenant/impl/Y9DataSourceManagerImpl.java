package net.risesoft.y9public.manager.tenant.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.DbType;
import com.alibaba.druid.pool.DruidDataSource;

import lombok.extern.slf4j.Slf4j;

import net.risesoft.enums.platform.DataSourceTypeEnum;
import net.risesoft.enums.platform.TenantTypeEnum;
import net.risesoft.id.IdType;
import net.risesoft.id.Y9IdGenerator;
import net.risesoft.y9.configuration.Y9Properties;
import net.risesoft.y9.util.base64.Y9Base64Util;
import net.risesoft.y9public.entity.tenant.Y9DataSource;
import net.risesoft.y9public.manager.tenant.Y9DataSourceManager;
import net.risesoft.y9public.repository.tenant.Y9DataSourceRepository;

/**
 * 数据源 Manager 实现类
 *
 * @author shidaobang
 * @date 2023/06/14
 * @since 9.6.2
 */
@Service
@Transactional(value = "rsPublicTransactionManager", readOnly = true)
@Slf4j
public class Y9DataSourceManagerImpl implements Y9DataSourceManager {

    private final JdbcTemplate jdbcTemplate4Public;
    private final Y9Properties y9config;
    private final Y9DataSourceRepository datasourceRepository;

    public Y9DataSourceManagerImpl(@Qualifier("jdbcTemplate4Public") JdbcTemplate jdbcTemplate4Public,
        Y9Properties y9config, Y9DataSourceRepository datasourceRepository) {
        this.jdbcTemplate4Public = jdbcTemplate4Public;
        this.y9config = y9config;
        this.datasourceRepository = datasourceRepository;
    }

    /**
     * 生成四位随机字符串
     *
     * @return {@link String}
     */
    public static String generateRandomString() {
        String characters = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 4;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            char randomChar = characters.charAt(index);
            sb.append(randomChar);
        }

        return sb.toString();
    }

    @Override
    public String buildDataSourceName(String shortName, TenantTypeEnum tenantType, String systemName) {
        String dataSourceName = shortName;
        if (Objects.equals(tenantType, TenantTypeEnum.ISV)) {
            dataSourceName = "isv_" + shortName;
        }
        if (Objects.equals(tenantType, TenantTypeEnum.TENANT) && !"default".equals(shortName)) {
            dataSourceName = "yt_" + generateRandomString() + "_" + shortName;
        }
        if (StringUtils.isNotBlank(systemName)) {
            dataSourceName = dataSourceName + "_" + systemName;
        }
        return dataSourceName;
    }

    @Override
    @Transactional(readOnly = false)
    public Y9DataSource createTenantDefaultDataSource(String dbName) {
        return this.createTenantDefaultDataSource(dbName, null);
    }

    @Override
    @Transactional(readOnly = false)
    public Y9DataSource createTenantDefaultDataSource(String dbName, String specifyId) {
        if (StringUtils.isNotBlank(specifyId)) {
            Optional<Y9DataSource> y9DataSourceOptional = datasourceRepository.findById(specifyId);
            if (y9DataSourceOptional.isPresent()) {
                return y9DataSourceOptional.get();
            }
        }

        Optional<Y9DataSource> y9DataSourceOptional = datasourceRepository.findByJndiName(dbName);
        if (y9DataSourceOptional.isPresent()) {
            return y9DataSourceOptional.get();
        }

        DruidDataSource dds = (DruidDataSource)jdbcTemplate4Public.getDataSource();
        String dbType = dds.getDbType();

        String url = null;
        String username = null;
        String password = null;

        if (DbType.mysql.name().equals(dbType)) {
            url = replaceDatabaseNameInMysqlJdbcUrl(dds.getUrl(), dbName);
            username = dds.getUsername();
            password = dds.getPassword();

            String sql = "CREATE DATABASE IF NOT EXISTS " + dbName + " DEFAULT CHARACTER SET UTF8 COLLATE UTF8_BIN";

            try {
                jdbcTemplate4Public.update(sql);
            } catch (DataAccessException e) {
                LOGGER.warn("创建数据源失败", e);
                return null;
            }
        }

        if (DbType.oracle.name().equals(dbType)) {
            url = dds.getUrl();
            username = dbName.toUpperCase();
            password = dds.getPassword();

            String newTableSpace = y9config.getApp().getY9DigitalBase().getOrclNewTableSpace() + username + "_DATA.DBF";
            // 创建临时表空间
            // String newTempSpace = y9config.getApp().getY9DigitalBase().getOrclNewTableSpace()+username+"_TEMP.DBF";
            // String sql0 = "CREATE TEMPORARY TABLESPACE "+username+"_TEMP TEMPFILE '"+newTempSpace+"' SIZE 10M
            // AUTOEXTEND ON NEXT 10M EXTENT MANAGEMENT LOCAL";

            // 创建表空间
            String sql1 = "CREATE TABLESPACE " + username + "_DATA DATAFILE '" + newTableSpace
                + "' SIZE 100M AUTOEXTEND ON NEXT 10M MAXSIZE UNLIMITED EXTENT MANAGEMENT LOCAL";
            // 创建用户
            String sql2 = "CREATE USER " + username + " IDENTIFIED BY " + password
                + " ACCOUNT UNLOCK DEFAULT TABLESPACE " + username + "_DATA TEMPORARY TABLESPACE TEMP PROFILE DEFAULT";
            // 给用户授权
            String sql3 = "GRANT DBA TO " + username + " WITH ADMIN OPTION";
            // 修改权限
            String sql4 = "ALTER USER " + username + " DEFAULT ROLE DBA";

            try {
                // jdbcTemplate4Public.update(sql0);
                jdbcTemplate4Public.update(sql1);
                jdbcTemplate4Public.update(sql2);
                jdbcTemplate4Public.update(sql3);
                jdbcTemplate4Public.update(sql4);
            } catch (DataAccessException e) {
                LOGGER.warn("创建数据源失败", e);
                return null;
            }
        }

        if (DbType.kingbase.name().equals(dbType)) {
            // 创建模式并授权
            String sql1 = "CREATE SCHEMA " + dbName;
            try {
                jdbcTemplate4Public.update(sql1);
            } catch (DataAccessException e) {
                LOGGER.warn("创建数据源失败", e);
                return null;
            }
            username = dds.getUsername();
            url = dds.getUrl().split("=")[0] + "=" + dbName.toUpperCase();
            password = dds.getPassword();
        }

        Y9DataSource y9DataSource = new Y9DataSource();
        y9DataSource.setJndiName(dbName);
        y9DataSource.setUrl(url);
        y9DataSource.setType(DataSourceTypeEnum.DRUID);
        y9DataSource.setUsername(username);
        y9DataSource.setPassword(password);
        y9DataSource.setInitialSize(dds.getInitialSize());
        y9DataSource.setMaxActive(dds.getMaxActive());
        y9DataSource.setMinIdle(dds.getMinIdle());
        y9DataSource.setId(Optional.ofNullable(specifyId).orElse(Y9IdGenerator.genId(IdType.SNOWFLAKE)));
        return this.save(y9DataSource);
    }

    @Override
    @Transactional(readOnly = false)
    public Y9DataSource createTenantDefaultDataSource(String shortName, TenantTypeEnum tenantType, String systemName) {
        String dataSourceName = this.buildDataSourceName(shortName, tenantType, systemName);
        return this.createTenantDefaultDataSource(dataSourceName, null);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(String id) {
        datasourceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = false)
    public void dropTenantDefaultDataSource(String dataSourceId, String dbName) {
        if (StringUtils.isNotBlank(dataSourceId)) {
            this.delete(dataSourceId);
        }
        if (StringUtils.isNotBlank(dbName)) {
            DruidDataSource dds = (DruidDataSource)jdbcTemplate4Public.getDataSource();
            if (dds != null) {
                if (DbType.mysql.name().equals(dds.getDbType())) {
                    String sql = "DROP DATABASE IF EXISTS " + dbName;
                    jdbcTemplate4Public.execute(sql);
                } else if (DbType.oracle.name().equals(dds.getDbType())) {
                    String username = dbName.toUpperCase();
                    String sql1 = "DROP USER " + username + " CASCADE";
                    String sql2 = "DROP TABLESPACE " + username + "_DATA INCLUDING CONTENTS AND DATAFILES";
                    jdbcTemplate4Public.execute(sql1);
                    jdbcTemplate4Public.execute(sql2);
                } else if (DbType.kingbase.name().equals(dds.getDbType())) {
                    String username = dbName.toUpperCase();
                    String sql1 = "DROP SCHEMA " + username + " CASCADE;";
                    jdbcTemplate4Public.execute(sql1);
                }
            }
        }
    }

    public String replaceDatabaseNameInMysqlJdbcUrl(String originalJdbcUrl, String newDatabaseName) {
        // 假设原始的 JDBC URL 格式为：jdbc:mysql://localhost:3306/y9_public?allowPublicKeyRetrieval=true
        int dbNameStart = originalJdbcUrl.lastIndexOf("/") + 1;
        int dbNameEnd = originalJdbcUrl.indexOf("?");

        if (dbNameStart >= 0 && dbNameEnd > dbNameStart) {
            String oldDatabaseName = originalJdbcUrl.substring(dbNameStart, dbNameEnd);

            return originalJdbcUrl.replace(oldDatabaseName, newDatabaseName);
        } else {
            // 如果无法提取数据库名称部分或者替换失败，返回原始的 JDBC URL
            return originalJdbcUrl;
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Y9DataSource save(Y9DataSource y9DataSource) {
        if (StringUtils.isBlank(y9DataSource.getId())) {
            y9DataSource.setId(Y9IdGenerator.genId(IdType.SNOWFLAKE));
        }
        if (y9DataSource.getPassword() != null) {
            y9DataSource.setPassword(Y9Base64Util.encode(y9DataSource.getPassword()));
        }
        return datasourceRepository.save(y9DataSource);
    }

}
