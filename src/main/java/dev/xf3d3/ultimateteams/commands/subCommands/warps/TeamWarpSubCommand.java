package dev.xf3d3.ultimateteams.commands.subCommands.warps;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.models.Team;
import dev.xf3d3.ultimateteams.models.TeamWarp;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeamWarpSubCommand {
    private final FileConfiguration messagesConfig;
    private final UltimateTeams plugin;
    private static final ConcurrentHashMap<UUID, Long> warpCoolDownTimer = new ConcurrentHashMap<>();
    private static final String TIME_LEFT = "%TIMELEFT%";

    public TeamWarpSubCommand(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
        this.messagesConfig = plugin.msgFileManager.getMessagesConfig();
    }

    public void WarpCommand(CommandSender sender, String name) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("player-only-command")));
            return;
        }

        if (!plugin.getSettings().teamWarpEnabled()) {
            player.sendMessage(Utils.Color(messagesConfig.getString("function-disabled")));
            return;
        }

        Team team;
        if (plugin.getTeamStorageUtil().findTeamByOwner(player) != null) {
            team = plugin.getTeamStorageUtil().findTeamByOwner(player);
        } else {
            team = plugin.getTeamStorageUtil().findTeamByPlayer(player);
        }

        if (team == null) {
            player.sendMessage(Utils.Color(messagesConfig.getString("failed-not-in-team")));
            return;
        }

        UUID uuid = player.getUniqueId();

        if (plugin.getSettings().teamWarpCooldownEnabled()) {
            if (!player.hasPermission("ultimateteams.bypass.warpcooldown") && warpCoolDownTimer.containsKey(uuid)) {
                if (warpCoolDownTimer.get(uuid) > System.currentTimeMillis()) {
                    long timeLeft = (warpCoolDownTimer.get(uuid) - System.currentTimeMillis()) / 1000;

                    player.sendMessage(Utils.Color(messagesConfig.getString("home-cool-down-timer-wait")
                            .replaceAll(TIME_LEFT, Long.toString(timeLeft))));
                } else {
                    warpCoolDownTimer.put(uuid, System.currentTimeMillis() + (plugin.getSettings().getTeamHomeCooldownValue() * 1000L));
                    tpWarp(player, team, name);
                }
            } else {
                tpWarp(player, team, name);
                warpCoolDownTimer.put(uuid, System.currentTimeMillis() + (plugin.getSettings().getTeamHomeCooldownValue() * 1000L));
            }
        } else {
            tpWarp(player, team, name);

            player.sendMessage(Utils.Color(messagesConfig.getString("successfully-teleported-to-home")));
        }

    }

    private void tpWarp(Player player, Team team, String name) {
        final TeamWarp warp = team.getTeamWarp(name);

        if (warp == null) {
            player.sendMessage(Utils.Color(messagesConfig.getString("team-warp-not-found")));
            return;
        }

        plugin.getUtils().teleportPlayer(player, warp.getLocation(), Utils.TeleportType.WARP, name);
        player.sendMessage(Utils.Color(messagesConfig.getString("team-warp-teleported-successful").replaceAll("%WARP_NAME%", warp.getName())));
    }
}
