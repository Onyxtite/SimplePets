package simplepets.brainsynder.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import simple.brainsynder.api.ParticleMaker;
import simple.brainsynder.nbt.JsonToNBT;
import simple.brainsynder.nbt.NBTException;
import simple.brainsynder.nbt.StorageTagCompound;
import simple.brainsynder.sound.SoundMaker;
import simple.brainsynder.utils.Base64Wrapper;
import simple.brainsynder.utils.Valid;
import simplepets.brainsynder.PetCore;
import simplepets.brainsynder.api.entity.IEntityControllerPet;
import simplepets.brainsynder.api.entity.ITameable;
import simplepets.brainsynder.api.event.pet.PetNameChangeEvent;
import simplepets.brainsynder.api.event.pet.PetRemoveEvent;
import simplepets.brainsynder.api.pet.IPet;
import simplepets.brainsynder.nms.anvil.AnvilGUI;
import simplepets.brainsynder.nms.anvil.AnvilSlot;
import simplepets.brainsynder.pet.Pet;
import simplepets.brainsynder.pet.PetDefault;

import java.util.*;

public class PetOwner {
    private static Map<UUID, PetOwner> ownerMap = new HashMap<>();
    /**
     * JSONArray contains all the pets the player has owned while Vault was Enabled.
     */
    private JSONArray ownedPets = new JSONArray();
    /**
     * Players Pet name, Will return null if empty.
     */
    private String petName = null;
    /**
     * Will return the players active pet, Will return null if there is no pet.
     */
    IPet pet = null;
    /**
     * Will return an instance of the Player (Pets Owner)
     */
    private Player player = null;
    private UUID uuid = null;
    /**
     * Returns the OwnerFile, Where all the information is stored.
     */
    private OwnerFile file = null;
    /**
     * This little boolean is for checking if a player is renaming their pet via chat.
     */
    private boolean renaming = false;
    /**
     * Handles Pet respawning when the player teleports, dies, etc...
     */
    private StorageTagCompound petToRespawn = null;
    private JSONObject storedInventory = null;
    private List<StorageTagCompound> savedPets = new ArrayList<>();

    private PetOwner(Player player) {
        Valid.notNull(player, "Player can not be null");
        this.player = player;
        uuid = player.getUniqueId();
        reloadData();
    }

    private PetOwner(String name) {
        this(Bukkit.getPlayerExact(name));
    }

    /**
     * @param name Player username
     * @return PetOwner Instance
     */
    public static PetOwner getPetOwner(String name) {
        Valid.notNull(name, "PlayerName can not be null");
        return getPetOwner(Bukkit.getPlayerExact(name));
    }

    public static PetOwner getPetOwner(UUID uuid) {
        Valid.notNull(uuid, "UUID can not be null");
        if (ownerMap.containsKey(uuid)) return ownerMap.get(uuid);
        return null;
    }

    public static Collection<PetOwner> values() {
        return ownerMap.values();
    }

    /**
     * @param player org.bukkit.entity.Player instance
     * @return PetOwner Instance
     */
    public static PetOwner getPetOwner(Player player) {
        if ((player == null) /*|| (!player.isOnline()) */) return null;
        if (ownerMap.containsKey(player.getUniqueId())) {
            return ownerMap.get(player.getUniqueId());
        }
        PetOwner owner = new PetOwner(player);
        ownerMap.put(player.getUniqueId(), owner);
        return owner;
    }

    public static void removePlayer(UUID uuid) {
        ownerMap.remove(uuid);
    }

    /**
     * Activates a Data Reload for the OwnerFile.
     */
    public void reloadData() {
        if (file == null) this.file = new OwnerFile(this);
        file.reload();
    }

    void setRawPetName(String name) {
        this.petName = name;
    }

    void setRawOwned(JSONArray ownedPets) {
        this.ownedPets = ownedPets;
    }

    /**
     * This changes the Pets name, Will fire the PetNameChangeEvent Event.
     *
     * @param name Pets New name
     */
    public void setPetName(String name) {
        setPetName(name, false);
    }

    void updateSavedPets(JSONArray array) {
        if (array.isEmpty()) return;

        array.forEach(obj -> {
            String json = Base64Wrapper.decodeString(String.valueOf(obj));
            try {
                savedPets.add(JsonToNBT.getTagFromJson(json));
            } catch (NBTException ignored) {
            }
        });
    }

    public boolean containsPetSave(StorageTagCompound compound) {
        if (savedPets.isEmpty()) return false;
        for (StorageTagCompound stc : savedPets) {
            if (stc.toString().equals(compound.toString())) return true;
        }
        return false;
    }

    public void setSavedPets(List<StorageTagCompound> savedPets) {
        this.savedPets = savedPets;
    }

    public List<StorageTagCompound> getSavedPets() {
        return savedPets;
    }

    JSONArray getSavedPetsArray() {
        JSONArray array = new JSONArray();
        if (!savedPets.isEmpty()) {
            savedPets.forEach(compound -> {
                array.add(Base64Wrapper.encodeString(compound.toString()));
            });
        }
        return array;
    }

    public void setPetName(String name, boolean override) {
        boolean hasLimit = PetCore.get().getConfiguration().getBoolean("RenamePet.Limit-Number-Of-Characters");
        boolean color = PetCore.get().getConfiguration().getBoolean("ColorCodes");
        boolean k = PetCore.get().getConfiguration().getBoolean("Use&k");
        if (name != null) name = name.replace("~", " ");

        if (!override) {
            if (hasLimit && (!player.hasPermission("Pet.name.bypassLimit"))) {
                int limit = PetCore.get().getConfiguration().getInt("RenamePet.CharacterLimit");
                if (name.length() > limit) {
                    name = name.substring(0, limit);
                }
            }

            PetNameChangeEvent event = new PetNameChangeEvent(player, name, color, k);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                play(player.getEyeLocation(), ParticleMaker.Particle.VILLAGER_ANGRY, 0.5F, 0.5F, 0.5F);
                SoundMaker.BLOCK_ANVIL_LAND.playSound(player.getLocation(), 0.5F, 0.5F);
                player.sendMessage(PetCore.get().getMessages().getString("Pet-RenameFailure", true).replace("{name}", ChatColor.translateAlternateColorCodes('&', name)));
                return;
            }
            name = event.getNewName();
            if (event.canUseColor()) {
                name = ChatColor.translateAlternateColorCodes('&', event.canUseMagic() ? name : name.replace("&k", "k"));
            }
            player.sendMessage(PetCore.get().getMessages().getString("Pet-Name-Changed", true).replace("%petname%", name).replace("%player%", player.getName()));
        } else {
            if (name != null) name = ChatColor.translateAlternateColorCodes('&', k ? name : name.replace("&k", "k"));
        }
        petName = name;
        if (!hasPet()) {
            return;
        }
        if (name == null) {
            if (name == null || name.equals("null"))
                name = PetCore.get().getDefaultPetName(pet.getPetType(), player);
            name = PetCore.get().translateName(name);
        }

        pet.getEntity().getEntity().setCustomName(name.replace("%player%", player.getName()));
        if (PetCore.get().getConfiguration().getBoolean("ShowParticles") && (!override)) {
            play(pet.getEntity().getEntity().getLocation(), ParticleMaker.Particle.VILLAGER_HAPPY, 1.0F, 1.0F, 1.0F);
        }
    }

    /**
     * This method is normally used as a Backend code method for spawning Pets.
     *
     * @param pet instance if IPet class
     */
    public void setPet(IPet pet) {
        this.pet = pet;
        if (pet != null) {
            String name = petName;
            if (name == null || name.equals("null")) {
                name = PetCore.get().getDefaultPetName(pet.getPetType(), player);
            }
            PetCore.get().getUtilities().handlePathfinders(player, pet.getEntity().getEntity(), pet.getPetType().getSpeed());
            if (pet.getEntity() instanceof ITameable) {
                ITameable wolf = (ITameable) pet.getEntity();
                wolf.setTamed(true);
            }

            if (name == null || name.equals("null")) name = PetCore.get().getDefaultPetName(pet.getPetType(), player);
            pet.getEntity().getEntity().setCustomNameVisible(true);
            pet.getEntity().getEntity().setCustomName(PetCore.get().translateName(name));
        }
    }

    /**
     * @return true= "Player has a Pet" | false= "Player does not have a pet"
     */
    public boolean hasPet() {
        return (pet != null);
    }

    public void addPurchasedPet(String petName) {
        if (!ownedPets.contains(petName))
            ownedPets.add(petName);
    }

    /**
     * Removes the players current pet, if the player does not have a pet method will do nothing.
     */
    public void removePet() {
        if (hasPet()) {
            PetRemoveEvent removeEvent = new PetRemoveEvent((Pet) pet);
            Bukkit.getServer().getPluginManager().callEvent(removeEvent);
            if (removeEvent.isCancelled()) {
                play(player.getEyeLocation(), ParticleMaker.Particle.VILLAGER_ANGRY, 0.5F, 0.5F, 0.5F);
                SoundMaker.BLOCK_ANVIL_LAND.playSound(player.getLocation(), 0.5F, 0.5F);
                return;
            }
            if (pet.getEntity().getEntity().getPassenger() instanceof Player) {
                pet.getEntity().getEntity().eject();
            }

            if (PetCore.get().getConfiguration().getBoolean("ShowParticles")) {
                play(pet.getEntity().getEntity().getLocation(), ParticleMaker.Particle.LAVA, 1.0F, 1.0F, 1.0F);
            }
            if (pet.getEntity() instanceof IEntityControllerPet) {
                ((IEntityControllerPet) pet.getEntity()).remove();
            } else {
                pet.getEntity().getEntity().remove();
            }
            this.pet = null;
        }
    }

    /**
     * Opens a Anvil GUI which allows the owner to rename their pet.
     */
    public void renamePet() {
        if (PetCore.get().getConfiguration().getBoolean("RenamePet.ViaAnvil")) {
            AnvilGUI gui = new AnvilGUI(PetCore.get(), player, event -> {
                if (event.getSlot() != AnvilSlot.OUTPUT) {
                    event.setWillClose(false);
                    event.setWillDestroy(false);
                    event.setCanceled(true);
                    return;
                }
                event.setCanceled(true);
                event.setWillClose(true);
                event.setWillDestroy(true);

                if (event.getName().equalsIgnoreCase("reset")) {
                    setPetName(null, true);
                } else {
                    setPetName(event.getName(), false);
                }
            });
            gui.setSlot(AnvilSlot.INPUT_LEFT, new ItemStack(Material.NAME_TAG));
            gui.open();
            player.sendMessage(PetCore.get().getMessages().getString("Pet-RenameViaAnvil", true));
            return;
        }
        if (renaming) {
            renaming = false;
            return;
        }
        renaming = true;
        player.sendMessage(PetCore.get().getMessages().getString("Pet-RenameViaChat", true));
    }

    public void respawnPetFully() {
        respawnPetFully(10);
    }

    public void respawnPetFully(int delay) {
        if (!hasPet()) return;
        IPet pet = getPet();
        if (pet.getVisableEntity() == null) return;
        if (hasPetToRespawn()) return;
        setPetToRespawn(pet.getVisableEntity().asCompound());
        removePet();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasPetToRespawn()) {
                    if (!player.isOnline()) {
                        setPetToRespawn(null);
                        return;
                    }

                    respawnPet();
                }
            }
        }.runTaskLater(PetCore.get(), delay);
    }

    public void respawnPet() {
        if (hasPet()) return;
        if (!hasPetToRespawn()) return;
        if (!petToRespawn.hasKey("PetType")) return;

        PetDefault type = PetCore.get().getTypeManager().getType(petToRespawn.getString("PetType"));
        type.setPet(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasPet()) {
                    getPet().getVisableEntity().applyCompound(petToRespawn);
                    petToRespawn = null;
                }
            }
        }.runTaskLater(PetCore.get(), 2);
    }

    public void respawnPet(StorageTagCompound compound) {
        if (!compound.hasKey("PetType")) return;
        if (hasPet()) {
            removePet();
        }

        PetDefault type = PetCore.get().getTypeManager().getType(compound.getString("PetType"));
        type.setPet(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (hasPet()) {
                    getPet().getVisableEntity().applyCompound(compound);
                }
            }
        }.runTaskLater(PetCore.get(), 2);
    }

    public StorageTagCompound getPetToRespawn() {
        return petToRespawn;
    }

    public boolean hasPetToRespawn() {
        return (petToRespawn != null) && (pet == null);
    }

    private void play(Location location, ParticleMaker.Particle effect, float offsetX, float offsetY, float offsetZ) {
        ParticleMaker maker = new ParticleMaker(effect, 20, offsetX, offsetY, offsetZ);
        maker.sendToLocation(location);
    }

    void play(Location location, ParticleMaker.Particle effect, int amount) {
        ParticleMaker maker = new ParticleMaker(effect, amount, 0.5, 0.5, 0.5);
        maker.sendToLocation(location);
    }

    public JSONArray getOwnedPets() {
        return this.ownedPets;
    }

    public JSONObject getStoredInventory() {
        return storedInventory;
    }

    public void setStoredInventory(JSONObject storedInventory) {
        setStoredInventory(storedInventory, true);
    }

    public void setStoredInventory(JSONObject storedInventory, boolean save) {
        this.storedInventory = storedInventory;
        if (save) file.save(false);
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPetName() {
        return this.petName;
    }

    public IPet getPet() {
        return this.pet;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public OwnerFile getFile() {
        return this.file;
    }

    public boolean isRenaming() {
        return this.renaming;
    }

    public void setRenaming(boolean renaming) {
        this.renaming = renaming;
    }

    public void setPetToRespawn(StorageTagCompound petToRespawn) {
        this.petToRespawn = petToRespawn;
    }
}
