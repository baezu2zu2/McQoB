package com.kbazu.mcQoB

import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

val elements = mutableListOf<QoBElement>()

abstract class QoBElement(type: Material, amount: Int, name: String?=null, vararg lores: String, var takeAble: Boolean=false, var lockItemMeta: ItemMeta?=null){

    var itemStack = ItemStack(type, amount)
    var locked = false
        get
        set(value) {
            field = true
            if (lockItemMeta != null) itemStack.itemMeta = lockItemMeta
            field = value
        }

    init{
        var itemMeta = itemStack.itemMeta
        itemMeta?.setDisplayName(name)
        itemMeta?.lore = lores.toMutableList()

        itemStack.itemMeta = itemMeta

        elements.add(this)
    }

    abstract fun onClick(event: InventoryClickEvent, pos: QoBPosition)

    final fun remove(){
        elements.remove(this)
    }
}

abstract class QoBAnywhereElement(type: Material, amount: Int, name: String?=null, vararg lores: String, takeAble: Boolean=false, lockItemMeta: ItemMeta?=null): QoBElement(type, amount, name, *lores, takeAble=takeAble, lockItemMeta=lockItemMeta){
    abstract fun onClickAnywhere(event: InventoryClickEvent, element: QoBElement, pos: QoBPosition)
}

abstract class QoBSettingElement<T>(type: Material, amount: Int, name: String, val leftDTick: Int, val rightDTick: Int, val data: QoBData<T>, vararg lores: String, takeAble: Boolean=false, lockItemMeta: ItemMeta?=null): QoBAnywhereElement(type, amount, name, *lores, takeAble=takeAble, lockItemMeta=lockItemMeta){
    init{
        renewDisplay()
    }

    abstract fun onLeftClick(event: InventoryClickEvent)
    abstract fun onRightClick(event: InventoryClickEvent)
    abstract fun displayStr(): List<String>

    fun renewDisplay(){
        val itemMeta = itemStack.itemMeta
        itemMeta?.lore = displayStr()

        itemStack.itemMeta = itemMeta
    }

    final override fun onClick(event: InventoryClickEvent, pos: QoBPosition) {
        if(event.click != ClickType.DOUBLE_CLICK) {
            if (event.isLeftClick) {
                onLeftClick(event)
            } else if (event.isRightClick) {
                onRightClick(event)
            }
        }
    }

    final override fun onClickAnywhere(event: InventoryClickEvent, element: QoBElement, pos: QoBPosition) {
        renewDisplay()

        pos.page[pos] = this
    }
}

class QoBItemElement(val airType: Material, val airName: String?, val data: QoBData<ItemStack?>, vararg val lores: String, lockMeta: ItemMeta?=null): QoBElement(airType, 1, airName, *lores, takeAble=false, lockItemMeta = lockMeta){
    var isAir = true
    val resetRunnable = object: BukkitRunnable(){
        override fun run() {
            reset()
        }
    }

    init{
        if(data.data == null){
            isAir = true
        }else{
            this.itemStack = data.data!!
            isAir = false
        }
    }

    fun reset(){
        itemStack.type=airType
        itemStack.amount = 1
        val itemMeta = itemStack.itemMeta
        itemMeta?.setDisplayName(airName)
        itemMeta?.lore = lores.toMutableList()

        itemStack.itemMeta = itemMeta
        data.data = null

        isAir = true
    }

    fun set(item: ItemStack?){
        if(isAir) {
            if(item != null){
                itemStack.type = item.type
                itemStack.amount = item.amount
                itemStack.itemMeta = item.itemMeta
                item.type = Material.AIR
                item.amount = 0

                if (itemStack.type == Material.AIR) {
                    isAir = true
                    reset()
                }
                else isAir = false
            }else{
                reset()

                isAir = true
            }
        }else{
            if (item != null) {
                if(itemStack.isSimilar(item) && itemStack.itemMeta != null){
                    if(item.amount+itemStack.amount > item.maxStackSize){
                        val left = item.amount+itemStack.amount-item.maxStackSize
                        itemStack.amount = item.maxStackSize
                        item.amount = left
                    }else{
                        itemStack.amount += item.amount
                        item.amount = 0
                        item.type = Material.AIR
                    }
                }else {
                    val type = item.type
                    val amount = item.amount
                    val itemMeta = item.itemMeta

                    item.type = itemStack.type
                    item.amount = itemStack.amount
                    item.itemMeta = itemStack.itemMeta

                    itemStack.type = type
                    itemStack.amount = amount
                    itemStack.itemMeta = itemMeta

                    if (itemStack.type == Material.AIR) {
                        reset()
                    } else isAir = false
                }
            }else{
                reset()
            }
        }
    }

    fun delayedReset(plugin: JavaPlugin){
        resetRunnable.runTaskLater(plugin, 1)
    }

    override fun onClick(event: InventoryClickEvent, pos: QoBPosition) {
        val cursor = event.cursor
        if(event.isLeftClick){
            if(isAir){
                if(cursor != null && !cursor.type.isAir){
                    set(cursor)
                }
            }else{
                set(cursor)
            }
        }else if(event.isRightClick){
            if(isAir){
                if(cursor != null && !cursor.type.isAir){
                    itemStack.type = cursor.type
                    itemStack.amount = 1
                    itemStack.itemMeta = cursor.itemMeta

                    cursor.amount--

                    isAir = false
                }
            }else{
                if(cursor != null && !cursor.type.isAir){
                    if(cursor.isSimilar(itemStack)){
                        itemStack.amount++
                        cursor.amount--
                    }
                }else if(cursor != null){
                    cursor.type = itemStack.type
                    cursor.amount = itemStack.amount / 2 + itemStack.amount % 2
                    cursor.itemMeta = itemStack.itemMeta

                    itemStack.amount /= 2
                }
            }
        }

        event.currentItem = itemStack
        event.view.cursor = cursor

        if(isAir) data.data = null
        else data.data = itemStack
    }
}