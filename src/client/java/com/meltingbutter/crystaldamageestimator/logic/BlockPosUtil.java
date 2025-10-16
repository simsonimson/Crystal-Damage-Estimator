package com.meltingbutter.crystaldamageestimator.logic;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class BlockPosUtil {
    private BlockPosUtil() {}

    public static BlockPos at(Vec3d vec) {
        return BlockPos.ofFloored(vec.x, vec.y, vec.z);
    }
}



