package com.kbazu.mcQoB

import java.io.File

val datas = mutableListOf<QoBData<*>>()

fun save(file: File){
    if(!file.exists()) file.createNewFile()

    val fout = file.outputStream()

    for (data in datas){
        fout.write("${data.serialize()}\n".toByteArray())
    }
}

abstract class QoBData<T>: Cloneable{
    init{
        datas.add(this)
    }

    protected constructor()

    abstract var data: T
    abstract val key: String

    abstract fun serialize(): String
}

abstract class QoBPlayerData<T>(override val key: String, val name: String, override var data: T): QoBData<T>()

abstract class QoBGlobalData<T>(override val key: String, override var data: T): QoBData<T>()

class QoBGlobalIntData(key: String, data: Int): QoBGlobalData<Int>(key, data){
    override fun serialize(): String = "$key: $data"
}

class QoBGlobalStringData(key: String, data: String): QoBGlobalData<String>(key, data){
    override fun serialize(): String = "$key: $data"
}

class QoBGlobalDoubleData(key: String, data: Double): QoBGlobalData<Double>(key, data){
    override fun serialize(): String = "$key: $data"
}