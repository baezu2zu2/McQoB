package com.kbazu.mcQoB

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.java.JavaPlugin
import kotlin.math.min

val pages = mutableListOf<QoBPage>()

fun printPage(sender: CommandSender, page: QoBPage){
    sender.sendMessage("$titleColor[${page.title} 페이지]")
    sender.sendMessage("${helpColor}요소 개수: ${page.elements.size}")
    sender.sendMessage("${helpColor}줄 개수: ${page.inventory.size/9}")
}

abstract class QoBPage(val plugin: JavaPlugin, val title: String, line: Int, var cannotClose: Boolean=false): Listener{
    val inventory: Inventory = Bukkit.createInventory(null, min(line, 6)*9, title)
    val elements: MutableMap<QoBPosition, QoBElement> = mutableMapOf()
    val hash: Int
    init {
        Bukkit.getPluginManager().registerEvents(this, plugin)

        hash = pages.size
        pages.add(this)
    }

    fun position(element: QoBElement): QoBPosition?{
        for (pair in elements){
            if(pair.value == element){
                return pair.key
            }
        }

        return null
    }

    operator fun set(pos: QoBPosition, element: QoBElement) {
        if(pos in this) {
            elements.replace(pos, element)
        } else {
            elements.put(pos, element)
        }

        inventory.setItem(pos.idx, element.itemStack)
    }

    operator fun set(idx: Int, element: QoBElement) =
        run { this[QoBPosition(idx, this)] = element }
    operator fun set(x: Int, y: Int, element: QoBElement) =
        run { this[QoBPosition(x+y*9, this)] = element }

    operator fun get(idx: Int): QoBElement? = this[QoBPosition(idx, this)]
    operator fun get(pos: QoBPosition): QoBElement? = elements[pos]
    operator fun get(x: Int, y: Int): QoBElement? = this[QoBPosition(x+y*9, this)]

    operator fun contains(element: QoBElement): Boolean = this.position(element) != null
    operator fun contains(pos: QoBPosition): Boolean = elements.containsKey(pos)

    operator fun invoke(humanEntity: HumanEntity) {
        humanEntity.openInventory(inventory)
        onOpened(humanEntity)
    }
    operator fun minusAssign(pos: QoBPosition): Unit = run { elements.remove(pos) }
    operator fun minusAssign(element: QoBElement) {
        val pos = position(element)
        if(pos != null) this -= pos
    }
    operator fun minusAssign(idx: Int) = run { this -= QoBPosition(idx, this) }

    @EventHandler
    fun onClicked(event: InventoryClickEvent){
        if(event.clickedInventory == inventory) {
            val element = this[event.slot]
            if (element != null) {
                if (!element.takeAble) event.isCancelled = true
                element.onClick(event, QoBPosition(event.slot, this))

                for (anyPair in elements) {
                    val anyElement = anyPair.value
                    if (anyElement is QoBAnywhereElement) {
                        anyElement.onClickAnywhere(event, element, anyPair.key)
                    }
                }
            }
        }
    }

    @EventHandler
    protected open fun onClosedInternal(event: InventoryCloseEvent) {
        if(event.view.topInventory == inventory || event.view.bottomInventory == inventory) {
            if (cannotClose) {
                event.player.openInventory(inventory)
                return
            }

            onClosed(event)
        }
    }

    final fun remove(){
        pages.remove(this)
    }

    abstract fun onClosed(event: InventoryCloseEvent)
    abstract fun onOpened(player: HumanEntity)
}

abstract class QoBCloseAskPage(plugin: JavaPlugin, title: String, line: Int, cannotClose: Boolean=false): QoBPage(plugin, title, line, cannotClose){
    abstract fun onAskClose(event: InventoryCloseEvent): Boolean

    override fun onClosedInternal(event: InventoryCloseEvent) {
        cannotClose = !onAskClose(event)

        super.onClosedInternal(event)
    }
}

data class QoBPosition(var idx: Int, var page: QoBPage){
    constructor(x: Int, y: Int, page: QoBPage): this(y*9+x, page)

    fun getX(): Int = idx%9
    fun getY(): Int = idx/9

    override fun equals(other: Any?): Boolean {
        return if(other is QoBPosition) other.idx == idx && other.page == page else super.equals(other)
    }

    override fun hashCode(): Int {
        var result = idx
        result = 31 * result + page.hashCode()
        return result
    }
}