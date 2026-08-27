package fr.nocsy.mcpets.data.flags;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.utils.ServerTasks;

public class DismountPetFlag extends AbstractFlag implements StoppableFlag {

    private ScheduledTask task;

    public static String NAME = "mcpets-dismount";

    public DismountPetFlag(final MCPets instance) {
        super(NAME, false, instance);
    }

    @Override
    public void register() {
        super.register();
    }

    @Override
    public void launch() {
        if (getFlag() == null) {
            MCPets.getLog().warning("Flag " + getFlagName() + " couldn't not be launched as it's null. Please contact Nocsy.");
            return;
        }
        else {
            MCPets.getLog().info("Starting flag " + getFlagName() + ".");
        }

        task = ServerTasks.runGlobalTimer(() -> {
            if (MCPets.getMythicMobs() == null)
                return;

            for (final UUID owner : new ArrayList<>(Pet.getActivePets().keySet())) {
                final Player p = Bukkit.getPlayer(owner);
                if (p == null)
                    continue;

                ServerTasks.runOn(p, () -> {
                    for (final Pet pet : Pet.getActivePetsForOwner(owner)) {
                        if (!pet.isMountable())
                            continue;

                        if (!pet.hasMount(p))
                            continue;

                        if (testState(p.getLocation())) {
                            pet.dismount(p);
                            Language.NOT_MOUNTABLE_HERE.sendMessage(p);
                        }
                    }
                });
            }
        }, 20L, 20L);
    }

    @Override
    public void stop() {
        ServerTasks.cancel(task);
        task = null;
    }
}
