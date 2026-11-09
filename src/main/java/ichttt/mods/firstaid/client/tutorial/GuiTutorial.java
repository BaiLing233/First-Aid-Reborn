/*
 * FirstAid
 * Copyright (C) 2017-2024 ichttt (original First Aid)
 * Copyright (C) 2026 BaiLing233 (1.21.1 NeoForge port)
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

package ichttt.mods.firstaid.client.tutorial;

import com.mojang.blaze3d.vertex.PoseStack;
import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.client.ClientHooks;
import ichttt.mods.firstaid.client.gui.GuiHealthScreen;
import ichttt.mods.firstaid.client.util.HealthRenderUtils;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import ichttt.mods.firstaid.common.network.MessageClientRequest;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class GuiTutorial extends Screen {
    private final GuiHealthScreen parent;
    private final AbstractPlayerDamageModel demoModel;
    private int guiTop;
    private final TutorialAction action;
    private boolean tutorialCompleteSent = false;

    @SuppressWarnings("deprecation") // we still need this method
    public GuiTutorial() {
        super(Component.translatable("firstaid.tutorial"));
        this.demoModel = new PlayerDamageModel();
        this.parent = new GuiHealthScreen(demoModel);
        this.action = new TutorialAction(this);

        this.action.addTextWrapper("firstaid.tutorial.welcome");
        this.action.addTextWrapper("firstaid.tutorial.line1");
        this.action.addTextWrapper("firstaid.tutorial.line2");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_FOOT.damage(4F, null, false));
        this.action.addTextWrapper("firstaid.tutorial.line3");
        //We need the deprecated version
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.applyMorphine());
        this.action.addTextWrapper("firstaid.tutorial.line4");
        this.action.addTextWrapper("firstaid.tutorial.line5");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.LEFT_FOOT.heal(3F, null, false));
        if (FirstAidConfig.SERVER.sleepHealPercentage.get() != 0D)
            this.action.addTextWrapper("firstaid.tutorial.sleephint");
        this.action.addTextWrapper("firstaid.tutorial.line6");
        this.action.addActionCallable(guiTutorial -> guiTutorial.demoModel.HEAD.damage(16F, null, false));
        this.action.addTextWrapper("firstaid.tutorial.line7");
        this.action.addTextWrapper("firstaid.tutorial.line8", ClientHooks.SHOW_WOUNDS.getTranslatedKeyMessage().getString());
        this.action.addTextWrapper("firstaid.tutorial.end");

        this.action.next();
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
        parent.init(minecraft, this.width, this.height);
        guiTop = parent.guiTop - 30;

        // The tutorial owns its own buttons: reusing the parent's widgets made them unclickable
        // (they belong to another screen's child list).
        addRenderableWidget(Button.builder(Component.literal(">"), button -> onNext())
                .bounds(parent.guiLeft + GuiHealthScreen.xSize - 34, guiTop + 4, 32, 20).build());

        for (AbstractWidget button : parent.getButtons()) {
            if (button == parent.cancelButton) continue;
            addRenderableWidget(button);
        }
        parent.getButtons().clear();

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(this.width / 2 - 100, this.height - 50, 200, 20).build());
    }

    private void onNext() {
        try {
            if (action.hasNext()) {
                action.next();
            } else {
                finishTutorial();
            }
        } catch (Throwable throwable) {
            FirstAid.LOGGER.error("Tutorial step failed", throwable);
            finishTutorial();
        }
    }

    private void finishTutorial() {
        sendTutorialComplete();
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(minecraft.player);
        if (damageModel != null) {
            minecraft.setScreen(new GuiHealthScreen(damageModel));
        } else {
            onClose();
        }
    }

    /** Marks the tutorial as done. Without this every H press re-opens the tutorial. */
    private void sendTutorialComplete() {
        if (tutorialCompleteSent) {
            return;
        }
        tutorialCompleteSent = true;
        FirstAidNetwork.sendToServer(new MessageClientRequest(MessageClientRequest.Type.TUTORIAL_COMPLETE));
    }

    @Override
    public void onClose() {
        sendTutorialComplete();
        GuiHealthScreen.isOpen = false;
        super.onClose();
    }

    public void drawOffsetString(GuiGraphics guiGraphics, String s, int yOffset) {
        // White text with the vanilla shadow, drawn on the dark strip added in render().
        guiGraphics.drawString(minecraft.font, s, parent.guiLeft + 30, guiTop + yOffset, 0xFFFFFFFF);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        // No blur: 1.20.1 only dimmed the background, the blur arrived with 1.20.2 and makes the
        // tutorial text and the player model look out of focus.
        this.renderTransparentBackground(guiGraphics);
        PoseStack stack = guiGraphics.pose();
        stack.pushPose();
        parent.render(guiGraphics, mouseX, mouseY, partialTicks);
        stack.popPose();
        guiGraphics.blit(HealthRenderUtils.SHOW_WOUNDS_LOCATION, parent.guiLeft, guiTop, 0, 139, GuiHealthScreen.xSize, 28);
        // Dark strip behind the explanation text: white-on-light-grey is unreadable at high GUI
        // scales, the community version also draws its tutorial text on a dark panel.
        guiGraphics.fill(parent.guiLeft + 2, guiTop + 2, parent.guiLeft + GuiHealthScreen.xSize - 2, guiTop + 26, 0xE0101010);
        stack.pushPose();
        this.action.draw(guiGraphics);
        stack.popPose();
        guiGraphics.fill(parent.guiLeft + 2, parent.guiTop + 124, parent.guiLeft + GuiHealthScreen.xSize - 2, parent.guiTop + 134, 0xE0101010);
        guiGraphics.drawCenteredString(minecraft.font, I18n.get("firstaid.tutorial.notice"), parent.guiLeft + (GuiHealthScreen.xSize / 2), parent.guiTop + 127, 0xFFFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }
}
