package cc.baka9.catseedlogin.bukkit.object;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private static final Set<LoginPlayer> set = new HashSet<>();

    public static List<LoginPlayer> getList(){
        return new ArrayList<>(set);
    }

    public static void add(LoginPlayer lp){
        synchronized (set) {

            set.add(lp);
        }
    }

    public static void remove(LoginPlayer lp){
        synchronized (set) {

            set.remove(lp);
        }
    }

    public static void remove(String name){
        synchronized (set) {
            for (LoginPlayer lp : set) {
                if (lp.getName().equals(name)) {
                    set.remove(lp);
                    break;
                }
            }
        }
    }

    public static boolean isLogin(String name){
        synchronized (set) {
            if (Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name)){
                return true;
            }
            if (Config.Settings.LoginwiththesameIP && recordCurrentIP(name)){
                return true;
            }
            for (LoginPlayer lp : set) {
                if (lp.getName().equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isRegister(String name){
        if (Config.Settings.BedrockLoginBypass && isFloodgatePlayer(name)){
            return true;
        }
        return Cache.getIgnoreCase(name) != null;

    }

    public static boolean recordCurrentIP(String name) {
        Player player = Bukkit.getPlayerExact(name);
        return player != null && recordCurrentIP(player);
    }

public static boolean recordCurrentIP(Player player) {
    String playerName = player.getName();
    String currentIP = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : null;
    if (currentIP == null) {
        return false;
    }
    long currentTime = System.currentTimeMillis();
    LoginPlayer loginPlayer = Cache.getIgnoreCase(playerName);

    if (loginPlayer != null) {
        List<String> storedIPs = getStoredIPs(loginPlayer);
        long lastLoginTime = loginPlayer.getLastAction();
        long timeoutInTicks = (Config.Settings.IPTimeout * 60 * 1000) / 50;

        if (storedIPs.contains(currentIP) || (currentTime - lastLoginTime) <= timeoutInTicks) {
            return true;
        }
    }
    return false;
}


public static List<String> getStoredIPs(LoginPlayer lp) {
    List<String> storedIPs = new ArrayList<>();

    String ipsString = lp.getIps();
    String[] ipsArray = ipsString.split(";");

    storedIPs.addAll(Arrays.asList(ipsArray));

    return storedIPs;
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
        if (loginPlayer == null) {
            return null;
        }
        return loginPlayer.getLastAction();
    }

    // 记录登录IP
    public static void recordCurrentIP(Player player, LoginPlayer lp){
        String currentIp = player.getAddress().getAddress().getHostAddress();
        List<String> ipsList = lp.getIpsList();
        ipsList.add(currentIp);
        ipsList = ipsList.stream().distinct().collect(Collectors.toList());
        if (ipsList.size() > 0) {
            ipsList.remove(0);
        }
        lp.setIps(String.join(";", ipsList.toArray(new String[0])));
        CatSeedLogin.instance.runTaskAsync(() -> {
            try {
                CatSeedLogin.sql.edit(lp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ProtocolLib发包空背包
    public static void sendBlankInventoryPacket(Player player){
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        PacketContainer inventoryPacket = protocolManager.createPacket(PacketType.Play.Server.WINDOW_ITEMS);
        inventoryPacket.getIntegers().write(0, 0);
        int inventorySize = 45;

        ItemStack[] blankInventory = new ItemStack[inventorySize];
        Arrays.fill(blankInventory, new ItemStack(Material.AIR));


        StructureModifier<ItemStack[]> itemArrayModifier = inventoryPacket.getItemArrayModifier();
        if (itemArrayModifier.size() > 0) {
            itemArrayModifier.write(0, blankInventory);
        } else {

            StructureModifier<List<ItemStack>> itemListModifier = inventoryPacket.getItemListModifier();
            itemListModifier.write(0, Arrays.asList(blankInventory));
        }

        try {
            protocolManager.sendServerPacket(player, inventoryPacket, false);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
