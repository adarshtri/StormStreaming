package pojos;

public class ObdCall {

    private String campaign_id;
    private String status;
    private String attempts;
    private String callTime;


    public ObdCall(String campaign_id, String attempts, String status, String callTime){
        this.setCampaign_id(campaign_id);
        this.setAttempts(attempts);
        this.setStatus(status);
        this.setCallTime(callTime);
    }

    public String getCampaign_id() {
        return campaign_id;
    }

    public void setCampaign_id(String campaign_id) {
        this.campaign_id = campaign_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAttempts() {
        return attempts;
    }

    public void setAttempts(String attempts) {
        this.attempts = attempts;
    }

    public String getCallTime() {
        return callTime;
    }

    public void setCallTime(String callTime) {
        this.callTime = callTime;
    }
}
