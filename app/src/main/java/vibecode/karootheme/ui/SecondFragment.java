package vibecode.karootheme.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

import vibecode.karootheme.service.OfflineMapFileService;
import vibecode.karootheme.service.MapColorStorage;

public class SecondFragment extends Fragment {
    public SecondFragment() {
        super(R.layout.fragment_second);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button restoreColorsButton = view.findViewById(R.id.button_restore_colors);
        final Button restoreFileButton = view.findViewById(R.id.button_restore_file);
        final TextView currentFileNameText = view.findViewById(R.id.text_current_file_name);
        final SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        restoreColorsButton.setOnClickListener(v ->
                MapColorStorage.writeColors(requireContext(), MapColorStorage.getDefaultColors()));
        restoreFileButton.setOnClickListener(v -> {
            final File offlineFile = OfflineMapFileService.findLatestOfflineFile();
            final boolean restored = OfflineMapFileService.restoreFromBackup(offlineFile);
            if (restored) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Successfully restored")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
        viewModel.getCurrentFileName().observe(getViewLifecycleOwner(),
                value -> currentFileNameText.setText(value == null ? "" : value));
    }
}
