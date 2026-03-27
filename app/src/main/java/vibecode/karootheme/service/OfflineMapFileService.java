package vibecode.karootheme.service;

import android.os.Environment;

import vibecode.karootheme.ui.MapColors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class OfflineMapFileService {
    private static final File LEGACY_SDCARD_DIRECTORY = new File("/sdcard");
    private static final Pattern OFFLINE_FILE_PATTERN = Pattern.compile("^offline_v(\\d+)\\.xml$");
    private static final String FARM_AREA_VALUE = "farm|farmyard|farmland|orchard|vineyard";
    private static final String SCRUB_AREA_VALUE = "grassland|scrub";

    private OfflineMapFileService() {
    }

    public static File findLatestOfflineFile() {
        final File searchDirectory = getSearchDirectory();
        return findLatestOfflineFile(searchDirectory);
    }

    static File findLatestOfflineFile(final File searchDirectory) {
        final File[] files = searchDirectory.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        int latestVersion = -1;
        File latestFile = null;
        for (final File file : files) {
            if (file == null || !file.isFile()) {
                continue;
            }

            final Matcher matcher = OFFLINE_FILE_PATTERN.matcher(file.getName());
            if (!matcher.matches()) {
                continue;
            }

            final int version = Integer.parseInt(matcher.group(1));
            if (version <= latestVersion) {
                continue;
            }

            latestVersion = version;
            latestFile = file;
        }
        return latestFile;
    }

    public static String getStorageDebugInfo() {
        File searchDirectory = getSearchDirectory();
        return "searchDirectory=" + searchDirectory.getAbsolutePath()
                + ", exists=" + searchDirectory.exists()
                + ", canRead=" + searchDirectory.canRead();
    }

    public static void backupIfNeeded(File sourceFile) {
        if (sourceFile == null) {
            return;
        }

        File backupFile = getBackupFile(sourceFile);
        if (backupFile.exists()) {
            return;
        }

        try {
            Files.copy(sourceFile.toPath(), backupFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
    }

    public static boolean isBackupMissing(File sourceFile) {
        if (sourceFile == null) {
            return false;
        }
        return !getBackupFile(sourceFile).exists();
    }

    public static boolean restoreFromBackup(File sourceFile) {
        if (sourceFile == null) {
            return false;
        }

        File backupFile = getBackupFile(sourceFile);
        if (!backupFile.exists() || !backupFile.isFile()) {
            return false;
        }

        try {
            Files.copy(backupFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    public static boolean applyColors(File sourceFile, MapColors mapColors) {
        if (sourceFile == null || mapColors == null || !sourceFile.exists()) {
            return false;
        }

        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(sourceFile);
            applyWaterColor(document, mapColors);
            applyAreaColor(document, "landuse|natural", "forest|wood",
                    toHexColor(mapColors.getForestColor()));
            applyAreaColor(document, null, FARM_AREA_VALUE,
                    toHexColor(mapColors.getFarmColor()));
            applyAreaColor(document, "natural", SCRUB_AREA_VALUE,
                    toHexColor(mapColors.getScrubColor()));
            applyAreaColor(document, "landuse", "grass",
                    toHexColor(mapColors.getGrassColor()));

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(document), new StreamResult(sourceFile));
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static void applyWaterColor(Document document, MapColors mapColors) {
        NodeList styleAreaNodes = document.getElementsByTagName("style-area");
        for (int index = 0; index < styleAreaNodes.getLength(); index++) {
            Element styleAreaElement = (Element) styleAreaNodes.item(index);
            if ("water".equals(styleAreaElement.getAttribute("id"))) {
                styleAreaElement.setAttribute("fill", toHexColor(mapColors.getRiverColor()));
            }
        }
    }

    private static void applyAreaColor(final Document document, final String key, final String value,
                                       final String fillColor) {
        final NodeList styleNodes = document.getElementsByTagName("m");
        for (int index = 0; index < styleNodes.getLength(); index++) {
            final Element styleElement = (Element) styleNodes.item(index);
            final boolean hasExpectedKey = key == null || key.equals(styleElement.getAttribute("k"));
            if (!hasExpectedKey) {
                continue;
            }
            if (!value.equals(styleElement.getAttribute("v"))) {
                continue;
            }

            final NodeList areaNodes = styleElement.getElementsByTagName("area");
            for (int areaIndex = 0; areaIndex < areaNodes.getLength(); areaIndex++) {
                final Element areaElement = (Element) areaNodes.item(areaIndex);
                areaElement.setAttribute("fill", fillColor);
            }
        }
    }

    private static String toHexColor(int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
    }

    private static File getBackupFile(File sourceFile) {
        return new File(sourceFile.getAbsolutePath() + ".bak");
    }

    private static File getSearchDirectory() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        if (externalStorageDirectory != null
                && externalStorageDirectory.exists()
                && externalStorageDirectory.canRead()) {
            return externalStorageDirectory;
        }
        return LEGACY_SDCARD_DIRECTORY;
    }
}
