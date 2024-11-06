package com.kbazu.mcQoB

interface QoBData<T>{
    var data: T
}

data class QoBPlayerData<T>(val name: String, override var data: T): QoBData<T>{

}

data class QoBGlobalData<T>(override var data: T): QoBData<T>