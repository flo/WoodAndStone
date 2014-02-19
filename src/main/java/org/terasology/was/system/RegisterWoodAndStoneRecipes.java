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
package org.terasology.was.system;

import com.google.common.base.Predicate;
import org.terasology.crafting.component.CraftInHandRecipeComponent;
import org.terasology.crafting.component.CraftingStationMaterialComponent;
import org.terasology.crafting.system.CraftInHandRecipeRegistry;
import org.terasology.crafting.system.CraftingWorkstationProcess;
import org.terasology.crafting.system.CraftingWorkstationProcessFactory;
import org.terasology.crafting.system.recipe.hand.CompositeTypeBasedCraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.crafting.system.recipe.hand.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.behaviour.PresenceItemCraftBehaviour;
import org.terasology.crafting.system.recipe.hand.behaviour.ReduceItemDurabilityCraftBehaviour;
import org.terasology.crafting.system.recipe.workstation.SimpleWorkstationRecipe;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.multiBlock.BasicHorizontalSizeFilter;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.UniformBlockReplacementCallback;
import org.terasology.multiBlock.recipe.UniformMultiBlockFormItemRecipe;
import org.terasology.registry.In;
import org.terasology.was.WoodAndStone;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.world.block.BlockManager;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RegisterWoodAndStoneRecipes extends BaseComponentSystem {
    @In
    private CraftInHandRecipeRegistry recipeRegistry;
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockFormRecipeRegistry;
    @In
    private BlockManager blockManager;
    @In
    private PrefabManager prefabManager;

    @Override
    public void initialise() {
        workstationRegistry.registerProcessFactory(WoodAndStone.BASIC_WOODCRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        workstationRegistry.registerProcessFactory(WoodAndStone.BASIC_STONECRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());
        workstationRegistry.registerProcessFactory(WoodAndStone.ADVANCED_STONECRAFTING_PROCESS_TYPE, new CraftingWorkstationProcessFactory());

        addWorkstationFormingRecipes();

        addCraftInHandRecipes();

        addStandardWoodWorkstationBlockShapeRecipes();

        addBasicStoneWorkstationBlockShapeRecipes();
    }

    private void addWorkstationFormingRecipes() {
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(new ToolTypeEntityFilter("axe"), new UseOnTopFilter(),
                        new StationTypeFilter("WoodAndStone:BasicWoodcrafting"), new BasicHorizontalSizeFilter(2, 1, 1, 1),
                        "WoodAndStone:BasicWoodcrafting",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("WoodAndStone:BasicWoodStation"))));
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(new ToolTypeEntityFilter("hammer"), new UseOnTopFilter(),
                        new StationTypeFilter("WoodAndStone:BasicStonecrafting"), new BasicHorizontalSizeFilter(2, 1, 1, 1),
                        "WoodAndStone:BasicStonecrafting",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("WoodAndStone:BasicStoneStation"))));
    }

    private void addBasicStoneWorkstationBlockShapeRecipes() {
        addWorkstationBlockShapesRecipe(WoodAndStone.BASIC_STONECRAFTING_PROCESS_TYPE, "Building|Cobble Stone|WoodAndStone:CobbleBlock",
                "WoodAndStone:stone", 2, "hammer", 1, "Core:CobbleStone", 1);
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_STONECRAFTING_PROCESS_TYPE, "Building|Bricks|WoodAndStone:BrickBlock",
                "WoodAndStone:brick", 2, "hammer", 1, "Core:Brick", 1);
    }

    private void addStandardWoodWorkstationBlockShapeRecipes() {
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, "Building|Planks|WoodAndStone:PlankBlock",
                "WoodAndStone:plank", 2, "axe", 1, "Core:Plank", 4);
        addWorkstationBlockShapesRecipe(WoodAndStone.ADVANCED_WOODCRAFTING_PROCESS_TYPE, "Building|Fine Planks|WoodAndStone:FinePlankBlock",
                "WoodAndStone:plank", 4, "hammer", 1, "WoodAndStone:FinePlank", 1);
    }

    private void addCraftInHandRecipes() {
        addCraftInHandRecipe("WoodAndStone:Seeding", new SeedingFruitsRecipe());

        for (Prefab prefab : prefabManager.listPrefabs(CraftInHandRecipeComponent.class)) {
            parseCraftInHandRecipe(prefab.getComponent(CraftInHandRecipeComponent.class));
        }
    }

    private void addShapeRecipe(String processType, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                String tool, int toolDurability, String blockResultPrefix, int blockResultCount,
                                String shape, int ingredientMultiplier, int resultMultiplier, int toolDurabilityMultiplier) {
        SimpleWorkstationRecipe shapeRecipe = new SimpleWorkstationRecipe();
        shapeRecipe.addIngredient(ingredient, ingredientBasicCount * ingredientMultiplier);
        shapeRecipe.addRequiredTool(tool, toolDurability * toolDurabilityMultiplier);
        shapeRecipe.setBlockResult(blockResultPrefix + ":Engine:" + shape, (byte) (blockResultCount * resultMultiplier));

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(processType, recipeNamePrefix + shape, shapeRecipe));
    }

    private void addWorkstationBlockShapesRecipe(String processType, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                                 String tool, int toolDurability, String blockResultPrefix, int blockResultCount) {
        SimpleWorkstationRecipe fullBlockRecipe = new SimpleWorkstationRecipe();
        fullBlockRecipe.addIngredient(ingredient, ingredientBasicCount);
        fullBlockRecipe.addRequiredTool(tool, toolDurability);
        fullBlockRecipe.setBlockResult(blockResultPrefix, (byte) blockResultCount);

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(processType, recipeNamePrefix, fullBlockRecipe));

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Stair", 3, 4, 2);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Slope", 1, 2, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "UpperHalfSlope", 1, 2, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SlopeCorner", 1, 2, 2);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SteepSlope", 1, 1, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "QuarterSlope", 1, 8, 2);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfBlock", 1, 2, 1);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlope", 1, 4, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlopeCorner", 1, 6, 1);

        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarTop", 1, 1, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Pillar", 1, 1, 2);
        addShapeRecipe(processType, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarBase", 1, 1, 2);
    }

    private void parseCraftInHandRecipe(CraftInHandRecipeComponent recipeComponent) {
        String recipeId = recipeComponent.recipeId;
        String prefab = (recipeComponent.blockResult != null) ? recipeComponent.blockResult : recipeComponent.itemResult;
        boolean block = (recipeComponent.blockResult != null);

        CompositeTypeBasedCraftInHandRecipe recipe = new CompositeTypeBasedCraftInHandRecipe(prefab, block);

        if (recipeComponent.recipeComponents != null) {
            for (String component : recipeComponent.recipeComponents) {
                String[] split = component.split("\\*");
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new ConsumeItemCraftBehaviour(type, count));
            }
        }
        if (recipeComponent.recipeTools != null) {
            for (String tool : recipeComponent.recipeTools) {
                String[] split = tool.split("\\*");
                int durability = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new ReduceItemDurabilityCraftBehaviour(type, durability));
            }
        }
        if (recipeComponent.recipeActivators != null) {
            for (String activator : recipeComponent.recipeActivators) {
                String[] split = activator.split("\\*");
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new PresenceItemCraftBehaviour(type, count));
            }
        }
        addCraftInHandRecipe(recipeId, recipe);
    }

    private void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe) {
        recipeRegistry.addCraftInHandRecipe(recipeId, craftInHandRecipe);
    }

    private final class StationTypeFilter implements Predicate<EntityRef> {
        private String stationType;

        private StationTypeFilter(String stationType) {
            this.stationType = stationType;
        }

        @Override
        public boolean apply(EntityRef entity) {
            CraftingStationMaterialComponent stationMaterial = entity.getComponent(CraftingStationMaterialComponent.class);
            return stationMaterial != null && stationMaterial.stationType.equals(stationType);
        }
    }
}
