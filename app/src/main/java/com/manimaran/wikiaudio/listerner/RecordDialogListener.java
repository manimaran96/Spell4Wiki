package com.manimaran.wikiaudio.listerner;

public interface RecordDialogListener {
    void startRecord();
    void endRecord();
    void uploadStart();
    void uploadSuccess();
    void uploadFailed();
}
