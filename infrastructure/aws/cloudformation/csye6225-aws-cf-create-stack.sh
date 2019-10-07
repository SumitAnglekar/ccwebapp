echo "Enter the profile name: "
read profile

if [[ ! "$profile" ]]
then   echo "abcd" profile = "dev"
fi

echo "Enter the Stack name: "
read stack_name

echo "Initiating the script..."
echo "Checking if the stack already exists..."

if  aws cloudformation --profile dev describe-stacks --stack-name $stack_name ; then

   echo "Stack already exists, terminating a stack..."
    
 else
    echo "Enter the vpc name: "
    read vpc_name
    echo "Enter the AWS region: "
    read aws_region
    echo "Enter the vpc CIDR block: "
    read vpc_cidr_block
    echo "Enter the subnet1 cidr block: "
    read subnet1_cidr_block
    echo "Enter the subnet2 cidr block: "
    read subnet2_cidr_block
    echo "Enter the subnet3 cidr block: "
    read subnet3_cidr_block

    if  -z "$stack_name" || -z "$vpc_name" || -z "$aws_region" || -z "$vpc_cidr_block" || -z "$subnet1_cidr_block" || -z "$subnet2_cidr_block" || -z "$subnet3_cidr_block" 
    then
        echo "Please enter all parameters, none of them should be blank!!!"
        exit 1
    else
        echo -e "Stack does not exist, creating a stack..."
        # aws_response=$(aws cloudformation create-stack --stack-name $stack_name --template-body file://csye6225-cf-networking.json --on-failure DELETE)

        SUBNET01 = $vpc_name-subnet1
        SUBNET02 = $vpc_name-subnet2
        SUBNET03 = $vpc_name-subnet3
        ROUTETABLE = $vpc_name-routetable
        INTERNETGATEWAY = $vpc_name-InternetGateway

        aws cloudformation --profile dev create-stack --stack-name $stack_name --template-body file://csye6225-cf-networking.json --parameters ParameterKey=vpcName,ParameterValue=$vpc_name ParameterKey=VPCCIDR,ParameterValue=$vpc_cidr_block ParameterKey=Subnet01CIDR,ParameterValue=$subnet1_cidr_block ParameterKey=Subnet02CIDR,ParameterValue=$subnet2_cidr_block ParameterKey=Subnet03CIDR,ParameterValue=$subnet3_cidr_block ParameterKey=Region,ParameterValue=$aws_region ParameterKey=Subnet01Name,ParameterValue=$SUBNET01 ParameterKey=Subnet02Name,ParameterValue=$SUBNET02 ParameterKey=Subnet03Name,ParameterValue=$SUBNET03 ParameterKey=InternetGatewayName,ParameterValue=$INTERNETGATEWAY ParameterKey=RouteTableName,ParameterValue=$ROUTETABLE
            if [ $? -eq 0 ]; then
            aws cloudformation --profile dev wait stack-create-complete --stack-name $stack_name
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

