package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Objects;

public class RecordedRequestResponse {
    private String endpoint;
    private String type;
    private JsonNode requestObject;
    private String responseStatus;
    private JsonNode responseObject;

    public RecordedRequestResponse(String endpoint, String type, JsonNode requestObject, String responseStatus, JsonNode ResponseObject) {
        this.endpoint = endpoint;
        this.type = type;
        this.requestObject = requestObject;
        this.responseStatus = responseStatus;
        responseObject = ResponseObject;
    }

    public RecordedRequestResponse() {
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getType() {
        return type;
    }

    public JsonNode getRequestObject() {
        return requestObject;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public JsonNode getResponseObject() {
        return responseObject;
    }

    public boolean matchesRequest(String endpoint, String type, JsonNode requestObject) {
        boolean isMatchingRequestObjectNull = requestObject == null || requestObject.isNull();
        boolean isComparingRequestObjectNull = this.requestObject == null || this.requestObject.isNull();
        boolean requestPresent = !isComparingRequestObjectNull && !isMatchingRequestObjectNull;

        return this.endpoint.equals(endpoint) && this.type.equals(type)
                && (isMatchingRequestObjectNull && isComparingRequestObjectNull
                || requestPresent && requestObject.equals(this.requestObject));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecordedRequestResponse that = (RecordedRequestResponse) o;
        return Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(type, that.type) &&
                Objects.equals(requestObject, that.requestObject) &&
                Objects.equals(responseStatus, that.responseStatus) &&
                Objects.equals(responseObject, that.responseObject);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, type, requestObject, responseStatus, responseObject);
    }

    @Override
    public String toString() {
        return "RecordedRequestResponse{" +
                "endpoint='" + endpoint + '\'' +
                ", type='" + type + '\'' +
                ", requestObject=" + requestObject +
                ", responseStatus='" + responseStatus + '\'' +
                ", responseObject=" + responseObject +
                '}';
    }
}
