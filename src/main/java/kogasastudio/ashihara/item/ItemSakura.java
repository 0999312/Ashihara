package kogasastudio.ashihara.item;

import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import static kogasastudio.ashihara.Ashihara.MATERIALS;

public class ItemSakura extends Item
{
    public ItemSakura()
    {
        super
        (
            new Properties().group(MATERIALS)
            .food(new Food.Builder().hunger(1).effect(new EffectInstance(Effects.INSTANT_HEALTH, 1, 1), 1.0F).build())
        );
    }
}
