# AppDynamics Rackspace Cloud Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Rackspace Cloud is a set of cloud computing services from Rackspace. This extension monitors various services offered by RackSpace Cloud accessed over a RESTful API and reports the metrics to AppDynamics Controller.


##Installation

1. Run 'mvn clean install' from the rackspace-monitoring-extension directory
2. Download the file RackspaceMonitor.zip located in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file
4. In \<machineagent install dir\>/monitors/RackspaceMonitor/, open monitor.xml and configure the Rackspace parameters.
<pre>
&lt;argument name="username" is-required="true" default-value="" /&gt;
&lt;argument name="api-key" is-required="true" default-value="" /&gt;
&lt;!-- US/UK --&gt;
&lt;argument name="account-base" is-required="true" default-value="US" /&gt;
&lt;argument name="metric-path" is-required="false" default-value="" /&gt;
</pre>
5. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Rackspace in the case of default metric path


## Metrics

### FirstGen Server Metrics
The following metrics are reported under \<FirstGenServers\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|{Region}/{ServerName}/progress	| The percentage value of the build status|
|{Region}/{ServerName}/ram	| RAM in bytes|
|{Region}/{ServerName}/swap	| Swap space in bytes|
|{Region}/{ServerName}/vcpus	| |
|{Region}/{ServerName}/disk	| |

### NextGen Server Metrics
The following metrics are reported under \<NextGenServers\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|Limits/maxTotalInstances	| The maximum number of Cloud Servers at any one time|
|Limits/maxTotalRAMSize(MB)	| The maximum total amount of RAM (MB) of all Cloud Servers at any one time|
|Limits/totalCoresUsed		| The total number of cores used|
|Limits/totalFloatingIpsUsed	| The total number of FloatingIps used|
|Limits/totalInstancesUsed	| The total number of Cloud Servers|
|Limits/totalPrivateNetworksUsed| The total number of PrivateNetworks|
|Limits/totalRAMUsed(GB)	| The total amount of RAM (GB) used for all Cloud Servers|
|Limits/totalSecurityGroupsUsed	| The total number of security groups used|
|{Region}/{ServerName}/progress	| The percentage value of the build status|
|{Region}/{ServerName}/ram	| RAM in bytes|
|{Region}/{ServerName}/status	||
|{Region}/{ServerName}/swap	| Swap space in bytes|
|{Region}/{ServerName}/vcpus	| |
|{Region}/{ServerName}/disk	| |

### CloudFiles Metrics
The following metrics are reported under \<Files\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|{Region}/{ContainerName}/count	| Number of objects in the container|
|{Region}/{ContainerName}/bytes	| Number of bytes in the container|

### Database Metrics
The following metrics are reported under \<Databases\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|{Region}/{InstanceName}/status	| |
|{Region}/{InstanceName}/volume-size| |

### LoadBalancer Metrics
The following metrics are reported under \<LoadBalancers\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|{Region}/{InstanceName}/status	| |
|{Region}/{InstanceName}/nodeCount| Number of nodes this loadbalancer caters to servicing the request|

## Custom Dashboard
![]()

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere]() community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:help@appdynamics.com).


