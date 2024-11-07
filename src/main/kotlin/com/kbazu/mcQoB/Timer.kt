package com.kbazu.mcQoB

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarFlag
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.text.DecimalFormat

val timers = mutableListOf<QoBBossBarTimer>()

fun timeFormat(tick: Int): String{
    return "${if (tick/20/60/60==0) "" else "${String.format("%02d", tick/20/60/60)}:"}${String.format("%02d", tick/20/60%60)}:${String.format("%02d", tick/20%60)}.${5*tick%20}"
}

fun printTimer(sender: CommandSender, timer: QoBBossBarTimer){
    sender.sendMessage("$titleColor${timer.title} 타이머")
    sender.sendMessage("$helpColor  - 최대 시간: ${timeFormat(timer.maxTick)}")
    sender.sendMessage("$helpColor  - 현재 지난 시간: ${timeFormat(timer.nowTick)}")
    sender.sendMessage("$helpColor  - 진행률: ${String.format("%.2f", timer.progression())}%")
    sender.sendMessage("$helpColor  - 다음 경고: ${if(timer.alertAt==-1) "없음" else timeFormat(if(timer.isTimer) timer.maxTick-timer.alertAt else timer.alertAt)}")
}

abstract class QoBBossBarTimer(val plugin: JavaPlugin, var maxTick: Int, var nameColor: ChatColor, name: String, color: BarColor, style: BarStyle, vararg flags: BarFlag, val isTimer: Boolean=false, var alertAt: Int=-1) {
    var title: String = "${ChatColor.DARK_RED}[만약 이 글을 보셨다면 위험한 상황이니 도망치세요]"
        get() = field
        set(value) {
            bossBar.setTitle("${nameColor}${value}")
            field = value
        }
    val bossBar: BossBar
    var nowTick = 0
    var speed = 1
    var runnable: BukkitRunnable? = null
    init{
        timers.add(this)

        bossBar = Bukkit.createBossBar("${nameColor}${name}", color, style, *flags)

        if(isTimer){
            nowTick = maxTick
            speed = -1
        }

        this.title = name
    }

    constructor(plugin: JavaPlugin, minutes: Int, seconds: Int, nameColor: ChatColor, name: String,
                color: BarColor, style: BarStyle, vararg flags: BarFlag,
                isTimer: Boolean=false, alertAt: Int=-1):
            this(plugin, minutes*20*60+seconds*20, nameColor, name, color, style, *flags, isTimer=isTimer, alertAt=alertAt)

    constructor(plugin: JavaPlugin, hours: Int, minutes: Int, seconds: Int, nameColor: ChatColor, name: String,
                color: BarColor, style: BarStyle,
                vararg flags: BarFlag, isTimer: Boolean=false, alertAt: Int=-1):
            this(plugin, (hours*60*60+minutes*60+seconds)*20, nameColor, name, color, style, *flags, isTimer=isTimer, alertAt=alertAt)

    fun start(vararg players: Player){
        if(runnable == null) {
            for (player in players){
                bossBar.addPlayer(player)
            }

            runnable = object : BukkitRunnable() {
                override fun run() {
                    bossBar.progress = nowTick.toDouble() / maxTick
                    nowTick += speed

                    if (nowTick in alertAt until alertAt+speed) onAlert()

                    if(isTimer){
                        if (nowTick < 0) {
                            pause()
                            onEnd()
                        }
                    }else{
                        if (nowTick >= maxTick) {
                            pause()
                            onEnd()
                        }
                    }
                }
            }

            runnable!!.runTaskTimer(plugin, 0, 1)
        }
    }

    fun pause(){
        if(runnable != null) {
            runnable!!.cancel()
            runnable = null
        }
    }

    fun hide(vararg players: Player){
        for (player in players){
            bossBar.removePlayer(player)
        }
    }

    fun progression(): Double{
        return if(isTimer) (maxTick-nowTick).toDouble() / maxTick*100 else nowTick.toDouble() / maxTick*100
    }

    abstract fun onAlert()
    abstract fun onEnd()

    final fun remove(){
        timers.remove(this)
    }
}