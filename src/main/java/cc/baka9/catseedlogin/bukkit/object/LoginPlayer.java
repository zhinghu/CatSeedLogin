package cc.baka9.catseedlogin.bukkit.object;

import cc.baka9.catseedlogin.util.Crypt;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ToString
@Getter
@Setter
public class LoginPlayer {
    private String name, password, email, ips;
    private long lastAction;

    public LoginPlayer(String name, String password) {
        this.name = name; this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof LoginPlayer && Objects.equals(name, ((LoginPlayer) o).name));
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public List<String> getIpsList() {
        return (ips != null) ? new ArrayList<>(Arrays.asList(ips.split(";"))) : new ArrayList<>();
    }

    public void crypt() {
        password = Crypt.encrypt(name, password);
    }
}
