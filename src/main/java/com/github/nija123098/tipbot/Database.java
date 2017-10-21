package com.github.nija123098.tipbot;

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import sx.blah.discord.handle.obj.IUser;
import com.github.nija123098.tipbot.utility.Config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class Database {
    private static final Connection CONNECTION;
    private static final QueryRunner RUNNER;
    static {
        Connection c;
        try {
            MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
            dataSource.setUser(Config.DB_USER);
            dataSource.setPassword(Config.DB_PASS);
            dataSource.setServerName(Config.DB_HOST);
            dataSource.setPort(3306);
            dataSource.setDatabaseName(Config.DB_NAME);
            dataSource.setZeroDateTimeBehavior("convertToNull");
            dataSource.setUseUnicode(true);
            c = dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Could not init database connection!", e);
        }
        CONNECTION = c;
        HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20);
        config.setJdbcUrl("jdbc:mariadb://" + Config.DB_HOST + ":" + Config.DB_PORT + "/" + Config.DB_NAME);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setUsername(Config.DB_USER);
        config.setPassword(Config.DB_PASS);
        RUNNER = new QueryRunner(new HikariDataSource(config));
        query("SET NAMES utf8mb4");
    }

    // DB stuff
    private static <E> E select(String sql, ResultSetHandler<E> handler) {
        try{return RUNNER.query(sql, handler);
        } catch (SQLException e) {
            throw new RuntimeException("Could not select: ERROR: " + e.getErrorCode() + " " + sql, e);
        }
    }

    private static void query(String sql) {
        try{RUNNER.update(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not query: ERROR: " + e.getErrorCode() + " " + sql, e);
        }
    }

    private static void insert(String sql) {// set
        try{RUNNER.update(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not insert: ERROR: " + e.getErrorCode() + " " + sql, e);
        }
    }

    private static String quote(String id){
        return Character.isDigit(id.charAt(0)) ? id : "'" + id + "'";
    }

    private static final Set<String> EXISTING_TABLES = new HashSet<>();
    private static void ensureTableExistence(String table){
        if (!EXISTING_TABLES.add(table)) return;
        try (ResultSet rs = CONNECTION.getMetaData().getTables(null, null, table, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(table)) return;
            }// make
            Database.query("CREATE TABLE `" + table + "` (id TINYTEXT, value TINYTEXT)");
        } catch (SQLException e) {
            throw new RuntimeException("Could not ensure table existence", e);
        }
    }

    // HELPERS
    public static void setValue(String table, IUser user, String value){
        ensureTableExistence(table);
        Database.query("DELETE FROM " + table + " WHERE id = " + user.getStringID());
        Database.insert("INSERT INTO " + table + " (`id`, `value`, `millis`) VALUES ('" + user.getStringID() + "','" + value + "');");
    }

    public static String getValue(String table, IUser user, String defaul){
        ensureTableExistence(table);
        return Database.select("SELECT * FROM " + table + " WHERE id = " + Database.quote(user.getStringID()), set -> {
            try{set.next();
                return set.getString(2);
            } catch (SQLException e) {
                if (!e.getMessage().equals("Current position is after the last row")) throw new RuntimeException("Error while getting value", e);
                return defaul;
            }
        });
    }
}
