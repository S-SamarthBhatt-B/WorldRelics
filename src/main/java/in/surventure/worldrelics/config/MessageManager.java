package in.surventure.worldrelics.config;

import in.surventure.worldrelics.WorldRelicsPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageManager {

    private final WorldRelicsPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration messagesConfig;

    public MessageManager(WorldRelicsPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(file);
    }

    public String getRawMessage(String path) {
        return messagesConfig.getString(path, "<red>Missing message: " + path + "</red>");
    }

    public Component getComponent(String path, TagResolver... resolvers) {
        String raw = getRawMessage(path);
        String prefix = messagesConfig.getString("prefix", "");
        if (!path.equals("prefix") && !raw.startsWith("<") && !raw.contains("\n")) {
            raw = prefix + raw;
        }
        return miniMessage.deserialize(raw, resolvers);
    }

    public void sendMessage(CommandSender sender, String path, TagResolver... resolvers) {
        sender.sendMessage(getComponent(path, resolvers));
    }

    public void broadcast(String path, TagResolver... resolvers) {
        Component component = getComponent(path, resolvers);
        plugin.getServer().broadcast(component);
    }

    public void broadcastRaw(Component component) {
        plugin.getServer().broadcast(component);
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }
}
