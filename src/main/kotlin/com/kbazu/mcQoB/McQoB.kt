package com.kbazu.mcQoB

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.math.abs

var errorColor = ChatColor.RED
var commandColor = ChatColor.AQUA
var helpColor = ChatColor.WHITE
var titleColor = ChatColor.DARK_AQUA

lateinit var configFile: File
lateinit var dataFile: File

class McQoB: JavaPlugin() {
    lateinit var config: YamlConfiguration
    override fun onEnable() {
        config = YamlConfiguration()
        config.createSection("QoBData_Path")
        config.addDefault("QoBData_Path", "${this.dataFolder}${File.separator}datas.bzu")

        configFile = File("${this.dataFolder}${File.separator}config.yml")
        if(!configFile.exists()) {
            configFile.createNewFile()
            config.save(configFile)
        }

        loadConfig()

        if(dataFile.exists()) qoBLoad(dataFile)

        qoBCommands()
    }

    override fun onDisable() {

        qoBSave(dataFile)
    }

    fun loadConfig(){
        config.load(configFile)

        val path = config.getString("QoBData_Path")
        if(path != null) dataFile = File(path)
    }

    fun qoBCommands(){
        QoBCommand(this, "바쭈플러그인", mutableListOf(
            object: QoBFixedArg("버전", "mcQoB 플러그인의 버전을 출력합니다", true){
                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    sender.sendMessage("${helpColor}현재 mcQoB의 버전: ${state.versionStr(version)}")

                    return true
                }
            },
            object: QoBFixedArg("컨피그", "", false){
                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    return false
                }
            }.appendArgs(
                object: QoBFixedArg("리로드", "컨피그 파일을 리로드 합니다"){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        try{
                            loadConfig()
                            sender.sendMessage("${helpColor}로드되었습니다")
                            return true
                        }catch(e: Exception){
                            sender.sendMessage("${helpColor}로드에 실패했습니다")
                            return true
                        }
                    }
                },
                object: QoBFixedArg("파일", "컨피그 파일을 경로를 봅니다"){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        sender.sendMessage("${helpColor}컨피고 파일의 경로: ${configFile.path}")

                        return true
                    }
                }
            ),
            object: QoBFixedArg("색", "mcQoB에 사용되는 채팅 색깔 조합을 출력합니다", true){
                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    sender.sendMessage("${helpColor}일반 메세지의 색깔: ${helpColor.name}")
                    sender.sendMessage("${commandColor}커맨드 용법 메세지의 색깔: ${commandColor.name}")
                    sender.sendMessage("${errorColor}에러 메세지의 색깔: ${errorColor.name}")

                    return true
                }
            }.appendArgs(
                object: QoBFixedArg("에러", "mcQoB에 사용되는 에러 메세지의 색깔을 출력합니다", true){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        sender.sendMessage("${errorColor}에러 메세지의 색깔: ${errorColor.name}")

                        return true
                    }

                }.appendArgs(
                    object: QoBColorArg("에러 메세지의 색깔을 정합니다", true, completed = true){
                        override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            return super.check(sender, cmd, cmdStr) && sender.isOp()
                        }

                        override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean {
                            return super.check(sender, cmd, cmdStr, arg) && sender.isOp
                        }

                        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            if(result != null){
                                errorColor = result!!

                                sender.sendMessage("${errorColor}에러 메세지의 색이 ${errorColor.name}으로 설정되었습니다")
                                return true
                            }
                            return false
                        }
                    }
                ),
                object: QoBFixedArg("커맨드", "mcQoB에 사용되는 커맨드 메세지의 색깔을 출력합니다", true){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        sender.sendMessage("${commandColor}커맨드 용법 메세지의 색깔: ${commandColor.name}")

                        return true
                    }

                }.appendArgs(
                    object: QoBColorArg("커맨드 메세지의 색깔을 정합니다", true, completed = true){
                        override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            return super.check(sender, cmd, cmdStr) && sender.isOp()
                        }

                        override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean {
                            return super.check(sender, cmd, cmdStr, arg) && sender.isOp
                        }

                        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            if(result != null){
                                commandColor = result!!

                                sender.sendMessage("${commandColor}커맨드 메세지의 색이 ${commandColor.name}으로 설정되었습니다")
                                return true
                            }
                            return false
                        }
                    }
                ),
                object: QoBFixedArg("일반", "mcQoB에 사용되는 일반 메세지의 색깔을 출력합니다", true){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        sender.sendMessage("${helpColor}일반 메세지의 색깔: ${helpColor.name}")

                        return true
                    }

                }.appendArgs(
                    object: QoBColorArg("일반 메세지의 색깔을 정합니다", true, completed = true){
                        override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            return super.check(sender, cmd, cmdStr) && sender.isOp()
                        }

                        override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean {
                            return super.check(sender, cmd, cmdStr, arg) && sender.isOp
                        }

                        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            if(result != null){
                                helpColor = result!!

                                sender.sendMessage("${helpColor}일반 메세지의 색이 ${helpColor.name}으로 설정되었습니다")
                                return true
                            }
                            return false
                        }
                    }
                )
            ),
            object: QoBFixedArg("페이지", "현재 생성된 페이지들의 정보를 출력합니다"){
                override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = super.check(sender, cmd, cmdStr) && sender.isOp
                override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean = super.check(sender, cmd, cmdStr, arg) && sender.isOp

                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    sender.sendMessage("총 ${pages.size}개의 페이지가 있습니다")
                    for (page in pages){
                        printPage(sender, page)
                    }

                    return true
                }
            }.appendArgs(
                object: QoBStringArg("[페이지 제목]", "페이지를 엽니다(플레이어만 실행 가능)"){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        if(sender !is Player) return false
                        for (page in pages){
                            if(result == page.title){
                                printPage(sender, page)

                                return true
                            }
                        }
                        return false
                    }
                }
            ),
            object: QoBFixedArg("데이터", "현재 저장된 QoBData들의 키를 봅니다"){
                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    sender.sendMessage("${helpColor}현재 ${datas.size}개의 QoBData가 있습니다")

                    var str = "${helpColor}"
                    for (data in datas){
                        str = str + data.key + ", "
                    }

                    sender.sendMessage(str)

                    return true
                }
            }.appendArgs(
                object: QoBStringArg("[데이터 키]", "데이터를 읽어와 출력합니다"){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        for (data in datas){
                            if(result == data.key){
                                sender.sendMessage("${titleColor}${data.key}")
                                sender.sendMessage("${helpColor}${data.serialize()}")
                                sender.sendMessage("${helpColor}${if(data.shouldNotLoad) "저장 진행됨" else "저장 진행되지 않음"}")

                                return true
                            }
                        }

                        return false
                    }

                },
                object: QoBFixedArg("파일", "데이터를 저장하는 파일 경로를 출력합니다"){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        sender.sendMessage(dataFile.path)

                        return true
                    }

                },
                object: QoBFixedArg("저장", "데이터를 저장합니다"){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        try{
                            qoBSave(dataFile)
                            sender.sendMessage("저장되었습니다")
                        }catch(e: Exception){
                            sender.sendMessage("저장에 실패했습니다")
                        }

                        return true
                    }
                },
                object: QoBFixedArg("불러오기", "데이터를 불러옵니다"){
                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        try{
                            qoBLoad(dataFile)
                            sender.sendMessage("저장되었습니다")
                        }catch(e: Exception){
                            sender.sendMessage("저장에 실패했습니다")
                        }

                        return true
                    }
                }
            ),
            object: QoBFixedArg("타이머", "타이머들의 정보를 출력합니다", true){
                override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = super.check(sender, cmd, cmdStr) && sender.isOp
                override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean = super.check(sender, cmd, cmdStr, arg) && sender.isOp

                override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                    sender.sendMessage("총 ${timers.size}개의 타이머가 있습니다")
                    for (timer in timers){
                        printTimer(sender, timer)
                    }

                    return true
                }
            }.appendArgs(
                object: QoBValuedArg<QoBBossBarTimer>("[타이머 제목]", "존재하는 타이머의 정보를 출력합니다", completed = true){
                    override var result: QoBBossBarTimer? = null

                    override fun value(sender: CommandSender, cmd: Command, cmdStr: String, arg: String) {
                        for (timer in timers){
                            if(timer.title == arg){
                                result = timer
                            }
                        }
                    }

                    override fun check(sender: CommandSender, cmd: Command, cmdStr: String, arg: String): Boolean = timers.any { it.title == arg }

                    override fun check(sender: CommandSender, cmd: Command, cmdStr: String): Boolean = true

                    override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                        if(result != null) {
                            printTimer(sender, result!!)
                            return true
                        }

                        return false
                    }

                    override fun tabComplete(): MutableList<String> {
                        val complete = mutableListOf<String>()
                        for (timer in timers){
                            complete.add(timer.title)
                        }

                        return complete
                    }

                }.appendArgs(
                    object: QoBFixedArg("일시정지", "타이머를 일시정지합니다", completed = true){
                        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            if(beforeArgs[1] is QoBValuedArg<*> && (beforeArgs[1] as QoBValuedArg<*>).result != null && (beforeArgs[1] as QoBValuedArg<*>).result is QoBBossBarTimer){
                                val timer = ((beforeArgs[1] as QoBValuedArg<*>).result as QoBBossBarTimer)
                                timer.pause()

                                sender.sendMessage("${helpColor}타이머가 일시정지 되었습니다")
                            }

                            return true
                        }
                    },
                    object: QoBFixedArg("계속", "일시정지한 타이머를 다시 시작합니다", completed = true){
                        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            if(beforeArgs[1] is QoBValuedArg<*> && (beforeArgs[1] as QoBValuedArg<*>).result != null && (beforeArgs[1] as QoBValuedArg<*>).result is QoBBossBarTimer){
                                val timer = ((beforeArgs[1] as QoBValuedArg<*>).result as QoBBossBarTimer)
                                timer.start()

                                sender.sendMessage("${helpColor}타이머가 계속 진행됩니다")
                            }

                            return true
                        }
                    },
                    object: QoBFixedArg("배속", "현재 타이머가 몇배속으로 흘러가는지 봅니다", completed = true){
                        override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                            if(beforeArgs[1] is QoBValuedArg<*> && (beforeArgs[1] as QoBValuedArg<*>).result != null && (beforeArgs[1] as QoBValuedArg<*>).result is QoBBossBarTimer){
                                val timer = ((beforeArgs[1] as QoBValuedArg<*>).result as QoBBossBarTimer)
                                sender.sendMessage("${helpColor}현재 타이머는 ${abs(timer.speed)}배속으로 흘러가는 중입니다")
                            }

                            return true
                        }
                    }.appendArgs(
                        object: QoBIntArg("[배속 정수]", "타이머가 몇배속으로 흘러갈지 설정합니다(최대 40배속).", range = 1..40){
                            override var result: Int? = null

                            override fun run(sender: CommandSender, cmd: Command, cmdStr: String): Boolean {
                                if(beforeArgs[1] is QoBValuedArg<*> && beforeArgs[1] is QoBValuedArg<*> && (beforeArgs[1] as QoBValuedArg<*>).result != null && (beforeArgs[1] as QoBValuedArg<*>).result is QoBBossBarTimer){
                                    val timer = ((beforeArgs[1] as QoBValuedArg<*>).result as QoBBossBarTimer)

                                    if(result != null) timer.speed = result!!
                                    else return false

                                    sender.sendMessage("${helpColor}타이머 속도가 ${abs(timer.speed)}배속으로 설정되었습니다")
                                }

                                return true
                            }
                        }
                    )
                )
            )
        ))
    }
}

val version: Float = 4.0f
val state: State = State.Alpha

interface StateFunc{
    val str: String
    fun versionStr(version: Float): String{
        return str+version.toString()
    }
}

enum class State(override val str: String) : StateFunc {
    Alpha("a"),
    Beta("b"),
    Release("")
}