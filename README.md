# Wheel GitOps Agent
[![Build Status](https://travis-ci.com/wheel-sh/wheel-agent.svg?branch=master)](https://travis-ci.com/wheel-sh/wheel-agent)

<img src="https://github.com/wheel-sh/wheel-agent/raw/master/img/wheel.png" width="128">

---

The Wheel GitOps Agent enables the synchronization of structured configurations of app environments in Git to an OpenShift cluster. The basic idea is to combine a resource template (be it an OpenShift template or a helmet chart) with its parameters to describe the desired state of a namespace. Wheel provides a clear structure of how this configuration is stored in the Git. The  Agent reacts to changes in the Git repository or cluster.

The current Kubernetes target distribution is OpenShift/OKD, but it will also be possible later to run the agent on a standard Kubernetes cluster.

---


## Getting Started

These instructions will get you the Wheel GitOps Agent up and running on your OpenShift Cluster for development and testing purposes. 

### Prerequisites

You need a running OpenShift cluster where you have cluster-admin rights. 

If you do not have a cluster, you can test and develop the following options:
* Minishift (provides an OpenShift Cluster locally) https://docs.okd.io/latest/minishift/getting-started/installing.html
* All-in-one cluster (How to get a 'real' cluster on one server running) https://blog.openshift.com/openshift-all-in-one-aio-for-labs-and-fun/


### Installing

First the necessary Git config repository must be created. Here is an example repository that can be used as a basis for now:

https://github.com/wheel-sh/demo-cluster-config

Just make a fork/copy of this repository

If possible, the agent should be deployed in its own namespace.

```
oc new-project wheel
```

To deploy the agent, the provided OpenShift template can be used. This creates the following resources:

* DeploymentConfig for the Agent
* Service to get the REST API from the agent to respond to Git Hooks
* Route to expose the service
* ServiceAccount under which the agent runs
* ClusterRoleBinding to the roles self-access-reviewer, self-provisioner, system:basic-user. For this purpose, an own, precisely tailored ClusterRole will be developed later.
 
```
oc process -f https://raw.githubusercontent.com/wheel-sh/wheel-agent/master/openshift/wheel-agent.yaml \
-p AGENT_NAMESPACE=wheel \
-p CONFIG_REPOSITORY_URL=https://github.com/wheel-sh/demo-cluster-config.git
-p CONFIG_REPOSITORY_BRANCH=master
-p WHEEL_IMAGE=registry.cloud.nikio.io/cicd/wheel-agent:latest \
| oc apply -f -
```

In this example, the parameters must be adjusted accordingly if necessary. The referenced container image is currently in a private registry, but can also be pulled anonymously. Later, the image will also be made available on Docker Hub. However, it is also possible to build the image yourself.

After the Wheel Agent has been deployed, a Git Hook should be set up for Push Events. To get to the hook URL of the Wheel Agent, execute the following command in the namespace of the agent:

```
echo "https://$(oc get route wheel-agent --no-headers -o custom-columns=HOST:.spec.host)/git-hook"
```

You can then set up a push hook for this URL in the configuration repository. This makes changes in the repository active in the cluster within a few seconds. 

### Build

To build the Wheel Agent yourself a JDK8, Maven and Docker is needed. To build the Wheel Agent image simply clone this repository and simply run:

```
mvn package
docker build -t wheel-agent .
```

## Built With

* [Spring Boot](https://spring.io/projects/spring-boot)
* [Maven](https://maven.apache.org/) - Dependency Management
* [Fabric8 Kubernetes Client](https://github.com/fabric8io/kubernetes-client) - Used take care about authentication. 

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. 

## Authors

* **Nikolas Philips** - *Creator* - [niiku](https://github.com/niiku)

See also the list of [contributors](https://github.com/wheel-sh/wheel-agent/contributors) who participated in this project.

## License

This project is licensed under the Apache License 2.0 License - see the [LICENSE](https://github.com/wheel-sh/wheel-agent/blob/master/LICENSE) file for details

