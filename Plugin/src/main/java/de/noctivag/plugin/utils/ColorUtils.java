package de.noctivag.plugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

public class ColorUtils {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("gradient:(#[A-Fa-f0-9]{6}):(#[A-Fa-f0-9]{6}):(.+)");
    private static final Pattern MULTI_PATTERN = Pattern.compile("multi:((#[A-Fa-f0-9]{6}:)+)(.+)");

    // Cache für häufig verwendete Farben und Komponenten
    private static final Map<String, TextColor> COLOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;

    public static Component parseColor(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // Prüfe Cache für häufig verwendete Texte
        Component cached = COMPONENT_CACHE.get(text);
        if (cached != null) {
            return cached;
        }

        try {
            Component result;
            // Prüfe auf Farbverlauf
            Matcher gradientMatcher = GRADIENT_PATTERN.matcher(text);
            if (gradientMatcher.matches()) {
                result = createGradient(
                    gradientMatcher.group(3),
                    parseHex(gradientMatcher.group(1)),
                    parseHex(gradientMatcher.group(2))
                );
            }
            // Prüfe auf mehrere Farben
            else {
                Matcher multiMatcher = MULTI_PATTERN.matcher(text);
                if (multiMatcher.matches()) {
                    result = createMultiColorEfficient(multiMatcher);
                } else {
                    result = createSingleColor(text);
                }
            }

            // Cache das Ergebnis, wenn der Text nicht zu lang ist
            if (text.length() <= 100 && COMPONENT_CACHE.size() < MAX_CACHE_SIZE) {
                COMPONENT_CACHE.put(text, result);
            }

            return result;
        } catch (Exception e) {
            return Component.text(text);
        }
    }

    @NotNull
    private static Component createGradient(@NotNull String text, int startColor, int endColor) {
        if (text.isEmpty()) return Component.empty();
        if (text.length() == 1) return Component.text(text).color(TextColor.color(startColor));

        Component result = Component.empty();
        int length = text.length();
        float[] startRGB = extractRGB(startColor);
        float[] endRGB = extractRGB(endColor);

        for (int i = 0; i < length; i++) {
            float ratio = length > 1 ? (float) i / (length - 1) : 0;
            int[] rgb = interpolateRGB(startRGB, endRGB, ratio);
            result = result.append(
                Component.text(String.valueOf(text.charAt(i)))
                    .color(TextColor.color(rgb[0], rgb[1], rgb[2]))
            );
        }

        return result;
    }

    private static Component createMultiColorEfficient(Matcher matcher) {
        String colorsStr = matcher.group(1);
        String content = matcher.group(3);

        if (content.isEmpty()) return Component.empty();

        // Extrahiere Farben effizient
        Matcher colorMatcher = HEX_PATTERN.matcher(colorsStr);
        int[] colors = new int[8]; // Max 8 Farben
        int colorCount = 0;

        while (colorMatcher.find() && colorCount < 8) {
            colors[colorCount++] = parseHex(colorMatcher.group());
        }

        if (colorCount == 0) return Component.text(content);

        // Verteile Text auf Farben
        Component result = Component.empty();
        int charPerColor = Math.max(1, content.length() / colorCount);
        int currentPos = 0;

        for (int i = 0; i < colorCount && currentPos < content.length(); i++) {
            int endPos = Math.min(currentPos + charPerColor, content.length());
            String segment = content.substring(currentPos, endPos);
            result = result.append(Component.text(segment)
                .color(getCachedColor(colors[i])));
            currentPos = endPos;
        }

        return result;
    }

    private static Component createSingleColor(String text) {
        StringBuilder result = new StringBuilder();
        Matcher hexMatcher = HEX_PATTERN.matcher(text);
        TextColor color = null;
        int lastEnd = 0;

        if (hexMatcher.find()) {
            color = getCachedColor(parseHex(hexMatcher.group()));
            lastEnd = hexMatcher.end();
        }

        String remainingText = text.substring(lastEnd);
        if (remainingText.isEmpty()) return Component.empty();
        return color != null ? Component.text(remainingText).color(color) : Component.text(text);
    }

    private static TextColor getCachedColor(int rgb) {
        String key = String.valueOf(rgb);
        return COLOR_CACHE.computeIfAbsent(key, k -> TextColor.color(rgb));
    }

    private static float[] extractRGB(int color) {
        return new float[] {
            (color >> 16) & 0xFF,
            (color >> 8) & 0xFF,
            color & 0xFF
        };
    }

    private static int[] interpolateRGB(float[] start, float[] end, float ratio) {
        return new int[] {
            Math.max(0, Math.min(255, (int) (start[0] + (end[0] - start[0]) * ratio))),
            Math.max(0, Math.min(255, (int) (start[1] + (end[1] - start[1]) * ratio))),
            Math.max(0, Math.min(255, (int) (start[2] + (end[2] - start[2]) * ratio)))
        };
    }

    private static int parseHex(String hex) {
        try {
            hex = hex.replace("#", "").trim();
            return Integer.parseInt(hex, 16);
        } catch (NumberFormatException e) {
            return 0xFFFFFF; // Fallback zu Weiß
        }
    }

    /**
     * Translates legacy Minecraft color codes (&a, &b, etc.) to actual colored text
     */
    public static String translateColorCodes(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.replace("&", "§");
    }

    /**
     * Combines rank prefix with custom prefix
     * @param rankPrefix The rank prefix (can be null or empty)
     * @param customPrefix The custom prefix (can be null or empty)
     * @return Combined prefix string
     */
    public static String combinePrefix(String rankPrefix, String customPrefix) {
        if (rankPrefix == null) rankPrefix = "";
        if (customPrefix == null) customPrefix = "";
        
        if (rankPrefix.isEmpty()) {
            return customPrefix;
        } else if (customPrefix.isEmpty()) {
            return rankPrefix;
        } else {
            return rankPrefix + " " + customPrefix;
        }
    }
}
