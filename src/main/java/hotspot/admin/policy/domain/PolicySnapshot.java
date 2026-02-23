package hotspot.admin.policy.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PolicySnapshot {

    private List<DayOfWeek> days;

    private String startTime;

    private String endTime;

    private Integer durationMinutes;

    @JsonIgnore
    public LocalTime getStartLocalTime() {
        return startTime != null ? LocalTime.parse(startTime) : null;
    }

    @JsonIgnore
    public LocalTime getEndLocalTime() {
        return endTime != null ? LocalTime.parse(endTime) : null;
    }

    @JsonIgnore
    public boolean isScheduledPolicy() {
        return days != null && !days.isEmpty() && startTime != null && endTime != null;
    }

    @JsonIgnore
    public boolean isOncePolicy() {
        return (durationMinutes != null && durationMinutes > 0) || (startTime != null && endTime != null);
    }
}
