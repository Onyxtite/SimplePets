package simplepets.brainsynder.nms.v1_12_R1.entities.list;

import net.minecraft.server.v1_12_R1.World;
import simplepets.brainsynder.api.Size;
import simplepets.brainsynder.api.entity.hostile.IEntityGiantPet;
import simplepets.brainsynder.api.pet.IPet;
import simplepets.brainsynder.nms.v1_12_R1.entities.EntityPet;

@Size(width = 5.5F, length = 5.5F)
public class EntityGiantPet extends EntityPet implements IEntityGiantPet {
    public EntityGiantPet(World world, IPet pet) {
        super(world, pet);
    }
    public EntityGiantPet(World world) {
        super(world);
    }
}
