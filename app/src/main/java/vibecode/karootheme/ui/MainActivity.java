package vibecode.karootheme.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.OnBackPressedCallback;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import vibecode.karootheme.service.OfflineMapFileService;
import vibecode.karootheme.service.MapColorStorage;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1001;

    private BottomNavigationView bottomNavigationView;
    private FrameLayout fragmentContainer;
    private View statusBarSpacer;
    private boolean storageAccessRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapColorStorage.ensureAndLoad(this);
        getWindow().setStatusBarColor(Color.parseColor("#1F1F1F"));
        setContentView(R.layout.activity_main);

        statusBarSpacer = findViewById(R.id.status_bar_spacer);
        fragmentContainer = findViewById(R.id.fragment_container);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        applySystemInsets();
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_first) {
                showFragment(new FirstFragment(), FirstFragment.class.getSimpleName());
                return true;
            }

            if (item.getItemId() == R.id.navigation_second) {
                showFragment(new SecondFragment(), SecondFragment.class.getSimpleName());
                return true;
            }

            return false;
        });

        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.navigation_first);
        } else {
            syncSelectionFromCurrentFragment();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ensureManageExternalStorageAccess()) {
            return;
        }
        initializeOfflineMapFile();
    }

    private void applySystemInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(fragmentContainer, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            statusBarSpacer.getLayoutParams().height = insets.top;
            statusBarSpacer.requestLayout();
            view.setPadding(0, 0, 0, 0);
            return windowInsets;
        });
        ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    insets.bottom
            );
            return windowInsets;
        });
        ViewCompat.requestApplyInsets(fragmentContainer);
        ViewCompat.requestApplyInsets(bottomNavigationView);
    }

    private void initializeOfflineMapFile() {
        File offlineFile = OfflineMapFileService.findLatestOfflineFile();
        boolean shouldShowNewThemeFileAlert = OfflineMapFileService.isBackupMissing(offlineFile);
        OfflineMapFileService.backupIfNeeded(offlineFile);
        SharedViewModel viewModel = new ViewModelProvider(this).get(SharedViewModel.class);
        viewModel.setCurrentFileName(offlineFile == null ? "" : offlineFile.getName());
        if (shouldShowNewThemeFileAlert) {
            showNewThemeFileAlert();
        }
    }

    private boolean ensureManageExternalStorageAccess() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return ensureLegacyStorageAccess();
        }
        if (Environment.isExternalStorageManager()) {
            return true;
        }

        if (storageAccessRequested) {
            showStorageAccessRequiredDialog();
            return false;
        }

        storageAccessRequested = true;
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
        return false;
    }

    private boolean ensureLegacyStorageAccess() {
        final boolean hasReadPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED;
        final boolean hasWritePermission = Build.VERSION.SDK_INT > Build.VERSION_CODES.P
                || ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED;
        if (hasReadPermission && hasWritePermission) {
            return true;
        }

        if (storageAccessRequested) {
            showStorageAccessRequiredDialog();
            return false;
        }

        storageAccessRequested = true;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST_CODE
            );
            return false;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                STORAGE_PERMISSION_REQUEST_CODE
        );
        return false;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String[] permissions,
                                           final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != STORAGE_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (ensureManageExternalStorageAccess()) {
            initializeOfflineMapFile();
        }
    }

    private void showStorageAccessRequiredDialog() {
        new AlertDialog.Builder(this)
                .setMessage("All files access is required. The application will now close.")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                .show();
    }

    private void showNewThemeFileAlert() {
        new AlertDialog.Builder(this)
                .setMessage("A new theme file!.\nPlease APPLY it again.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showFragment(androidx.fragment.app.Fragment fragment, String tag) {
        getSupportFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container, fragment, tag)
                .commit();
    }

    private void syncSelectionFromCurrentFragment() {
        androidx.fragment.app.Fragment fragment =
                getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        int selectedItemId = fragment instanceof SecondFragment
                ? R.id.navigation_second
                : R.id.navigation_first;
        Menu menu = bottomNavigationView.getMenu();
        menu.findItem(selectedItemId).setChecked(true);
    }
}
