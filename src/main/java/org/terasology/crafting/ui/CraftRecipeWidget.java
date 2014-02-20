/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.ui;

import org.terasology.asset.Assets;
import org.terasology.crafting.system.recipe.render.CraftIngredientRenderer;
import org.terasology.crafting.system.recipe.render.CraftProcessDisplay;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.input.Keyboard;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.math.Rect2i;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.world.block.Block;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CraftRecipeWidget extends CoreWidget {
    private EntityRef entity;
    private CraftProcessDisplay processDisplay;
    private final CreationCallback callback;

//    private List<ItemIcon> ingredientsIcons = new LinkedList<>();

    private ItemIcon result;

    private UIButton button;

    private int leftIndent;

    private int multiplier = 1;

    private int maxMultiplier;

    public CraftRecipeWidget(int leftIndent, EntityRef entity,
                             final CraftProcessDisplay processDisplay, CreationCallback callback) {
        this.leftIndent = leftIndent;
        this.entity = entity;
        this.processDisplay = processDisplay;
        this.callback = callback;

        maxMultiplier = processDisplay.getMaxMultiplier();

        result = new ItemIcon();
        Block resultBlock = processDisplay.getResultBlock();
        Prefab resultItem = processDisplay.getResultItem();
        if (resultBlock != null) {
            result.setMesh(resultBlock.getMesh());
            result.setMeshTexture(Assets.getTexture("engine:terrain"));
            result.setTooltip(resultBlock.getDisplayName());
        } else if (resultItem != null) {
            result.setIcon(resultItem.getComponent(ItemComponent.class).icon);
            DisplayNameComponent displayName = resultItem.getComponent(DisplayNameComponent.class);
            if (displayName != null) {
                result.setTooltip(displayName.name);
            }
        }
        result.bindQuantity(
                new Binding<Integer>() {
                    @Override
                    public Integer get() {
                        return processDisplay.getResultQuantity() * multiplier;
                    }

                    @Override
                    public void set(Integer value) {
                    }
                }
        );


        button = new UIButton();
        button.setText("Craft");
        button.subscribe(
                new ActivateEventListener() {
                    @Override
                    public void onActivated(UIWidget widget) {
                        produce();
                    }
                }
        );
    }

    private void produce() {
        callback.create(multiplier);
    }

    @Override
    public void onDraw(Canvas canvas) {
        int x = leftIndent;
        Vector2i size = canvas.size();

        for (CraftIngredientRenderer craftIngredientRenderer : processDisplay.getIngredients(entity)) {
            Vector2i preferredSize = craftIngredientRenderer.getPreferredSize(multiplier);
            craftIngredientRenderer.render(canvas, Rect2i.createFromMinAndSize(x, 0, preferredSize.x, preferredSize.y), multiplier);
            x += preferredSize.x;
        }

        Vector2i resultSize = canvas.calculatePreferredSize(result);
        Vector2i buttonSize = canvas.calculatePreferredSize(button);

        canvas.drawWidget(result, Rect2i.createFromMinAndSize(size.x - resultSize.x - buttonSize.x - 5, 0, resultSize.x, resultSize.y));
        canvas.drawWidget(button, Rect2i.createFromMinAndSize(size.x - buttonSize.x, (size.y - buttonSize.y) / 2, buttonSize.x, buttonSize.y));
    }

    @Override
    public void update(float delta) {
        if (Keyboard.isKeyDown(Keyboard.KeyId.LEFT_SHIFT)) {
            multiplier = Math.min(maxMultiplier, 5);
        } else if (Keyboard.isKeyDown(Keyboard.KeyId.LEFT_CTRL)) {
            multiplier = maxMultiplier;
        } else {
            multiplier = 1;
        }

        result.update(delta);
        button.update(delta);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        int maxX = canvas.size().x;
        int maxY = 0;

        for (CraftIngredientRenderer craftIngredientRenderer : processDisplay.getIngredients(entity)) {
            maxY = Math.max(maxY, craftIngredientRenderer.getPreferredSize(multiplier).y);
        }

        maxY = Math.max(maxY, canvas.calculatePreferredSize(result).y);
        maxY = Math.max(maxY, canvas.calculatePreferredSize(button).y);

        return new Vector2i(maxX, maxY);
    }

    @Override
    public Vector2i getMaxContentSize(Canvas canvas) {
        return getPreferredContentSize(canvas, null);
    }
}
