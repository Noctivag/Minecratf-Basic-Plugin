package de.noctivag.plugin.crafting;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import de.noctivag.plugin.utils.ColorUtils;

import java.io.File;
import java.util.List;

public class RecipeManager {

    private final Plugin plugin;
    private FileConfiguration recipeConfig;

    public RecipeManager(Plugin plugin) {
        this.plugin = plugin;
        loadRecipes();
    }

    private void loadRecipes() {
        File recipeFile = new File(plugin.getDataFolder(), "crafting.yml");
        if (!recipeFile.exists()) {
            plugin.saveResource("crafting.yml", false);
        }
        recipeConfig = YamlConfiguration.loadConfiguration(recipeFile);
    }

    public void registerRecipes() {
        ConfigurationSection recipesSection = recipeConfig.getConfigurationSection("recipes");
        if (recipesSection == null) {
            return;
        }

        for (String recipeName : recipesSection.getKeys(false)) {
            ConfigurationSection recipeDetails = recipesSection.getConfigurationSection(recipeName);
            if (recipeDetails != null && recipeDetails.getBoolean("enabled", false)) {
                createAndRegisterRecipe(recipeName, recipeDetails);
            }
        }
    }

    private void createAndRegisterRecipe(String recipeName, ConfigurationSection recipeDetails) {
        Material outputMaterial = Material.getMaterial(recipeDetails.getString("output", ""));
        if (outputMaterial == null) {
            plugin.getLogger().warning("Invalid output material for recipe: " + recipeName);
            return;
        }

        int amount = recipeDetails.getInt("amount", 1);
        ItemStack result = new ItemStack(outputMaterial, amount);
        ItemMeta meta = result.getItemMeta();
        
        if (meta != null) {
            // Set display name
            if (recipeDetails.contains("display-name")) {
                meta.displayName(ColorUtils.parseColor(recipeDetails.getString("display-name")));
            }
            
            // Set lore (skip for invisible_item_frame to remove description)
            if (!"invisible_item_frame".equals(recipeName) && recipeDetails.contains("lore")) {
                List<String> lore = recipeDetails.getStringList("lore");
                meta.lore(lore.stream()
                    .map(ColorUtils::parseColor)
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            // Set persistent data
            if (recipeDetails.contains("persistent-data")) {
                ConfigurationSection pdSection = recipeDetails.getConfigurationSection("persistent-data");
                if (pdSection != null) {
                    String key = pdSection.getString("key");
                    String type = pdSection.getString("type", "string");
                    String value = pdSection.getString("value");
                    
                    if (key != null && value != null) {
                        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
                        switch (type.toLowerCase()) {
                            case "byte" ->
                                meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.BYTE, Byte.parseByte(value));
                            case "integer" ->
                                meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.INTEGER, Integer.parseInt(value));
                            default ->
                                meta.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, value);
                        }
                    }
                }
            }
        }
        result.setItemMeta(meta);

        NamespacedKey recipeKey = new NamespacedKey(plugin, recipeName);
        ShapedRecipe recipe = new ShapedRecipe(recipeKey, result);

        List<String> shape = recipeDetails.getStringList("shape");
        recipe.shape(shape.toArray(new String[0]));

        ConfigurationSection ingredients = recipeDetails.getConfigurationSection("ingredients");
        if (ingredients != null) {
            for (String key : ingredients.getKeys(false)) {
                Material ingredientMaterial = Material.getMaterial(ingredients.getString(key, ""));
                if (ingredientMaterial != null) {
                    recipe.setIngredient(key.charAt(0), ingredientMaterial);
                } else {
                    plugin.getLogger().warning("Invalid ingredient material for recipe: " + recipeName);
                }
            }
        }
        Bukkit.addRecipe(recipe);
    }
}
