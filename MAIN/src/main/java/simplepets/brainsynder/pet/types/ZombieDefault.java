package simplepets.brainsynder.pet.types;

import simple.brainsynder.sound.SoundMaker;
import simplepets.brainsynder.PetCore;
import simplepets.brainsynder.api.entity.IEntityPet;
import simplepets.brainsynder.api.entity.hostile.IEntityZombiePet;
import simplepets.brainsynder.pet.PetData;
import simplepets.brainsynder.pet.PetDefault;
import simplepets.brainsynder.utils.ItemBuilder;
import simplepets.brainsynder.utils.Utilities;
import simplepets.brainsynder.wrapper.EntityWrapper;

public class ZombieDefault extends PetDefault {
    public ZombieDefault(PetCore plugin) {
        super(plugin, "zombie", SoundMaker.ENTITY_ZOMBIE_AMBIENT, EntityWrapper.ZOMBIE);
    }

    @Override
    public ItemBuilder getDefaultItem() {
        return Utilities.getSkullMaterial(Utilities.SkullType.ZOMBIE).toBuilder(1).withName("&f&lZombie Pet");
    }

    @Override
    public Class<? extends IEntityPet> getEntityClass() {
        return IEntityZombiePet.class;
    }

    @Override
    public PetData getPetData() {
        return PetData.ZOMBIE;
    }
}
