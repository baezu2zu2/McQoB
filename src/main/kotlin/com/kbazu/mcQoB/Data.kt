package com.kbazu.mcQoB

import org.bukkit.Bukkit
import java.io.File

val datas = mutableListOf<QoBData<*>>()

fun qoBSave(file: File){
    if(!file.exists()) file.createNewFile()

    val fout = file.outputStream()

    for (data in datas){
        if(data.shouldNotLoad) continue
        fout.write("${data.key}: ${data.serialize()}\n".toByteArray())
    }
}

fun qoBLoad(file: File){
    val reader = file.reader()
    val lines = reader.readLines()

    var idx = 0
    while(idx < lines.size){
        try {
            lines[idx].replace(" ", "")
            val dst = lines[idx].split(":")
            val key = dst[0]
            val value = dst.slice(1..dst.size).joinToString()
            for (data in datas) {
                if(data.shouldNotLoad) continue
                if(data.key == key){
                    data.load(value)
                    break
                }
            }
            idx++
        }catch(e: Exception) {
            Bukkit.getConsoleSender().sendMessage(e.stackTrace.contentToString())
        }
    }
}

abstract class QoBData<T>: Cloneable{
    init{
        datas.add(this)
    }

    protected constructor()

    abstract var data: T
    abstract val key: String
    open val shouldNotLoad: Boolean = false

    abstract fun deserialize(value: String): T
    abstract fun serialize(): String
    final fun load(value: String){
        data = deserialize(value)
    }

    final fun remove(){
        datas.remove(this)
    }

}

abstract class QoBPlayerData<T>(override val key: String, val name: String, override var data: T, override val shouldNotLoad: Boolean=false): QoBData<T>()
abstract class QoBGlobalData<T>(override val key: String, override var data: T, override val shouldNotLoad: Boolean=false): QoBData<T>()

class QoBGlobalIntData(key: String, data: Int, shouldNotLoad: Boolean=false): QoBGlobalData<Int>(key, data, shouldNotLoad){
    override fun serialize(): String = data.toString()
    override fun deserialize(str: String): Int = str.toInt()
}
class QoBGlobalStringData(key: String, data: String, shouldNotLoad: Boolean=false): QoBGlobalData<String>(key, data, shouldNotLoad){
    override fun serialize(): String = "$key: $data"
    override fun deserialize(str: String): String = str
}
class QoBGlobalDoubleData(key: String, data: Double, shouldNotLoad: Boolean=false): QoBGlobalData<Double>(key, data, shouldNotLoad){
    override fun serialize(): String = "$key: $data"
    override fun deserialize(str: String): Double = str.toDouble()
}

class QoBPlayerIntData(key: String, name: String, data: Int, shouldNotLoad: Boolean=false): QoBPlayerData<Int>(key, name, data, shouldNotLoad){
    override fun deserialize(value: String): Int = value.toInt()
    override fun serialize(): String = "${name}_${key}"
}
class QoBPlayerDoubleData(key: String, name: String, data: Double, shouldNotLoad: Boolean=false): QoBPlayerData<Double>(key, name, data, shouldNotLoad){
    override fun deserialize(value: String): Double = value.toDouble()
    override fun serialize(): String = "${name}_${key}"
}
class QoBPlayerStringData(key: String, name: String, data: String, shouldNotLoad: Boolean=false): QoBPlayerData<String>(key, name, data, shouldNotLoad){
    override fun deserialize(value: String): String = value
    override fun serialize(): String = "${name}_${key}"
}

class QoBPlayersData<T>{
    val playerDatas = mutableSetOf<QoBPlayerData<T>>()

    operator fun get(name: String): QoBPlayerData<T>? = playerDatas.firstOrNull { it.name == name }
    operator fun set(name: String, data: QoBPlayerData<T>){
        val playerData = this[name]
        if(playerData == null){
            playerDatas.add(data)
        }else{
            playerDatas.remove(playerData)
            playerDatas.add(data)
        }
    }
    operator fun set(name: String, data: T){
        this[name]?.data = data
    }
    operator fun minusAssign(name: String){
        playerDatas.remove(this[name])
    }
    operator fun plusAssign(data: QoBPlayerData<T>){
        playerDatas.add(data)
    }
    operator fun contains(name: String): Boolean = playerDatas.count { it.name == name } > 0
    operator fun contains(data: QoBPlayerData<T>): Boolean = playerDatas.contains(data)
}