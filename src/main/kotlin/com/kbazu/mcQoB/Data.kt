package com.kbazu.mcQoB

import com.google.common.base.Charsets
import java.io.File

val datas = mutableListOf<QoBData<*>>()

fun qoBSave(file: File){
    if(!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }

    val fout = file.outputStream()

    for (data in datas){
        if(data.shouldNotLoad) continue
        fout.write("${data.key}: ${data.extractString()}\n".toByteArray(Charsets.UTF_8))
    }

    fout.flush()

    fout.close()
}

fun qoBLoad(file: File){
    if(!file.exists()) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }

    val reader = file.inputStream()

    while(reader.available() > 0){
        val charArray = mutableListOf<Char>()
        do{
            val c = reader.read().toChar()
            if(c != ' ' && c != '\n') charArray.add(c)
        }while(c != '\n')

        val line = String(charArray.toCharArray())
        val dst = line.split(":")
        val key = dst[0]
        val value = dst.slice(1 until dst.size).joinToString(":")

        for (data in datas){
            if(data.key == key){
                if(data.shouldNotLoad) continue
                data.load(value)
            }
        }
    }

    reader.close()
}

abstract class QoBData<T>: Cloneable{
    init{
        datas.add(this)
    }

    protected constructor()

    abstract var data: T
    abstract val key: String
    open val shouldNotLoad: Boolean = false

    abstract fun extractData(value: String): T
    abstract fun extractString(): String
    final fun load(value: String){
        data = extractData(value)
    }

    final fun remove(){
        datas.remove(this)
    }

}

abstract class QoBPlayerData<T>(key: String, val name: String, override var data: T, override val shouldNotLoad: Boolean=false): QoBData<T>(){
    override val key: String = "${name}_$key"
}
abstract class QoBGlobalData<T>(override val key: String, override var data: T, override val shouldNotLoad: Boolean=false): QoBData<T>()

class QoBGlobalIntData(key: String, data: Int, shouldNotLoad: Boolean=false): QoBGlobalData<Int>(key, data, shouldNotLoad){
    override fun extractString(): String = "$data"
    override fun extractData(str: String): Int = str.toInt()
}
class QoBGlobalStringData(key: String, data: String, shouldNotLoad: Boolean=false): QoBGlobalData<String>(key, data, shouldNotLoad){
    override fun extractString(): String = data
    override fun extractData(str: String): String = str
}
class QoBGlobalDoubleData(key: String, data: Double, shouldNotLoad: Boolean=false): QoBGlobalData<Double>(key, data, shouldNotLoad){
    override fun extractString(): String = "$data"
    override fun extractData(str: String): Double = str.toDouble()
}

class QoBPlayerIntData(key: String, name: String, data: Int, shouldNotLoad: Boolean=false): QoBPlayerData<Int>(key, name, data, shouldNotLoad){
    override fun extractData(value: String): Int = value.toInt()
    override fun extractString(): String = "$data"
}
class QoBPlayerDoubleData(key: String, name: String, data: Double, shouldNotLoad: Boolean=false): QoBPlayerData<Double>(key, name, data, shouldNotLoad){
    override fun extractData(value: String): Double = value.toDouble()
    override fun extractString(): String = "$data"
}
class QoBPlayerStringData(key: String, name: String, data: String, shouldNotLoad: Boolean=false): QoBPlayerData<String>(key, name, data, shouldNotLoad){
    override fun extractData(value: String): String = value
    override fun extractString(): String = data
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
            playerData.remove()
            playerDatas.add(data)
        }
    }
    operator fun set(name: String, data: T){
        this[name]?.data = data
    }
    operator fun minusAssign(name: String){
        val playerData = this[name]
        playerDatas.remove(playerData)
        playerData?.remove()
    }
    operator fun plusAssign(data: QoBPlayerData<T>){
        playerDatas.add(data)
    }
    operator fun contains(name: String): Boolean = playerDatas.count { it.name == name } > 0
    operator fun contains(data: QoBPlayerData<T>): Boolean = playerDatas.contains(data)
}