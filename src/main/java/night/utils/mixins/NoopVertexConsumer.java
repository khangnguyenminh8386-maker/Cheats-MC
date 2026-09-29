/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 */
package night.utils.mixins;

import com.mojang.blaze3d.vertex.VertexConsumer;

public enum NoopVertexConsumer implements VertexConsumer
{
    INSTANCE;


    public VertexConsumer addVertex(float x, float y, float z) {
        return this;
    }

    public VertexConsumer setColor(int r, int g, int b, int a) {
        return this;
    }

    public VertexConsumer setColor(int color) {
        return this;
    }

    public VertexConsumer setLineWidth(float width) {
        return this;
    }

    public VertexConsumer setUv(float u, float v) {
        return this;
    }

    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    public VertexConsumer setNormal(float x, float y, float z) {
        return this;
    }
}

