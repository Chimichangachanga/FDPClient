/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.ClickWindowEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraft.network.play.client.C16PacketClientStatus
import java.util.*

object InventoryUtils : MinecraftInstance(), Listenable {
    val CLICK_TIMER = MSTimer()
    val INV_TIMER = MSTimer()
    val BLOCK_BLACKLIST = Arrays.asList(Blocks.enchanting_table, Blocks.chest, Blocks.ender_chest, Blocks.trapped_chest,
        Blocks.anvil, Blocks.sand, Blocks.web, Blocks.torch, Blocks.crafting_table, Blocks.furnace, Blocks.waterlily,
        Blocks.dispenser, Blocks.stone_pressure_plate, Blocks.wooden_pressure_plate, Blocks.red_flower, Blocks.flower_pot, Blocks.yellow_flower,
        Blocks.noteblock, Blocks.dropper, Blocks.standing_banner, Blocks.wall_banner)

    fun findItem(startSlot: Int, endSlot: Int, item: Item): Int {
        for (i in startSlot until endSlot) {
            val stack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (stack != null && stack.item === item)
                return i
        }
        return -1
    }

    fun hasSpaceHotbar(): Boolean {
        for (i in 36..44) {
            mc.thePlayer.inventoryContainer.getSlot(i).stack ?: return true
        }
        return false
    }

    fun findAutoBlockBlock(): Int {
        for (i in 36..44) {
            val itemStack = mc.thePlayer.inventoryContainer.getSlot(i).stack
            if (itemStack != null && itemStack.item is ItemBlock) {
                val itemBlock = itemStack.item as ItemBlock
                val block = itemBlock.getBlock()
                if (canPlaceBlock(block))
                    return i
            }
        }
        return -1
    }

    fun canPlaceBlock(block: Block): Boolean {
        return block.isFullCube && !BLOCK_BLACKLIST.contains(block)
    }

    fun isBlockListBlock(itemBlock: ItemBlock): Boolean {
        val block = itemBlock.getBlock()
        return BLOCK_BLACKLIST.contains(block) || !block.isFullCube
    }

    @EventTarget
    fun onClick(event: ClickWindowEvent) {
        CLICK_TIMER.reset()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C0EPacketClickWindow || packet is C08PacketPlayerBlockPlacement)
            INV_TIMER.reset()
        if (packet is C08PacketPlayerBlockPlacement)
            CLICK_TIMER.reset()
    }

    fun openPacket(){
        mc.netHandler.addToSendQueue(C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT))
    }

    fun closePacket(){
        mc.netHandler.addToSendQueue(C0DPacketCloseWindow())
    }

    override fun handleEvents() = true
}