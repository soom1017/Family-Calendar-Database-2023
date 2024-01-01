package calendar.data.model;

public class RsvpResponseResult {
    private String recipientName;
    private String recipientEmail;
    private int status;

    public RsvpResponseResult(String recipientName, String recipientEmail, int status) {
        this.recipientName = recipientName;
        this.recipientEmail = recipientEmail;
        this.status = status;
    }
    public String getRecipientName() {
        return recipientName;
    }
    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getStatus() {
        String statusToString = "";
        switch (status) {
            case 0:
                statusToString = "답변 대기 중";
                break;
            case 1:
                statusToString = "수락";
                break;
            case 2:
                statusToString = "거절";
                break;
            default:
                break;
        }
        return statusToString;
    }
    public void setStatus(int status) {
        this.status = status;
    }
}
