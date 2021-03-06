package simplepets.brainsynder.nms.v1_13_R1.entities.list;

import net.minecraft.server.v1_13_R1.World;
import simplepets.brainsynder.api.Size;
import simplepets.brainsynder.api.entity.hostile.IEntityIllusionerPet;
import simplepets.brainsynder.api.pet.IPet;
import simplepets.brainsynder.nms.v1_13_R1.entities.branch.EntityIllagerWizardPet;
import simplepets.brainsynder.nms.v1_13_R1.registry.Types;

@Size(width = 0.6F, length = 1.95F)
public class EntityIllusionerPet extends EntityIllagerWizardPet implements IEntityIllusionerPet {
    public EntityIllusionerPet(World world) {
        super(Types.ILLUSIONER, world);
    }
    public EntityIllusionerPet(World world, IPet pet) {
        super(Types.ILLUSIONER, world, pet);
    }
}
