### There are 3 scripts:

    "csye6225-aws-cf-create-stack.sh" - This is the script to create a stack to setup AWS network.
    "csye6225-aws-cf-terminate-stack.sh" - This is to terminate the entire network stack.

### Network Setup Script:

    The "csye6225-aws-cf-create-stack.sh" takes the stack name,vpc name,AWS region,VPC CIDR block
    and 3Subnet CIDR block from the user, checks whether the satck exists or not.
    If the stack exists then the message is displayed as the stack exists and terminates it.
    If the stack does not exist then the stack name is passed to the "csye6225-cf-networking.json" template.
    The VPC, 3 subnets, 1 private, public route table, 1 internet gateway is created to setup the network.
    Finally once the stack is created successfully and the script is excuted the messages are displayed accordingly.
    The script "csye6225-aws-cf-terminate-stack.sh" for termination also checks if the stack exists or not.
    If the stack exists then the script terminates the stack resource and waits for the resource termination.
    After successful completion of termination the message is displyed.


