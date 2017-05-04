package ro.coderdojo.ctf;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

public class LobbyListener implements Listener {

	World lobby;

	public LobbyListener(World lobby) {
		this.lobby = lobby;
	}

	@EventHandler
	public void playerJoined(PlayerJoinEvent event) throws Exception {
		Player player = event.getPlayer();
		player.setGameMode(GameMode.ADVENTURE);
		player.getInventory().clear();
		player.getInventory().addItem(new ItemStack(Material.WOOD_SWORD, 1));
		AttributeInstance healthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		healthAttribute.setBaseValue(20.00);
//		player.teleport(new Location(lobby, 19.589, 231, 21.860));
		player.teleport(new Location(lobby, 29, 231, 10));
		ScoresAndTeams.addNoTeamPlayerLobby(player);

//		if (ScoresAndTeams.lobbyNoTeamPlayers.size() == 1) {
//			new BukkitRunnable() {
//				//EnumParticle.PORTAL // mov
//				//EnumParticle.REDSTONE // toaate culorile
//				EnumParticle particle = EnumParticle.VILLAGER_HAPPY;
//
//				@Override
//				public void run() {
//					//params: particula,dacă e enable, locația, offset-ul, viteza, numar particule
//					PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particle, true, player.getLocation().getBlockX(), player.getLocation().getBlockY() + 2, player.getLocation().getBlockZ(), 1, 5, 1, 1, 30);
//					for (Player online : Bukkit.getOnlinePlayers()) {
//						((CraftPlayer) online).getHandle().playerConnection.sendPacket(packet);
//					}
//				}
//			}.runTaskTimer(CaptureTheFlagPlugin.plugin, 0, 1);
//		}

		// b is the Bukkit block that has changed
		Block b = player.getLocation().getBlock();
		net.minecraft.server.v1_11_R1.Chunk c = ((org.bukkit.craftbukkit.v1_11_R1.CraftChunk) b.getChunk()).getHandle();
		c.initLighting();
//		((org.bukkit.craftbukkit.v1_11_R1.CraftWorld) player.getWorld()).getHandle().notify();

	}

	@EventHandler
	public void playerLeave(PlayerQuitEvent event) throws Exception {
		ScoresAndTeams.playerLeave(event.getPlayer());
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		if (!ScoresAndTeams.isInLobby(event.getPlayer())) {
			return;
		}
		event.setRespawnLocation(new Location(lobby, 19.589, 231, 21.860));
	}

	@EventHandler
	public void onKill(PlayerDeathEvent event) {
		Player killed = event.getEntity();
		Player killer = event.getEntity().getKiller();

		if (killer == null) {
			return;
		}
		if (ScoresAndTeams.isRed(killer)) {
			ScoresAndTeams.redKills++;
		}

		if (ScoresAndTeams.isBlue(killer)) {
			ScoresAndTeams.blueKills++;
		}

		ScoresAndTeams.refreshLobbyBoards(event.getEntity());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockBreak(BlockBreakEvent event) {
		event.getPlayer().sendMessage(ChatColor.YELLOW + " Nu poti sparge " + ChatColor.RED + "arena!");
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!ScoresAndTeams.isInLobby(event.getPlayer())) {
			return;
		}
		Block walkingBlock = event.getTo().getBlock().getRelative(BlockFace.DOWN);
		if (walkingBlock.getType() == Material.WOOL) {
			Wool wool = (Wool) walkingBlock.getState().getData();
			if (wool.getColor() == DyeColor.RED) {
				ScoresAndTeams.addRedToLobby(event.getPlayer());
			}
			if (wool.getColor() == DyeColor.BLUE) {
				ScoresAndTeams.addBlueToLobby(event.getPlayer());
			}
		}

//		System.out.println();
//		System.out.println("*************** MOVE ********************");
//		
//		Block from = event.getFrom().getBlock().getRelative(BlockFace.UP);
//		Block to = event.getTo().getBlock().getRelative(BlockFace.UP);
//		
//		System.out.println("---------------------------------");
//		System.out.println("from up " + from.getType());
//		System.out.println("to up " + to.getType());
//		
//		Block from2 = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
//		Block to2 = event.getTo().getBlock().getRelative(BlockFace.DOWN);
//		
//		System.out.println("---------------------------------");
//		System.out.println("from down " + from2.getType());
//		System.out.println("to down " + to2.getType());
//		
//		Block from3 = event.getFrom().getBlock();
//		Block to3 = event.getTo().getBlock();
//		
//		System.out.println("---------------------------------");
//		System.out.println("from --- " + from3.getType());
//		System.out.println("to --- " + to3.getType());
//EnumParticl
//
//		!!!
//PacketPlayOutWorldParticles
//		!!!!
		Block from = event.getFrom().getBlock().getRelative(BlockFace.DOWN);
		Block to = event.getTo().getBlock().getRelative(BlockFace.DOWN);

		boolean movedToAnotherBlock = true;
		if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
			movedToAnotherBlock = false;
		}

		if (movedToAnotherBlock && to.getType() != Material.AIR) { //&& !(to.getType() != Material.AIR || to.getType() != Material.WATER)
			//restore last bloack
			if (lastBlock != null) {
				lastBlock.update(true);
			}
			//save current block
			lastBlock = walkingBlock.getState();
			//replace current wuth glowstone
			to.setType(Material.BEACON);
		}
	}

	static BlockState lastBlock;

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (!ScoresAndTeams.isInLobby(event.getPlayer())) {
			return;
		}
		Player player = event.getPlayer();
		Action action = event.getAction();
		if (event.getClickedBlock() == null) {
			return;
		}
		Material material = event.getClickedBlock().getState().getType();
		Location location = event.getClickedBlock().getState().getLocation();
		if (action == Action.RIGHT_CLICK_BLOCK && material == Material.STONE_BUTTON) {
			if (ScoresAndTeams.isMatchStarted) {
				player.sendMessage("Meciul este deja pornit! Asteapta sa se termine!");
				return;
			}
			Location b1 = new Location(lobby, 21.0, 233.0, 21.0, location.getYaw(), location.getPitch());
			Location b2 = new Location(lobby, 19.0, 233.0, 19.0, location.getYaw(), location.getPitch());
			Location b3 = new Location(lobby, 17.0, 233.0, 21.0, location.getYaw(), location.getPitch());
			Location b4 = new Location(lobby, 19.0, 233.0, 23.0, location.getYaw(), location.getPitch());
			if (location.equals(b1) || location.equals(b2) || location.equals(b3) || location.equals(b4)) {
				timer();
				player.sendMessage("Ai pornit meciul!");
				CaptureTheFlagPlugin.plugin.getServer().broadcastMessage("Meciul a fost pornit!");
			}
		}
	}

	private void timer() {
		CountDownTimer timer = new CountDownTimer(this);
		timer.runTaskTimer(CaptureTheFlagPlugin.plugin, 3, 1);
	}
}
