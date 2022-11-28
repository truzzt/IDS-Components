package de.fraunhofer.iais.eis.ids.component.core;

public class SerializedPayload {

    public static SerializedPayload EMPTY = new SerializedPayload();
    private byte[] serialization;
    private String contentType, filename;

    private SerializedPayload() {
    }

    public SerializedPayload(byte[] serialization) {
        this.serialization = serialization;
    }

    public SerializedPayload(byte[] serialization, String contentType) {
        this.serialization = serialization;
        this.contentType = contentType;
    }

    public SerializedPayload(byte[] serialization, String contentType, String filename) {
        this.serialization = serialization;
        this.contentType = contentType;
        this.filename = filename;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getSerialization() {
        return serialization;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilename() {
        return filename;
    }

}
