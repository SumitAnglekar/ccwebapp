## There are 2 scripts to create and destroy the AWS Network Infrastructure:

1. csye6225-aws-networking-setup.sh : Creates the Infrastructure.
2. csye6225-aws-networking-teardown.sh : Destroys the Infrastructure.

### AWS Infrastructure creation script:
1. This script does the following:
    - Creates a new VPC.
    - Creates 3 new subnets in different availability zones, in the same region as the VPC.
    - Creates a new Internet Gateway and attaches to the VPC.
    - Creates a new public routing table and attaches all the 3 subnets to the route table.
    - Create a new public route in the public routing table created with destination CIDR block 0.0.0.0/0 and internet gateway created.
2. The script takes the following input arguments:
    - AWS region
    - VPC CIDR Block
    - Subnet CIDR Blocks (3 different blocks for 3 different subnets)
    - VPC Name
3. The script requires the AWS_PROFILE is set in the environment, and will error if it is not set.
4. For any other errors, the script will error out with the appropriate error message.

### AWS Infrastructure termination script:
1. This script does the following:
    - Deletes the public route.
    - Disassociates all the subnets from the routing table.
    - Deletes the routing table.
    - Detaches the internet gateway from the VPC.
    - Deletes the internet gateway.
    - Deletes the subnets.
    - Deletes the VPC.
2. The script takes the following input arguments:
    - AWS region
    - VPC Name
3. The script requires the AWS_PROFILE is set in the environment, and will error if it is not set.
4. For any other errors, the script will error out with the appropriate error message.

### Instructions to run the script:
1. Clone the repository.
2. Open shell terminal on your machine and nagivate to the scripts directory (`<REPO_DIR>/infrastructure/aws/script`).
3. Ensure your AWS CLI is installed and configured. If not, follow the steps mentioned in https://docs.aws.amazon.com/cli/latest/userguide/install-linux.html
4. Ensure `AWS_PROFILE` environment variable is set. Eg. `export AWS_PROFILE=dev`
5. Ensure your `csye6225-aws-networking-setup.sh` and `csye6225-aws-networking-teardown.sh` scripts are executable.
6. Run the `csye6225-aws-networking-setup.sh` script to create the AWS infrastructure using the following syntax:
    > `./csye6225-aws-networking-setup.sh <AWS_REGION> <VPC_CIDR_BLOCK> <SUBNET1_CIDR_BLOCK> <SUBNET2_CIDR_BLOCK> <SUBNET3_CIDR_BLOCK> <VPC_NAME>`
    
    Example:
    
    > `./csye6225-aws-networking-setup.sh us-east-1 10.0.0.0/16 10.0.1.0/24 10.0.2.0/24 10.0.3.0/24 vpc_test_dev`
7. Run the `csye6225-aws-networking-teardown.sh` script to destroy the AWS infrastructure using the following syntax:
    > `./csye6225-aws-networking-teardown.sh <AWS_REGION> <VPC_NAME>`
    
    Example:
    
    > `./csye6225-aws-networking-teardown.sh us-east-1 vpc_test_dev`
