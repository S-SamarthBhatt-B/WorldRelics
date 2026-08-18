package in.surventure.worldrelics;

import in.surventure.worldrelics.api.WorldRelicsAPI;
import in.surventure.worldrelics.command.RelicTabCompleter;
import in.surventure.worldrelics.command.WorldRelicCommand;
import in.surventure.worldrelics.config.ConfigManager;
import in.surventure.worldrelics.config.MessageManager;
import in.surventure.worldrelics.database.DatabaseManager;
import in.surventure.worldrelics.gui.RelicHistoryGUI;
import in.surventure.worldrelics.gui.RelicMenuGUI;
import in.surventure.worldrelics.hook.HookManager;
import in.surventure.worldrelics.listener.PlayerEventListener;
import in.surventure.worldrelics.listener.RelicAbilityListener;
import in.surventure.worldrelics.listener.RelicItemListener;
import in.surventure.worldrelics.listener.RelicProtectionListener;
import in.surventure.worldrelics.manager.RelicAbilityManager;
import in.surventure.worldrelics.manager.RelicManager;
import in.surventure.worldrelics.relic.RelicItemFactory;
import in.surventure.worldrelics.scheduler.RelicTaskScheduler;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldRelicsPlugin extends JavaPlugin {

    private static WorldRelicsPlugin instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private RelicItemFactory itemFactory;
    private RelicAbilityManager abilityManager;
    private HookManager hookManager;
    private RelicManager relicManager;
    private RelicTaskScheduler taskScheduler;
    private RelicMenuGUI menuGUI;
    private RelicHistoryGUI historyGUI;
    private in.surventure.worldrelics.gui.RelicLeaderboardGUI leaderboardGUI;

    @Override
    public void onEnable() {
        instance = this;
        WorldRelicsAPI.setPlugin(this);

        getLogger().info("=======================================");
        getLogger().info("   WorldRelics v" + getDescription().getVersion() + " Enabing...");
        getLogger().info("=======================================");

        // Load Configs & Messages
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig();

        this.messageManager = new MessageManager(this);
        this.messageManager.loadMessages();

        // Initialize Database
        this.databaseManager = new DatabaseManager(this);
        this.databaseManager.initialize();

        // Initialize Utilities & Managers
        this.itemFactory = new RelicItemFactory(this);
        this.abilityManager = new RelicAbilityManager(this);
        this.hookManager = new HookManager(this);

        this.menuGUI = new RelicMenuGUI(this);
        this.historyGUI = new RelicHistoryGUI(this);
        this.leaderboardGUI = new in.surventure.worldrelics.gui.RelicLeaderboardGUI(this);

        this.relicManager = new RelicManager(this);
        this.taskScheduler = new RelicTaskScheduler(this);

        // Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        getServer().getPluginManager().registerEvents(new RelicItemListener(this), this);
        getServer().getPluginManager().registerEvents(new RelicProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new RelicAbilityListener(this), this);
        getServer().getPluginManager().registerEvents(menuGUI, this);
        getServer().getPluginManager().registerEvents(historyGUI, this);
        getServer().getPluginManager().registerEvents(leaderboardGUI, this);

        // Register Command & TabCompleter
        PluginCommand cmd = getCommand("worldrelic");
        if (cmd != null) {
            WorldRelicCommand executor = new WorldRelicCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(new RelicTabCompleter(this));
        }

        // Initialize Startup Recovery & Active Relic State
        this.relicManager.initializeStateOnStartup();

        // Start Tasks
        this.taskScheduler.startTasks();

        getLogger().info("[WorldRelics] Successfully enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[WorldRelics] Disabling plugin...");

        if (taskScheduler != null) {
            taskScheduler.cancelTasks();
        }

        if (relicManager != null && relicManager.getDisplayManager() != null) {
            relicManager.getDisplayManager().removeBossBar();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("[WorldRelics] Disabled successfully.");
    }

    public static WorldRelicsPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public RelicItemFactory getItemFactory() {
        return itemFactory;
    }

    public RelicAbilityManager getAbilityManager() {
        return abilityManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public RelicManager getRelicManager() {
        return relicManager;
    }

    public RelicMenuGUI getMenuGUI() {
        return menuGUI;
    }

    public RelicHistoryGUI getHistoryGUI() {
        return historyGUI;
    }

    public in.surventure.worldrelics.gui.RelicLeaderboardGUI getLeaderboardGUI() {
        return leaderboardGUI;
    }
}
