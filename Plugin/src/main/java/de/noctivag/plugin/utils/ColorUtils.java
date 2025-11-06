package de.noctivag.plugin.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class ColorUtils {
    // Patterns für verschiedene Farbformate
    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile("<gradient:([^>]+)>(.+?)</gradient>");
    private static final Pattern RAINBOW_PATTERN = Pattern.compile("<rainbow>(.+?)</rainbow>");
    private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    
    // Cache für häufig verwendete Farben und Komponenten
    private static final Map<String, TextColor> COLOR_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Component> COMPONENT_CACHE = new ConcurrentHashMap<>();
    private static final int MAX_CACHE_SIZE = 1000;

    /**
     * Parst Text mit verschiedenen Farbformaten:
     * - Legacy codes: &a, &c, etc.
     * - Hex codes: #FF0000 oder &#FF0000
     * - Gradients: <gradient:#FF0000:#0000FF>text</gradient>
     * - Rainbow: <rainbow>text</rainbow>
     * - MiniMessage-ähnlich: <#FF0000>text
     */
    public static Component parseColor(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // Prüfe Cache
        Component cached = COMPONENT_CACHE.get(text);
        if (cached != null) {
            return cached;
        }

        try {
            // Verarbeite verschiedene Formate
            String processed = text;
            
            // 1. Konvertiere &#RRGGBB zu #RRGGBB
            processed = LEGACY_HEX_PATTERN.matcher(processed).replaceAll("#$1");
            
            Component result = processAdvancedFormats(processed);

            // Cache das Ergebnis
            if (text.length() <= 100 && COMPONENT_CACHE.size() < MAX_CACHE_SIZE) {
                COMPONENT_CACHE.put(text, result);
            }

            return result;
        } catch (Exception e) {
            // Fallback zu Legacy-Format
            return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
        }
    }

    /**
     * Verarbeitet erweiterte Formate wie Gradients und Rainbow
     */
    private static Component processAdvancedFormats(String text) {
        Component result = Component.empty();
        int lastEnd = 0;
        
        // Suche nach Gradient-Tags
        Matcher gradientMatcher = GRADIENT_PATTERN.matcher(text);
        boolean foundAdvanced = false;
        
        while (gradientMatcher.find()) {
            foundAdvanced = true;
            // Text vor dem Gradient
            if (gradientMatcher.start() > lastEnd) {
                String before = text.substring(lastEnd, gradientMatcher.start());
                result = result.append(processSimpleFormats(before));
            }
            
            // Verarbeite Gradient
            String colorsPart = gradientMatcher.group(1);
            String content = gradientMatcher.group(2);
            String[] colors = colorsPart.split(":");
            
            if (colors.length >= 2) {
                result = result.append(createGradient(content, colors));
            } else {
                result = result.append(Component.text(content));
            }
            
            lastEnd = gradientMatcher.end();
        }
        
        // Suche nach Rainbow-Tags
        Matcher rainbowMatcher = RAINBOW_PATTERN.matcher(text);
        if (!foundAdvanced) {
            while (rainbowMatcher.find()) {
                foundAdvanced = true;
                // Text vor dem Rainbow
                if (rainbowMatcher.start() > lastEnd) {
                    String before = text.substring(lastEnd, rainbowMatcher.start());
                    result = result.append(processSimpleFormats(before));
                }
                
                // Verarbeite Rainbow
                String content = rainbowMatcher.group(1);
                result = result.append(createRainbow(content));
                
                lastEnd = rainbowMatcher.end();
            }
        }
        
        // Rest des Textes
        if (lastEnd < text.length()) {
            result = result.append(processSimpleFormats(text.substring(lastEnd)));
        }
        
        // Wenn keine erweiterten Formate gefunden wurden
        if (!foundAdvanced && result.equals(Component.empty())) {
            return processSimpleFormats(text);
        }
        
        return result;
    }

    /**
     * Verarbeitet einfache Formate wie Hex-Codes und Legacy-Codes
     */
    private static Component processSimpleFormats(String text) {
        if (text.isEmpty()) return Component.empty();
        
        // Zuerst Legacy-Codes mit & verarbeiten
        String processed = text;
        
        // Konvertiere Hex-Codes zu MiniMessage-Format
        Matcher hexMatcher = HEX_PATTERN.matcher(processed);
        StringBuffer sb = new StringBuffer();
        
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            hexMatcher.appendReplacement(sb, "<#" + hex + ">");
        }
        hexMatcher.appendTail(sb);
        processed = sb.toString();
        
        // Verarbeite Text mit Adventure-API
        Component result = Component.empty();
        StringBuilder currentText = new StringBuilder();
        TextColor currentColor = null;
        
        int i = 0;
        while (i < processed.length()) {
            // Prüfe auf MiniMessage-Hex-Code: <#RRGGBB>
            if (processed.startsWith("<#", i) && i + 9 < processed.length() && processed.charAt(i + 9) == '>') {
                // Füge aktuellen Text hinzu
                if (currentText.length() > 0) {
                    Component textComponent = Component.text(currentText.toString());
                    if (currentColor != null) {
                        textComponent = textComponent.color(currentColor);
                    }
                    result = result.append(textComponent);
                    currentText = new StringBuilder();
                }
                
                // Parse Hex-Code
                String hex = processed.substring(i + 2, i + 8);
                try {
                    currentColor = TextColor.fromHexString("#" + hex);
                } catch (Exception e) {
                    currentColor = null;
                }
                i += 10;
            }
            // Prüfe auf Legacy-Codes &a, &c, etc.
            else if (processed.charAt(i) == '&' && i + 1 < processed.length()) {
                char code = processed.charAt(i + 1);
                
                // Füge aktuellen Text hinzu
                if (currentText.length() > 0) {
                    Component textComponent = Component.text(currentText.toString());
                    if (currentColor != null) {
                        textComponent = textComponent.color(currentColor);
                    }
                    result = result.append(textComponent);
                    currentText = new StringBuilder();
                }
                
                // Setze Farbe basierend auf Legacy-Code
                currentColor = getLegacyColor(code);
                i += 2;
            }
            else {
                currentText.append(processed.charAt(i));
                i++;
            }
        }
        
        // Füge restlichen Text hinzu
        if (currentText.length() > 0) {
            Component textComponent = Component.text(currentText.toString());
            if (currentColor != null) {
                textComponent = textComponent.color(currentColor);
            }
            result = result.append(textComponent);
        }
        
        return result;
    }

    /**
     * Erstellt einen Farbverlauf über den Text
     */
    @NotNull
    private static Component createGradient(@NotNull String text, String[] colors) {
        if (text.isEmpty()) return Component.empty();
        if (colors.length < 2) return Component.text(text);

        List<TextColor> colorList = new ArrayList<>();
        for (String colorStr : colors) {
            try {
                colorStr = colorStr.trim();
                if (!colorStr.startsWith("#")) colorStr = "#" + colorStr;
                TextColor color = TextColor.fromHexString(colorStr);
                if (color != null) {
                    colorList.add(color);
                }
            } catch (Exception e) {
                // Ignoriere ungültige Farben
            }
        }
        
        if (colorList.size() < 2) return Component.text(text);
        
        Component result = Component.empty();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            float ratio = length > 1 ? (float) i / (length - 1) : 0;
            TextColor color = interpolateColors(colorList, ratio);
            result = result.append(Component.text(String.valueOf(text.charAt(i))).color(color));
        }
        
        return result;
    }

    /**
     * Erstellt einen Rainbow-Effekt
     */
    @NotNull
    private static Component createRainbow(@NotNull String text) {
        if (text.isEmpty()) return Component.empty();
        
        Component result = Component.empty();
        int length = text.length();
        
        for (int i = 0; i < length; i++) {
            float hue = (float) i / Math.max(1, length - 1);
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f);
            TextColor color = TextColor.color(rgb);
            result = result.append(Component.text(String.valueOf(text.charAt(i))).color(color));
        }
        
        return result;
    }

    /**
     * Interpoliert zwischen mehreren Farben
     */
    private static TextColor interpolateColors(List<TextColor> colors, float ratio) {
        if (colors.size() == 1) return colors.get(0);
        
        // Bestimme zwischen welchen Farben interpoliert werden soll
        float segmentSize = 1.0f / (colors.size() - 1);
        int segmentIndex = Math.min((int) (ratio / segmentSize), colors.size() - 2);
        float segmentRatio = (ratio - (segmentIndex * segmentSize)) / segmentSize;
        
        TextColor start = colors.get(segmentIndex);
        TextColor end = colors.get(segmentIndex + 1);
        
        int startRGB = start.value();
        int endRGB = end.value();
        
        int r = interpolate((startRGB >> 16) & 0xFF, (endRGB >> 16) & 0xFF, segmentRatio);
        int g = interpolate((startRGB >> 8) & 0xFF, (endRGB >> 8) & 0xFF, segmentRatio);
        int b = interpolate(startRGB & 0xFF, endRGB & 0xFF, segmentRatio);
        
        return TextColor.color(r, g, b);
    }

    /**
     * Interpoliert zwischen zwei Werten
     */
    private static int interpolate(int start, int end, float ratio) {
        return Math.max(0, Math.min(255, (int) (start + (end - start) * ratio)));
    }

    /**
     * Gibt die Legacy-Farbe für einen Code zurück
     */
    private static TextColor getLegacyColor(char code) {
        return switch (Character.toLowerCase(code)) {
            case '0' -> TextColor.color(0x000000); // Schwarz
            case '1' -> TextColor.color(0x0000AA); // Dunkelblau
            case '2' -> TextColor.color(0x00AA00); // Dunkelgrün
            case '3' -> TextColor.color(0x00AAAA); // Dunkel Aqua
            case '4' -> TextColor.color(0xAA0000); // Dunkelrot
            case '5' -> TextColor.color(0xAA00AA); // Dunkel Lila
            case '6' -> TextColor.color(0xFFAA00); // Gold
            case '7' -> TextColor.color(0xAAAAAA); // Grau
            case '8' -> TextColor.color(0x555555); // Dunkelgrau
            case '9' -> TextColor.color(0x5555FF); // Blau
            case 'a' -> TextColor.color(0x55FF55); // Grün
            case 'b' -> TextColor.color(0x55FFFF); // Aqua
            case 'c' -> TextColor.color(0xFF5555); // Rot
            case 'd' -> TextColor.color(0xFF55FF); // Lila
            case 'e' -> TextColor.color(0xFFFF55); // Gelb
            case 'f' -> TextColor.color(0xFFFFFF); // Weiß
            default -> null;
        };
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
}
