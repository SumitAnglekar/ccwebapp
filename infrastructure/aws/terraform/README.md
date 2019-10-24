## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
| Ishita Sequeira| 001403357 | sequeira.i@husky.neu.edu |
| Tejas Shah | 001449694 | shah.te@husky.neu.edu |
| Sumit Anglekar | 001475969 | anglekar.s@husky.neu.edu |

## Installing Terraform:
    Kindly follow the steps given in the following link:
    https://askubuntu.com/questions/983351/how-to-install-terraform-in-ubuntu

## Initializating script:

    1. `terraform init` 
    - The terraform init command is used to initialize a working directory containing Terraform configuration files. This is the first command that should be run after writing a new Terraform configuration or cloning an existing one from version control. It is safe to run this command multiple times.

## There are 2 scripts:

    1. `terraform apply` -   This is the script to create a stack to setup AWS network infrastructure.
    2. `terraform destroy` - This is to terminate the entire network stack.

## File significance:
    1. Building modules for individual instances:
        a. Each module will have a single instance related to an environment and aws region.
        b. For our understanding we have created just modules, namely, module and childmodule. They will have two different individual instances.

    2. "provider.tf" - This file has variable for defined or input  aws-profile and aws-regions for a given module. The entered profile        and region shall be defined in '.config' and '.credential' file while setting up the CLI environment.
    
    3. "main.tf"     - This file has the entire network infrastructure setup with all given resourse components.
    
    4. "variable.tf" - All the initialized variables in main.tf or provider.tf must be defined with appropriate type                      and description in this particular file.
    
    5."terraform.tfvars" - We can pre-define the inputs in the .tfvars file if aren't passing them via command line. 
    This file is optional.
    
    6. Miscellaneous - There are other files and folders like "terraform.tfstate", "terraform.tfstate.backup"                        which maintain the details of passed input parameters and map them in a particular structure. These file are unique to individual modules.


## Network Setup Script:
    
    1. Create a individual module for each individual instance.
    2. Add respective .tfvars in the each module and run terraform init
    1. The main.tf has 1 VPC, 3 subnets, 1 route table and 1 internet gateway to setup the network.
    2. The commandline or .tfvars has following input parameters:
            a. AWS environment (dev/prod) (type-String)
            b. AWS region (us-east-1,us-east-2) (type-String)
            c. VPC Name (type-String)
            d. VPC Cidr Block (type-String)
            e. VPC Subnet Cidr Block (type- List)
    3. We can create multiple VPC of same name in different regions. If we try to create a VPC of same name in a same region, it's value will be written or updated.
    4. If we enter inappropriate VPC or Subnet Cidr Block values the terraform will provide required error accordingly and it will rollback.
    5. After successful completion the appropriate message is displyed.

## Instructions to run script:

    1. Clone the repository
    2. Now navigate to script folder using command "cd infrastructure/aws/terraform/"
    3. create modules if need or run `terraform init` in each module
    4. run `terraform apply` to input the resource values via command line.
    5. run `terraform destroy` and input all the required paramters  specific to that particular vpc

## S3 Bucket
### SET the following properties in application.properties for inregards to S3 bucket
1. spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect    
2. spring.jpa.hibernate.ddl-auto=update
3. spring.jpa.show-sql=true
4. spring.datasource.url=${SPRING_DATASOURCE_URL}
5. spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
6. spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
7. spring.datasource.initialization-mode=always
8. spring.datasource.continue-on-error=true
9.  Here we set the s3 endpoint for our bucket:
`aws.s3.endpointURL=s3.amazonaws.com`
10. Setting the bucket name: 
`aws.s3.bucketname=${AWS_S3_BUCKET_NAME}`
11. aws.region=${AWS_REGION}
12. aws.profile=${AWS_PROFILE}
13. cloud.aws.credentials.instanceProfile=true