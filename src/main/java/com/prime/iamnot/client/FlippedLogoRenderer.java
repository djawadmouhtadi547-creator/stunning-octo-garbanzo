package com.prime.iamnot.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;

/** Draws the Minecraft logo upside down. */
public class FlippedLogoRenderer extends LogoRenderer {

    public FlippedLogoRenderer(boolean keepLogoThroughFade) {
        super(keepLogoThroughFade);
    }

    @Override
    public void renderLogo(GuiGraphics graphics, int width, float alpha, int height) {
        PoseStack pose = graphics.pose();
        float cx = width / 2.0F;
        float cy = height + 26.0F;
        pose.pushPose();
        pose.translate(cx, cy, 0.0F);
        pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
        pose.translate(-cx, -cy, 0.0F);
        super.renderLogo(graphics, width, alpha, height);
        pose.popPose();
    }
}
