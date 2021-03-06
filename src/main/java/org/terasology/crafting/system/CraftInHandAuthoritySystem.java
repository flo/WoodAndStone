/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.crafting.system;

import org.terasology.crafting.event.UserCraftInHandRequest;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.PickupBuilder;
import org.terasology.logic.location.LocationComponent;
import org.terasology.registry.In;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class CraftInHandAuthoritySystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private CraftInHandRecipeRegistry recipeRegistry;

    private PickupBuilder pickupBuilder;

    @Override
    public void initialise() {
        pickupBuilder = new PickupBuilder(entityManager);
    }

    @ReceiveEvent
    public void craftInHandRequestReceived(UserCraftInHandRequest event, EntityRef character) {
        if (!recipeRegistry.isCraftingInHandDisabled()) {
            String recipeId = event.getRecipeId();
            final List<String> parameters = event.getParameters();
            CraftInHandRecipe craftInHandRecipe = recipeRegistry.getRecipes().get(recipeId);
            if (craftInHandRecipe != null) {
                CraftInHandRecipe.CraftInHandResult result = craftInHandRecipe.getResultByParameters(parameters);
                if (result != null) {
                    EntityRef resultEntity = result.craft(character, event.getCount());
                    if (resultEntity.exists()) {
                        pickupBuilder.createPickupFor(resultEntity, character.getComponent(LocationComponent.class).getWorldPosition(), 200, true);
                    }
                }
            }
        }
    }
}
