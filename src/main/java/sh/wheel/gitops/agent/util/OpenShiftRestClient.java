package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import sh.wheel.gitops.agent.model.ApiResource;
import sh.wheel.gitops.agent.model.ApiResourceRequest;
import sh.wheel.gitops.agent.model.Resource;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OpenShiftRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String apiServerUrl;
    private String accessToken;
    private RestTemplate restTemplate;

    public OpenShiftRestClient(String apiServerUrl, String accessToken, RestTemplate restTemplate) {
        this.apiServerUrl = apiServerUrl;
        this.accessToken = accessToken;
        this.restTemplate = restTemplate;
    }

    public static OpenShiftRestClient create(String apiServerUrl, String accessToken) {
        return new OpenShiftRestClient(apiServerUrl, accessToken, new RestTemplate());
    }

    public List<Resource> fetchResources(ApiResource apiResource, String namespace) {
        final HttpHeaders headers = createHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String baseUrl;
        if(apiResource.getApiGroup().isEmpty()) {
            baseUrl = "/api/"+apiResource.getApiVersion();
        }
        String endpoint = apiServerUrl + "/apis/project.openshift.io/v1/projects";
        ResponseEntity<ObjectNode> user = restTemplate.exchange(endpoint, HttpMethod.GET, entity, ObjectNode.class);

        return null;
    }

    public String whoAmI() {
        final HttpHeaders headers = createHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String endpoint = apiServerUrl + "/apis/user.openshift.io/v1/users/~";
        ResponseEntity<ObjectNode> user = restTemplate.exchange(endpoint, HttpMethod.GET, entity, ObjectNode.class);
        return user.getBody().get("metadata").get("name").textValue();
    }

    public List<JsonNode> getAllProjects() {
        final HttpHeaders headers = createHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String endpoint = apiServerUrl + "/apis/project.openshift.io/v1/projects";
        LOG.info("GET {}", endpoint);
        ResponseEntity<ObjectNode> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, ObjectNode.class);
        JsonNode items = Objects.requireNonNull(response.getBody()).get("items");
        return StreamSupport.stream(items.spliterator(), false).collect(Collectors.toList());
    }

    private HttpHeaders createHttpHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private ApiResource createApiResource(JsonNode r, String apiGroup, String version) {
        String name = r.get("name").textValue();
        boolean isSubresource = name.contains("/");
        String kind = r.get("kind").textValue();
        boolean namespaced = r.get("namespaced").booleanValue();
        List<String> verbs = StreamSupport.stream(r.get("verbs").spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());

        return new ApiResource(name, isSubresource, kind, apiGroup, version, namespaced, verbs);
    }

    public List<ApiResource> getManageableResources(String user, String namespace, List<String> requiredVerbs, List<ApiResource> relevantApiResources) {
        List<JsonNode> rules = fetchRules(user, namespace);
        long start = System.currentTimeMillis();
        List<ApiResource> manageableResources = relevantApiResources.stream().filter(r -> doRulesApply(r, rules, requiredVerbs)).collect(Collectors.toList());
        LOG.info("Time to calculate manageable resources: " + (System.currentTimeMillis() - start) + "ms");
        return manageableResources;
    }

    private List<JsonNode> fetchRules(String user, String namespace) {
        long start = System.currentTimeMillis();
        String postRequest = "{\"kind\":\"SubjectRulesReview\",\"apiVersion\":\"authorization.openshift.io/v1\",\"spec\":{\"user\":\"" + user + "\",\"groups\":null,\"scopes\":null},\"status\":{\"rules\":null}}";
        final HttpHeaders headers = createHttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(postRequest, headers);
        String endpointUrl = apiServerUrl + "/apis/authorization.openshift.io/v1/namespaces/" + namespace + "/subjectrulesreviews";
        ResponseEntity<ObjectNode> apis = restTemplate.exchange(endpointUrl, HttpMethod.POST, entity, ObjectNode.class);
        List<JsonNode> rules = StreamSupport.stream(apis.getBody().get("status").get("rules").spliterator(), false)
                .collect(Collectors.toList());
        LOG.info("Time to gather rules: " + (System.currentTimeMillis() - start) + "ms");
        return rules;
    }

    public List<ApiResource> getFilteredApiResources(boolean namespaced, List<String> requiredVerbs) {
        long start = System.currentTimeMillis();
        List<ApiResource> result = this.getAllApiResources().stream()
                .filter(apiResource1 -> apiResource1.isNamespaced() == namespaced)
                .filter(apiResource -> !apiResource.isSubresource())
                .filter(r -> r.getVerbs().containsAll(requiredVerbs))
                .collect(Collectors.toList());
        LOG.info("Time to gather filtered api resources: " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    private boolean doRulesApply(ApiResource r, List<JsonNode> rules, List<String> requiredVerbs) {
        for (JsonNode rule : rules) {
            String resource = rule.get("resources").get(0).textValue();
            String apiGroup = rule.get("apiGroups").get(0).textValue();
            if (doesRuleMatchApiResource(rule, r, requiredVerbs)) {
                return true;
            }
        }
        return false;
    }

    public List<ApiResource> getAllApiResources() {
        List<ApiResource> result = new ArrayList<>();
        ResponseEntity<ObjectNode> apis = fetchApis();
        ResponseEntity<ObjectNode> coreApis = fetchCoreApis();
        List<ApiResourceRequest> requests = generateApiRequests(coreApis, apis);
        return fetchApiResourceRequests(requests);
    }

    private boolean doesRuleMatchApiResource(JsonNode rule, ApiResource apiResource, List<String> requiredVerbs) {
        String resource = rule.get("resources").get(0).textValue();
        String apiGroup = rule.get("apiGroups").get(0).textValue();
        List<String> verbs = StreamSupport.stream(rule.get("verbs").spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());
        if (apiResource.getName().equals(resource) || resource.equals("*")) {
            if (apiResource.getApiGroup().isEmpty() && apiGroup.isEmpty()
                    || (!apiGroup.isEmpty() && apiResource.getApiGroup().startsWith(apiGroup))
                    || apiGroup.equals("*")) {
                if (verbs.contains("*") || verbs.containsAll(requiredVerbs)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ResponseEntity<ObjectNode> fetchApis() {
        final HttpHeaders headers = createHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(apiServerUrl + "/apis?timeout=32s", HttpMethod.GET, entity, ObjectNode.class);
    }

    private ResponseEntity<ObjectNode> fetchCoreApis() {
        final HttpHeaders headers = createHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(apiServerUrl + "/api?timeout=32s", HttpMethod.GET, entity, ObjectNode.class);
    }

    private List<ApiResourceRequest> generateApiRequests(ResponseEntity<ObjectNode> coreApis, ResponseEntity<ObjectNode> apis) {
        List<ApiResourceRequest> requests = new ArrayList<>();
        JsonNode coreVersions = coreApis.getBody().get("versions");
        for (JsonNode coreVersion : coreVersions) {
            String coreApiEndpoint = apiServerUrl + "/api/" + coreVersion.textValue() + "?timeout=32s";
            requests.add(new ApiResourceRequest(coreApiEndpoint, ""));
        }
        JsonNode groups = apis.getBody().get("groups");
        for (JsonNode group : groups) {
            JsonNode versions = group.get("versions");
            String apiGroup = group.get("name").textValue();
            for (JsonNode version : versions) {
                String groupVersion = version.get("groupVersion").textValue();
                String customApiEndpoint = apiServerUrl + "/apis/" + groupVersion + "?timeout=32s";
                requests.add(new ApiResourceRequest(customApiEndpoint, apiGroup));
            }
        }
        return requests;
    }

    private List<ApiResource> fetchApiResourceRequests(List<ApiResourceRequest> requests) {
        final HttpHeaders headers = createHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        List<CompletableFuture<List<ApiResource>>> completableFutures = requests.stream().map(r -> fetchApiResource(entity, r)).collect(Collectors.toList());
        return completableFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private CompletableFuture<List<ApiResource>> fetchApiResource(HttpEntity<String> entity, ApiResourceRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            List<ApiResource> result = new ArrayList<>();
            ResponseEntity<ObjectNode> apiResources = restTemplate.exchange(request.getApiEndpoint(), HttpMethod.GET, entity, ObjectNode.class);
            ObjectNode body = apiResources.getBody();
            JsonNode resources = body.get("resources");
            String apiVersion = body.get("apiVersion") != null ? body.get("apiVersion").textValue() : "";
            String groupVersion = body.get("groupVersion").textValue();
            for (JsonNode resource : resources) {
                result.add(createApiResource(resource, request.getApiGroup(), apiVersion));
            }
            return result;
        });
    }

}
