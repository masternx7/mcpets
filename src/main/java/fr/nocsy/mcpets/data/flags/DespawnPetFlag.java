package fr.nocsy.mcpets.data.flags;

import java.util.UUID;
import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import fr.nocsy.mcpets.MCPets;
import fr.nocsy.mcpets.data.Pet;
import fr.nocsy.mcpets.data.config.Language;
import fr.nocsy.mcpets.data.PetDespawnReason;
import fr.nocsy.mcpets.utils.ServerTasks;

public class DespawnPetFlag extends AbstractFlag implements StoppableFlag {

    private ScheduledTask task;

    public static String NAME = "mcpets-despawn";

    public DespawnPetFlag(final MCPets instance) {
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

        MCPets.getLog().info("Starting flag " + getFlagName() + ".");

        task = ServerTasks.runGlobalTimer(() -> {
            if (MCPets.getMythicMobs() == null) return;

            for (UUID owner : new ArrayList<>(Pet.getActivePets().keySet())) {
                Player pl = Bukkit.getPlayer(owner);
                if (pl == null) continue;

                ServerTasks.runOn(pl, () -> {
                    if (!testState(pl.getLocation())) return;

                    for (Pet pet : new ArrayList<>(Pet.getActivePetsForOwner(owner))) {
                        pet.despawn(PetDespawnReason.TELEPORT);
                    }

                    Language.CANT_FOLLOW_HERE.sendMessage(pl);
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
