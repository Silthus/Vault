/* This file is part of Vault.

    Vault is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Vault is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Vault.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.milkbowl.vault.permission.plugins;

import de.raidcraft.permissions.PermissionsPlugin;
import de.raidcraft.permissions.groups.Group;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

public class Permission_RCPermissions extends Permission {

    private final String name = "PermissionsEx";
    private PermissionsPlugin permission = null;

    public Permission_RCPermissions(Plugin plugin) {

        this.plugin = plugin;
        Bukkit.getServer().getPluginManager().registerEvents(new PermissionServerListener(this), plugin);

        // Load Plugin in case it was loaded before
        if (permission == null) {
            Plugin perms = plugin.getServer().getPluginManager().getPlugin("RCPermissions");
            if (perms != null) {
                if (perms.isEnabled()) {
                    try {
                        if (Double.valueOf(perms.getDescription().getVersion()) < 1.16) {
                            log.info(String.format("[%s][Permission] %s below 1.16 is not compatible with Vault! Falling back to SuperPerms only mode. PLEASE UPDATE!", plugin.getDescription().getName(), name));
                        }
                    } catch (NumberFormatException e) {
                        // Do nothing
                    }
                    permission = (PermissionsPlugin) perms;
                    log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), name));
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {

        return permission != null && permission.isEnabled();
    }

    @Override
    public boolean playerInGroup(String worldName, String playerName, String groupName) {


        return permission.getGroupManager().isPlayerInGroup(worldName, playerName, groupName);
    }

    public class PermissionServerListener implements Listener {

        Permission_RCPermissions permission = null;

        public PermissionServerListener(Permission_RCPermissions permission) {
            this.permission = permission;
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginEnable(PluginEnableEvent event) {
            if (permission.permission == null) {
                Plugin perms = plugin.getServer().getPluginManager().getPlugin("RCPermissions");

                if (perms != null) {
                    try {
                        if (Double.valueOf(perms.getDescription().getVersion()) < 1.16) {
                            log.info(String.format("[%s][Permission] %s below 1.16 is not compatible with Vault! Falling back to SuperPerms only mode. PLEASE UPDATE!", plugin.getDescription().getName(), name));
                            return;
                        }
                    } catch (NumberFormatException e) {
                        // Do nothing
                    }
                    permission.permission = (PermissionsPlugin) perms;
                    log.info(String.format("[%s][Permission] %s hooked.", plugin.getDescription().getName(), permission.name));
                }
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPluginDisable(PluginDisableEvent event) {
            if (permission.permission != null) {
                if (event.getPlugin().getDescription().getName().equals("RCPermissions")) {
                    permission.permission = null;
                    log.info(String.format("[%s][Permission] %s un-hooked.", plugin.getDescription().getName(), permission.name));
                }
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean playerAddGroup(String worldName, String playerName, String groupName) {

        Group group = permission.getGroupManager().addPlayerToGroup(playerName, groupName);
        return group != null;
    }

    @Override
    public boolean playerRemoveGroup(String worldName, String playerName, String groupName) {

        Group group = permission.getGroupManager().removePlayerFromGroup(playerName, groupName);
        return group != null;
    }

    @Override
    public boolean playerAdd(String worldName, String playerName, String node) {

        throw new UnsupportedOperationException("You can only add permissions thru groups!");
    }

    @Override
    public boolean playerRemove(String worldName, String playerName, String node) {

        throw new UnsupportedOperationException("Only permissions thru groups are supported!");
    }

    @Override
    public boolean groupAdd(String worldName, String groupName, String node) {

        Group group = permission.getGroupManager().getGroup(groupName);
        if (group != null) {
            group.addPermission(worldName, node);
            return true;
        }
        return false;
    }

    @Override
    public boolean groupRemove(String worldName, String groupName, String node) {

        Group group = permission.getGroupManager().getGroup(groupName);
        if (group != null) {
            group.removePermission(worldName, node);
            return true;
        }
        return false;
    }

    @Override
    public boolean groupHas(String worldName, String groupName, String node) {

        Group group = permission.getGroupManager().getGroup(groupName);
        return group != null && group.hasPermission(node, worldName);
    }

    @Override
    public String[] getPlayerGroups(String world, String playerName) {

        Set<String> playerGroups = permission.getProvider().getPlayerGroups(playerName);
        return playerGroups.toArray(new String[playerGroups.size()]);
    }

    @Override
    public String getPrimaryGroup(String world, String playerName) {

        return permission.getProvider().getDefaultGroup().getName();
    }

    @Override
    public boolean playerHas(String worldName, String playerName, String node) {

        return permission.getPlayerManager().hasPermission(playerName, worldName, node);
    }

    @Override
    public String[] getGroups() {

        HashSet<String> groupStrings = new HashSet<String>();
        Set<Group> groups = permission.getGroupManager().getGroups();
        for (Group group : groups) {
            groupStrings.add(group.getName());
        }
        return groupStrings.toArray(new String[groupStrings.size()]);
    }

    @Override
    public boolean hasSuperPermsCompat() {

        return true;
    }

    @Override
    public boolean hasGroupSupport() {

        return true;
    }
}
