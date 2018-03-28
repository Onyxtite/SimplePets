package simplepets.brainsynder.menu.menuItems.sizes;

import org.bukkit.Material;
import simple.brainsynder.api.ItemMaker;
import simplepets.brainsynder.api.entity.IEntityPet;
import simplepets.brainsynder.api.entity.hostile.IEntityMagmaCubePet;
import simplepets.brainsynder.api.entity.hostile.IEntitySlimePet;
import simplepets.brainsynder.menu.menuItems.base.MenuItemAbstract;
import simplepets.brainsynder.pet.PetDefault;
import simplepets.brainsynder.utils.ItemBuilder;

public class Size2 extends MenuItemAbstract {
    private ItemBuilder item = type.getDataItemByName("size2");

    public Size2(PetDefault type, IEntityPet entityPet) {
        super(type, entityPet);
    }
    public Size2(PetDefault type) {
        super(type);
    }

    @Override
    public ItemBuilder getItem() {
        return item;
    }

    @Override
    public ItemBuilder getDefaultItem() {
        return new ItemBuilder(Material.SLIME_BLOCK).withName("&6&lSize: &e2");
    }

    @Override
    public void onLeftClick() {
        if (entityPet instanceof IEntitySlimePet) {
            IEntitySlimePet slime = (IEntitySlimePet) entityPet;
            slime.setSize(2);
        }
        if (entityPet instanceof IEntityMagmaCubePet) {
            IEntityMagmaCubePet slime = (IEntityMagmaCubePet) entityPet;
            slime.setSize(2);
        }

    }
}