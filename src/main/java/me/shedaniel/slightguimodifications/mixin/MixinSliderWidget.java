package me.shedaniel.slightguimodifications.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.slightguimodifications.SlightGuiModifications;
import me.shedaniel.slightguimodifications.config.SlightGuiModificationsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SliderWidget.class)
public abstract class MixinSliderWidget extends AbstractButtonWidget {
    public MixinSliderWidget(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }
    
    @Shadow
    protected abstract void setValue(double mouseX);
    
    @Shadow protected double value;
    
    @Inject(method = "setValueFromMouse", at = @At("HEAD"), cancellable = true)
    private void setValueFromMouse(double mouseX, CallbackInfo ci) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            this.setValue((mouseX - (double) (this.x + grabberWidth / 2)) / (double) (this.width - grabberWidth));
            ci.cancel();
        }
    }
    
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void preKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            this.width += 8;
            this.width -= grabberWidth;
        }
    }
    
    @Inject(method = "keyPressed", at = @At("RETURN"), cancellable = true)
    private void postKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            this.width += grabberWidth;
            this.width -= 8;
        }
    }
    
    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    private void renderBg(MatrixStack matrices, MinecraftClient client, int mouseX, int mouseY, CallbackInfo ci) {
        SlightGuiModificationsConfig.Gui config = SlightGuiModifications.getGuiConfig();
        if (config.sliderModifications.enabled) {
            int grabberWidth = config.sliderModifications.grabberWidth;
            if (config.sliderModifications.customBackgroundTexture) {
                client.getTextureManager().bindTexture(new Identifier("slightguimodifications:textures/gui/slider" + (this.isHovered() ? "_hovered.png" : ".png")));
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int grabberX = this.x + (int) (this.value * (double) (this.width - grabberWidth));
                drawTexturedQuad(matrices.peek().getModel(), grabberX, grabberX + grabberWidth, y, y + 20, getZOffset(), 0F, 1F, 0F, 1F);
                ci.cancel();
            } else if (grabberWidth != 8) {
                client.getTextureManager().bindTexture(WIDGETS_LOCATION);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int i = (this.isHovered() ? 2 : 1) * 20;
                this.drawTexture(matrices, this.x + (int) (this.value * (double) (this.width - grabberWidth)), this.y, 0, 46 + i, grabberWidth / 2, 20);
                this.drawTexture(matrices, this.x + (int) (this.value * (double) (this.width - grabberWidth)) + (grabberWidth / 2), this.y, 200 - (grabberWidth / 2), 46 + i, grabberWidth / 2, 20);
                ci.cancel();
            }
        }
    }
}
