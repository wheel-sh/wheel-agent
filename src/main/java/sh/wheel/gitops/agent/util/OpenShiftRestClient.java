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
import sh.wheel.gitops.agent.model.ResourceKey;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OpenShiftRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String apiServerUrl;
    private String accessToken;
    private RestTemplate restTemplate;
    private HttpEntity<String> httpEntity;

    public OpenShiftRestClient(String apiServerUrl, String accessToken, RestTemplate restTemplate, HttpEntity<String> defaultHttpEntity) {
        this.apiServerUrl = apiServerUrl;
        this.accessToken = accessToken;
        this.restTemplate = restTemplate;
        this.httpEntity = defaultHttpEntity;
    }

    public static OpenShiftRestClient create(String apiServerUrl, String accessToken) {
        final HttpHeaders headers = createHttpHeaders(accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);
        return new OpenShiftRestClient(apiServerUrl, accessToken, new RestTemplate(), httpEntity);
    }

    public List<Resource> fetchNamespacedResourceList(ApiResource apiResource, String namespace) {
        String baseUrl;
        if (apiResource.isCoreApi()) {
            baseUrl = "/api/" + apiResource.getGroupVersion() + "/namespaces/" + namespace + "/";
        } else {
            baseUrl = "/apis/" + apiResource.getGroupVersion() + "/namespaces/" + namespace + "/";
        }
        String endpoint = apiServerUrl + baseUrl + apiResource.getName();
        ResponseEntity<ObjectNode> resourceList = restTemplate.exchange(endpoint, HttpMethod.GET, httpEntity, ObjectNode.class);
        String listKind = resourceList.getBody().get("kind").textValue();
        String kind = listKind.substring(0, listKind.lastIndexOf("List"));
        String apiVersion = resourceList.getBody().get("apiVersion").textValue();
        return StreamSupport.stream(resourceList.getBody().get("items").spliterator(), false)
                .map(jsonNode -> mapToResource(jsonNode, apiVersion, kind))
                .collect(Collectors.toList());
    }

    private Resource mapToResource(JsonNode jsonNode, String apiVersion, String kind) {
        String name = jsonNode.get("metadata").get("name").textValue();
        String uid = jsonNode.get("metadata").get("uid").textValue();
        return new Resource(new ResourceKey(name, kind, apiVersion), uid, jsonNode);
    }

    public String whoAmI() {
        String endpoint = apiServerUrl + "/apis/user.openshift.io/v1/users/~";
        JsonNode me = get(endpoint);
        return me.get("metadata").get("name").textValue();
    }

    public List<JsonNode> getAllProjects() {
        String endpoint = apiServerUrl + "/apis/project.openshift.io/v1/projects";
        JsonNode projects = get(endpoint);
        JsonNode items = projects.get("items");
        return StreamSupport.stream(items.spliterator(), false).collect(Collectors.toList());
    }

    private static HttpHeaders createHttpHeaders(String accessToken) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        return headers;
    }

    private JsonNode get(String url) {
        ResponseEntity<ObjectNode> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, ObjectNode.class);
        return response.getBody();
    }

    private ApiResource createApiResource(JsonNode r, String groupName, String groupVersion, String apiVersion) {
        String name = r.get("name").textValue();
        boolean isSubresource = name.contains("/");
        String kind = r.get("kind").textValue();
        boolean namespaced = r.get("namespaced").booleanValue();
        List<String> verbs = StreamSupport.stream(r.get("verbs").spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());
        return ApiResource.newBuilder()
                .name(name)
                .kind(kind)
                .groupName(groupName)
                .groupVersion(groupVersion)
                .apiVersion(apiVersion)
                .coreApi(groupVersion.isEmpty())
                .subresource(isSubresource)
                .namespaced(namespaced)
                .verbs(verbs)
                .build();
    }

    public List<ApiResource> fetchManageableResources(String user, String namespace, List<String> requiredVerbs, List<ApiResource> relevantApiResources) {
        List<JsonNode> rules = fetchRules(user, namespace);
        long start = System.currentTimeMillis();
        List<ApiResource> manageableResources = relevantApiResources.stream().filter(r -> doRulesApply(r, rules, requiredVerbs)).collect(Collectors.toList());
        LOG.info("Time to calculate manageable resources: " + (System.currentTimeMillis() - start) + "ms");
        return manageableResources;
    }

    private List<JsonNode> fetchRules(String user, String namespace) {
        long start = System.currentTimeMillis();
        String postRequest = "{\"kind\":\"SubjectRulesReview\",\"apiVersion\":\"authorization.openshift.io/v1\",\"spec\":{\"user\":\"" + user + "\",\"groups\":null,\"scopes\":null},\"status\":{\"rules\":null}}";
        final HttpHeaders headers = createHttpHeaders(accessToken);
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
        JsonNode apis = fetchApis();
        JsonNode coreApis = fetchCoreApis();
        List<ApiResourceRequest> requests = generateApiRequests(coreApis, apis);
        return fetchApiResourceRequests(requests);
    }

    private boolean doesRuleMatchApiResource(JsonNode rule, ApiResource apiResource, List<String> requiredVerbs) {
        String resource = rule.get("resources").get(0).textValue();
        String apiGroup = rule.get("apiGroups").get(0).textValue();
        List<String> verbs = StreamSupport.stream(rule.get("verbs").spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());
        if (apiResource.getName().equals(resource) || resource.equals("*")) {
            if (apiResource.getGroupVersion().isEmpty() && apiGroup.isEmpty()
                    || (!apiGroup.isEmpty() && apiResource.getGroupVersion().startsWith(apiGroup))
                    || apiGroup.equals("*")) {
                if (verbs.contains("*") || verbs.containsAll(requiredVerbs)) {
                    return true;
                }
            }
        }
        return false;
    }

    private JsonNode fetchApis() {
        String endpoint = apiServerUrl + "/apis?timeout=32s";
        return get(endpoint);
    }

    private JsonNode fetchCoreApis() {
        String endpoint = apiServerUrl + "/api?timeout=32s";
        return get(endpoint);
    }

    private List<ApiResourceRequest> generateApiRequests(JsonNode coreApis, JsonNode apis) {
        List<ApiResourceRequest> requests = new ArrayList<>();
        JsonNode coreVersions = coreApis.get("versions");
        for (JsonNode coreVersion : coreVersions) {
            String coreApiEndpoint = apiServerUrl + "/api/" + coreVersion.textValue() + "?timeout=32s";
            requests.add(new ApiResourceRequest(coreApiEndpoint, ""));
        }
        JsonNode groups = apis.get("groups");
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
        List<CompletableFuture<List<ApiResource>>> completableFutures = requests.stream().map(this::fetchApiResource).collect(Collectors.toList());
        return completableFutures.stream()
                .map(CompletableFuture::join)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private CompletableFuture<List<ApiResource>> fetchApiResource(ApiResourceRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            List<ApiResource> result = new ArrayList<>();
            JsonNode apiResources = get(request.getApiEndpoint());
            String apiVersion = apiResources.get("apiVersion") != null ? apiResources.get("apiVersion").textValue() : "";
            String groupVersion = apiResources.get("groupVersion").textValue();
            JsonNode resources = apiResources.get("resources");
            for (JsonNode resource : resources) {
                result.add(createApiResource(resource, request.getGroupName(), groupVersion, apiVersion));
            }
            return result;
        });
    }

    public List<Resource> fetchResourcesFromNamespace(List<ApiResource> manageableResources, String namespace) {
        long start = System.currentTimeMillis();
        List<CompletableFuture<List<Resource>>> requests = manageableResources.stream().map(ar -> CompletableFuture.supplyAsync(() -> fetchNamespacedResourceList(ar, namespace))).collect(Collectors.toList());
        List<Resource> result = requests.stream().map(CompletableFuture::join).flatMap(Collection::stream).collect(Collectors.toList());
        LOG.info("Time to gather " + manageableResources.size() + " resources: " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    public Resource fetchProject(String projectName) {
        String endpoint = apiServerUrl + "/apis/project.openshift.io/v1/projects/" + projectName;
        JsonNode projectNode = get(endpoint);
        return mapToResource(projectNode, "project.openshift.io/v1", "Project");
    }

    public void delete(ResourceKey resource, String namespace) {
    }
}
