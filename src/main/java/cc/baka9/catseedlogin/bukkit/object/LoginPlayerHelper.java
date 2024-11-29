package cc.baka9.catseedlogin.bukkit.object;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.geysermc.floodgate.api.FloodgateApi;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.Config;
import cc.baka9.catseedlogin.bukkit.database.Cache;

public class LoginPlayerHelper {
    private static final Set<LoginPlayer> set = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> playerExitTimes = new ConcurrentHashMap<>();

    public static List<LoginPlayer> getList() { return new ArrayList<>(set); }
    public static void add(LoginPlayer lp) { set.add(lp); }
    public static void remove(LoginPlayer lp) { set.remove(lp); }
    
    public static void remove(String name) {
        set.removeIf(lp -> lp.getName().equals(name));
    }

    public static boolean isLogin(String name) {
        return Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name) ||
                Config.Settings.LoginwiththesameIP && recordCurrentIP(name) ||
                set.stream().anyMatch(lp -> lp.getName().equals(name));
    }

    public static boolean isRegister(String name) {
        return Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name) || Cache.getIgnoreCase(name) != null;
    }

    public static boolean recordCurrentIP(String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player != null && recordCurrentIP(player);
    }

    public static boolean recordCurrentIP(Player player) {
        String currentIP = Optional.ofNullable(player.getAddress()).map(addr -> addr.getAddress().getHostAddress()).orElse(null);
        if (currentIP == null) return false;

        LoginPlayer storedPlayer = Cache.getIgnoreCase(player.getName());
        if (storedPlayer != null) {
            List<String> storedIPs = getStoredIPs(storedPlayer);
            Long exitTime = playerExitTimes.get(player.getName());

            if (isLoopbackAddress(currentIP)) return false;
            return Config.Settings.IPTimeout == 0 ? storedIPs.contains(currentIP) :
                   exitTime != null && storedIPs.contains(currentIP) && (System.currentTimeMillis() - exitTime) <= (long) Config.Settings.IPTimeout * 60 * 1000L;
        }

        return false;
    }

    public void recordPlayerExitTime(String playerName) {
        if (Config.Settings.IPTimeout != 0 && isLogin(playerName)) {
            playerExitTimes.put(playerName, System.currentTimeMillis());
        }
    }

    public void onPlayerQuit(String playerName) {
        recordPlayerExitTime(playerName);
    }

    public static List<String> getStoredIPs(LoginPlayer lp) {
        return (lp.getIps() != null) ? new ArrayList<>(Arrays.asList(lp.getIps().split(";"))) : new ArrayList<>();
    }

    public static boolean isFloodgatePlayer(String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player != null && isFloodgatePlayer(player);
    }

    public static boolean isFloodgatePlayer(Player player) {
        return Bukkit.getPluginManager().getPlugin("floodgate") != null && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

    public static Long getLastLoginTime(String name) {
        LoginPlayer loginPlayer = Cache.getIgnoreCase(name);
        return (loginPlayer != null) ? loginPlayer.getLastAction() : null;
    }

    public static void recordCurrentIP(Player player, LoginPlayer lp) {
        String currentIp = player.getAddress().getAddress().getHostAddress();
        List<String> ipsList = lp.getIpsList();
        ipsList = ipsList.stream().distinct().collect(Collectors.toList());
        if (!ipsList.isEmpty()) ipsList.remove(0);
        ipsList.add(currentIp);
        lp.setIps(String.join(";", ipsList));
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                CatSeedLogin.sql.edit(lp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendBlankInventoryPacket(Player player) {
        if (!Config.Settings.Emptybackpack) return;

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer inventoryPacket = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
        inventoryPacket.getIntegers().write(0, 0);
        ItemStack[] blankInventory = new ItemStack[45];
        Arrays.fill(blankInventory, new ItemStack(Material.AIR));
        
        StructureModifier<ItemStack[]> itemArrayModifier = inventoryPacket.getItemArrayModifier();
        if (itemArrayModifier.size() > 0) {
            itemArrayModifier.write(0, blankInventory);
        } else {
            StructureModifier<List<ItemStack>> itemListModifier = inventoryPacket.getItemListModifier();
            itemListModifier.write(0, Arrays.asList(blankInventory));
        }

        protocolManager.sendServerPacket(player, inventoryPacket, false);
    }

    private static boolean isLoopbackAddress(String currentIP) {
        try {
            return InetAddress.getByName(currentIP).isLoopbackAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }
}
