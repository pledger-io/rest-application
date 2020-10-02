package com.jongsoft.finance.rest.file;

class UploadResponse {

    private String fileCode;

    public UploadResponse(String fileCode) {
        this.fileCode = fileCode;
    }

    public String getFileCode() {
        return fileCode;
    }
}
