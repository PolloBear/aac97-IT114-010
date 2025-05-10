package Project.Common;

import java.util.Map;

public class PointsPayload extends Payload {
    private Map<Long, Integer> pointsMap;

    public PointsPayload() {
        setPayloadType(PayloadType.POINTS_SYNC);
    }

    public Map<Long, Integer> getPointsMap() {
        return pointsMap;
    }

    public void setPointsMap(Map<Long, Integer> pointsMap) {
        this.pointsMap = pointsMap;
    }

    @Override
    public String toString() {
        return "PointsPayload{" +
                "pointsMap=" + pointsMap +
                '}';
    }
}
