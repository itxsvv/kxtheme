package vibecode.karootheme.service;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import vibecode.karootheme.ui.MapColors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class MapColorStorage {
    private static final String FILE_NAME = "map_colors_v2.json";
    private static final Gson GSON = new Gson();

    private MapColorStorage() {
    }

    public static MapColors getDefaultColors() {
        return new MapColors(0xFFa8bc9a, 0xFFa7c28f, 0xFF8ed496, 0xFFc4deab, 0xFF6aabb8);
    }

    public static MapColors ensureAndLoad(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        MapColors defaultColors = getDefaultColors();

        if (!file.exists() || file.length() == 0L) {
            writeColors(context, defaultColors);
            return defaultColors;
        }

        MapColors colors = readColors(context);
        if (colors == null) {
            writeColors(context, defaultColors);
            return defaultColors;
        }
        return colors;
    }

    public static void writeColors(Context context, MapColors colors) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        String json = GSON.toJson(colors);

        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            outputStream.write(json.getBytes());
        } catch (IOException ignored) {
        }
    }

    private static MapColors readColors(Context context) {
        File file = new File(context.getFilesDir(), FILE_NAME);
        MapColors defaultColors = getDefaultColors();
        byte[] buffer = new byte[(int) file.length()];

        try (FileInputStream inputStream = new FileInputStream(file)) {
            int readBytes = inputStream.read(buffer);
            if (readBytes <= 0) {
                return null;
            }
        } catch (IOException ignored) {
            return null;
        }

        try {
            MapColors colors = GSON.fromJson(new String(buffer).trim(), MapColors.class);
            if (colors == null) {
                return null;
            }
            if (colors.getForestColor() == 0) {
                colors.setForestColor(defaultColors.getForestColor());
            }
            if (colors.getFarmColor() == 0) {
                colors.setFarmColor(defaultColors.getFarmColor());
            }
            if (colors.getScrubColor() == 0) {
                colors.setScrubColor(defaultColors.getScrubColor());
            }
            if (colors.getGrassColor() == 0) {
                colors.setGrassColor(defaultColors.getGrassColor());
            }
            if (colors.getRiverColor() == 0) {
                colors.setRiverColor(defaultColors.getRiverColor());
            }
            return colors;
        } catch (JsonSyntaxException ignored) {
            return null;
        }
    }

}
