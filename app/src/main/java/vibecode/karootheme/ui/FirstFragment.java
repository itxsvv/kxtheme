package vibecode.karootheme.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import vibecode.karootheme.service.OfflineMapFileService;
import vibecode.karootheme.service.MapColorStorage;

import java.io.File;

public class FirstFragment extends Fragment {
    public FirstFragment() {
        super(R.layout.fragment_first);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedViewModel viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        MapCanvasView mapCanvasView = view.findViewById(R.id.map_canvas);
        Button applyButton = view.findViewById(R.id.button_apply);
        mapCanvasView.setOnRegionTapListener(viewModel::setSharedText);
        applyButton.setOnClickListener(v -> {
            File offlineFile = OfflineMapFileService.findLatestOfflineFile();
            boolean applied = OfflineMapFileService.applyColors(
                    offlineFile,
                    MapColorStorage.ensureAndLoad(requireContext())
            );
            if (applied) {
                new AlertDialog.Builder(requireContext())
                        .setMessage("Not required, but it is recommended to restart the device.")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }
}
