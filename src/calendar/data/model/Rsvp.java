package calendar.data.model;

import java.sql.Timestamp;

public class Rsvp {
    private int formId;
    private int senderId;
    private String eventName;
    private Timestamp eventStartAt;
    private Timestamp eventEndAt;
    private Timestamp expiresAt;

    public Rsvp(String eventName, Timestamp eventStartAt, Timestamp eventEndAt, Timestamp expiresAt) {
        this(-1, -1, eventName, eventStartAt, eventEndAt, expiresAt);
    }

    public Rsvp(int formId, int senderId, String eventName, Timestamp eventStartAt, Timestamp eventEndAt, Timestamp expiresAt) {
        this.formId = formId;
        this.senderId = senderId;
        this.eventName = eventName;
        this.eventStartAt = eventStartAt;
        this.eventEndAt = eventEndAt;
        this.expiresAt = expiresAt;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Timestamp getEventStartAt() {
        return eventStartAt;
    }

    public void setEventStartAt(Timestamp eventStartAt) {
        this.eventStartAt = eventStartAt;
    }

    public Timestamp getEventEndAt() {
        return eventEndAt;
    }

    public void setEventEndAt(Timestamp eventEndAt) {
        this.eventEndAt = eventEndAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "Rsvp{" +
                "formId=" + formId +
                ", senderId=" + senderId +
                ", eventName='" + eventName + '\'' +
                ", eventStartAt=" + eventStartAt +
                ", eventEndAt=" + eventEndAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
