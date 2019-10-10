### There are 2 scripts:

"csye6225-aws-cf-create-stack.sh" - This is the script to create a stack to setup AWS network infrastructure.
"csye6225-aws-cf-terminate-stack.sh" - This is to terminate the entire network stack.

### Network Setup Script:

1. The "csye6225-aws-cf-create-stack.sh" takes the stack name,vpc name,AWS region,VPC CIDR block
   and 3Subnet CIDR blocks as parameters. It also checks whether the stack exists or not.
2. If the stack exists then the message is displayed as "The stack already exists" and terminates the script.
3. If the stack does not exist then the stack name is passed to the "csye6225-cf-networking.json"    template along with other parameters.
4. 1 VPC, 3 subnets, 1 private and public route table, 1 internet gateway is created to setup the     network.
5. Finally once the stack is created successfully and the script displays the messages accordingly.
6. The script "csye6225-aws-cf-terminate-stack.sh" for termination takes stack name and the AWS region as parameters and checks if the stack exists or not.
7. If the stack exists then the script terminates the stack resources and waits for the termination of all resources.
8. After successful completion of termination the appropriate message is displyed.
9. Both the scripts take AWS_PROFILE as the input to set environment variable `AWS_PROFILE` for scripts to execute

## Instructions to run script

1. Clone the repository
2. Now navigate to script folder using command "cd infrastructure/aws/cloudformation/"
3. To setup a cloudformation stack run below shell script 
    `bash csye6225-aws-cf-create-stack.sh <stack-name> <vpc-name> <aws-region> <vpc-cidr-block> <subnet-1-cidr-block> <subnet-2-cidr-block> <subnet-3-cidr-block>`
   Example to create network infrastructure using cloudformation using the shell script
   
    `bash csye6225-aws-cf-create-stack.sh abc33 abc-vpc us-east-2 10.0.0.0/16 10.0.1.0/24 10.0.2.0/24 10.0.3.0/24`

4. To terminate a cloudformation stack run shell script 
    `bash csye6225-aws-cf-terminate-stack.sh <stack-name> <aws-region>`
   Example terminate stack shell script command: 
    `bash csye6225-aws-cf-terminate-stack.sh teststack us-east-1`
