package com.aquarius0715.rpgplugin

import com.aquarius0715.rpgplugin.mysql.MySQLManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.TabCompleteEvent
import java.util.*


class BanCommands(plugin: RPGPlugin) : Listener {

    var badCommandList: MutableList<String> = ArrayList()

    val mySQLManager = MySQLManager(plugin, "RPGPlugin")

    //このメソッドで禁止コマンドを指定する
    //指定されたコマンドはタブ保管も不可能になる

    private fun initBadCommand() {

        badCommandList.add("/pl")
        badCommandList.add("/plugins")
        badCommandList.add("/bukkit:pl")
        badCommandList.add("/bukkit:plugins")
        badCommandList.add("/?")
        badCommandList.add("/bukkit:?")
        badCommandList.add("/help")
        badCommandList.add("/bukkit:help")
        badCommandList.add("/ver")
        badCommandList.add("/version")
        badCommandList.add("/bukkit:version")
        badCommandList.add("/bukkit:ver")

    }



    @EventHandler

    fun onCommandExecute(e: PlayerCommandPreprocessEvent) {

        if (badCommandList.contains(e.message)) {

            if (e.player.hasPermission("admin")) {

                return

            }

            e.player.sendMessage("このコマンドは使用を禁止されています。")

            mySQLManager.execute("INSERT INTO EXECUTED_BAN_COMMAND_PLAYER " +
                    "(UUID, PLAYER_NAME, COMMAND) VALUE " +
                    "('${e.player.uniqueId}', '${e.player.name}', '${e.message}'" +
                    ");")

            e.isCancelled = true

        }

    }



    @EventHandler

    fun onTabComplete(event: TabCompleteEvent) {

        if (!event.buffer.startsWith("/")) {

            return  // not a command, or console entered command. ignore.

        }

        val sender = event.sender

        if (sender.hasPermission("brpg.badcom.bypass")) {

            return  // has bypass. ignore

        }

        for (bad in badCommandList) {

            if (event.buffer.startsWith(bad)) {

                event.completions = ArrayList()

                return

            }

        }

        val comple = event.completions

        comple.removeAll(badCommandList)
        event.completions = comple
    }

    init {
        initBadCommand()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }
}