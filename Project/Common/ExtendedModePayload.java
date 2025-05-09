package Project.Common;

public class ExtendedModePayload extends Payload {
    private boolean extended;

    public ExtendedModePayload() {
        setPayloadType(PayloadType.EXTENDED_MODE);
    }

    public boolean isExtended() {
        return extended;
    }

    public void setExtended(boolean extended) {
        this.extended = extended;
    }
}
