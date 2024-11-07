package com.kbazu.mcQoB

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.min

abstract class QoBElement(type: Material, amount: Int, name: String?=null, vararg lores: String, var takeAble: Boolean=false, var lockItemMeta: ItemMeta?=null): ItemStack(type, amount){
    var locked = false
        get
        set(value) {
            field = true
            if (lockItemMeta != null) this.itemMeta = lockItemMeta
            field = value
        }

    init{
        var itemMeta = this.itemMeta
        itemMeta?.setDisplayName(name)
        itemMeta?.lore = lores.toMutableList()

        this.itemMeta = itemMeta

        elements.add(this)
    }

    abstract fun onClick(event: InventoryClickEvent, pos: QoBPosition)
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
        val itemMeta = itemMeta
        itemMeta?.lore = displayStr()

        setItemMeta(itemMeta)
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
    init{
        data.data = null
    }

    var isAir = true
    val resetRunnable = object: BukkitRunnable(){
        override fun run() {
            reset()
        }
    }

    fun reset(){
        type=airType
        amount = 1
        val itemMeta = itemMeta
        itemMeta?.setDisplayName(airName)
        itemMeta?.lore = lores.toMutableList()

        this.itemMeta = itemMeta
        data.data = null

        isAir = true
    }

    fun set(item: ItemStack?){
        if(isAir) {
            if(item != null){
                this.type = item.type
                this.amount = item.amount
                this.itemMeta = item.itemMeta
                item.type = Material.AIR
                item.amount = 0

                if (this.type == Material.AIR) {
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
                if(isSimilar(item) && itemMeta != null){
                    if(item.amount+amount > item.maxStackSize){
                        val left = item.amount+amount-item.maxStackSize
                        amount = item.maxStackSize
                        item.amount = left
                    }else{
                        amount += item.amount
                        item.amount = 0
                        item.type = Material.AIR
                    }
                }else {
                    val type = item.type
                    val amount = item.amount
                    val itemMeta = item.itemMeta

                    item.type = this.type
                    item.amount = this.amount
                    item.itemMeta = this.itemMeta

                    this.type = type
                    this.amount = amount
                    this.itemMeta = itemMeta

                    if (this.type == Material.AIR) {
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
                    this.type = cursor.type
                    this.amount = 1
                    this.itemMeta = cursor.itemMeta

                    cursor.amount--

                    isAir = false
                }
            }else{
                if(cursor != null && !cursor.type.isAir){
                    if(cursor.isSimilar(this)){
                        this.amount++
                        cursor.amount--
                    }
                }else if(cursor != null){
                    cursor.type = type
                    cursor.amount = amount / 2 + amount % 2
                    cursor.itemMeta = itemMeta

                    amount /= 2
                }
            }
        }

        event.currentItem = this
        event.view.cursor = cursor

        if(isAir) data.data = null
        else data.data = this
    }
}