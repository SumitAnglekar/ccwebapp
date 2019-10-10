#!/bin/sh
echo "Enter the profile name: "
read profile

if [[ -z "$profile" ]]
then   
    echo "Profile cannot be blank!! Please enter appropriate profile!!";
    exit 1;
fi

export AWS_PROFILE=$profile

stack_name=$1

echo "Initiating the script..."
echo "Checking if the stack already exists..."

if  aws cloudformation describe-stacks --stack-name $stack_name > /dev/null 2>&1; 
then
   echo "Stack already exists!!!";
else
    # defined variable for vpc name
    vpc_name=$2
    # defined variable for  aws_region
    aws_region=$3
    # defined variable for vpc_cidr_block
    vpc_cidr_block=$4
    # defined variable for subnet1_cidr_block
    subnet1_cidr_block=$5
    # defined variable for subnet2_cidr_block
    subnet2_cidr_block=$6
    # defined variable for subnet3_cidr_block
    subnet3_cidr_block=$7

    aws_profile_region=$(aws configure get region)

    if [[ $aws_profile_region -ne $aws_region ]]
    then
        echo "AWS regions do not match to the profile!!!"
        exit 1
    fi

    if [[ -z "$stack_name" || -z "$vpc_name" || -z "$aws_region" || -z "$vpc_cidr_block" || -z "$subnet1_cidr_block" || -z "$subnet2_cidr_block" || -z "$subnet3_cidr_block" ]]
    then
        echo "Please enter all parameters, none of them should be blank!!!"
        echo "The order of arguments is - Stack Name, VPC Name, AWS Region, VPC CIDR Block, Subnet1 CIDR Block, Subnet2 CIDR Block, Subnet3 CIDR Block"
        exit 1
    else
        echo -e "Stack does not exist, creating a stack..."

        #subnet 1,2,3 route table and internetgateway name has been defined
        SUBNET01=$vpc_name-subnet1
        SUBNET02=$vpc_name-subnet2
        SUBNET03=$vpc_name-subnet3
        ROUTETABLE=$vpc_name-routetable
        INTERNETGATEWAY=$vpc_name-InternetGateway

        aws cloudformation create-stack --stack-name $stack_name --template-body file://csye6225-cf-networking.json --parameters ParameterKey=vpcName,ParameterValue=$vpc_name ParameterKey=VPCCIDR,ParameterValue=$vpc_cidr_block ParameterKey=Subnet01CIDR,ParameterValue=$subnet1_cidr_block ParameterKey=Subnet02CIDR,ParameterValue=$subnet2_cidr_block ParameterKey=Subnet03CIDR,ParameterValue=$subnet3_cidr_block ParameterKey=Region,ParameterValue=$aws_region ParameterKey=Subnet01Name,ParameterValue=$SUBNET01 ParameterKey=Subnet02Name,ParameterValue=$SUBNET02 ParameterKey=Subnet03Name,ParameterValue=$SUBNET03 ParameterKey=InternetGatewayName,ParameterValue=$INTERNETGATEWAY ParameterKey=RouteTableName,ParameterValue=$ROUTETABLE
        if [ $? -eq 0 ]; then
            aws cloudformation wait stack-create-complete --stack-name $stack_name
            if [ $? -eq 0 ]; then
                echo "Stack created successfully"
            else
                echo "Stack creation unsuccessful"
            fi
        else
            echo "Stack creation unsuccessful"
        fi
    fi
fi

