package org.example.model.domain;

/**
 * Result of a music generation request.
 * Simple encapsulated data holder.
 */
public class GenerationResult {

    private String taskId;
    private String status;
    private String audioUrl;

    public String getTaskId() {
        return taskId;
    }

    public GenerationResult setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public GenerationResult setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public GenerationResult setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
        return this;
    }

    @Override
    public String toString() {
        return "GenerationResult{" +
                "taskId='" + taskId + '\'' +
                ", status='" + status + '\'' +
                ", audioUrl='" + audioUrl + '\'' +
                '}';
    }
}
