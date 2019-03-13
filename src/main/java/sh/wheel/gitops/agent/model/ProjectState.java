package sh.wheel.gitops.agent.model;

import java.util.Map;
import java.util.Objects;

public class ProjectState {
    private Resource project;
    private Map<ResourceKey, Resource> resourcesByKey;

    public ProjectState(Resource project, Map<ResourceKey, Resource> resourcesByKey) {
        this.project = project;
        this.resourcesByKey = resourcesByKey;
    }


    public Map<ResourceKey, Resource> getResourcesByKey() {
        return resourcesByKey;
    }

    public Resource getProject() {
        return project;
    }

    public String getName() {
        return project.getName();
    }

    @Override
    public String toString() {
        return "ProjectState{" +
                " project=" + project +
                ", resourcesByKey=" + resourcesByKey +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectState that = (ProjectState) o;
        return Objects.equals(project, that.project) &&
                Objects.equals(resourcesByKey, that.resourcesByKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(project, resourcesByKey);
    }
}
