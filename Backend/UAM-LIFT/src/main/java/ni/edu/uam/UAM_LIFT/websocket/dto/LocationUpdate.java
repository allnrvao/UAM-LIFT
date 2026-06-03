package ni.edu.uam.UAM_LIFT.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdate {
    // id representa el viaje actual.
    private String id;
    private Double latitude;
    private Double longitude;
    private String timestamp;
}
