#!/bin/sh

# Get the VPC name
vpc_name=$1

if [ -z $vpc_name ]
then
    echo "Please provide a vpc_name"
    exit 1
fi

# Fetch subnet ids
subnet1_id=$(aws ec2 describe-tags \
                --filter Name=tag:Name,Values=${vpc_name}_subnet1 \
            | jq '.Tags[0].ResourceId' | tr -d '"'
            )
subnet2_id=$(aws ec2 describe-tags \
                --filter Name=tag:Name,Values=${vpc_name}_subnet2 \
            | jq '.Tags[0].ResourceId' | tr -d '"'
            )
subnet3_id=$(aws ec2 describe-tags \
                --filter Name=tag:Name,Values=${vpc_name}_subnet3 \
            | jq '.Tags[0].ResourceId' | tr -d '"'
            )

# Fetch VPC Id
vpc_id=$(aws ec2 describe-tags \
                --filter Name=tag:Name,Values=${vpc_name} \
            | jq '.Tags[0].ResourceId' | tr -d '"'
            )

# Delete the subnets
{
    aws ec2 delete-subnet --subnet-id ${subnet1_id}
    aws ec2 delete-subnet --subnet-id ${subnet2_id}
    aws ec2 delete-subnet --subnet-id ${subnet3_id}
    echo "Subnets deleted"
} || {
    echo "Error deleting subnets"
    exit 1
}

# Delete the VPC
{
    aws ec2 delete-vpc --vpc-id ${vpc_id}
} || {
    echo "Error deleting the VPC"
    exit 1
}

echo "VPC ${vpc_name} delete successfully"