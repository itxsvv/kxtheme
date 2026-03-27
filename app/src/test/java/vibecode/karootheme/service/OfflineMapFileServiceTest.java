package vibecode.karootheme.service;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OfflineMapFileServiceTest {
    @Rule
    public final TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void backupIfNeeded_createsBackupWhenMissing() throws IOException {
        final File sourceFile = createFile("offline_v15.xml", "original");

        OfflineMapFileService.backupIfNeeded(sourceFile);

        final File backupFile = new File(sourceFile.getAbsolutePath() + ".bak");
        assertTrue(backupFile.exists());
        assertEquals("original", readFile(backupFile));
    }

    @Test
    public void backupIfNeeded_doesNotOverrideExistingBackup() throws IOException {
        final File sourceFile = createFile("offline_v15.xml", "original");
        final File backupFile = createFile("offline_v15.xml.bak", "backup");

        OfflineMapFileService.backupIfNeeded(sourceFile);

        assertEquals("backup", readFile(backupFile));
    }

    @Test
    public void isBackupMissing_returnsTrueWhenBackupDoesNotExist() throws IOException {
        final File sourceFile = createFile("offline_v15.xml", "original");

        final boolean backupMissing = OfflineMapFileService.isBackupMissing(sourceFile);

        assertTrue(backupMissing);
    }

    @Test
    public void isBackupMissing_returnsFalseWhenBackupExists() throws IOException {
        final File sourceFile = createFile("offline_v15.xml", "original");
        createFile("offline_v15.xml.bak", "backup");

        final boolean backupMissing = OfflineMapFileService.isBackupMissing(sourceFile);

        assertFalse(backupMissing);
    }

    @Test
    public void restoreFromBackup_restoresSourceAndKeepsBackup() throws IOException {
        final File sourceFile = createFile("offline_v15.xml", "modified");
        final File backupFile = createFile("offline_v15.xml.bak", "backup");

        final boolean restored = OfflineMapFileService.restoreFromBackup(sourceFile);

        assertTrue(restored);
        assertEquals("backup", readFile(sourceFile));
        assertTrue(backupFile.exists());
        assertEquals("backup", readFile(backupFile));
    }

    @Test
    public void restoreFromBackup_returnsFalseWhenBackupIsMissing() throws IOException {
        final File sourceFile = createFile("offline_v15.xml", "modified");

        final boolean restored = OfflineMapFileService.restoreFromBackup(sourceFile);

        assertFalse(restored);
        assertEquals("modified", readFile(sourceFile));
    }

    @Test
    public void findLatestOfflineFile_returnsFileWithLargestVersion() throws IOException {
        createFile("offline_v2.xml", "2");
        final File latestFile = createFile("offline_v15.xml", "15");
        createFile("offline_v9.xml", "9");
        createFile("offline_v15.xml.bak", "backup");
        createFile("notes.txt", "ignored");

        final File foundFile = OfflineMapFileService.findLatestOfflineFile(temporaryFolder.getRoot());

        assertNotNull(foundFile);
        assertEquals(latestFile.getName(), foundFile.getName());
    }

    @Test
    public void findLatestOfflineFile_returnsNullWhenDirectoryHasNoMatchingFiles() throws IOException {
        createFile("offline_v15.xml.bak", "backup");
        createFile("notes.txt", "ignored");

        final File foundFile = OfflineMapFileService.findLatestOfflineFile(temporaryFolder.getRoot());

        assertNull(foundFile);
    }

    private File createFile(final String fileName, final String content) throws IOException {
        final File file = temporaryFolder.newFile(fileName);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    private String readFile(final File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}
