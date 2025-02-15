package kogasastudio.ashihara.interaction.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import kogasastudio.ashihara.Ashihara;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MillRecipe implements IRecipe<RecipeWrapper>
{
    public static IRecipeType<MillRecipe> TYPE = IRecipeType.register(Ashihara.MODID + ":mill");
    public static final Serializer SERIALIZER = new Serializer();

    private final NonNullList<Ingredient> input;
    private final Map<Ingredient, Byte> inputCosts;
    private final NonNullList<ItemStack> output;

    public ResourceLocation id;
    public String group;
    public byte round;
    public int roundTicks;
    public float exp;

    public MillRecipe(ResourceLocation recipeId, String groupId, NonNullList<Ingredient> inputIn, Map<Ingredient, Byte> inputCostsIn, NonNullList<ItemStack> outputIn, byte roundIn, int roundTicksIn, float expIn)
    {
        this.id = recipeId;
        this.group = groupId;

        this.input = inputIn;
        this.inputCosts = inputCostsIn;
        this.output = outputIn;
        this.round = roundIn;
        this.roundTicks = roundTicksIn;
        this.exp = expIn;
    }

    public byte getCosts(Ingredient ingredient)
    {
        return this.inputCosts.get(ingredient);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {return this.input;}

    @Override
    public String getGroup() {return this.group;}

    @Override
    public ResourceLocation getId() {return this.id;}

    @Override
    public boolean matches(RecipeWrapper inv, World worldIn)
    {
        ArrayList<ItemStack> inputs = new ArrayList<>();
        boolean costMatches = false;

        for (int j = 0; j < 4; ++j)
        {
            ItemStack itemstack = inv.getStackInSlot(j);
            if (!itemstack.isEmpty()) {inputs.add(itemstack);}
        }
        for (Ingredient ingredient : this.input)
        {
            for (ItemStack stack : inputs)
            {
                if (ingredient.test(stack) && stack.getCount() >= this.inputCosts.get(ingredient)) {costMatches = true;break;}
                else costMatches = false;
            }
            if (!costMatches) break;
        }
        return RecipeMatcher.findMatches(inputs, this.input) != null && costMatches;
    }

    //这个没啥用
    @Override
    public ItemStack getCraftingResult(RecipeWrapper inv) {return this.output.get(0);}

    //这个也没啥用
    @Override
    public ItemStack getRecipeOutput() {return this.output.get(0).copy();}

    //出结果的
    public NonNullList<ItemStack> getCraftingResult() {return this.output;}

    @Override
    public boolean canFit(int width, int height) {return width * height >= this.input.size();}

    @Override
    public IRecipeType<?> getType() {return TYPE;}

    @Override
    public IRecipeSerializer<?> getSerializer() {return SERIALIZER;}

    private static class Serializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<MillRecipe>
    {
        Serializer() {this.setRegistryName(new ResourceLocation(Ashihara.MODID, "mill"));}

        @Override
        public MillRecipe read(ResourceLocation recipeId, JsonObject json)
        {
            final String groupIn = JSONUtils.getString(json, "group", "");
            final NonNullList<Ingredient> inputIn = readIngredients(JSONUtils.getJsonArray(json, "ingredients"));
            if (inputIn.isEmpty()) {throw new JsonParseException("No ingredients for mill recipe");}
            else if (inputIn.size() > 4) {throw new JsonParseException("Too many ingredients for mill recipe! The max is 4");}
            else
            {
                final Map<Ingredient, Byte> costsIn = serializeCosts(inputIn, JSONUtils.getJsonArray(json, "costs"));
                final NonNullList<ItemStack> outputIn = readOutput(JSONUtils.getJsonArray(json, "result"));
                final byte roundIn = JSONUtils.getByte(json, "rounds", (byte) 1);
                final int roundTicksIn = JSONUtils.getInt(json, "roundTicks", 100);
                final float expIn = JSONUtils.getFloat(json, "experience", 20.0f);
                return new MillRecipe(recipeId, groupIn, inputIn, costsIn, outputIn, roundIn, roundTicksIn, expIn);
            }
        }

        private static NonNullList<Ingredient> readIngredients(JsonArray ingredientArray)
        {
            NonNullList<Ingredient> nonnulllist = NonNullList.create();

            for (int i = 0; i < ingredientArray.size(); ++i)
            {
                Ingredient ingredient = Ingredient.deserialize(ingredientArray.get(i));
                if (!ingredient.hasNoMatchingItems()) {nonnulllist.add(ingredient);}
            }

            return nonnulllist;
        }

        private static Map<Ingredient, Byte> serializeCosts(NonNullList<Ingredient> ingredients, JsonArray costArray)
        {
            if (costArray.size() > ingredients.size()) {throw new JsonParseException("Cost counts does not match ingredient counts!");}
            Map<Ingredient, Byte> map = new HashMap<>(ingredients.size());
            for (int i = 0; i < ingredients.size(); i += 1)
            {
                byte b = costArray.get(i).getAsByte();
                if (b > 64) {throw new JsonParseException("Costing over 64!");}
                map.put(ingredients.get(i), costArray.get(i).getAsByte());
            }
            return map;
        }

        private static NonNullList<ItemStack> readOutput(JsonArray itemStackArray)
        {
            NonNullList<ItemStack> nonnulllist = NonNullList.create();

            for (int i = 0; i < itemStackArray.size(); ++i)
            {
                ItemStack stack = CraftingHelper.getItemStack(itemStackArray.get(i).getAsJsonObject(), true);
                if (!stack.isEmpty()) {nonnulllist.add(stack);}
            }

            return nonnulllist;
        }

        @Override
        public MillRecipe read(ResourceLocation recipeId, PacketBuffer buffer)
        {
            String groupIn = buffer.readString(32767);
            Map<Ingredient, Byte> costsIn = new HashMap<>(4);
            NonNullList<Ingredient> inputItemsIn = NonNullList.withSize(4, Ingredient.EMPTY);
            NonNullList<ItemStack> outputItemsIn = NonNullList.withSize(4, ItemStack.EMPTY);

            for (int j = 0; j < inputItemsIn.size(); ++j)
            {
                inputItemsIn.set(j, Ingredient.read(buffer));
                costsIn.put(Ingredient.read(buffer), buffer.readByteArray()[j]);
            }
            CompoundNBT nbt = buffer.readCompoundTag();
            if (nbt != null) ItemStackHelper.loadAllItems(nbt, outputItemsIn);

            byte roundIn = buffer.readByte();
            int roundTicksIn = buffer.readVarInt();
            float expIn = buffer.readFloat();
            return new MillRecipe(recipeId, groupIn, inputItemsIn, costsIn, outputItemsIn, roundIn, roundTicksIn, expIn);
        }

        @Override
        public void write(PacketBuffer buffer, MillRecipe recipe)
        {
            buffer.writeString(recipe.group);
            byte[] bytes = new byte[recipe.input.size()];
            int i = 0;

            for (Ingredient ingredient : recipe.input)
            {
                ingredient.write(buffer);
                bytes[i] = recipe.inputCosts.get(ingredient);
                i += 1;
            }
            CompoundNBT nbt = buffer.readCompoundTag();
            if (nbt != null) ItemStackHelper.saveAllItems(nbt, recipe.output);

            buffer.writeByte(recipe.round);
            buffer.writeVarInt(recipe.roundTicks);
            buffer.writeFloat(recipe.exp);

            buffer.writeBytes(bytes);
        }
    }
}
