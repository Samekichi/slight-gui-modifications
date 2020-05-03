package me.shedaniel.slightguimodifications.mixin;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.math.api.Point;
import me.shedaniel.slightguimodifications.SlightGuiModifications;
import me.shedaniel.slightguimodifications.config.SlightGuiModificationsConfig;
import me.shedaniel.slightguimodifications.gui.MenuEntry;
import me.shedaniel.slightguimodifications.gui.MenuWidget;
import me.shedaniel.slightguimodifications.gui.TextMenuEntry;
import me.shedaniel.slightguimodifications.listener.MenuWidgetListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

@Mixin(TextFieldWidget.class)
public abstract class MixinTextFieldWidget extends AbstractButtonWidget implements Drawable, Element {
    @Shadow private boolean editable;
    
    @Shadow private int editableColor;
    
    @Shadow private int uneditableColor;
    
    @Shadow
    protected abstract boolean hasBorder();
    
    @Shadow
    public abstract boolean isVisible();
    
    @Shadow private int selectionStart;
    
    @Shadow private int selectionEnd;
    
    @Shadow
    public abstract void write(String text);
    
    @Shadow
    public abstract void setCursorToEnd();
    
    @Shadow
    public abstract void setSelectionEnd(int i);
    
    @Shadow
    protected abstract boolean isEditable();
    
    @Shadow
    public abstract String getSelectedText();
    
    @Shadow
    public abstract void setText(String text);
    
    @Shadow private boolean focusUnlocked;
    
    @Shadow
    public abstract boolean isMouseOver(double mouseX, double mouseY);
    
    public MixinTextFieldWidget(int x, int y, String text) {
        super(x, y, text);
    }
    
    @Redirect(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;hasBorder()Z", ordinal = 0))
    private boolean hasBorder(TextFieldWidget textFieldWidget) {
        boolean border = hasBorder();
        if (border && SlightGuiModifications.getConfig().textFieldModifications.enabled && SlightGuiModifications.getConfig().textFieldModifications.backgroundMode == SlightGuiModificationsConfig.TextFieldModifications.BackgroundMode.TEXTURE) {
            renderTextureBorder();
            return false;
        }
        return border;
    }
    
    @Unique
    private void renderTextureBorder() {
        MinecraftClient.getInstance().getTextureManager().bindTexture(SlightGuiModifications.TEXT_FIELD_TEXTURE);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.blendFunc(770, 771);
        // 9 Patch Texture
        
        // Four Corners
        blit(x - 1, y - 1, getBlitOffset(), 0, 0, 8, 8, 256, 256);
        blit(x + width - 7, y - 1, getBlitOffset(), 248, 0, 8, 8, 256, 256);
        blit(x - 1, y + height - 7, getBlitOffset(), 0, 248, 8, 8, 256, 256);
        blit(x + width - 7, y + height - 7, getBlitOffset(), 248, 248, 8, 8, 256, 256);
        
        // Sides
        DrawableHelper.innerBlit(x + 7, x + width - 7, y - 1, y + 7, getBlitOffset(), (8) / 256f, (248) / 256f, (0) / 256f, (8) / 256f);
        DrawableHelper.innerBlit(x + 7, x + width - 7, y + height - 7, y + height + 1, getBlitOffset(), (8) / 256f, (248) / 256f, (248) / 256f, (256) / 256f);
        DrawableHelper.innerBlit(x - 1, x + 7, y + 7, y + height - 7, getBlitOffset(), (0) / 256f, (8) / 256f, (8) / 256f, (248) / 256f);
        DrawableHelper.innerBlit(x + width - 7, x + width + 1, y + 7, y + height - 7, getBlitOffset(), (248) / 256f, (256) / 256f, (8) / 256f, (248) / 256f);
        
        // Center
        DrawableHelper.innerBlit(x + 7, x + width - 7, y + 7, y + height - 7, getBlitOffset(), (8) / 256f, (248) / 256f, (8) / 256f, (248) / 256f);
    }
    
    @ModifyArg(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;fill(IIIII)V", ordinal = 0),
               index = 4)
    private int modifyBorderColor(int color) {
        return SlightGuiModifications.getConfig().textFieldModifications.enabled ? SlightGuiModifications.getConfig().textFieldModifications.borderColor | 255 << 24 : color;
    }
    
    @ModifyArg(method = "renderButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/TextFieldWidget;fill(IIIII)V", ordinal = 1),
               index = 4)
    private int modifyBackgroundColor(int color) {
        return SlightGuiModifications.getConfig().textFieldModifications.enabled ? SlightGuiModifications.getConfig().textFieldModifications.backgroundColor | 255 << 24 : color;
    }
    
    @Inject(method = "drawSelectionHighlight", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;color4f(FFFF)V", ordinal = 0),
            cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void drawSelectionHighlight(int x1, int y1, int x2, int y2, CallbackInfo ci, Tessellator tessellator, BufferBuilder buffer) {
        if (!SlightGuiModifications.getConfig().textFieldModifications.enabled || SlightGuiModifications.getConfig().textFieldModifications.selectionMode != SlightGuiModificationsConfig.TextFieldModifications.SelectionMode.HIGHLIGHT)
            return;
        ci.cancel();
        int tmp;
        if (x1 < x2) {
            tmp = x1;
            x1 = x2;
            x2 = tmp;
        }
        
        if (y1 < y2) {
            tmp = y1;
            y1 = y2;
            y2 = tmp;
        }
        
        if (x2 > this.x + this.width) {
            x2 = this.x + this.width;
        }
        
        if (x1 > this.x + this.width) {
            x1 = this.x + this.width;
        }
        
        int color = this.editable ? this.editableColor : this.uneditableColor;
        int r = (color >> 16 & 255);
        int g = (color >> 8 & 255);
        int b = (color & 255);
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.blendFuncSeparate(770, 771, 1, 0);
        RenderSystem.shadeModel(7425);
        buffer.begin(7, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y2, getBlitOffset() + 50d).color(r, g, b, 120).next();
        buffer.vertex(x2, y2, getBlitOffset() + 50d).color(r, g, b, 120).next();
        buffer.vertex(x2, y1, getBlitOffset() + 50d).color(r, g, b, 120).next();
        buffer.vertex(x1, y1, getBlitOffset() + 50d).color(r, g, b, 120).next();
        tessellator.draw();
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableTexture();
    }
    
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void preMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (isMouseOver(mouseX, mouseY) && this.isVisible() && SlightGuiModifications.getConfig().textFieldModifications.rightClickActions && button == 1) {
            if (isEditable()) {
                if (selectionStart - selectionEnd != 0) {
                    ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, mouseY + 2), createSelectingMenu()));
                } else {
                    ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, mouseY + 2), createNonSelectingMenu()));
                }
            } else {
                if (selectionStart - selectionEnd != 0) {
                    ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, mouseY + 2), createSelectingNotEditableMenu()));
                } else {
                    ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).applyMenu(new MenuWidget(new Point(mouseX + 2, mouseY + 2), createNonSelectingNotEditableMenu()));
                }
            }
            cir.setReturnValue(true);
            boolean boolean_1 = mouseX >= (double) this.x && mouseX < (double) (this.x + this.width) && mouseY >= (double) this.y && mouseY < (double) (this.y + this.height);
            if (this.focusUnlocked) {
                this.setFocused(boolean_1);
            }
        }
    }
    
    @Unique
    private void removeSelfMenu() {
        ((MenuWidgetListener) MinecraftClient.getInstance().currentScreen).removeMenu();
    }
    
    @Unique
    private List<MenuEntry> createNonSelectingNotEditableMenu() {
        return ImmutableList.of(
                new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
                    this.setCursorToEnd();
                    this.setSelectionEnd(0);
                    removeSelfMenu();
                })
        );
    }
    
    @Unique
    private List<MenuEntry> createNonSelectingMenu() {
        return ImmutableList.of(
                new TextMenuEntry(I18n.translate("text.slightguimodifications.paste"), () -> {
                    if (this.editable) {
                        this.write(MinecraftClient.getInstance().keyboard.getClipboard());
                    }
                    removeSelfMenu();
                }),
                new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
                    this.setCursorToEnd();
                    this.setSelectionEnd(0);
                    removeSelfMenu();
                })
        );
    }
    
    @Unique
    private List<MenuEntry> createSelectingNotEditableMenu() {
        return ImmutableList.of(
                new TextMenuEntry(I18n.translate("text.slightguimodifications.copy"), () -> {
                    MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                    removeSelfMenu();
                }),
                new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
                    this.setCursorToEnd();
                    this.setSelectionEnd(0);
                    removeSelfMenu();
                }),
                new TextMenuEntry(I18n.translate("text.slightguimodifications.clearAll"), () -> {
                    this.setText("");
                    removeSelfMenu();
                })
        );
    }
    
    @Unique
    private List<MenuEntry> createSelectingMenu() {
        return ImmutableList.of(
                new TextMenuEntry(I18n.translate("text.slightguimodifications.copy"), () -> {
                    MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                    removeSelfMenu();
                }),
                new TextMenuEntry(I18n.translate("text.slightguimodifications.cut"), () -> {
                    MinecraftClient.getInstance().keyboard.setClipboard(this.getSelectedText());
                    if (this.editable) {
                        this.write("");
                    }
                    removeSelfMenu();
                }),
                new TextMenuEntry(I18n.translate("text.slightguimodifications.paste"), () -> {
                    if (this.editable) {
                        this.write(MinecraftClient.getInstance().keyboard.getClipboard());
                    }
                    removeSelfMenu();
                }),
                new TextMenuEntry(I18n.translate("text.slightguimodifications.selectAll"), () -> {
                    this.setCursorToEnd();
                    this.setSelectionEnd(0);
                    removeSelfMenu();
                }),
                new TextMenuEntry(I18n.translate("text.slightguimodifications.clearAll"), () -> {
                    this.setText("");
                    removeSelfMenu();
                })
        );
    }
}