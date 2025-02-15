package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.swimmingtuna.lotm.util.BeyonderUtil.spawnDoorTeleportationOnly;

public class CreateDoor extends SimpleAbilityItem {
    public CreateDoor(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 9, 70, 200);
    }

    @Override
    public InteractionResult useAbilityOnBlock(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos();
        BlockPos posRelativeTo = context.getClickedPos().relative(context.getClickedFace());
        Direction direction = context.getClickedFace().getOpposite();
        if (!checkAll(player)){
            return InteractionResult.FAIL;
        }
        if(!canCreateDoor(player, targetPos, posRelativeTo)){
            return InteractionResult.FAIL;
        }
        createDoor(player, level, targetPos, posRelativeTo, direction);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand){
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if(!checkAll(player)){
            return InteractionResult.FAIL;
        }
        checkAmount(player, holder);
        return InteractionResult.SUCCESS;
    }

    public static boolean canCreateDoor(Player player, BlockPos pos, BlockPos posRelativeTo){
        Level level = player.level();
        if(!level.isEmptyBlock(pos) && !level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() + 1, pos.getZ()))){
            if(level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() + 1, posRelativeTo.getZ()))){
                if(!level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() - 1, posRelativeTo.getZ()))){
                    return true;
                }
            }
        }
        if(!level.isEmptyBlock(pos) && !level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() - 1, pos.getZ()))){
            if(level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() - 1, posRelativeTo.getZ()))){
                if(!level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() - 2, posRelativeTo.getZ()))){
                    return true;
                }
            }
        }
        return false;
    }

    public static void createDoor(Player player, Level level, BlockPos pos, BlockPos posRelativeTo, Direction direction){
        if(!player.level().isClientSide){
            int cordModifier;
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            Boolean canCreate = true;
            int sequence = BeyonderUtil.getSequence(player);
            int yModifier = 0;
            int maxBlocks = 100 - (97 * sequence / 9);
            int loop = 0;
            if(direction == Direction.WEST || direction == Direction.NORTH){
                cordModifier = -1;
            }else{
                cordModifier = 1;
            }

            if(!player.isShiftKeyDown()){
                if(!level.isEmptyBlock(pos) && !level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() + 1, pos.getZ()))){
                    if(level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() + 1, posRelativeTo.getZ()))){
                        yModifier = 0;
                    }
                }
                if(!level.isEmptyBlock(pos) && !level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() - 1, pos.getZ()))){
                    if(level.isEmptyBlock(posRelativeTo) && level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() - 1, posRelativeTo.getZ()))){
                        yModifier = -1;
                    }
                }
            }

            if(!player.isShiftKeyDown()){
                if(level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() + yModifier, posRelativeTo.getZ()))
                        && level.isEmptyBlock(BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY() + yModifier + 1, posRelativeTo.getZ()))){
                    if(!level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() + yModifier, pos.getZ()))
                    && !level.isEmptyBlock(BlockPos.containing(pos.getX(), pos.getY() + yModifier + 1, pos.getZ()))){
                        if(direction == Direction.SOUTH || direction == Direction.NORTH){
                            while(maxBlocks > 0){
                                loop ++;
                                if(level.isEmptyBlock(BlockPos.containing(x, y + yModifier, z + loop * cordModifier)) && canCreate){
                                    if(level.isEmptyBlock(BlockPos.containing(x, (y + yModifier) + 1, z + loop * cordModifier))){
                                        maxBlocks = 0;
                                        canCreate = false;
                                        spawnDoorTeleportationOnly(level, BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY()  + yModifier, posRelativeTo.getZ()), (x + 0.5), (y + yModifier), (z + loop * cordModifier + (0.5 * cordModifier - 0.2) * cordModifier), player, direction, 90, player);
                                    }
                                }else{
                                    maxBlocks --;
                                }
                                if(maxBlocks == 0 && canCreate){
                                    canCreate = false;
                                    spawnDoorTeleportationOnly(level, BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY()  + yModifier, posRelativeTo.getZ()), (x + 0.5), (y + yModifier), (z + loop * cordModifier + (0.5 * cordModifier - 0.2) * cordModifier), player, direction, 90, player);
                                }
                            }
                        }else {
                            while(maxBlocks > 0){
                                loop ++;
                                if(level.isEmptyBlock(BlockPos.containing(x + loop * cordModifier, y + yModifier, z)) && canCreate){
                                    if(level.isEmptyBlock(BlockPos.containing(x + loop * cordModifier, (y + yModifier) + 1, z))){
                                        maxBlocks = 0;
                                        canCreate = false;
                                        spawnDoorTeleportationOnly(level, BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY()  + yModifier, posRelativeTo.getZ()), (x + loop * cordModifier + (0.5 * cordModifier - 0.2) * cordModifier), (y + yModifier), (z + 0.5), player, direction,90, player);
                                    }
                                }else{
                                    maxBlocks --;
                                }
                                if(maxBlocks == 0 && canCreate){
                                    canCreate = false;
                                    spawnDoorTeleportationOnly(level, BlockPos.containing(posRelativeTo.getX(), posRelativeTo.getY()  + yModifier, posRelativeTo.getZ()), (x + loop * cordModifier + (0.5 * cordModifier - 0.2) * cordModifier), (y + yModifier), (z + 0.5), player, direction,90, player);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void checkAmount(Player player, BeyonderHolder holder){
        if(player.isShiftKeyDown()){
            player.displayClientMessage(Component.literal("Amount of blocks that can be opened: ").withStyle(ChatFormatting.WHITE).append(Component.literal(String.valueOf(100 - (97 * holder.getCurrentSequence() / 9))).withStyle(ChatFormatting.BLUE)), true);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, creates a conceptual door that can be used to pass trough a few blocks"));
        tooltipComponents.add(Component.literal("Use while sneaking to see how many blocks can be passed."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("70").withStyle(ChatFormatting.YELLOW)));
        Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.BLUE);
    }
}