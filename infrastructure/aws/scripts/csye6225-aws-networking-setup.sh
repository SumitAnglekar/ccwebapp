#!/bin/sh

# Get arguments and Set default values
aws_region=${1:-"us-east-1"}
vpc_cidr_block=${2:-"10.0.0.0/16"}
subnet_cidr_block1=${3:-"10.0.1.0/24"}
subnet_cidr_block2=${4:-"10.0.2.0/24"}
subnet_cidr_block3=${5:-"10.0.3.0/24"}
vpc_name=${6:-"NEW_VPC"}

# Run create-vpc command
{
    script_response="$(aws ec2 create-vpc --cidr-block "${vpc_cidr_block}" --output json)"
} || {
    echo "VPC creation failed"
    exit 1
}
# Fetch the VPC ID
{
    vpc_id=$(echo "$script_response" | /usr/bin/jq '.Vpc.VpcId' | tr -d '"')
} || {
    echo "Unable to fetch VPC Id"
    exit 1
}
echo "VPC with VPC ID: $vpc_id created"

# Add the name tag to the VPC
aws ec2 create-tags --resources "$vpc_id" --tags Key=Name,Value="$vpc_name"

# TODO
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
    subnet1="$(aws ec2 create-subnet --availability-zone-id "${subnet_avbl_zone1}" \
                            --cidr-block "${subnet_cidr_block1}" \
                            --vpc-id "${vpc_id}" \
                            --output json)"
    subnet_id1=$(echo "$subnet1" | /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
    aws ec2 create-tags --resources "$subnet_id1" --tags Key=Name,Value="${vpc_name}_subnet1"
    echo "Created subnet: ${subnet_id1}"

    subnet2="$(aws ec2 create-subnet --availability-zone-id "${subnet_avbl_zone2}" \
                            --cidr-block "${subnet_cidr_block2}" \
                            --vpc-id "${vpc_id}" \
                            --output json)"
    subnet_id2=$(echo "$subnet2" | /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
    aws ec2 create-tags --resources "$subnet_id2" --tags Key=Name,Value="${vpc_name}_subnet2"
    echo "Created subnet: ${subnet_id2}"
    
    subnet3="$(aws ec2 create-subnet --availability-zone-id "${subnet_avbl_zone3}" \
                            --cidr-block "${subnet_cidr_block3}" \
                            --vpc-id "${vpc_id}" \
                            --output json)"
    subnet_id3=$(echo "$subnet3" | /usr/bin/jq '.Subnet.SubnetId' | tr -d '"')
    aws ec2 create-tags --resources "$subnet_id3" --tags Key=Name,Value="${vpc_name}_subnet3"
    echo "Created subnet: ${subnet_id3}"
} || {
    echo "Unable to create subnets"
    exit 1
}


# TODO
# Create Internet Gateway resource. and attach the Internet Gateway to the VPC.


# TODO
# Create a public route table. Attach all subnets created above to the route table.


# TODO
# Create a public route in the public route table created above with 
# destination CIDR block 0.0.0.0/0 and internet gateway created above as the target.


