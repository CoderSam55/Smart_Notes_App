package com.sampatil.smartnotes.models;

import com.google.gson.annotations.SerializedName;

public class Note {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("subject")
    private String subject;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("topic")
    private String topic;
    
    @SerializedName("local_pdf_path")
    private String localPdfPath;
    
    @SerializedName("text_content")
    private String textContent;
    
    @SerializedName("color_hex")
    private String colorHex;
    
    @SerializedName("drawing_path")
    private String drawingPath;
    
    @SerializedName("created_at")
    private String createdAt;

    public Note() {}

    public Note(String id, String subject, String topic, String localPdfPath) {
        this.id = id;
        this.subject = subject;
        this.topic = topic;
        this.localPdfPath = localPdfPath;
        this.colorHex = "#B2FAB4";
    }
    
    public Note(String id, String subject, String textContent, String colorHex, boolean isText) {
        this.id = id;
        this.subject = subject;
        this.textContent = textContent;
        this.colorHex = colorHex;
        this.topic = "Text Note";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getLocalPdfPath() { return localPdfPath; }
    public void setLocalPdfPath(String localPdfPath) { this.localPdfPath = localPdfPath; }

    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }

    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }

    public String getDrawingPath() { return drawingPath; }
    public void setDrawingPath(String drawingPath) { this.drawingPath = drawingPath; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
