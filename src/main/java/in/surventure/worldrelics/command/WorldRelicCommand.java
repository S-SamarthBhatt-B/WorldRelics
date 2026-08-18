package in.surventure.worldrelics.command;

import in.surventure.worldrelics.WorldRelicsPlugin;
import in.surventure.worldrelics.model.ActiveRelic;
import in.surventure.worldrelics.model.RelicDefinition;
import in.surventure.worldrelics.model.RelicState;
import in.surventure.worldrelics.util.LocationUtils;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class WorldRelicCommand implements CommandExecutor {

    private final WorldRelicsPlugin plugin;

    public WorldRelicCommand(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            handleStatus(sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "locate" -> handleLocate(sender);
            case "info" -> handleInfo(sender);
            case "list" -> handleList(sender);
            case "menu" -> handleMenu(sender);
            case "top", "leaderboard" -> handleTop(sender);
            case "duel" -> handleDuel(sender, args);
            case "spawn" -> handleSpawn(sender, args);
            case "despawn" -> handleDespawn(sender);
            case "reset" -> handleReset(sender, args);
            case "give" -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            default -> sender.sendMessage(plugin.getMessageManager().getComponent("prefix").append(
                    net.kyori.adventure.text.Component.text("Unknown subcommand. Use /wr [status|locate|info|list|menu|top|duel|spawn|despawn|reset|give|reload]", net.kyori.adventure.text.format.NamedTextColor.RED)
            ));
        }

        return true;
    }

    private void handleStatus(CommandSender sender) {
        if (!sender.hasPermission("worldrelics.command.status")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }

        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() == RelicState.NO_RELIC || relic.getStatus() == RelicState.EXPIRED) {
            plugin.getMessageManager().sendMessage(sender, "relic-locate-no-relic");
            return;
        }

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
        String name = def != null ? def.getDisplayName() : relic.getRelicTypeId();
        String owner = relic.getOwnerName() != null ? relic.getOwnerName() : "Unclaimed";
        String time = plugin.getRelicManager().getDisplayManager().formatRemainingTime(relic.getRemainingMillis());

        plugin.getMessageManager().sendMessage(sender, "relic-status",
                Placeholder.parsed("relic_name", name),
                Placeholder.parsed("rarity", relic.getRarity().getDisplayName()),
                Placeholder.unparsed("owner_name", owner),
                Placeholder.unparsed("status", relic.getStatus().name()),
                Placeholder.unparsed("time_remaining", time)
        );
    }

    private void handleLocate(CommandSender sender) {
        if (!sender.hasPermission("worldrelics.command.locate")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }

        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null || relic.getStatus() == RelicState.NO_RELIC || relic.getStatus() == RelicState.EXPIRED) {
            plugin.getMessageManager().sendMessage(sender, "relic-locate-no-relic");
            return;
        }

        if (relic.getOwnerUuid() != null) {
            plugin.getMessageManager().sendMessage(sender, "relic-locate-claimed",
                    Placeholder.unparsed("owner_name", relic.getOwnerName() != null ? relic.getOwnerName() : "a player")
            );
            return;
        }

        Location relicLoc = relic.getLocation();
        if (relicLoc == null) {
            plugin.getMessageManager().sendMessage(sender, "relic-locate-no-relic");
            return;
        }

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(relic.getRelicTypeId());
        String relicName = def != null ? def.getDisplayName() : relic.getRelicTypeId();

        Location from = (sender instanceof Player p) ? p.getLocation() : relicLoc;
        double dist = from.distance(relicLoc);
        String direction = LocationUtils.getFuzzyDirectionString(from, relicLoc);
        String formattedDist = String.format("%,d", (int) dist);

        plugin.getMessageManager().sendMessage(sender, "relic-locate-unclaimed",
                Placeholder.parsed("relic_name", relicName),
                Placeholder.unparsed("x", String.valueOf(relicLoc.getBlockX())),
                Placeholder.unparsed("y", String.valueOf(relicLoc.getBlockY())),
                Placeholder.unparsed("z", String.valueOf(relicLoc.getBlockZ())),
                Placeholder.unparsed("world", relicLoc.getWorld().getName()),
                Placeholder.unparsed("direction", direction),
                Placeholder.unparsed("distance", formattedDist)
        );
    }

    private void handleInfo(CommandSender sender) {
        if (!sender.hasPermission("worldrelics.command.info")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }
        handleStatus(sender);
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(plugin.getMessageManager().getComponent("prefix").append(
                net.kyori.adventure.text.Component.text("Available Relic Definitions:", net.kyori.adventure.text.format.NamedTextColor.GOLD)
        ));
        for (RelicDefinition def : plugin.getConfigManager().getRelicDefinitions().values()) {
            sender.sendMessage(plugin.getMessageManager().getMiniMessage().deserialize(
                    "  <gray>• </gray>" + def.getDisplayName() + " <dark_gray>(" + def.getRarity() + ")</dark_gray>"
            ));
        }
    }

    private void handleMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by players.");
            return;
        }
        if (!player.hasPermission("worldrelics.command.menu")) {
            plugin.getMessageManager().sendMessage(player, "no-permission");
            return;
        }
        plugin.getMenuGUI().openMenu(player);
    }

    private void handleTop(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by players.");
            return;
        }
        plugin.getLeaderboardGUI().openGUI(player);
    }

    private void handleDuel(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be executed by players.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage("Usage: /wr duel <player|accept>");
            return;
        }

        if (args[1].equalsIgnoreCase("accept")) {
            plugin.getRelicManager().getDuelManager().acceptDuelRequest(player);
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("Player not found.");
            return;
        }

        plugin.getRelicManager().getDuelManager().sendDuelRequest(player, target);
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("worldrelics.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }
        sender.sendMessage(plugin.getMessageManager().getComponent("prefix").append(
                net.kyori.adventure.text.Component.text("Initiating forced relic spawn cycle...", net.kyori.adventure.text.format.NamedTextColor.YELLOW)
        ));
        plugin.getRelicManager().triggerNewRelicSpawnCycle(true);
    }

    private void handleDespawn(CommandSender sender) {
        if (!sender.hasPermission("worldrelics.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }
        plugin.getRelicManager().handleRelicExpiration(false);
        sender.sendMessage(plugin.getMessageManager().getComponent("prefix").append(
                net.kyori.adventure.text.Component.text("Current relic despawned.", net.kyori.adventure.text.format.NamedTextColor.GREEN)
        ));
    }

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.hasPermission("worldrelics.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }
        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            plugin.getMessageManager().sendMessage(sender, "command-reset-confirm");
            return;
        }
        plugin.getRelicManager().resetRelicSystem();
        plugin.getMessageManager().sendMessage(sender, "command-reset-success");
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("worldrelics.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }
        if (args.length < 3) {
            sender.sendMessage("Usage: /wr give <player> <relic_id|locator>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found.");
            return;
        }

        String itemType = args[2].toLowerCase();
        if (itemType.equals("locator") || itemType.equals("relic_locator") || itemType.equals("compass")) {
            ItemStack locator = plugin.getItemFactory().createRelicLocatorItem();
            target.getInventory().addItem(locator);
            sender.sendMessage("Gave Relic Locator to " + target.getName());
            return;
        }

        if (itemType.equals("owner_locator") || itemType.equals("ownerlocator") || itemType.equals("ownertracker")) {
            ItemStack ownerTracker = plugin.getItemFactory().createOwnerLocatorItem();
            target.getInventory().addItem(ownerTracker);
            sender.sendMessage("Gave Relic Owner Tracker to " + target.getName());
            return;
        }

        RelicDefinition def = plugin.getConfigManager().getRelicDefinition(args[2]);
        if (def == null) {
            sender.sendMessage("Relic definition not found: " + args[2] + " (Use 'locator' to give a Relic Locator compass)");
            return;
        }

        ActiveRelic relic = plugin.getRelicManager().getActiveRelic();
        if (relic == null) {
            sender.sendMessage("No active relic in world to assign.");
            return;
        }

        ItemStack item = plugin.getItemFactory().createRelicItem(relic, def);
        target.getInventory().addItem(item);
        plugin.getRelicManager().claimRelic(target, item);

        sender.sendMessage("Gave relic " + def.getId() + " to " + target.getName());
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("worldrelics.admin")) {
            plugin.getMessageManager().sendMessage(sender, "no-permission");
            return;
        }
        plugin.getConfigManager().loadConfig();
        plugin.getMessageManager().loadMessages();
        plugin.getMessageManager().sendMessage(sender, "command-reload");
    }
}
