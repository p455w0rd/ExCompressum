package net.blay09.mods.excompressum.handler;

import com.google.common.collect.Lists;
import net.blay09.mods.excompressum.config.ToolsConfig;
import net.blay09.mods.excompressum.item.ICompressedHammer;
import net.blay09.mods.excompressum.item.ItemChickenStick;
import net.blay09.mods.excompressum.item.ModItems;
import net.blay09.mods.excompressum.entity.EntityAngryChicken;
import net.blay09.mods.excompressum.registry.ExRegistro;
import net.blay09.mods.excompressum.registry.chickenstick.ChickenStickRegistry;
import net.blay09.mods.excompressum.registry.compressedhammer.CompressedHammerRegistry;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class ChickenStickHandler {

	@SubscribeEvent
	public void onAttack(AttackEntityEvent event) {
		if(!ToolsConfig.allowChickenStickCreation) {
			return;
		}
		if(event.getTarget() instanceof EntityChicken && !((EntityChicken) event.getTarget()).isChild()) {
			ItemStack heldItem = event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND);
			if(heldItem != null && heldItem.getItem() == Items.STICK) {
				event.getTarget().setDead();
				if(!event.getTarget().worldObj.isRemote) {
					heldItem.stackSize--;
					if(heldItem.stackSize <= 0) {
						event.getEntityPlayer().setHeldItem(EnumHand.MAIN_HAND, null);
					}
					EntityAngryChicken angryChicken = new EntityAngryChicken(event.getTarget().worldObj);
					angryChicken.setLocationAndAngles(event.getTarget().posX, event.getTarget().posY, event.getTarget().posZ, event.getTarget().rotationYaw, event.getTarget().rotationPitch);
					event.getTarget().worldObj.spawnEntityInWorld(angryChicken);
					event.getTarget().worldObj.playSound(angryChicken.posX, angryChicken.posY, angryChicken.posZ, SoundEvents.ENTITY_CHICKEN_HURT, SoundCategory.NEUTRAL, 1f, 0.5f, false);
				}
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onHarvestDrops(BlockEvent.HarvestDropsEvent event) {
		if(event.getHarvester() != null) {
			ItemStack heldItem = event.getHarvester().getHeldItemMainhand();
			if(heldItem != null && heldItem.getItem() instanceof ItemChickenStick && ChickenStickRegistry.isHammerable(event.getState())) {
				final int fortune = 0;
				List<ItemStack> rewards = Lists.newArrayList(CompressedHammerRegistry.rollHammerRewards(event.getState(), fortune, event.getWorld().rand));
				rewards.addAll(ExRegistro.rollHammerRewards(event.getState(), 0, fortune, event.getWorld().rand));
				if(!rewards.isEmpty()) {
					event.setDropChance(1f);
					event.getDrops().clear();
					event.getDrops().addAll(rewards);

					((ItemChickenStick) heldItem.getItem()).playChickenSound(event.getWorld(), event.getPos());
					if (event.getWorld().rand.nextFloat() <= ToolsConfig.chickenStickSpawnChance) {
						EntityChicken entityChicken = new EntityChicken(event.getWorld());
						entityChicken.setPosition(event.getPos().getX() + 0.5, event.getPos().getY() + 0.5, event.getPos().getZ() + 0.5);
						event.getWorld().spawnEntityInWorld(entityChicken);
					}
				}
			}
		}
	}

}
