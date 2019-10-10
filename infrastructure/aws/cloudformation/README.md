### There are 2 scripts:

    "csye6225-aws-cf-create-stack.sh" - This is the script to create a stack to setup AWS network.
    "csye6225-aws-cf-terminate-stack.sh" - This is to terminate the entire network stack.

### Network Setup Script:

1. The "csye6225-aws-cf-create-stack.sh" takes the stack name,vpc name,AWS region,VPC CIDR block
   and 3Subnet CIDR block from the user, checks whether the satck exists or not.
2. If the stack exists then the message is displayed as the stack exists and terminates it.
3. If the stack does not exist then the stack name is passed to the "csye6225-cf-networking.json"    template.
4. The VPC, 3 subnets, 1 private, public route table, 1 internet gateway is created to setup the     network.
5. Finally once the stack is created successfully and the script is excuted the messages are         displayed accordingly.
6. The script "csye6225-aws-cf-terminate-stack.sh" for termination also checks if the stack          exists or not.
7. If the stack exists then the script terminates the stack resource and waits for the resource      termination.
8. After successful completion of termination the message is displyed.

## Instructions to run script

1. Clone repository
2. Now navigate to script folder using command "cd infrastructure/aws/cloudformation/"
3. To setup a cloudformation stack run shell script "bash csye6225-aws-cf-create-stack.sh abc        abc-vpc us-east-1 10.0.0.0/16 10.0.1.0/24 10.0.2.0/24 10.0.3.0/24 "
   Example to create network infrastructure using cloudformation using the shell script
   ./csye6225-aws-cf-create-stack.sh test 10.0.0.0/16 10.0.1.0/24 10.0.2.0/24 10.0.3.0/24
4. To delete a cloudformation stack run shell script "bash csye6225-aws-cf-terminate-stack.sh        'STACK_NAME';
   Example delete stack shell script command : bash csye6225-aws-cf-terminate-stack.sh teststack
