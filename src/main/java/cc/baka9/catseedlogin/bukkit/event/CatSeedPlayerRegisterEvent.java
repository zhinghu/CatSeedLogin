package cc.baka9.catseedlogin.bukkit.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
public class CatSeedPlayerRegisterEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;

    public CatSeedPlayerRegisterEvent(Player player) {
        this.player = player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
