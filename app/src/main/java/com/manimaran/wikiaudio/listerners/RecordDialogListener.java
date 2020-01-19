package com.manimaran.wikiaudio.listerners;

public interface RecordDialogListener {
    void startRecord();
    void endRecord();
    void uploadStart();
    void uploadSuccess();
    void uploadFailed();
}
