/*
 * FirstAid
 * Copyright (C) 2017-2024
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.healing.ItemHealing;
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.client.util.EventCalendar;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.network.MessageApplyHealingItem;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuiHealthScreen extends Screen {
    public static final int xSize = 256;
    public static final int ySize = 137;
    public static final ItemStack BED_ITEMSTACK = new ItemStack(Items.RED_BED);
    private static final DecimalFormat FORMAT = new DecimalFormat("##.#");

    public static GuiHealthScreen INSTANCE;
    public static boolean isOpen = false;
    private static int funTicks = 0; // mod 500

    private final AbstractPlayerDamageModel damageModel;
    private final List<GuiHoldButton> holdButtons = new ArrayList<>();
    private final boolean disableButtons;

    public int guiLeft;
    public int guiTop;
    public AbstractButton cancelButton;
    private AbstractButton head, leftArm, leftLeg, leftFoot, body, rightArm, rightLeg, rightFoot;
    private InteractionHand activeHand;

    public GuiHealthScreen(AbstractPlayerDamageModel damageModel) {
        super(Component.translatable("firstaid.gui.healthscreen"));
        this.damageModel = damageModel;
        disableButtons = true;
    }

    public GuiHealthScreen(AbstractPlayerDamageModel damageModel, InteractionHand activeHand) {
        super(Component.translatable("firstaid.gui.healthscreen"));
        this.damageModel = damageModel;
        this.activeHand = activeHand;
        disableButtons = false;
    }

    public static void tickFun() {
        funTicks++;
        if (funTicks > 500) {
            funTicks = (int) (Math.random() * 100);
        }
    }

    /**
     * Hard override: 1.20.2+ renders a gaussian blur in renderBackground. This screen must stay
     * crisp, so the method is neutralised even if something else calls it.
     */
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // intentionally empty - no blur, no dimming from the vanilla path
    }
    @Override
    public void init() {
        isOpen = true;
        this.guiLeft = (this.width - xSize) / 2;
        this.guiTop = (this.height - ySize) / 2;

        head = new GuiHoldButton(1, this.guiLeft + 4, this.guiTop + 8, 52, 20, Component.translatable("firstaid.gui.head"), false);
        addRenderableWidget(head);

        leftArm = new GuiHoldButton(2, this.guiLeft + 4, this.guiTop + 33, 52, 20, Component.translatable("firstaid.gui.left_arm"), false);
        addRenderableWidget(leftArm);
        leftLeg = new GuiHoldButton(3, this.guiLeft + 4, this.guiTop + 58, 52, 20, Component.translatable("firstaid.gui.left_leg"), false);
        addRenderableWidget(leftLeg);
        leftFoot = new GuiHoldButton(4, this.guiLeft + 4, this.guiTop + 83, 52, 20, Component.translatable("firstaid.gui.left_foot"), false);
        addRenderableWidget(leftFoot);

        body = new GuiHoldButton(5, this.guiLeft + 199, this.guiTop + 8, 52, 20, Component.translatable("firstaid.gui.body"), true);
        addRenderableWidget(body);

        rightArm = new GuiHoldButton(6, this.guiLeft + 199, this.guiTop + 33, 52, 20, Component.translatable("firstaid.gui.right_arm"), true);
        addRenderableWidget(rightArm);
        rightLeg = new GuiHoldButton(7, this.guiLeft + 199, this.guiTop + 58, 52, 20, Component.translatable("firstaid.gui.right_leg"), true);
        addRenderableWidget(rightLeg);
        rightFoot = new GuiHoldButton(8, this.guiLeft + 199, this.guiTop + 83, 52, 20, Component.translatable("firstaid.gui.right_foot"), true);
        addRenderableWidget(rightFoot);

        if (disableButtons) {
            head.active = false;
            leftArm.active = false;
            leftLeg.active = false;
            leftFoot.active = false;
            body.active = false;
            rightArm.active = false;
            rightLeg.active = false;
            rightFoot.active = false;
        }

        cancelButton = Button.builder(Component.translatable(disableButtons ? "gui.done" : "gui.cancel"), button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 50, 200, 20)
                .build();
        addRenderableWidget(cancelButton);

        if (this.minecraft.getDebugOverlay().showDebugScreen()) {
            Button refresh = Button.builder(Component.literal("resync"), button -> {
                FirstAidNetwork.sendToServer(new MessageClientRequest(MessageClientRequest.Type.REQUEST_REFRESH));
                FirstAid.LOGGER.info("Requesting refresh");
                minecraft.player.displayClientMessage(Component.literal("Re-downloading health data from server..."), true);
                onClose();
            }).bounds(this.guiLeft + 218, this.guiTop + 115, 36, 20).build();
            addRenderableWidget(refresh);
        }

        holdButtons.clear();
        for (AbstractWidget button : this.getButtons()) {
            if (button instanceof GuiHoldButton holdButton) {
                int holdTime = Integer.MAX_VALUE;
                if (activeHand != null) {
                    ItemStack itemInHand = minecraft.player.getItemInHand(activeHand);
                    if (itemInHand.getItem() instanceof ItemHealing itemHealing) {
                        holdTime = itemHealing.getApplyTime(itemInHand);
                    }
                }
                holdButton.setup(holdTime);
                holdButtons.add(holdButton);
            }
        }

        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        //Setup background - no blur here, the wound screen keeps the world readable.
        //(1.20.2+ made renderBackground apply the new blur effect, so use the plain dim instead.)
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.fillGradient(this.guiLeft, this.guiTop, this.guiLeft + xSize, this.guiTop + ySize, -16777216, -16777216);
        guiGraphics.blit(HealthRenderUtils.SHOW_WOUNDS_LOCATION, this.guiLeft, this.guiTop, 0, 0, xSize, ySize);

        //Player
        int modelX = this.guiLeft + xSize / 2;
        int modelY = this.guiTop + 56;
        // 1.21.1's renderEntityInInventoryFollowsMouse derives the look angle from the centre of the
        // (x1,y1)-(x2,y2) box and expects absolute mouse coordinates. The old 1.20.1 call passed
        // offsets instead, which made the head ignore the cursor.
        float entityLookX = mouseX;
        float entityLookY = mouseY;
        if (EventCalendar.isGuiFun()) {
            if (EventCalendar.isHalloween()) {
                // Make it spoooky
                if ((funTicks > 250 && funTicks < 270) || (funTicks > 330 && funTicks < 340)) {
                    entityLookX = modelX;
                    entityLookY = modelY;
                } else if ((funTicks > 480 && funTicks < 500) || (funTicks > 340 && funTicks < 350 )) {
                    entityLookX = 2 * modelX - mouseX;
                    entityLookY = 2 * modelY - mouseY;
                }
            } else {
                entityLookX = 2 * modelX - mouseX;
                entityLookY = 2 * modelY - mouseY;
            }
        }
        forceNearestSkinFilter();
        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, modelX - 45, modelY - 45, modelX + 45, modelY + 45, 45, 0.0F, entityLookX, entityLookY, minecraft.player);

        //Button
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        //Text info
        int morphineTicks = damageModel.getMorphineTicks();
        if (morphineTicks > 0)
            guiGraphics.drawCenteredString(this.minecraft.font, I18n.get("firstaid.gui.morphine_left", StringUtil.formatTickDuration(morphineTicks, 20.0F)), this.guiLeft + (xSize / 2), this.guiTop + ySize - (this.activeHand == null ? 21 : 29), 0xFFFFFFFF);
        if (this.activeHand != null)
            guiGraphics.drawCenteredString(this.minecraft.font, I18n.get("firstaid.gui.apply_hint"), this.guiLeft + (xSize / 2), this.guiTop + ySize - (morphineTicks == 0 ? 21 : 11), 0xFFFFFFFF);

        //Health
        drawHealth(guiGraphics, damageModel.HEAD, false, 14);
        drawHealth(guiGraphics, damageModel.LEFT_ARM, false, 39);
        drawHealth(guiGraphics, damageModel.LEFT_LEG, false, 64);
        drawHealth(guiGraphics, damageModel.LEFT_FOOT, false, 89);
        drawHealth(guiGraphics, damageModel.BODY, true, 14);
        drawHealth(guiGraphics, damageModel.RIGHT_ARM, true, 39);
        drawHealth(guiGraphics, damageModel.RIGHT_LEG, true, 64);
        drawHealth(guiGraphics, damageModel.RIGHT_FOOT, true, 89);

        //Tooltip
        guiGraphics.pose().pushPose();
        tooltipButton(guiGraphics, head, damageModel.HEAD, mouseX, mouseY);
        tooltipButton(guiGraphics, leftArm, damageModel.LEFT_ARM, mouseX, mouseY);
        tooltipButton(guiGraphics, leftLeg, damageModel.LEFT_LEG, mouseX, mouseY);
        tooltipButton(guiGraphics, leftFoot, damageModel.LEFT_FOOT, mouseX, mouseY);
        tooltipButton(guiGraphics, body, damageModel.BODY, mouseX, mouseY);
        tooltipButton(guiGraphics, rightArm, damageModel.RIGHT_ARM, mouseX, mouseY);
        tooltipButton(guiGraphics, rightLeg, damageModel.RIGHT_LEG, mouseX, mouseY);
        tooltipButton(guiGraphics, rightFoot, damageModel.RIGHT_FOOT, mouseX, mouseY);
        guiGraphics.pose().popPose();

        //Sleep info setup
        double sleepHealing = FirstAidConfig.SERVER.sleepHealPercentage.get();
        int bedX = guiLeft + 3;
        int bedY = (guiTop + ySize) - 19;

        //Sleep info icon
        org.joml.Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        guiGraphics.renderItem(BED_ITEMSTACK, bedX, bedY);
        modelViewStack.popMatrix();

        //Sleep info tooltip
        if (mouseX >= bedX && mouseY >= bedY && mouseX < bedX + 16 && mouseY < bedY + 16) {
            Component s = sleepHealing == 0D ? Component.translatable("firstaid.gui.no_sleep_heal") : Component.translatable("firstaid.gui.sleep_heal_amount", FORMAT.format(sleepHealing * 100));
            guiGraphics.renderTooltip(font, s, mouseX, mouseY);
        }

        holdButtonMouseCallback(guiGraphics); //callback: check if buttons are finish
    }

    private void tooltipButton(GuiGraphics guiGraphics, AbstractButton button, AbstractDamageablePart part, int mouseX, int mouseY) {
        boolean enabled = part.activeHealer == null;
        if (!enabled && button.isHoveredOrFocused()) {
            guiGraphics.renderComponentTooltip(font, Arrays.asList(Component.literal(I18n.get("firstaid.gui.active_item") + ": " + I18n.get(part.activeHealer.stack.getDescriptionId())), Component.translatable("firstaid.gui.next_heal", Math.round((part.activeHealer.ticksPerHeal.getAsInt() - part.activeHealer.getTicksPassed()) / 20F))), mouseX, mouseY);
        }
        if (!disableButtons) button.active = enabled;
    }

    public void drawHealth(GuiGraphics guiGraphics, AbstractDamageablePart damageablePart, boolean right, int yOffset) {
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        int xTranslation = guiLeft + (right ? getRightOffset(damageablePart) : 57);
        HealthRenderUtils.drawHealth(guiGraphics, this.minecraft.font, damageablePart, xTranslation, guiTop + yOffset, true);
        stack.popPose();
    }

    private static int getRightOffset(AbstractDamageablePart damageablePart) {
        if (HealthRenderUtils.drawAsString(damageablePart, true)) return 200 - 40;
        return 200 - Math.min(40, HealthRenderUtils.getMaxHearts(damageablePart.getMaxHealth()) * 9 + HealthRenderUtils.getMaxHearts(damageablePart.getAbsorption()) * 9 + 2);
    }

    /**
     * The preview magnifies the 64x64 skin a lot. Sampling it with linear filtering turns the model
     * into a blurred blob, so switch the skin texture back to nearest (the pixel-art look vanilla
     * uses) before drawing it.
     */
    private void forceNearestSkinFilter() {
        try {
            if (!(this.minecraft.player instanceof AbstractClientPlayer clientPlayer)) {
                return;
            }
            AbstractTexture skin = this.minecraft.getTextureManager().getTexture(clientPlayer.getSkin().texture());
            skin.setFilter(false, false);
        } catch (Throwable ignored) {
            // cosmetic only - never break the screen
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        // Fallback: if the widget chain did not consume the click (e.g. another mod replaced the
        // child list), still honour a click on the close button.
        if (!handled && cancelButton != null && cancelButton.isMouseOver(mouseX, mouseY)) {
            onClose();
            return true;
        }
        return handled;
    }

    @Override
    public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
        if (super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
            return true;
        if (ClientHooks.SHOW_WOUNDS.isActiveAndMatches(InputConstants.getKey(p_keyPressed_1_, p_keyPressed_2_))) {
            onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        holdButtonMouseCallback(null);
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public void mouseMoved(double xPos, double yPos) {
        for (GuiHoldButton holdButton : this.holdButtons) {
            holdButton.mouseMoved(xPos, yPos);
        }
    }

    protected void holdButtonMouseCallback(GuiGraphics guiGraphics) {
        for (GuiHoldButton button : this.holdButtons) {
            int timeLeft = button.getTimeLeft();
            if (timeLeft == 0) {
                //We are officially done
                button.reset();
                EnumPlayerPart playerPart = EnumPlayerPart.VALUES[button.id - 1];
                FirstAidNetwork.sendToServer(new MessageApplyHealingItem(playerPart, activeHand));
                AbstractDamageablePart part = damageModel.getFromEnum(playerPart);
                ItemStack itemInHand = minecraft.player.getItemInHand(this.activeHand);
                if (itemInHand.getItem() instanceof ItemHealing itemHealing) {
                    part.activeHealer = itemHealing.createNewHealer(itemInHand);
                }
                onClose();
            } else if (guiGraphics == null) {
                button.reset();
            } else if (timeLeft != -1) {
                float timeInSecs = (timeLeft / 1000F);
                if (timeInSecs < 0F) timeInSecs = 0F;
                guiGraphics.blit(HealthRenderUtils.SHOW_WOUNDS_LOCATION, button.getX() + (button.isRightSide ? 56 : -25), button.getY() - 2, button.isRightSide ? 2 : 0, 169, 22, 24);
                guiGraphics.drawString(font, HealthRenderUtils.TEXT_FORMAT.format(timeInSecs), button.getX() + (button.isRightSide ? 60 : -20), button.getY() + 6, 0xFFFFFFFF);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        INSTANCE = null;
        isOpen = false;
        super.onClose();
    }

    public List<AbstractWidget> getButtons() {
        return (List<AbstractWidget>) (Object) this.renderables;
    }
}