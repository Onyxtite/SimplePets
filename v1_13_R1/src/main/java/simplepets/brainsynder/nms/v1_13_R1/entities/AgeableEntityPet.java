package simplepets.brainsynder.nms.v1_13_R1.entities;

import net.minecraft.server.v1_13_R1.DataWatcher;
import net.minecraft.server.v1_13_R1.DataWatcherObject;
import net.minecraft.server.v1_13_R1.EntityTypes;
import net.minecraft.server.v1_13_R1.World;
import simple.brainsynder.nbt.StorageTagCompound;
import simplepets.brainsynder.api.entity.IAgeablePet;
import simplepets.brainsynder.api.entity.IEntityControllerPet;
import simplepets.brainsynder.api.pet.IPet;
import simplepets.brainsynder.nms.v1_13_R1.utils.DataWatcherWrapper;

public abstract class AgeableEntityPet extends EntityPet implements IAgeablePet {
    private static final DataWatcherObject<Boolean> BABY;

    static {
        BABY = DataWatcher.a(AgeableEntityPet.class, DataWatcherWrapper.BOOLEAN);
    }

    public AgeableEntityPet(EntityTypes<?> type, World world) {
        super(type, world);
    }

    public AgeableEntityPet(EntityTypes<?> type, World world, IPet pet) {
        super(type,world, pet);
    }

    @Override
    protected void registerDatawatchers() {
        super.registerDatawatchers();
        this.datawatcher.register(BABY, Boolean.FALSE);
    }

    @Override
    public StorageTagCompound asCompound() {
        StorageTagCompound object = super.asCompound();
        if (!(this instanceof IEntityControllerPet))
            object.setBoolean("baby", isBaby());
        return object;
    }

    @Override
    public void applyCompound(StorageTagCompound object) {
        super.applyCompound(object);
        if (!(this instanceof IEntityControllerPet))
            if (object.hasKey("baby")) setBaby(object.getBoolean("baby"));
    }

    public boolean isBaby() {
        return this.datawatcher.get(BABY);
    }

    public void setBaby(boolean flag) {
        this.datawatcher.set(BABY, flag);
    }
}
