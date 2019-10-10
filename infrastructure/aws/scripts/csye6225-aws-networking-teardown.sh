#!/bin/sh

# Get the VPC name
aws_region=$1
vpc_name=$2

if [ -z $vpc_name ]
then
    echo "Please provide a vpc_name"
    exit 1
fi

# Check if AWS_PROFILE is set?
if [ -z "$AWS_PROFILE" ]
then
    echo "Please set the AWS_PROFILE first!"
    exit 1
fi

if [ -z "$AWS_DEFAULT_REGION" ] && [ -z "$aws_region" ]
then
    echo "Please either set AWS_DEFAULT_REGION or specify the region while running the script!"
    exit 1
elif [ -z "$aws_region" ]
then
    echo "Using the AWS region set in AWS_DEFAULT_REGION environment variable..."
    aws_region=$AWS_DEFAULT_REGION
fi

export AWS_DEFAULT_REGION=$aws_region

# Fetch VPC Id
vpc_id=$(aws ec2 describe-tags \
                --filter Name=tag:Name,Values="${vpc_name}" \
                | jq '.Tags[0].ResourceId' | tr -d '"'
        )
if [ -z $vpc_id ] || [ $vpc_id = "null" ]
then
    echo "Error fetching the VPC Id"
    exit 1
fi

echo "Beginning AWS network setup teardown..."

# Fetch required ids
route_table_id=$(aws ec2 describe-tags --filter Name=tag:Name,Values=${vpc_name}_route_table \
                       | /usr/bin/jq '.Tags[0].ResourceId' | tr -d '"')
if [ -z $route_table_id ] || [ $route_table_id = "null" ]
then
    echo "Error fetching the route_table_id"
    exit 1
fi

route_table_associations=$(aws ec2 describe-route-tables --route-table-ids $route_table_id | /usr/bin/jq '.RouteTables[0].Associations')
subnet_association1=$(echo $route_table_associations | /usr/bin/jq '.[0].RouteTableAssociationId' | tr -d '"')
subnet_id1=$(echo $route_table_associations | /usr/bin/jq '.[0].SubnetId' | tr -d '"')
subnet_association2=$(echo $route_table_associations | /usr/bin/jq '.[1].RouteTableAssociationId' | tr -d '"')
subnet_id2=$(echo $route_table_associations | /usr/bin/jq '.[1].SubnetId' | tr -d '"')
subnet_association3=$(echo $route_table_associations | /usr/bin/jq '.[2].RouteTableAssociationId' | tr -d '"')
subnet_id3=$(echo $route_table_associations | /usr/bin/jq '.[2].SubnetId' | tr -d '"')

ig_id=$(aws ec2 describe-tags \
                --filter Name=tag:Name,Values=${vpc_name}_internet_gateway \
            | /usr/bin/jq '.Tags[0].ResourceId' | tr -d '"'
            )
if [ -z $ig_id ] || [ $ig_id = "null" ]
then
    echo "Error fetching the ig_id"
    exit 1
fi

# Delete the public route
echo "Deleting the public route..."
{
    aws ec2 delete-route --route-table-id "$route_table_id" \
                         --destination-cidr-block "0.0.0.0/0" \
                         >/dev/null
} || {
    echo "Error deleting the public route..."
    exit 1
}

# Disassociate all the subnets from routing table
echo "Disassociating the subnets from the routing table..."
{
    aws ec2 disassociate-route-table --association-id "$subnet_association1" &&
    aws ec2 disassociate-route-table --association-id "$subnet_association2" &&
    aws ec2 disassociate-route-table --association-id "$subnet_association3"
} || {
    echo "Error disassociating subnets from routing table"
    exit 1
}

# Delete the routing table
echo "Deleting the routing table..."
{
    aws ec2 delete-route-table --route-table-id "$route_table_id"
} || {
    echo "Error deleting routing table"
    exit 1
}

# Detach the internet gateway from VPC
echo "Detaching the internet gateway from VPC..."
{
    aws ec2 detach-internet-gateway --internet-gateway-id "$ig_id" --vpc-id "$vpc_id"
} || {
    echo "Error detaching the internet gateway from VPC"
    exit 1
}

# Delete the internet gateway
echo "Deleting the internet gateway..."
{
    aws ec2 delete-internet-gateway --internet-gateway-id "$ig_id"
} || {
    echo "Error deleting the internet gateway"
    exit 1
}

# Delete the subnets
echo "Deleting the subnets..."
{
    aws ec2 delete-subnet --subnet-id "$subnet_id1" &&
    aws ec2 delete-subnet --subnet-id "$subnet_id2" &&
    aws ec2 delete-subnet --subnet-id "$subnet_id3"
} || {
    echo "Error deleting the subnets"
    exit 1
}

# Delete the VPC
echo "Deleting the VPC..."
{
    aws ec2 delete-vpc --vpc-id ${vpc_id}
} || {
    echo "Error deleting the VPC"
    exit 1
}

echo "AWS Networking setup deleted via CLI."