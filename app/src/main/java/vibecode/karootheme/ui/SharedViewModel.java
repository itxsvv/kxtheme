package vibecode.karootheme.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<String> sharedText = new MutableLiveData<>("");
    private final MutableLiveData<String> currentFileName = new MutableLiveData<>("");

    public LiveData<String> getSharedText() {
        return sharedText;
    }

    public void setSharedText(String text) {
        sharedText.setValue(text == null ? "" : text);
    }

    public LiveData<String> getCurrentFileName() {
        return currentFileName;
    }

    public void setCurrentFileName(String fileName) {
        currentFileName.setValue(fileName == null ? "" : fileName);
    }
}
