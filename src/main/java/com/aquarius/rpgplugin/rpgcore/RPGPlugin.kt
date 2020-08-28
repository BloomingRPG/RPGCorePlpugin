package com.aquarius0715.rpgplugin

import com.aquarius0715.rpgplugin.mysql.MySQLManager
import org.bukkit.plugin.java.JavaPlugin

class RPGPlugin : JavaPlugin() {

    val prefix = ""

    val mySQLManager = MySQLManager(this, "RPGPlugin")

    override fun onEnable() {

    }

    override fun onDisable() {

    }

}