package me.shedaniel.slightguimodifications.mixin.rei;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.gui.OverlaySearchField;
import me.shedaniel.rei.gui.widget.TextFieldWidget;
import me.shedaniel.slightguimodifications.SlightGuiModifications;
import me.shedaniel.slightguimodifications.config.SlightGuiModificationsConfig;
import me.shedaniel.slightguimodifications.gui.MenuEntry;
import me.shedaniel.slightguimodifications.gui.MenuWidget;
import me.shedaniel.slightguimodifications.gui.TextMenuEntry;
import me.shedaniel.slightguimodifications.listener.MenuWidgetListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(OverlaySearchField.class)
public class MixinOverlaySearchField extends TextFieldWidget {
    public MixinOverlaySearchField(Rectangle rectangle) {super(rectangle);}
    
    @Inject(method = "renderBorder",
            at = @At(value = "HEAD", remap = false), remap = false, cancellable = true)
    private void renderBorder(MatrixStack matrices, CallbackInfo ci) {
        boolean border = hasBorder();
        if (border && SlightGuiModifications.getGuiConfig().textFieldModifications.enabled && SlightGuiModifications.getGuiConfig().textFieldModifications.backgroundMode == SlightGuiModificationsConfig.Gui.TextFieldModifications.BackgroundMode.TEXTURE) {
            renderTextureBorder(matrices);
            ci.cancel();
        }
    }
    
    @Unique
    private void renderTextureBorder(MatrixStack matrices) {
        MinecraftClient.getInstance().getTextureManager().bindTexture(SlightGuiModifications.TEXT_FIELD_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        int x = getBounds().x, y = getBounds().y, width = getBounds().width, height = getBounds().height;
        // 9 Patch Texture
        
        // Four Corners
        drawTexture(matrices, x - 1, y - 1, getZOffset(), 0, 0, 8, 8, 256, 256);
        drawTexture(matrices, x + width - 7, y - 1, getZOffset(), 248, 0, 8, 8, 256, 256);
        drawTexture(matrices, x - 1, y + height - 7, getZOffset(), 0, 248, 8, 8, 256, 256);
        drawTexture(matrices, x + width - 7, y + height - 7, getZOffset(), 248, 248, 8, 8, 256, 256);
        
        Matrix4f matrix = matrices.peek().getModel();
        // Sides
        DrawableHelper.drawTexturedQuad(matrix, x + 7, x + width - 7, y - 1, y + 7, getZOffset(), (8) / 256f, (248) / 256f, (0) / 256f, (8) / 256f);
        DrawableHelper.drawTexturedQuad(matrix, x + 7, x + width - 7, y + height - 7, y + height + 1, getZOffset(), (8) / 256f, (248) / 256f, (248) / 256f, (256) / 256f);
        DrawableHelper.drawTexturedQuad(matrix, x - 1, x + 7, y + 7, y + height - 7, getZOffset(), (0) / 256f, (8) / 256f, (8) / 256f, (248) / 256f);
        DrawableHelper.drawTexturedQuad(matrix, x + width - 7, x + width + 1, y + 7, y + height - 7, getZOffset(), (248) / 256f, (256) / 256f, (8) / 256f, (248) / 256f);
        
        // Center
        DrawableHelper.drawTexturedQuad(matrix, x + 7, x + width - 7, y + 7, y + height - 7, getZOffset(), (8) / 256f, (248) / 256f, (8) / 256f, (248) / 256f);
    }
    
    @ModifyArg(method = "renderBorder", at = @At(value = "INVOKE", target = "Lme/shedaniel/rei/gui/OverlaySearchField;fill(Lnet/minecraft/client/util/math/MatrixStack;IIIII)V", ordinal = 2), index = 4)
    private int modifyBackgroundColor(int color) {return SlightGuiModifications.getGuiConfig().textFieldModifications.enabled ? SlightGuiModifications.getGuiConfig().textFieldModifications.backgroundColor | 255 << 24 : color;}
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void preMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (getBounds().contains(mouseX, mouseY) && this.isVisible() && SlightGuiModifications.getGuiConfig().textFieldModifications.rightClickActions && button == 1) {
            if (editable) if (selectionStart - selectionEnd != 0)
                ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, getBounds().y - 2), createSelectingMenu()));
            else
                ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, getBounds().y - 2), createNonSelectingMenu()));
            else if (selectionStart - selectionEnd != 0)
                ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, getBounds().y - 2), createSelectingNotEditableMenu()));
            else
                ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, getBounds().y - 2), createNonSelectingNotEditableMenu()));
            cir.setReturnValue(true);
            boolean boolean_1 = mouseX >= (double) this.getBounds().x && mouseX < (double) (this.getBounds().x + this.getBounds().width) && mouseY >= (double) this.getBounds().y && mouseY < (double) (this.getBounds().y + this.getBounds().height);
            this.setFocused(boolean_1);
        }
    }
    
    @Unique
    private void removeSelfMenu() {((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).removeMenu();}
    
    @Unique
    private List<MenuEntry> createNonSelectingNotEditableMenu() {
        return ImmutableList.of(new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
            this.setCursorToEnd();
            this.setSelectionEnd(0);
            removeSelfMenu();
        }));
    }
    
    @Unique
    private List<MenuEntry> createNonSelectingMenu() {
        return ImmutableList.of(new TextMenuEntry(I18n.translate("text.slightguimodifications.paste"), () -> {
            if (this.editable) this.addText(MinecraftClient.getInstance().keyboard.getClipboard());
            removeSelfMenu();
        }), new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
            this.setCursorToEnd();
            this.setSelectionEnd(0);
            removeSelfMenu();
        }), new TextMenuEntry(I18n.translate("text.slightguimodifications.clearAll"), () -> {
            this.setText("");
            removeSelfMenu();
        }));
    }
    
    @Unique
    private List<MenuEntry> createSelectingNotEditableMenu() {
        return ImmutableList.of(new TextMenuEntry(I18n.translate("text.slightguimodifications.copy"), () -> {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            removeSelfMenu();
        }), new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
            this.setCursorToEnd();
            this.setSelectionEnd(0);
            removeSelfMenu();
        }));
    }
    
    @Unique
    private List<MenuEntry> createSelectingMenu() {
        return ImmutableList.of(new TextMenuEntry(I18n.translate("text.slightguimodifications.copy"), () -> {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            removeSelfMenu();
        }), new TextMenuEntry(I18n.translate("text.slightguimodifications.cut"), () -> {
            MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
            if (this.editable) this.addText("");
            removeSelfMenu();
        }), new TextMenuEntry(I18n.translate("text.slightguimodifications.paste"), () -> {
            if (this.editable) this.addText(MinecraftClient.getInstance().keyboard.getClipboard());
            removeSelfMenu();
        }), new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
            this.setCursorToEnd();
            this.setSelectionEnd(0);
            removeSelfMenu();
        }), new TextMenuEntry(I18n.translate("text.slightguimodifications.clearAll"), () -> {
            this.setText("");
            removeSelfMenu();
        }));
    }
}
