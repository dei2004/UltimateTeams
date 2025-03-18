package dev.xf3d3.ultimateteams.commands.subCommands;

import dev.xf3d3.ultimateteams.UltimateTeams;
import dev.xf3d3.ultimateteams.models.Team;
import dev.xf3d3.ultimateteams.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TeamPrefixSubCommand {

    private final int MIN_CHAR_LIMIT;
    private final int MAX_CHAR_LIMIT;
    private final FileConfiguration messagesConfig;
    private final Set<Map.Entry<UUID, Team>> teams;
    private final ArrayList<String> teamsPrefixList = new ArrayList<>();

    private final UltimateTeams plugin;

    public TeamPrefixSubCommand(@NotNull UltimateTeams plugin) {
        this.plugin = plugin;
        this.teams = plugin.getTeamStorageUtil().getTeams();
        this.messagesConfig = plugin.msgFileManager.getMessagesConfig();
        this.MAX_CHAR_LIMIT = plugin.getSettings().getTeamTagsMaxCharLimit();
        this.MIN_CHAR_LIMIT = plugin.getSettings().getTeamTagsMinCharLimit();
    }

    public void teamPrefixSubCommand(CommandSender sender, String prefix, List<String> bannedTags) {
        if (!(sender instanceof final Player player)) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("player-only-command")));
            return;
        }

        teams.forEach((teams) -> teamsPrefixList.add(teams.getValue().getTeamPrefix()));

        if (bannedTags.contains(prefix)) {
            player.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-is-banned").replace("%TEAMPREFIX%", prefix)));
            return;
        }

        if (teamsPrefixList.contains(prefix)) {
            player.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-already-taken").replace("%TEAMPREFIX%", prefix)));
            return;
        }

        if (!plugin.getTeamStorageUtil().isTeamOwner(player)) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("must-be-owner-to-change-prefix")));
            teamsPrefixList.clear();

            return;
        }

        final String prefixWithoutColorCodes = prefix.replaceAll("&[0-9a-fA-Fk-oK-OrR]", "");
        if (plugin.getSettings().isIgnoreColorCodes())
            prefix = prefixWithoutColorCodes;

        if (prefix.length() >= MIN_CHAR_LIMIT && prefix.length() <= MAX_CHAR_LIMIT) {
            Team playerTeam = plugin.getTeamStorageUtil().findTeamByOwner(player);
            plugin.getTeamStorageUtil().updatePrefix(player, prefix);

            String prefixConfirmation = Utils.Color(messagesConfig.getString("team-prefix-change-successful")).replace("%TEAMPREFIX%", playerTeam.getTeamPrefix());
            sender.sendMessage(prefixConfirmation);

        } else if (prefix.length() > MAX_CHAR_LIMIT) {
            sender.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-too-long").replace("%CHARMAX%", String.valueOf(MAX_CHAR_LIMIT))));
        } else {
            sender.sendMessage(Utils.Color(messagesConfig.getString("team-prefix-too-short").replace("%CHARMIN%", String.valueOf(MIN_CHAR_LIMIT))));
        }

        teamsPrefixList.clear();
    }
}
