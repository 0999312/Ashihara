package kogasastudio.ashihara.block.woodcrafts;

import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class SimpleFenceGateBlock extends FenceGateBlock
{
    public SimpleFenceGateBlock()
    {
        super
        (
            Properties.create(Material.WOOD)
            .hardnessAndResistance(2.0F, 3.0F)
            .sound(SoundType.WOOD)
        );
    }
}
