package sh.wheel.gitops.agent.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
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
    private RestTemplate restTemplate;

    public OpenShiftRestClient(String apiServerUrl, RestTemplate restTemplate) {
        this.apiServerUrl = apiServerUrl;
        this.restTemplate = restTemplate;
    }

    public static OpenShiftRestClient create() {
        Config config = Config.autoConfigure(null);
        Config sslConfig = new ConfigBuilder(config)
                .withMasterUrl(config.getMasterUrl())
                .withRequestTimeout(5000)
                .withConnectionTimeout(5000)
                .build();
        OkHttpClient client = HttpClientUtils.createHttpClient(sslConfig);
        OkHttp3ClientHttpRequestFactory requestFactory = new OkHttp3ClientHttpRequestFactory(client);
        RestTemplate template = new RestTemplate(requestFactory);
        return new OpenShiftRestClient(config.getMasterUrl(), template);
    }

    public String whoAmI() {
        String endpoint = "/apis/user.openshift.io/v1/users/~";
        JsonNode me = get(endpoint);
        return me.get("metadata").get("name").textValue();
    }

    public List<JsonNode> getAllProjects() {
        String endpoint = "/apis/project.openshift.io/v1/projects";
        JsonNode projects = get(endpoint);
        JsonNode items = projects.get("items");
        return StreamSupport.stream(items.spliterator(), false).collect(Collectors.toList());
    }

    public List<ApiResource> fetchManageableResources(String user, String namespace, List<String> requiredVerbs, List<ApiResource> relevantApiResources) {
        List<JsonNode> rules = fetchRules(user, namespace);
        long start = System.currentTimeMillis();
        List<ApiResource> manageableResources = relevantApiResources.stream().filter(r -> doRulesApply(r, rules, requiredVerbs)).collect(Collectors.toList());
        LOG.info("Time to calculate manageable resources in namespace '" + namespace + "': " + (System.currentTimeMillis() - start) + "ms");
        return manageableResources;
    }

    private List<JsonNode> fetchRules(String user, String namespace) {
        long start = System.currentTimeMillis();
        String postRequest = "{\"kind\":\"SubjectRulesReview\",\"apiVersion\":\"authorization.openshift.io/v1\",\"spec\":{\"user\":\"" + user + "\",\"groups\":null,\"scopes\":null},\"status\":{\"rules\":null}}";
        String endpointUrl = "/apis/authorization.openshift.io/v1/namespaces/" + namespace + "/subjectrulesreviews";
        JsonNode apis = post(endpointUrl, postRequest);
        List<JsonNode> rules = StreamSupport.stream(apis.get("status").get("rules").spliterator(), false)
                .collect(Collectors.toList());
        LOG.info("Time to gather rules for namespace '" + namespace + "': " + (System.currentTimeMillis() - start) + "ms");
        return rules;
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


    private boolean doesRuleMatchApiResource(JsonNode rule, ApiResource apiResource, List<String> requiredVerbs) {
        String resource = rule.get("resources").get(0).textValue();
        String apiGroup = rule.get("apiGroups").get(0).textValue();
        List<String> verbs = StreamSupport.stream(rule.get("verbs").spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());
        if (apiResource.getName().equals(resource) || resource.equals("*")) {
            if (apiResource.getGroupName().isEmpty() && apiGroup.isEmpty()
                    || (!apiGroup.isEmpty() && apiResource.getGroupName().startsWith(apiGroup))
                    || apiGroup.equals("*")) {
                if (verbs.contains("*") || verbs.containsAll(requiredVerbs)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ApiResource> getFilteredApiResources(boolean namespaced, List<String> requiredVerbs) {
        long start = System.currentTimeMillis();
        List<ApiResource> result = this.getAllApiResources().stream()
                .filter(ar -> ar.isNamespaced() == namespaced)
                .filter(ar -> !ar.isSubresource())
                .filter(r -> r.getVerbs().containsAll(requiredVerbs))
                .collect(Collectors.toList());
        LOG.info("Time to gather filtered api resources: " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    public List<ApiResource> getAllApiResources() {
        JsonNode apis = fetchApis();
        JsonNode coreApis = fetchCoreApis();
        List<ApiResourceRequest> requests = generateApiRequests(coreApis, apis);
        return fetchApiResourceRequests(requests);
    }

    private JsonNode fetchApis() {
        String endpoint = "/apis";
        return get(endpoint);
    }

    private JsonNode fetchCoreApis() {
        String endpoint = "/api";
        return get(endpoint);
    }

    private List<ApiResourceRequest> generateApiRequests(JsonNode coreApis, JsonNode apis) {
        List<ApiResourceRequest> requests = new ArrayList<>();
        JsonNode coreVersions = coreApis.get("versions");
        for (JsonNode coreVersion : coreVersions) {
            String coreApiEndpoint = "/api/" + coreVersion.textValue();
            requests.add(new ApiResourceRequest(coreApiEndpoint, ""));
        }
        JsonNode groups = apis.get("groups");
        for (JsonNode group : groups) {
            JsonNode versions = group.get("versions");
            String apiGroup = group.get("name").textValue();
            for (JsonNode version : versions) {
                String groupVersion = version.get("groupVersion").textValue();
                String customApiEndpoint = "/apis/" + groupVersion;
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
            String groupVersion = apiResources.get("groupVersion").textValue();
            JsonNode resources = apiResources.get("resources");
            for (JsonNode resource : resources) {
                result.add(createApiResource(resource, request.getGroupName(), groupVersion));
            }
            return result;
        });
    }

    private ApiResource createApiResource(JsonNode r, String groupName, String groupVersion) {
        String name = r.get("name").textValue();
        boolean isSubresource = name.contains("/");
        String kind = r.get("kind").textValue();
        boolean namespaced = r.get("namespaced").booleanValue();
        List<String> verbs = StreamSupport.stream(r.get("verbs").spliterator(), false).map(JsonNode::textValue).collect(Collectors.toList());
        return ApiResource.newBuilder()
                .name(name)
                .kind(kind)
                .groupName(groupName)
                .apiVersion(groupVersion)
                .coreApi(groupName.isEmpty())
                .subresource(isSubresource)
                .namespaced(namespaced)
                .verbs(verbs)
                .build();
    }

    public List<Resource> fetchResourcesFromNamespace(List<ApiResource> manageableResources, String namespace) {
        long start = System.currentTimeMillis();
        List<CompletableFuture<List<Resource>>> requests = manageableResources.stream().map(ar -> CompletableFuture.supplyAsync(() -> fetchNamespacedResourceList(ar, namespace))).collect(Collectors.toList());
        List<Resource> result = requests.stream().map(CompletableFuture::join).flatMap(Collection::stream).collect(Collectors.toList());
        LOG.info("Time to gather " + manageableResources.size() + " resources in namespace '" + namespace + "': " + (System.currentTimeMillis() - start) + "ms");
        return result;
    }

    public List<Resource> fetchNamespacedResourceList(ApiResource apiResource, String namespace) {
        String apiEndpoint = apiResource.getApiEndpoint(namespace);
        JsonNode resourceList = get(apiEndpoint);
        String apiVersion = resourceList.get("apiVersion").textValue();
        return StreamSupport.stream(resourceList.get("items").spliterator(), false)
                .map(jsonNode -> mapToResource(jsonNode, apiVersion, apiResource.getKind()))
                .collect(Collectors.toList());
    }

    private Resource mapToResource(JsonNode jsonNode, String apiVersion, String kind) {
        String name = jsonNode.get("metadata").get("name").textValue();
        String uid = jsonNode.get("metadata").get("uid").textValue();
        return new Resource(new ResourceKey(name, kind, apiVersion), uid, jsonNode);
    }

    public Resource fetchProject(String projectName) {
        String endpoint = "/apis/project.openshift.io/v1/projects/" + projectName;
        JsonNode projectNode = get(endpoint);
        return mapToResource(projectNode, "project.openshift.io/v1", "Project");
    }

    public JsonNode delete(Resource resource) {
        String resourceLink = resource.getJsonNode().get("metadata").get("selfLink").textValue();
        return delete(resourceLink);
    }


    public JsonNode newProject(String projectName) {
        String projectRequest = "{\"kind\":\"ProjectRequest\",\"apiVersion\":\"project.openshift.io/v1\",\"metadata\":{\"name\":\"" + projectName + "\",\"creationTimestamp\":null}}";
        String endpoint = "/apis/project.openshift.io/v1/projectrequests";
        return post(endpoint, projectRequest);
    }

    public JsonNode createNamespacedResource(Resource resource, ApiResource apiResource, String namespace) {
        String endpoint = "/" + apiResource.getApiEndpoint(namespace);
        return post(endpoint, resource.getJsonNode());
    }

    public JsonNode patchResource(Resource resource, JsonNode patch) {
        String endpoint = resource.getJsonNode().get("metadata").get("selfLink").textValue();
        return patch(endpoint, patch);

    }

    JsonNode patch(String endpoint, Object patchObject) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, "application/json-patch+json");
        HttpEntity<Object> entity = new HttpEntity<>(patchObject, headers);
        return restTemplate.exchange(apiServerUrl + endpoint, HttpMethod.PATCH, entity, ObjectNode.class).getBody();
    }

    JsonNode delete(String url) {
        ResponseEntity<ObjectNode> response = restTemplate.exchange(apiServerUrl + url, HttpMethod.DELETE, null, ObjectNode.class);
        return response.getBody();
    }

    JsonNode post(String endpoint, Object postRequest) {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(postRequest, headers);
        return restTemplate.exchange(apiServerUrl + endpoint, HttpMethod.POST, entity, ObjectNode.class).getBody();
    }

    JsonNode get(String url) {
        ResponseEntity<ObjectNode> response = restTemplate.exchange(apiServerUrl + url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), ObjectNode.class);
        return response.getBody();
    }

}
