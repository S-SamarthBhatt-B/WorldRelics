package in.surventure.worldrelics.command;

import in.surventure.worldrelics.WorldRelicsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class RelicTabCompleter implements TabCompleter {

    private final WorldRelicsPlugin plugin;

    public RelicTabCompleter(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> sub = List.of("status", "locate", "info", "list", "menu", "top", "duel", "spawn", "despawn", "reset", "give", "reload");
            for (String s : sub) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return null; // Player name auto-complete
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> options = new ArrayList<>(plugin.getConfigManager().getRelicDefinitions().keySet());
            options.add("locator");
            options.add("owner_locator");
            for (String id : options) {
                if (id.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(id);
                }
            }
        }
        return completions;
    }
}
