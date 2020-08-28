package com.aquarius0715.rpgplugin.mysql

import com.aquarius0715.rpgplugin.RPGPlugin
import org.bukkit.Bukkit
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.logging.Level

/**
 * Created by takatronix on 2017/03/05.
 *
 * Refactored by Aquarius0715 on 2020/08/14
 */

class MySQLManager(private val plugin: RPGPlugin, private val conName: String) {

    var debugMode = false
    private var host: String? = null
    private var db: String? = null
    private var log: Boolean = false
    private var user: String? = null
    private var pass: String? = null
    private var port: String? = null
    private var connected = false
    private var st: Statement? = null
    private var con: Connection? = null
    private var mysql: MySQLFunc? = null

    /////////////////////////////////
    //       設定ファイル読み込み
    /////////////////////////////////

    private fun loadConfig() {

        //   plugin.getLogger().info("MYSQL Config loading");

        plugin.reloadConfig()

        host = plugin.config.getString("mysql.host")
        user = plugin.config.getString("mysql.user")
        pass = plugin.config.getString("mysql.pass")
        port = plugin.config.getString("mysql.port")
        db = plugin.config.getString("mysql.db")
        log = plugin.config.getBoolean("mysql.log")

        //  plugin.getLogger().info("Config loaded");

    }

    fun commit() {

        try {

            con!!.commit()

        } catch (e: Exception) {

        }

    }

    ////////////////////////////////
    //       接続
    ////////////////////////////////

    private fun connect(host: String?, db: String?, user: String?, pass: String?, port: String?): Boolean {

        this.host = host
        this.db = db
        this.user = user
        this.pass = pass

        mysql = MySQLFunc(host, db, user, pass, port)

        con = mysql!!.open()

        if (con == null) {

            Bukkit.getLogger().info("failed to open MYSQL")

            return false

        }

        try {

            st = con!!.createStatement()

            connected = true

            plugin.logger.info("[$conName] Connected to the database.")

        } catch (var6: SQLException) {

            connected = false

            plugin.logger.info("[$conName] Could not connect to the database.")

        }

        mysql!!.close(con)

        return connected

    }

    //////////////////////////////////////////
    //         接続確認
    //////////////////////////////////////////

    private fun connectCheck(): Boolean {

        return connect(host, db, user, pass, port)

    }

    ////////////////////////////////
    //     行数を数える
    ////////////////////////////////

    fun countRows(table: String): Int {

        var count = 0

        val set = query(String.format("SELECT * FROM %s", table))

        try {

            while (set!!.next()) {

                ++count

            }

        } catch (var5: SQLException) {

            Bukkit.getLogger().log(Level.SEVERE, "Could not select all rows from table: " + table + ", error: " + var5.errorCode)

        }

        return count

    }

    ////////////////////////////////
    //     レコード数
    ////////////////////////////////

    fun count(table: String): Int {

        val count: Int

        val set = query(String.format("SELECT count(*) from %s", table))

        count = try {

            set!!.getInt("count(*)")

        } catch (var5: SQLException) {

            Bukkit.getLogger().log(Level.SEVERE, "Could not select all rows from table: " + table + ", error: " + var5.errorCode)

            return -1

        }

        return count

    }

    ////////////////////////////////
    //      実行
    ////////////////////////////////

    fun execute(query: String): Boolean {

        mysql = MySQLFunc(host, db, user, pass, port)

        con = mysql!!.open()

        if (con == null) {

            Bukkit.getLogger().info("failed to open MYSQL")

            return false

        }

        var ret = true

        if (debugMode) {

            plugin.logger.info("query:$query")

        }
        try {

            st = con!!.createStatement()

            st!!.execute(query)

        } catch (var3: SQLException) {

            plugin.logger.info("[" + conName + "] Error executing statement: " + var3.errorCode + ":" + var3.localizedMessage)

            plugin.logger.info(query)

            ret = false

        }

        close()

        return ret

    }

    ////////////////////////////////
    //      クエリ
    ////////////////////////////////

    fun query(query: String): ResultSet? {

        mysql = MySQLFunc(host, db, user, pass, port)

        con = mysql!!.open()

        var rs: ResultSet? = null

        if (con == null) {

            Bukkit.getLogger().info("failed to open MYSQL")

            return rs

        }

        if (debugMode) {

            plugin.logger.info("[DEBUG] query:$query")

        }

        try {

            st = con!!.createStatement()

            rs = st!!.executeQuery(query)

        } catch (var4: SQLException) {

            plugin.logger.info("[" + conName + "] Error executing query: " + var4.errorCode)

            plugin.logger.info(query)

        }

//        this.close();

        return rs

    }

    fun close() {

        try {

            st!!.close()

            con!!.close()

            mysql!!.close(con)

        } catch (var4: SQLException) {

        }

    }

    ////////////////////////////////
    //      コンストラクタ
    ////////////////////////////////

    init {

        connected = false

        loadConfig()

        connected = connect(host, db, user, pass, port)

        if (!connected) {

            plugin.logger.info("Unable to establish a MySQL connection.")

        }

            //LoginBonusTable
            execute("CREATE TABLE IF NOT EXISTS LOGIN_LOG (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "LAST_LOGIN DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "GET_BONUS DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ");")

            //BankAccountTable
            execute("CREATE TABLE IF NOT EXISTS BANK_ACCOUNT_LOG (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "DATE DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "DESCRIPTION VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "WITHDRAWS INT NOT NULL DEFAULT 0," +
                    "DEPOSITS INT NOT NULL DEFAULT 0," +
                    "BALANCE INT NOT NULL DEFAULT 0" +
                    ");")

            //PlayerDataTable
            execute("CREATE TABLE IF NOT EXISTS PLAYER_DATA (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "JOB VARCHAR(64) NOT NULL DEFAULT '遊び人'," +
                    "LEVEL INT NOT NULL DEFAULT '0'," +
                    "REBIRTH_COUNT INT NOT NULL DEFAULT '0'" +
                    ");")

            //JobStatsTable
            execute("CREATE TABLE IF NOT EXISTS JOB_DATA (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "JOB VARCHAR(64) NOT NULL DEFAULT '遊び人'," +
                    "SKILL_LEVEL INT NOT NULL DEFAULT 0," +
                    "OVER_FLOW INT NOT NULL DEFAULT 0" +
                    ");")

            //PlayerStatsTable
            execute("CREATE TABLE IF NOT EXISTS PLAYER_STATUS (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "STR INT NOT NULL DEFAULT 0," +
                    "VIT INT NOT NULL DEFAULT 0," +
                    "AGI INT NOT NULL DEFAULT 0," +
                    "DEX INT NOT NULL DEFAULT 0," +
                    "LUC INT NOT NULL DEFAULT 0," +
                    "SP INT NOT NULL DEFAULT 0," +
                    "STATS_POINT INT NOT NULL DEFAULT 0" +
                    ");")

            //SwordSkillTable
            execute("CREATE TABLE IF NOT EXISTS SWORD_SKILL (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "SKILL_POINT INT NOT NULL DEFAULT 0" +
                    ");")

            //BowSkillTable
            execute("CREATE TABLE IF NOT EXISTS BOW_SKILL (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "SKILL_POINT INT NOT NULL DEFAULT 0" +
                    ");")

            //DaggerSkillTable
            execute("CREATE TABLE IF NOT EXISTS DAGGER_SKILL (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "SKILL_POINT INT NOT NULL DEFAULT 0" +
                    ");")

            //ThePlayerExecuteBanCommand
            execute("CREATE TABLE IF NOT EXISTS EXECUTED_BAN_COMMAND_PLAYER (" +
                    "UUID VARCHAR(36) NOT NULL DEFAULT 'unknown'," +
                    "PLAYER_NAME VARCHAR(16) NOT NULL DEFAULT 'unknown'," +
                    "DATE DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "COMMAND VARCHAR(32) NOT NULL DEFAULT 'unknown'" +
                    ");")

    }

    /////////////////////////////////
    //接続確認
    /////////////////////////////////

    fun sqlConnectSafely(): Boolean {

        if (!connectCheck()) {

            Bukkit.broadcastMessage("${plugin.prefix}DB接続に失敗したためプラグインを停止します。")

            return false

        }

        return true

    }

}