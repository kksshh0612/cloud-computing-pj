package aws;

/*
* Cloud Computing
* 
* Dynamic Resource Management Tool
* using AWS Java SDK Library
* 
*/
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricDataResult;
import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
import com.amazonaws.services.cloudwatch.model.Metric;
import com.amazonaws.services.cloudwatch.model.MetricDataQuery;
import com.amazonaws.services.cloudwatch.model.MetricDataResult;
import com.amazonaws.services.cloudwatch.model.MetricStat;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagement;
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder;
import com.amazonaws.services.simplesystemsmanagement.model.GetCommandInvocationRequest;
import com.amazonaws.services.simplesystemsmanagement.model.GetCommandInvocationResult;
import com.amazonaws.services.simplesystemsmanagement.model.SendCommandRequest;
import com.amazonaws.services.simplesystemsmanagement.model.SendCommandResult;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.services.ec2.model.Filter;

public class awsTest {

	private static AmazonEC2 ec2;

	private static final String REGION = "ap-northeast-2";

	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();

		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. " +
					"Please make sure that your credentials file is at the correct " +
					"location (~/.aws/credentials), and is in valid format.",
					e);
		}
		ec2 = AmazonEC2ClientBuilder.standard()
			.withCredentials(credentialsProvider)
			.withRegion(REGION)	/* check the region at AWS console */
			.build();
	}

	public static void main(String[] args) throws Exception {

		init();

		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		int number = 0;
		
		while(true)
		{
			System.out.println("                                                            ");
			System.out.println("                                                            ");
			System.out.println("------------------------------------------------------------");
			System.out.println("           Amazon AWS Control Panel using SDK               ");
			System.out.println("------------------------------------------------------------");
			System.out.println("  1. list instance                2. available zones        ");
			System.out.println("  3. start instance               4. available regions      ");
			System.out.println("  5. stop instance                6. create instance        ");
			System.out.println("  7. reboot instance              8. list images            ");
			System.out.println("  9. check condor status          10. check instance usage  ");
			System.out.println("                                 99. quit                   ");
			System.out.println("------------------------------------------------------------");
			
			System.out.print("Enter an integer: ");
			
			if(menu.hasNextInt()){
				number = menu.nextInt();
				}else {
					System.out.println("concentration! Enter an integer ");
					break;
				}
			

			String instance_id = "";

			switch(number) {
			case 1: 
				listInstances();
				break;
				
			case 2: 
				availableZones();
				break;
				
			case 3: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					startInstance(instance_id);
				break;

			case 4: 
				availableRegions();
				break;

			case 5: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					stopInstance(instance_id);
				break;

			case 6: 
				System.out.print("Enter ami id: ");
				String ami_id = "";
				if(id_string.hasNext())
					ami_id = id_string.nextLine();
				
				if(!ami_id.trim().isEmpty()) 
					createInstance(ami_id);
				break;

			case 7: 
				System.out.print("Enter instance id: ");
				if(id_string.hasNext())
					instance_id = id_string.nextLine();
				
				if(!instance_id.trim().isEmpty()) 
					rebootInstance(instance_id);
				break;

			case 8: 
				listImages();
				break;

			case 9:
				condorStatus();
				break;

			case 10:
				instanceUsage();
				break;

			case 99: 
				System.out.println("bye!");
				menu.close();
				id_string.close();
				return;

			default: System.out.println("concentration!");
			}

		}
		
	}

	public static void listInstances() {
		
		System.out.println("Listing instances....");
		boolean done = false;
		
		DescribeInstancesRequest request = new DescribeInstancesRequest();

		while(!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for(Reservation reservation : response.getReservations()) {
				for(Instance instance : reservation.getInstances()) {
					System.out.printf(
						"[id] %s, " +
						"[AMI] %s, " +
						"[type] %s, " +
						"[state] %10s, " +
						"[monitoring state] %s",
						instance.getInstanceId(),
						instance.getImageId(),
						instance.getInstanceType(),
						instance.getState().getName(),
						instance.getMonitoring().getState());
				}
				System.out.println();
			}

			request.setNextToken(response.getNextToken());

			if(response.getNextToken() == null) {
				done = true;
			}
		}
	}
	
	public static void availableZones()	{

		System.out.println("Available zones....");
		try {
			DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
			Iterator <AvailabilityZone> iterator = availabilityZonesResult.getAvailabilityZones().iterator();
			
			AvailabilityZone zone;
			while(iterator.hasNext()) {
				zone = iterator.next();
				System.out.printf("[id] %s,  [region] %15s, [zone] %15s\n", zone.getZoneId(), zone.getRegionName(), zone.getZoneName());
			}
			System.out.println("You have access to " + availabilityZonesResult.getAvailabilityZones().size() +
					" Availability Zones.");

		} catch (AmazonServiceException ase) {
				System.out.println("Caught Exception: " + ase.getMessage());
				System.out.println("Reponse Status Code: " + ase.getStatusCode());
				System.out.println("Error Code: " + ase.getErrorCode());
				System.out.println("Request ID: " + ase.getRequestId());
		}
	
	}

	public static void startInstance(String instance_id)
	{
		
		System.out.printf("Starting .... %s\n", instance_id);
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StartInstancesRequest> dry_request =
			() -> {
			StartInstancesRequest request = new StartInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		StartInstancesRequest request = new StartInstancesRequest()
			.withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}
	
	
	public static void availableRegions() {
		
		System.out.println("Available regions ....");
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DescribeRegionsResult regions_response = ec2.describeRegions();

		for(Region region : regions_response.getRegions()) {
			System.out.printf(
				"[region] %15s, " +
				"[endpoint] %s\n",
				region.getRegionName(),
				region.getEndpoint());
		}
	}
	
	public static void stopInstance(String instance_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StopInstancesRequest> dry_request =
			() -> {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		try {
			StopInstancesRequest request = new StopInstancesRequest()
				.withInstanceIds(instance_id);
	
			ec2.stopInstances(request);
			System.out.printf("Successfully stop instance %s\n", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

	}
	
	public static void createInstance(String ami_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		RunInstancesRequest run_request = new RunInstancesRequest()
			.withImageId(ami_id)
			.withInstanceType(InstanceType.T2Micro)
			.withMaxCount(1)
			.withMinCount(1);

		RunInstancesResult run_response = ec2.runInstances(run_request);

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();

		System.out.printf(
			"Successfully started EC2 instance %s based on AMI %s",
			reservation_id, ami_id);
	
	}

	public static void rebootInstance(String instance_id) {
		
		System.out.printf("Rebooting .... %s\n", instance_id);
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		try {
			RebootInstancesRequest request = new RebootInstancesRequest()
					.withInstanceIds(instance_id);

				RebootInstancesResult response = ec2.rebootInstances(request);

				System.out.printf(
						"Successfully rebooted instance %s", instance_id);

		} catch(Exception e)
		{
			System.out.println("Exception: "+e.toString());
		}

		
	}
	
	public static void listImages() {
		System.out.println("Listing images....");
		
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();
		
		DescribeImagesRequest request = new DescribeImagesRequest();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		
		request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-worker"));
		request.setRequestCredentialsProvider(credentialsProvider);
		
		DescribeImagesResult results = ec2.describeImages(request);
		
		for(Image images :results.getImages()){
			System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n", 
					images.getImageId(), images.getName(), images.getOwnerId());
		}
	}

	public static void condorStatus() {
		System.out.println("Checking condor status....");
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();

		Instance targetInstance = null;
		while (!done) {
			DescribeInstancesResult result = ec2.describeInstances(request);

			for (Reservation reservation : result.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					if (instance.getState().getName().equals("running")) {
						targetInstance = instance;
						done = true;
						break;
					}
				}
				if (done){
					break;
				}
			}
		}
		if (targetInstance == null) {
			System.out.println("There is no running EC2 instance");
		}

		try {
			// SSM client 초기화
			AWSSimpleSystemsManagement ssmClient = AWSSimpleSystemsManagementClientBuilder
					.standard()
					.withRegion(REGION)
					.build();

			// 명령 생성
			SendCommandRequest sendCommandRequest = new SendCommandRequest()
					.withInstanceIds(targetInstance.getInstanceId())
					.withDocumentName("AWS-RunShellScript")        // 실행할 문서 이름 [AWS-RunShellScript : 리눅스 쉘 문서]
					.addParametersEntry("commands",
							java.util.Collections.singletonList("condor_status")); // Replace with your actual command

			// 명령 전송
			SendCommandResult sendCommandResult = ssmClient.sendCommand(sendCommandRequest);

			String commandId = sendCommandResult.getCommand().getCommandId();
			System.out.printf("Command ID: %s, Instance ID: %s\n", commandId, targetInstance.getInstanceId());


			// 결과
			GetCommandInvocationRequest getCommandInvocationRequest = new GetCommandInvocationRequest()
					.withCommandId(commandId)
					.withInstanceId(targetInstance.getInstanceId());

			Thread.sleep(1000);

			boolean isDone = false;
			while (!isDone) {
				GetCommandInvocationResult commandResult = ssmClient.getCommandInvocation(getCommandInvocationRequest);
				String status = commandResult.getStatus();

				System.out.println("status : " + status);

				switch (status) {
					case "Success":
						System.out.println("Command executed successfully!");
						System.out.println("Command output: ");
						System.out.println(commandResult.getStandardOutputContent());
						isDone = true;
						break;
					case "Failed":
						System.out.println("Command execution failed!");
						System.out.println("Error: ");
						System.out.println(commandResult.getStandardErrorContent());
						isDone = true;
						break;
					case "InProgress":
					case "Pending":
						System.out.println("Command is still running. Waiting...");
						Thread.sleep(1000);
						break;
					default:
						System.out.printf("Unknown command status: %s\n", status);
						isDone = true;
				}
			}
		} catch (Exception e) {
			System.out.println("Failed to send or retrieve command result: " + e.getMessage());
		}
	}

	public static void instanceUsage(){
		System.out.println("Checking All Intances Usages....");

		final AmazonCloudWatch cloudWatch = AmazonCloudWatchClientBuilder.defaultClient();

		// EC2 인스턴스 목록 조회
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		DescribeInstancesResult describeInstancesResult = ec2.describeInstances(describeInstancesRequest);

		for(Reservation reservation : describeInstancesResult.getReservations()){
			for(Instance instance : reservation.getInstances()){
				System.out.printf("[Instance ID]: %s [State]: %s ", instance.getInstanceId(), instance.getState().getName());
				if(instance.getState().getName().equals("running")){

					// CPU 사용량 메트릭 조회
					GetMetricDataResult cpuUsageResult = getInstanceMatric(cloudWatch, instance.getInstanceId(),
							"cpuUsage", "CPUUtilization", 3600 * 1000);
					GetMetricDataResult networkInResult = getInstanceMatric(cloudWatch, instance.getInstanceId(),
							"networkIn", "NetworkIn", 600 * 1000);
					GetMetricDataResult networkOutResult = getInstanceMatric(cloudWatch, instance.getInstanceId(),
							"networkOut", "NetworkOut", 600 * 1000);

					System.out.printf("[CPU Usage]: %.2f %% [Network In]: %f bytes [Network Out]: %f bytes\n",
							cpuUsageResult.getMetricDataResults().getFirst().getValues().get(0),
							networkInResult.getMetricDataResults().getFirst().getValues().get(0),
							networkOutResult.getMetricDataResults().getFirst().getValues().get(0)
					);
				}
				else{
					// 사용 중이지 않은 인스턴스는 상태 출력
					System.out.printf("[CPU Usage] : 0.00 %%\n");
				}
			}
		}
	}

	private static GetMetricDataResult getInstanceMatric(AmazonCloudWatch cloudWatch, String instanceId,
														String matricId, String matricName, long termTime){
		Date endTime = new Date();
		Date startTime = new Date(endTime.getTime() - termTime);

		// CloudWatch에서 CPU 사용량 메트릭을 가져옵니다.
		GetMetricDataRequest request = new GetMetricDataRequest()
				.withMetricDataQueries(new MetricDataQuery()
						.withId(matricId)
						.withMetricStat(new MetricStat()
								.withMetric(new Metric()
										.withNamespace("AWS/EC2")
										.withMetricName(matricName)
										.withDimensions(new Dimension()
												.withName("InstanceId")
												.withValue(instanceId)))
								.withPeriod(60)
								.withStat("Average"))
						.withReturnData(true))
				.withStartTime(startTime)
				.withEndTime(endTime);

		return cloudWatch.getMetricData(request);
	}

}
	