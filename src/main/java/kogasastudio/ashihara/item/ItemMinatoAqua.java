package kogasastudio.ashihara.item;

import kogasastudio.ashihara.block.BlockRegistryHandler;
import kogasastudio.ashihara.block.trees.CherryBlossomTree;
import net.minecraft.block.trees.Tree;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Objects;
import java.util.Random;

public class ItemMinatoAqua extends Item
{
    public ItemMinatoAqua()
    {
        super
        (
            new Properties()
            .group(ItemGroup.MATERIALS)
            .food(new Food.Builder().hunger(8).build())
        );
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context)
    {
        World world = context.getWorld();
        PlayerEntity playerIn = context.getPlayer();
        BlockPos pos = context.getPos();
        ItemStack item = context.getItem();
        Direction direction = context.getFace();
        Tree tree = new CherryBlossomTree();
        Random rand = context.getWorld().getRandom();

        if (world.getBlockState(pos).matchesBlock(BlockRegistryHandler.PAIL.get())) return ActionResultType.SUCCESS;

        if (!item.isEmpty() && Objects.requireNonNull(playerIn).canPlayerEdit(pos.offset(direction), direction, item) && !world.isRemote())
        {
            ServerWorld worldIn = (ServerWorld)world;
            tree.attemptGrowTree(worldIn, worldIn.getChunkProvider().getChunkGenerator(), pos, BlockRegistryHandler.CHERRY_LOG.get().getDefaultState(), rand);
            return ActionResultType.SUCCESS;
        }
        else return ActionResultType.PASS;
    }
}
