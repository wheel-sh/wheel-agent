package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class RecordedRequestResponse {
    private String endpoint;
    private String type;
    private JsonNode requestObject;
    private String responseStatus;
    private JsonNode responseObject;

    boolean matchesRequest(String endpoint, String type, JsonNode requestObject) {
        boolean isMatchingRequestObjectNull = requestObject == null || requestObject.isNull();
        boolean isComparingRequestObjectNull = this.requestObject == null || this.requestObject.isNull();
        boolean requestPresent = !isComparingRequestObjectNull && !isMatchingRequestObjectNull;

        return this.endpoint.equals(endpoint) && this.type.equals(type)
                && (isMatchingRequestObjectNull && isComparingRequestObjectNull
                || requestPresent && requestObject.equals(this.requestObject));
    }
}
