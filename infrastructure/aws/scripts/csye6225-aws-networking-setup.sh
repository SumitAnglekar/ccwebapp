#!/bin/sh

# Get arguments
aws_region=$1
vpc_cidr_block=$2
subnet_cidr_block1=$3
subnet_cidr_block2=$4
subnet_cidr_block3=$5
vpc_name=$6

if [ -z $aws_region ]
then
    echo "Error: AWS Region not provided."
    exit 1
fi

if [ -z $vpc_cidr_block ]
then
    echo "Error: VPC CIDR Block not provided."
    exit 1
fi

if [ -z $subnet_cidr_block1 ]
then
    echo "Error: Subnet CIDR Block 1 not provided."
    exit 1
fi

if [ -z $subnet_cidr_block2 ]
then
    echo "Error: Subnet CIDR Block 2 not provided."
    exit 1
fi

if [ -z $subnet_cidr_block3 ]
then
    echo "Error: Subnet CIDR Block 3 not provided."
    exit 1
fi

if [ -z $vpc_name ]
then
    echo "Error: VPC Name not provided."
    exit 1
fi

export AWS_DEFAULT_REGION=$aws_region

# Check if AWS_PROFILE is set?
if [ -z "$AWS_PROFILE" ]
then
    echo "Please set the AWS_PROFILE first!"
    exit 1
fi

# Check if VPC with the name already exists?
existing_vpc_id=$(aws ec2 describe-tags --filter Name=tag:Name,Values=${vpc_name} | /usr/bin/jq '.Tags[0].ResourceId' | tr -d '"')
if [ -z "$existing_vpc_id" ] || [ "$existing_vpc_id" = "null" ]
then
    echo "Creating a new VPC..."
else
    echo "VPC with Name ${vpc_name} already exists!"
    exit 1
fi

# Run create-vpc command
{
    vpc_id="$(aws ec2 create-vpc --cidr-block "${vpc_cidr_block}" --output json | /usr/bin/jq '.Vpc.VpcId' | tr -d '"')"
    # Add the name tag to the VPC
    aws ec2 create-tags --resources "$vpc_id" --tags Key=Name,Value="$vpc_name"
} || {
    echo "VPC creation failed"
    exit 1
}

# Enable DNS hostnames and DNS support for the VPC
{
    aws ec2 modify-vpc-attribute --enable-dns-hostnames "{\"Value\":true}" --vpc-id "$vpc_id"
} || {
    echo "Error occured while enabling DNS for the VPC"
    exit 1
}

# Create subnets in your VPC. You must create 3 subnets, each in different availability zone
# in the same region in the same VPC.
# Step 1: Get 3 different availability zones for subnets
{
    availability_zones="$(aws ec2 describe-availability-zones --region "${aws_region}" --output json)"
    subnet_avbl_zone1=$(echo "$availability_zones" | /usr/bin/jq '.AvailabilityZones | .[0] | .ZoneId' | tr -d '"')
    subnet_avbl_zone2=$(echo "$availability_zones" | /usr/bin/jq '.AvailabilityZones | .[1] | .ZoneId' | tr -d '"')
    subnet_avbl_zone3=$(echo "$availability_zones" | /usr/bin/jq '.AvailabilityZones | .[2] | .ZoneId' | tr -d '"')
} || {
    echo "error in fetching availability zones"
    exit 1
}

# Step 2: Create the subnets
{
    echo "Creating subnet1..."
    subnet1="$(aws ec2 create-subnet --availability-zone-id "${subnet_avbl_zone1}" \
                            --cidr-block "${subnet_cidr_block1}" \
                            --vpc-id "${vpc_id}" \
                            --output json)"
    subnet_id1=$(echo "$subnet1" | /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
    aws ec2 create-tags --resources "$subnet_id1" --tags Key=Name,Value="${vpc_name}_subnet1"

    echo "Creating subnet2..."
    subnet2="$(aws ec2 create-subnet --availability-zone-id "${subnet_avbl_zone2}" \
                            --cidr-block "${subnet_cidr_block2}" \
                            --vpc-id "${vpc_id}" \
                            --output json)"
    subnet_id2=$(echo "$subnet2" | /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
    aws ec2 create-tags --resources "$subnet_id2" --tags Key=Name,Value="${vpc_name}_subnet2"
    
    echo "Creating subnet3..."
    subnet3="$(aws ec2 create-subnet --availability-zone-id "${subnet_avbl_zone3}" \
                            --cidr-block "${subnet_cidr_block3}" \
                            --vpc-id "${vpc_id}" \
                            --output json)"
    subnet_id3=$(echo "$subnet3" | /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
    aws ec2 create-tags --resources "$subnet_id3" --tags Key=Name,Value="${vpc_name}_subnet3"
} || {
    echo "Unable to create subnets"
    exit 1
}

# Ensure public IP addresses are assigned for the subnets
{
    aws ec2 modify-subnet-attribute --map-public-ip-on-launch "{\"Value\":true}" --subnet-id "${subnet_id1}"
    aws ec2 modify-subnet-attribute --map-public-ip-on-launch "{\"Value\":true}" --subnet-id "${subnet_id2}"
    aws ec2 modify-subnet-attribute --map-public-ip-on-launch "{\"Value\":true}" --subnet-id "${subnet_id3}"
} || {
    echo "Error while enabling public ip for subnet"
    exit 1
}

# Create Internet Gateway resource. and attach the Internet Gateway to the VPC.
{
    echo "Creating Internet Gateway..."
    ig_id=$(aws ec2 create-internet-gateway | /usr/bin/jq '.InternetGateway.InternetGatewayId' | tr -d '"')
    aws ec2 create-tags --resources "$ig_id" --tags Key=Name,Value="${vpc_name}_internet_gateway"
    echo "Attaching the Internet Gateway to the VPC..."
    aws ec2 attach-internet-gateway --internet-gateway-id "$ig_id" --vpc-id "$vpc_id"
} || {
    echo "Error while Internet Gateway creation"
    exit 1
}

# Create a public route table. Attach all subnets created above to the route table.
{
    echo "Creating a public route table..." &&
    route_table_id=$(aws ec2 create-route-table --vpc-id "${vpc_id}" | /usr/bin/jq '.RouteTable.RouteTableId' | tr -d '"') &&
    aws ec2 create-tags --resources "$route_table_id" --tags Key=Name,Value="${vpc_name}_route_table" &&
    echo "Attaching subnet 1 to route table..." &&
    aws ec2 associate-route-table --route-table-id "${route_table_id}" --subnet-id "${subnet_id1}" >/dev/null &&
    echo "Attaching subnet 2 to route table..." &&
    aws ec2 associate-route-table --route-table-id "${route_table_id}" --subnet-id "${subnet_id2}" >/dev/null &&
    echo "Attaching subnet 3 to route table..." &&
    aws ec2 associate-route-table --route-table-id "${route_table_id}" --subnet-id "${subnet_id3}" >/dev/null
} || {
    echo "Error while creating route table."
    exit 1
}

# Create a public route in the public route table created above with 
# destination CIDR block 0.0.0.0/0 and internet gateway created above as the target.
{
    echo "Creating a public route..."
    aws ec2 create-route --route-table-id "${route_table_id}" --destination-cidr-block "0.0.0.0/0" --gateway-id "${ig_id}" >/dev/null
} || {
    echo "Error creating public route"
}

echo "AWS setup via CLI successful!"