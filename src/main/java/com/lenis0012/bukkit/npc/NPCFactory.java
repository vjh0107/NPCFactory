package com.lenis0012.bukkit.npc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R4.CraftServer;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_7_R4.metadata.PlayerMetadataStore;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * NPCFactory main class, intializes and creates npcs.
 * 
 * @author lenis0012
 */
public class NPCFactory implements Listener {
	private final Plugin plugin;
	private final NPCNetworkManager networkManager;
	
	public NPCFactory(Plugin plugin) {
		this.plugin = plugin;
		this.networkManager = new NPCNetworkManager();
          
                // See #19
                CraftServer server = (CraftServer) Bukkit.getServer();
                try {
                  Field field = server.getClass().getDeclaredField("playerMetadata");
                  field.setAccessible(true);
                  PlayerMetadataStore metadata = (PlayerMetadataStore) field.get(server);
                  if(!(metadata instanceof NPCMetadataStore)) {
                    field.set(server, new NPCMetadataStore());
                  }
                } catch (NoSuchFieldException e) {
                  e.printStackTrace();
                } catch (IllegalAccessException e) {
                  e.printStackTrace();
                }
          
		Bukkit.getPluginManager().registerEvents(this, plugin);
        }
	
	/**
	 * Spawn a new npc at a speciafied location.
	 * 
	 * @param location Location to spawn npc at.
	 * @param profile NPCProfile to use for npc
	 * @return New npc instance.
	 */
	public NPC spawnHumanNPC(Location location, NPCProfile profile) {
		World world = location.getWorld();
		NPCEntity entity = new NPCEntity(world, location, profile, networkManager);
		entity.getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(plugin, true));
		return entity;
	}
	
	/**
	 * Get npc from entity.
	 * 
	 * @param entity Entity to get npc from.
	 * @return NPC instance, null if entity is not an npc.
	 */
	public NPC getNPC(Entity entity) {
		if(!isNPC(entity)) {
			return null;
		}
		
		try {
			return (NPCEntity) ((CraftEntity) entity).getHandle();
		} catch(Exception ignored) {
			return null;
		}
	}
	
	/**
	 * Get all npc's from all worlds.
	 * 
	 * @return A list of all npc's
	 */
	public List<NPC> getNPCs() {
		List<NPC> npcList = new ArrayList<NPC>();
		for (World world : Bukkit.getWorlds()) {
			npcList.addAll(getNPCs(world));
		}
		
		return npcList;
	}
	
	/**
	 * Get all npc's from a specific world
	 * 
	 * @param world World to get npc's from
	 * @return A list of all npc's in the world
	 */
	public List<NPC> getNPCs(World world) {
		List<NPC> npcList = new ArrayList<NPC>();
		for (Entity entity : world.getEntities()) {
			if (isNPC(entity)) {
				npcList.add(getNPC(entity));
			}
		}
		
		return npcList;
	}
	
	/**
	 * Check if an entity is a NPC.
	 * 
	 * @param entity Entity to check.
	 * @return Entity is a npc?
	 */
	public boolean isNPC(Entity entity) {
		return entity.hasMetadata("NPC");
	}
	
	/**
	 * Despawn all npc's on all worlds.
	 */
	public void despawnAll() {
		for(World world : Bukkit.getWorlds()) {
			despawnAll(world);
		}
	}
	
	/**
	 * Despawn all npc's on a single world.
	 * 
	 * @param world World to despawn npc's on.
	 */
	public void despawnAll(World world) {
		for(Entity entity : world.getEntities()) {
			if(entity.hasMetadata("NPC")) {
				entity.remove();
			}
		}
	}
	
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if(event.getPlugin().equals(plugin)) {
			despawnAll();
		}
	}
}
