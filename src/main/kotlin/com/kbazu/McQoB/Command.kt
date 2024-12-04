package com.kbazu.mcQoB

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class QoBCommand(val plugin: JavaPlugin, val cmdStr: String, val firstArgs: MutableList<QoBArg>): CommandExecutor, TabCompleter{
    init{
        val cmd = plugin.getCommand(cmdStr)
        if(cmd != null){
            cmd.setExecutor(this)
            cmd.setTabCompleter(this)
        }

        firstArgs.addFirst(object: QoBFixedArg("도움말", "이 도움말을 보여줍니다", true){
            override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                help(helpColor, sender, cmd, cmdStr)
                return true
            }
        })
    }

    override fun onCommand(
        sender: CommandSender, cmd: Command, cmdStr: String, args: Array<out String>
    ): Boolean {
        var succeed = false
        val changedArgs = mutableListOf<QoBValuedArg<*>>()
        val nowArgs: MutableList<QoBArg> = mutableListOf()
        nowArgs.addAll(firstArgs)
        for (arg1 in args){
            val nextArgs = mutableListOf<QoBArg>()
            for (arg2 in nowArgs){
                if(arg2.check(sender, cmd, cmdStr, arg1)){
                    if(arg2 is QoBValuedArg<*>){
                        arg2.value(sender, cmd, cmdStr, arg1)
                        changedArgs.add(arg2)
                    }

                    if(args.indexOf(arg1) == args.size-1){
                        nowArgs.clear()
                        nowArgs.add(arg2)
                        break
                    }
                    nextArgs.addAll(arg2.nextArgs)
                }
            }
            if(args.indexOf(arg1) < args.size-1) {
                nowArgs.clear()
                nowArgs.addAll(nextArgs)
            }
        }

        if(nowArgs.size == 1 && nowArgs[0].completed){
            succeed = nowArgs[0].run(sender, cmd, cmdStr)
        }

        for (arg in changedArgs){
            arg.result = null
        }

        if(!succeed){
            help(errorColor, sender, cmd, cmdStr)
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender, cmd: Command, cmdStr: String, args: Array<out String>
    ): MutableList<String>? {
        var values = mutableListOf<String>()

        val checking: MutableList<QoBArg> = mutableListOf()
        checking.addAll(firstArgs)

        for (arg in args.slice(0 until args.size-1)) {
            while (checking.size > 0) {
                if (checking.first().check(sender, cmd, cmdStr, arg)){
                    val correct = checking.first()
                    checking.clear()
                    checking.addAll(correct.nextArgs)
                    break
                }

                checking.removeFirst()
            }
        }

        for (value in checking){
            if(value.check(sender, cmd, cmdStr)){
                for (complete in value.tabComplete()){
                    if(complete.startsWith(args.last())){
                        values.add(complete)
                    }
                }
            }
        }

        return values
    }

    fun help(color: ChatColor, sender: CommandSender, cmd: Command, cmdStr: String){
        for (arg in firstArgs){
            helpRecursive(arg, color, cmdStr, sender, cmd, cmdStr)
        }
    }

    fun helpRecursive(arg: QoBArg, color: ChatColor, str: String, sender: CommandSender, cmd: Command, cmdStr: String){
        if(arg.check(sender, cmd, cmdStr)){
            val newStr = str+" "+arg.helpStr
            if(arg.completed)
                sender.sendMessage("${commandColor}/${newStr}${ChatColor.RESET}${color}: ${arg.description}")
            for (nextArg in arg.nextArgs){
                helpRecursive(nextArg, color, newStr, sender, cmd, cmdStr)
            }
        }
    }
}

abstract class QoBIntArg(helpStr: String, description: String, vararg val completes: Int, val range: IntRange?=null, completed: Boolean=true): QoBValuedArg<Int>(helpStr, description, completed){
    val recommends = mutableListOf<String>()
    init{
        recommends.addAll(completes.map {
            it.toString()
        })
    }

    override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean = arg.toIntOrNull() != null && (range == null || arg.toInt() in range)
    override fun value(sender: CommandSender, cmd: Command, cmdStr: String, arg: String) = run { result = arg.toInt() }
    override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = true
    override fun tabComplete(): MutableList<String> = recommends
}

abstract class QoBColorArg(description: String, val onlyColor: Boolean=true, val includeReset: Boolean=true, completed: Boolean=true): QoBValuedArg<ChatColor>("[색깔]", description, completed){
    val ableColors = mutableListOf<ChatColor>()
    init{
        ableColors.addAll(ChatColor.entries)
        if(includeReset){
            ableColors.add(ChatColor.RESET)
        }

        if(onlyColor){
            ableColors.remove(ChatColor.RESET)
            ableColors.remove(ChatColor.BOLD)
            ableColors.remove(ChatColor.ITALIC)
            ableColors.remove(ChatColor.MAGIC)
            ableColors.remove(ChatColor.STRIKETHROUGH)
            ableColors.remove(ChatColor.UNDERLINE)
        }
    }

    override var result: ChatColor? = null

    override fun tabComplete(): MutableList<String> {
        val results = mutableListOf<String>()
        for (color in ableColors){
            results.add(color.name.lowercase())
        }

        return results
    }

    override fun value(sender: CommandSender, cmd: Command, cmdStr: String, arg: String){
        result = getColor(arg)
    }

    override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = true
    override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean {
        return getColor(arg) != null
    }

    fun getColor(arg: String): ChatColor?{
        for (color in ableColors){
            if(color.name.equals(arg, ignoreCase=true)){
                return color
            }
        }

        return null
    }
}

abstract class QoBStringArg(helpStr: String, description: String, vararg completes: String, completed: Boolean=true, val forceRecommendation: Boolean=false): QoBValuedArg<String>(helpStr, description, completed){
    val recommends = mutableListOf<String>()
    init{
        recommends.addAll(completes)
    }

    override fun tabComplete(): MutableList<String> {
        return recommends.toMutableList()
    }

    override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = true
    override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean = !forceRecommendation || arg in recommends
    override fun value(sender: CommandSender, cmd: Command, cmdStr: String, arg: String) = run { result = arg }
}

abstract class QoBPlayerArg(helpStr: String, description: String, completed: Boolean=true): QoBValuedArg<Player>(helpStr, description, completed){
    override fun tabComplete(): MutableList<String> {
        return Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
    }

    override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = true
    override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean = Bukkit.getPlayer(arg) != null
    override fun value(sender: CommandSender, cmd: Command, cmdStr: String, arg: String) {
        result = Bukkit.getPlayer(arg)
    }
}

abstract class QoBFixedArg(val str: String, description: String, completed: Boolean=true): QoBArg(str, description, completed){
    override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean = str.equals(arg, ignoreCase=true)
    override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = true
    override fun tabComplete(): MutableList<String> = mutableListOf(str)
}

abstract class QoBValuedArg<T>(helpStr: String, description: String, completed: Boolean=true): QoBArg(helpStr, description, completed){
    abstract var result: T?
    abstract fun value(sender: CommandSender, cmd: Command, cmdStr: String, arg: String)
}

abstract class QoBArg(val helpStr: String, val description: String, val completed: Boolean=true){
    val nextArgs = mutableListOf<QoBArg>()
    val beforeArgs = mutableListOf<QoBArg>()

    abstract fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean
    abstract fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean
    abstract fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean
    abstract fun tabComplete(): MutableList<String>

    fun appendArgs(vararg args: QoBArg): QoBArg {
        nextArgs.addAll(args)

        val allNext = mutableListOf<QoBArg>()
        allNext.addAll(args)
        var i = 0
        while (i < allNext.size){
            val next = allNext[i]
            if(next.nextArgs.isNotEmpty()){
                allNext.addAll(next.nextArgs)
            }

            i++
        }

        for (next in allNext){
            next.beforeArgs.addFirst(this)
        }

        return this
    }
}