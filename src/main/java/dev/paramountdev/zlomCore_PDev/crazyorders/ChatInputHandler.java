package dev.paramountdev.zlomCore_PDev.crazyorders;

import dev.paramountdev.zlomCore_PDev.ZlomCore_PDev;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputHandler implements Listener {
    private enum Stage { AMOUNT, PRICE }

    private static final Map<UUID, Material> selectedMaterial = new HashMap<>();
    private static final Map<UUID, Integer> selectedAmount = new HashMap<>();
    private static final Map<UUID, Stage> stages = new HashMap<>();

    public static void startAmountInput(Player player, Material material) {
        selectedMaterial.put(player.getUniqueId(), material);
        stages.put(player.getUniqueId(), Stage.AMOUNT);
        player.sendMessage("§aСколько нужно этого предмета?");
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!stages.containsKey(uuid)) return;

        e.setCancelled(true);
        Bukkit.getScheduler().runTask(ZlomCore_PDev.getInstance(), () -> {
            Stage stage = stages.get(uuid);
            String msg = e.getMessage();

            try {
                switch (stage) {
                    case AMOUNT -> {
                        int amount = Integer.parseInt(msg);
                        if (amount <= 0) throw new NumberFormatException();
                        selectedAmount.put(uuid, amount);
                        stages.put(uuid, Stage.PRICE);
                        player.sendMessage("§aСколько вы заплатите за 1 сделку?");
                    }
                    case PRICE -> {
                        double price = Double.parseDouble(msg);
                        if (price <= 0) throw new NumberFormatException();

                        Material mat = selectedMaterial.remove(uuid);
                        int amount = selectedAmount.remove(uuid);
                        stages.remove(uuid);

                        if (!ZlomCore_PDev.getInstance().getOrderManager().canCreateMoreOrders(uuid)) {
                            player.sendMessage("§cВы достигли лимита заказов");
                            return;
                        }

                        Order order = new Order(Bukkit.getPlayer(uuid), mat, amount, price);
                        ZlomCore_PDev.getInstance().getOrderManager().addOrder(order);

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                        player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 10);
                        player.sendMessage("§aЗаказ создан: " + amount + " x " + mat.name() + " по " + price + " за штуку.");
                        Bukkit.getScheduler().runTaskLater(ZlomCore_PDev.getInstance(), () -> {
                            new OrdersGUI(ZlomCore_PDev.getInstance().getOrderManager()).openOrdersMenu(player);
                        }, 20L);
                    }
                }
            } catch (NumberFormatException ex) {
                player.sendMessage("§cНеверный формат. Ввод отменён.");
                selectedMaterial.remove(uuid);
                selectedAmount.remove(uuid);
                stages.remove(uuid);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
            }
        });
    }
}

