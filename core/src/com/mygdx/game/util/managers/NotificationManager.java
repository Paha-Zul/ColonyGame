package com.mygdx.game.util.managers;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Stats;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;

import java.util.HashMap;
import java.util.function.Predicate;

/**
 * Created by Paha on 5/19/2015.
 */
public class NotificationManager {
    private static Array<Notification> notifications = new Array<>(); //For displaying notifications.
    private static Array<Notification> activeNotifications = new Array<>(); //For displaying notifications.
    private static HashMap<String, Notification> notificationMap = new HashMap<>(); //For getting notifications in fast time.
    private static PlayerManager.Player player; //The player that we will be working for.

    private static Timer updateTimer = new RepeatingTimer(100f, null);

    public static void init(PlayerManager.Player player, float updateTick){
        NotificationManager.player = player;
        updateTimer = new RepeatingTimer(updateTick, () -> {
            for(Notification not : notifications){
                boolean result = not.test(NotificationManager.player);
                //If it is not active and is turning active.
                if(result && !not.active) {
                    activeNotifications.add(not);
                    not.active = true;

                //If active and turning not active.
                }else if(!result && not.active) {
                    activeNotifications.removeValue(not, true);
                    not.active = false;
                }
            }
        });

        String foodTooltip = "The current level of food is low. Get more food or your colonists will starve! This can be done by directing the " +
                "colonists to gather food by selecting a colonist, clicking the 'gather' button at the bottom of the interface that appears, then selecting 'food' which " +
                "will toggle food to be collected.";
        //Some test notifications
        NotificationManager.addNotification("Low Food", "The current level of food is low.", currPlayer -> {
            Inventory.InventoryItem invItem = player.colony.getGlobalInv().get("food");
            return invItem == null || invItem.getAmount(false) <= 20;
        }).extendedTooltip = foodTooltip;

        String waterTooltip = "The current level of water is low. To get more water, select a colonist and direct him to gather water by clicking the 'gather' button at the bottom of the screen and " +
                "clicking on 'water' to toggle the gathering of water. Water may also be found in plants in the right environment.";
        //Some test notifications
        NotificationManager.addNotification("Low Water", "The current level of water is low.", currPlayer -> {
            Inventory.InventoryItem invItem = player.colony.getGlobalInv().get("water");
            return invItem == null || invItem.getAmount() <= 20;
        }).extendedTooltip = waterTooltip;

        String thirstTooltip = "One or more of your colonists is in danger of dying due to lack of water. You need to designate at least one of your colonists to search for water immediately. " +
                "Alternatively, some plants may contain some water based on the environment. Make sure to keep a healthy stock of water so that this doesn't happen again.";
        NotificationManager.addNotification("Thirstation", "One or more of your colonists is in danger of dying due to lack of water.", currPlayer -> {
            Array<Component> list = currPlayer.colony.getOwnedListFromColony(Colonist.class);
            for(Component comp : list){
                Colonist col = (Colonist)comp;
                Stats.Stat thirst = col.getStats().getStat("water");
                if(thirst != null && thirst.getCurrVal() <= 0) return true;
            }

            return false;
        }).extendedTooltip = thirstTooltip;

        String hungerTooltip = "One or more of your colonists is in danger of dying due to starvation. The starving colonist will slowly lose health and cannot regenerate until " +
                " he/she eats food. Direct some of your colonists to gather or hunt for food.";
        NotificationManager.addNotification("Starvation", "One or more of your colonists is in danger of dying due to starvation.", currPlayer -> {
            Array<Component> list = currPlayer.colony.getOwnedListFromColony(Colonist.class);
            for(Component comp : list){
                Colonist col = (Colonist)comp;
                Stats.Stat food = col.getStats().getStat("food");
                if(food != null && food.getCurrVal() == 0) return true;
            }

            return false;
        }).extendedTooltip = hungerTooltip;
    }

    public static void update(float delta){
        updateTimer.update(delta);
    }

    public static Notification addNotification(String name, String tooltip, Predicate<PlayerManager.Player> playerPredicate){
        Notification noti = new Notification(name, tooltip, playerPredicate);
        notificationMap.put(name, noti);
        notifications.add(noti);
        return noti;
    }

    public static Array<Notification> getActiveNotifications(){
        return activeNotifications;
    }

    public static class Notification{
        public String name, quickTooltip, extendedTooltip;
        public Predicate<PlayerManager.Player> playerTest;
        public boolean active = false;

        public Notification(String name, String tooltip, Predicate<PlayerManager.Player> playerTest) {
            this.name = name;
            this.quickTooltip = tooltip;
            this.playerTest = playerTest;
        }

        public boolean test(PlayerManager.Player player){
            return playerTest.test(player);
        }
    }
}
