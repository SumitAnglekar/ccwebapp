# CSYE 6225 - Fall 2019

## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
| Ishita Sequeira| 001403357 | sequeira.i@husky.neu.edu |
| Tejas Shah | 001449694 | shah.te@husky.neu.edu |
| Sumit Anglekar | 001475969 | anglekar.s@husky.neu.edu |

## Technology Stack
1. SpringBoot
2. Spring Security
3. Hibernate
4. Postgres Database
5. Version Control : Git

## Build Instructions
1. Clone the git repository: git@github.com:<username>/ccwebapp.git
   Usernames that can be used are SumitAnglekar94, shah-tejas, ishitasequeira
2. Traverse to the folder ./ccwebapp/webapp/recipe.
3. Run the command to build the module: mvn clean install.

## Deploy Instructions
1. Build a custom AMI instance using packer by building the template from https://github.com/csye6225-cloud-computing/fa19-team-002-ami.
    (For detailed steps on this follow the steps in the README at https://github.com/csye6225-cloud-computing/fa19-team-002-ami)
2. Navigate to `<REPO_DIR>/ccwebapp/infrastructure/aws/terraform` directory.
3. Build the EC2 instance using the terraform command: `terraform apply`.
4. Build the WAR file for the recipe application by running the command `mvn clean install` from  `<REPO_DIR>/ccwebapp/webapp/recipe` directory.
5. The WAR file from the above step would be created in `<REPO_DIR>/ccwebapp/webapp/recipe/target` directory.
6. SCP the WAR file to the EC2 instance created above. Example:
    `scp <path-to-source-file> centos@<ec2_instance_ip>:<destination_path>`
7. In the EC2 instance copy the WAR file to tomcat's webapp directory.
8. Restart tomcat to deploy the application.

## Application Endpoints
1. Register a User ({instance_ip}:8080/recipe/v1/user)
2. Get User records ({instance_ip}:8080/recipe/v1/user/self)
3. Update User records ({instance_ip}:8080/recipe/v1/user/self)
4. Register a Recipe ({instance_ip}:8080/recipe/v1/recipe/)
5. Get recipe Information ({instance_ip}:8080/recipe/v1/recipe/{id})
6. Delete a particular recipe ({instance_ip}:8080/recipe/v1/recipe/{id})
7. Update recipe Information ({instance_ip}:8080/recipe/v1/recipe/{id})
8. Get newest recipe information ({instance_ip}:8080/recipe/v1/recipes)
9. Register an image ({instance_ip}:8080/recipe/v1/recipe/{id}/image)
10. Get recipe image ({instance_ip}:8080/recipe/v1/recipe/{recipeId}/image/{imageId})
11. Delete recipe image ({instance_ip}:8080/recipe/v1/recipe/{recipeId}/image/{imageId})

## Running Tests
1. Run the "run all tests" configuration for JUnit.

## CLI Installation and Configuration
1. Kindly follow the steps in the given link for CLI setup and installation:
https://docs.aws.amazon.com/cli/latest/userguide/install-linux-al2017.html

2. Kindly follow the steps in the given link for CLI configuration:
https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-configure.html

## CI/CD
1. Create policy for circleci IAM user to give access to create new AMI: 
    `terraform plan -target module.application.aws_iam_user_policy_attachment.circleci_ec2_policy_attach -out run.plan`
    `terraform apply run.plan`
2. Trigger the circleci plan to build new AMI:
    `curl -u <CIRCLECI_TOKEN>: -d build_parameters[CIRCLE_JOB]=build https://circleci.com/api/v1.1/project/github/<ORGANIZATION>/csye6225-fall2019-ami/tree/<BRANCH>`
3. Once the build is completed a new AMI will be created and registered with AWS.
4. Create the remaining infrastructure using `terraform apply`.
5. Once successful, a new EC2 instance would be up and running.
6. Trigger the circleci build to deploy new version of the app:
    `curl -u <CIRCLECI_TOKEN>: -d build_parameters[CIRCLE_JOB]=build https://circleci.com/api/v1.1/project/github/<ORGANIZATION>/ccwebapp/tree/<BRANCH>`
7. The build should complete sucessfully deploying the application to the EC2 instance and also uploading the latest artifact in the S3 bucket.

## JMETER

1. Download and install jmeter locally from the link: http://jmeter.apache.org/download_jmeter.cgi
2. Launch jmeter and open the plan from the directory ./jmeter/recipeTest.jmx
3. To create a user, enable `USER Thread Group` and update the `User Post` request with the data you want to post
4. Similarly, to create recipes enable `RECIPE Thread Group` and update the credentials in `HTTP Authorization Manager` along with the recipe details in `recipepost` request
5. You can update the number of consecutive requests you want by updating the Thread group's Thread properties
