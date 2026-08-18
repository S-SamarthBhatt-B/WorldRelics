package in.surventure.worldrelics.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class VaultHook {

    public boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    public void deposit(Player player, double amount) {
        if (!isAvailable() || player == null || amount <= 0) return;
        try {
            org.bukkit.plugin.RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                    Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (rsp != null && rsp.getProvider() != null) {
                rsp.getProvider().depositPlayer(player, amount);
            }
        } catch (Throwable ignored) {}
    }
}
