package com.intellectualcrafters.plot.commands;

import com.intellectualcrafters.plot.config.C;
import com.intellectualcrafters.plot.database.DBFunc;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.util.EventUtil;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.Permissions;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.general.commands.Argument;
import com.plotsquared.general.commands.CommandDeclaration;
import java.util.UUID;

@CommandDeclaration(
        command = "trust",
        aliases = {"t"},
        requiredType = RequiredType.PLAYER,
        usage = "/plot trust <player>",
        description = "Allow a player to build in a plot",
        category = CommandCategory.SETTINGS)
public class Trust extends SubCommand {

    public Trust() {
        super(Argument.PlayerName);
    }

    @Override
    public boolean onCommand(PlotPlayer plr, String[] args) {
        Location loc = plr.getLocation();
        Plot plot = loc.getPlotAbs();
        if (plot == null) {
            return !sendMessage(plr, C.NOT_IN_PLOT);
        }
        if (!plot.hasOwner()) {
            MainUtil.sendMessage(plr, C.PLOT_UNOWNED);
            return false;
        }
        if (!plot.isOwner(plr.getUUID()) && !Permissions.hasPermission(plr, "plots.admin.command.trust")) {
            MainUtil.sendMessage(plr, C.NO_PLOT_PERMS);
            return true;
        }
        UUID uuid;
        if (args[0].equalsIgnoreCase("*")) {
            uuid = DBFunc.everyone;
        } else {
            uuid = UUIDHandler.getUUID(args[0], null);
        }
        if (uuid == null) {
            MainUtil.sendMessage(plr, C.INVALID_PLAYER, args[0]);
            return false;
        }
        if (plot.isOwner(uuid)) {
            MainUtil.sendMessage(plr, C.ALREADY_OWNER);
            return false;
        }
        if (plot.getTrusted().contains(uuid)) {
            MainUtil.sendMessage(plr, C.ALREADY_ADDED);
            return false;
        }
        if (plot.removeMember(uuid)) {
            plot.addTrusted(uuid);
        } else {
            if ((plot.getMembers().size() + plot.getTrusted().size()) >= plot.getArea().MAX_PLOT_MEMBERS) {
                MainUtil.sendMessage(plr, C.PLOT_MAX_MEMBERS);
                return false;
            }
            if (plot.getDenied().contains(uuid)) {
                plot.removeDenied(uuid);
            }
            plot.addTrusted(uuid);
        }
        EventUtil.manager.callTrusted(plr, plot, uuid, true);
        MainUtil.sendMessage(plr, C.TRUSTED_ADDED);
        return true;
    }
}
