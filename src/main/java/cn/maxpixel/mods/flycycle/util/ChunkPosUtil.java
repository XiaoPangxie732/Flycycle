package cn.maxpixel.mods.flycycle.util;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ChunkPosUtil {
    public static Stream<Chunk> rangeClosed(int minX, int minZ, int maxX, int maxZ, World level) {
        int length = Math.abs(minX - maxX) + 1;
        int width = Math.abs(minZ - maxZ) + 1;
        final int directionX = minX < maxX ? 1 : -1;
        final int directionZ = minZ < maxZ ? 1 : -1;
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<Chunk>((long) length * width, Spliterator.SIZED) {
            private int x = minX;
            private int z = minZ;

            @Override
            public boolean tryAdvance(Consumer<? super Chunk> action) {
                if(x == maxX) {
                    if(z == maxZ) return false;

                    x = minX;
                    z += directionZ;
                } else {
                    x += directionX;
                }

                action.accept(level.getChunkSource().getChunkNow(x, z));
                return true;
            }
        }, false);
    }
}