package net.mmogroup.mmolib.version.nms;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_13_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_13_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_13_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.BoundingBox;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import net.minecraft.server.v1_13_R1.BlockPosition;
import net.minecraft.server.v1_13_R1.Blocks;
import net.minecraft.server.v1_13_R1.ChatMessage;
import net.minecraft.server.v1_13_R1.ChatMessageType;
import net.minecraft.server.v1_13_R1.Container;
import net.minecraft.server.v1_13_R1.ContainerAnvil;
import net.minecraft.server.v1_13_R1.EntityHuman;
import net.minecraft.server.v1_13_R1.EntityPlayer;
import net.minecraft.server.v1_13_R1.EnumHand;
import net.minecraft.server.v1_13_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_13_R1.ItemStack;
import net.minecraft.server.v1_13_R1.MinecraftKey;
import net.minecraft.server.v1_13_R1.NBTTagCompound;
import net.minecraft.server.v1_13_R1.PacketPlayInArmAnimation;
import net.minecraft.server.v1_13_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_13_R1.PacketPlayOutChat;
import net.minecraft.server.v1_13_R1.PacketPlayOutCloseWindow;
import net.minecraft.server.v1_13_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_13_R1.PacketPlayOutTitle.EnumTitleAction;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.minecraft.server.v1_13_R1.PlayerConnection;
import net.minecraft.server.v1_13_R1.SoundEffect;
import net.minecraft.server.v1_13_R1.SoundEffectType;
import net.minecraft.server.v1_13_R1.TileEntitySkull;
import net.minecraft.server.v1_13_R1.World;

public class NMSHandler_1_13_R1 implements NMSHandler {
	@Override
	public void sendJson(Player player, String message) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a(message)));
	}

	@Override
	public void sendTitle(Player player, String msgTitle, String msgSubTitle, int fadeIn, int ticks, int fadeOut) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TITLE, ChatSerializer.a("{\"text\": \"" + msgTitle + "\"}")));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, ChatSerializer.a("{\"text\": \"" + msgSubTitle + "\"}")));
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutTitle(EnumTitleAction.TIMES, null, fadeIn, ticks, fadeOut));
	}

	@Override
	public void sendActionBar(Player player, String message) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutChat(ChatSerializer.a("{\"text\": \"" + message + "\"}"), ChatMessageType.GAME_INFO));
	}

	@Override
	public int getNextContainerId(Player player) {
		return ((CraftPlayer) player).getHandle().nextContainerCounter();
	}

	@Override
	public void handleInventoryCloseEvent(Player player) {
		CraftEventFactory.handleInventoryCloseEvent(((CraftPlayer) player).getHandle());
	}

	@Override
	public void sendPacketOpenWindow(Player player, int containerId) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerId, "minecraft:anvil", new ChatMessage(Blocks.ANVIL.a() + ".name")));
	}

	@Override
	public void sendPacketCloseWindow(Player player, int containerId) {
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutCloseWindow(containerId));
	}

	@Override
	public void setActiveContainerDefault(Player player) {
		((CraftPlayer) player).getHandle().activeContainer = ((CraftPlayer) player).getHandle().defaultContainer;
	}

	@Override
	public void setActiveContainer(Player player, Object container) {
		((CraftPlayer) player).getHandle().activeContainer = (Container) container;
	}

	@Override
	public void setActiveContainerId(Object container, int containerId) {
		((Container) container).windowId = containerId;
	}

	@Override
	public void addActiveContainerSlotListener(Object container, Player player) {
		((Container) container).addSlotListener(((CraftPlayer) player).getHandle());
	}

	@Override
	public Inventory toBukkitInventory(Object container) {
		return ((Container) container).getBukkitView().getTopInventory();
	}

	@Override
	public Object newContainerAnvil(Player player) {
		return new AnvilContainer(((CraftPlayer) player).getHandle());
	}

	private class AnvilContainer extends ContainerAnvil {
		public AnvilContainer(EntityHuman entityhuman) {
			super(entityhuman.inventory, entityhuman.world, new BlockPosition(0, 0, 0), entityhuman);
			this.checkReachable = false;
		}
	}

	@Override
	public NBTItem getNBTItem(org.bukkit.inventory.ItemStack item) {
		return new NBTItem_v1_13_R1(item);
	}

	public class NBTItem_v1_13_R1 extends NBTItem {
		private final ItemStack nms;
		private final NBTTagCompound compound;

		public NBTItem_v1_13_R1(org.bukkit.inventory.ItemStack item) {
			super(item);

			nms = CraftItemStack.asNMSCopy(item);
			compound = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
		}

		@Override
		public String getString(String path) {
			return compound.getString(path);
		}

		@Override
		public boolean hasTag(String path) {
			return compound.hasKey(path);
		}

		@Override
		public boolean getBoolean(String path) {
			return compound.getBoolean(path);
		}

		@Override
		public double getDouble(String path) {
			return compound.getDouble(path);
		}

		@Override
		public int getInteger(String path) {
			return compound.getInt(path);
		}

		@Override
		public NBTItem addTag(List<ItemTag> tags) {
			tags.forEach(tag -> {
				if (tag.getValue() instanceof Boolean)
					compound.setBoolean(tag.getPath(), (boolean) tag.getValue());
				else if (tag.getValue() instanceof Double)
					compound.setDouble(tag.getPath(), (double) tag.getValue());
				else if (tag.getValue() instanceof String)
					compound.setString(tag.getPath(), (String) tag.getValue());
				else if (tag.getValue() instanceof Integer)
					compound.setInt(tag.getPath(), (int) tag.getValue());
			});
			return this;
		}

		@Override
		public NBTItem removeTag(String... paths) {
			for (String path : paths)
				compound.remove(path);
			return this;
		}

		@Override
		public Set<String> getTags() {
			return compound.getKeys();
		}

		@Override
		public org.bukkit.inventory.ItemStack toItem() {
			nms.setTag(compound);
			return CraftItemStack.asBukkitCopy(nms);
		}
	}

	@Override
	public boolean isInBoundingBox(Entity entity, Location loc) {
		return entity.getBoundingBox().expand(.2, .2, .2, .2, .2, .2).contains(loc.toVector());
	}

	@Override
	public double distanceSquaredFromBoundingBox(Entity entity, Location loc) {
		BoundingBox box = entity.getBoundingBox().expand(.2, .2, .2, .2, .2, .2);

		double dx = loc.getX() > box.getMinX() && loc.getX() < box.getMaxX() ? 0 : Math.min(Math.abs(box.getMinX() - loc.getX()), Math.abs(box.getMaxX() - loc.getX()));
		double dy = loc.getY() > box.getMinY() && loc.getY() < box.getMaxY() ? 0 : Math.min(Math.abs(box.getMinY() - loc.getY()), Math.abs(box.getMaxY() - loc.getY()));
		double dz = loc.getZ() > box.getMinZ() && loc.getZ() < box.getMaxZ() ? 0 : Math.min(Math.abs(box.getMinZ() - loc.getZ()), Math.abs(box.getMaxZ() - loc.getZ()));

		return dx * dx + dx * dy + dz * dz;
	}

	@Override
	public void playArmAnimation(Player player) {
		EntityPlayer p = ((CraftPlayer) player).getHandle();
		PlayerConnection connection = p.playerConnection;
		PacketPlayOutAnimation armSwing = new PacketPlayOutAnimation(p, 0);
		connection.sendPacket(armSwing);
		connection.a(new PacketPlayInArmAnimation(EnumHand.MAIN_HAND));
	}

	@Override
	public Sound getBlockPlaceSound(org.bukkit.block.Block block) {
		try {
			World nmsWorld = ((CraftWorld) block.getWorld()).getHandle();

			net.minecraft.server.v1_13_R1.Block nmsBlock = nmsWorld.getType(new BlockPosition(block.getX(), block.getY(), block.getZ())).getBlock();
			SoundEffectType soundEffectType = nmsBlock.getStepSound();

			Field breakSound = SoundEffectType.class.getDeclaredField("q");
			breakSound.setAccessible(true);
			SoundEffect nmsSound = (SoundEffect) breakSound.get(soundEffectType);

			Field keyField = SoundEffect.class.getDeclaredField("b");
			keyField.setAccessible(true);
			MinecraftKey nmsString = (MinecraftKey) keyField.get(nmsSound);

			return Sound.valueOf(nmsString.getKey().replace(".", "_").toUpperCase());
		} catch (IllegalAccessException | NoSuchFieldException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	@Override
	public String getSkullValue(Block block) {
		TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
		if (skullTile.getGameProfile() == null)
			return "";
		return skullTile.getGameProfile().getProperties().get("textures").iterator().next().getValue();
	}

	@Override
	public void setSkullValue(Block block, String value) {
		TileEntitySkull skullTile = (TileEntitySkull) ((CraftWorld) block.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(), block.getY(), block.getZ()));
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		profile.getProperties().put("textures", new Property("textures", value));
		skullTile.setGameProfile(profile);
		skullTile.update();
	}
}
