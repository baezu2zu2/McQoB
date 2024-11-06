package com.kbazu.mcQoB

import org.bukkit.Material
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

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

class QoBItemElement(val airType: Material, val airName: String?, val data: QoBData<ItemStack?>, vararg lores: String, lockMeta: ItemMeta?=null): QoBElement(airType, 1, airName, *lores, takeAble=true, lockItemMeta = lockMeta){
    init{
        data.data = this
    }

    var isAir = true
    val resetRunnable = object: BukkitRunnable(){
        override fun run() {
            type=airType
            amount = 1
            val itemMeta = itemMeta
            itemMeta?.setDisplayName(airName)

            this@QoBItemElement.itemMeta = itemMeta
            data.data = null
        }
    }

    fun set(item: ItemStack?){
        if(item != null){
            type = item.type
            amount = item.amount
            itemMeta = item.itemMeta

            item.type = Material.AIR
            item.amount = 0

            data.data = item
        }
    }

    fun reset(plugin: JavaPlugin){
        resetRunnable.runTaskLater(plugin, 1)
    }

    final override fun onClick(event: InventoryClickEvent, pos: QoBPosition) {
        event.isCancelled = true
        when(event.action){
            InventoryAction.CLONE_STACK->{
                if(!isAir) event.isCancelled = false
            }
            InventoryAction.COLLECT_TO_CURSOR->{
                if(!isAir) event.isCancelled = false
            }
            InventoryAction.PICKUP_ALL->{
                if(!isAir) event.isCancelled = false
            }
            InventoryAction.PICKUP_HALF->{
                if(!isAir) event.isCancelled = false
            }
            InventoryAction.SWAP_WITH_CURSOR->{
                event.isCancelled = false
                if(isAir) {
                    set(event.cursor)
                }
            }
            else -> {}
        }
    }
}