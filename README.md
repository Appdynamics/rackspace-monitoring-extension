# AppDynamics Rackspace Cloud Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Rackspace Cloud is a set of cloud computing services from Rackspace. This extension monitors various services offered by RackSpace Cloud accessed over a RESTful API and reports the metrics to AppDynamics Controller.


##Installation

1. Run 'mvn clean install' from the rackspace-monitoring-extension directory
2. Download the file RackspaceMonitor.zip located in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file
4. In \<machineagent install dir\>/monitors/RackspaceMonitor/, open monitor.xml and configure the Rackspace parameters. This extension uses UserName and API key as Authentication parameters. Refer [here](http://docs.rackspace.com/auth/api/v2.0/auth-client-devguide/content/QuickStart-000.html) for details 
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
|{Region}/{ServerName}/Progress	| The percentage value of the build status|
|{Region}/{ServerName}/RAM	| RAM in bytes|
|{Region}/{ServerName}/Swap	| Swap space in bytes|
|{Region}/{ServerName}/vCPUs	| vCPUs|
|{Region}/{ServerName}/Disk Space| Disk Space|

### NextGen Server Metrics
The following metrics are reported under \<NextGenServers\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|Limits/Max Total Instances	| The maximum number of Cloud Servers at any one time|
|Limits/Max Total RAM Size(MB)	| The maximum total amount of RAM (MB) of all Cloud Servers at any one time|
|Limits/Total Cores Used		| The total number of cores used|
|Limits/Total Floating Ips Used	| The total number of FloatingIps used|
|Limits/Total Instances Used	| The total number of Cloud Servers|
|Limits/Total Private Networks Used| The total number of PrivateNetworks|
|Limits/Total RAM Used(GB)	| The total amount of RAM (GB) used for all Cloud Servers|
|Limits/Total Security Groups Used	| The total number of security groups used|
|{Region}/{ServerName}/Progress	| The percentage value of the build status|
|{Region}/{ServerName}/RAM	| RAM in bytes|
|{Region}/{ServerName}/Status	|Current Sever state|
|{Region}/{ServerName}/Swap	| Swap space in bytes|
|{Region}/{ServerName}/vCPUs	|vCPUs |
|{Region}/{ServerName}/Disk Space	| Disk Space|

### CloudFiles Metrics
The following metrics are reported under \<Files\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|{Region}/{ContainerName}/Count	| Number of objects in the container|
|{Region}/{ContainerName}/Bytes	| Number of bytes in the container|

### Database Metrics
The following metrics are reported under \<Databases\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|{Region}/{InstanceName}/Status	| Current instance state|
|{Region}/{InstanceName}/Volume-size|Volume-size |

### LoadBalancer Metrics
The following metrics are reported under \<LoadBalancers\>

| Metric Name 			| Description |
|-------------------------------|-------------|
|{Region}/{InstanceName}/Status	| Current instance state|
|{Region}/{InstanceName}/Node Count| Number of nodes this loadbalancer caters to servicing the request|

Status values from Rackspace REST api are represented as integers in this extension. Refer [here](https://github.com/Appdynamics/rackspace-monitoring-extension/blob/master/StatusDescription) for details.

## Custom Dashboard
![](https://github.com/Appdynamics/rackspace-monitoring-extension/raw/master/Dashboard.png)

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere]() community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).


