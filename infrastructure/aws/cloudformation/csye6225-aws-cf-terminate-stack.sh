#!/bin/sh

#Taking profile name from user
echo "Enter the profile name: "
read profile

if [[ -z "$profile" ]]; then
    echo "Profile cannot be blank!! Please enter appropriate profile!!"
    exit 1
fi

export AWS_PROFILE=$profile

stackName=$1
aws_region=$2

#Checking if profile or region is not empty
if [[ -z "$1" || -z "$2" ]]; then
    echo "Enter Stack name and in which AWS REGION that needs to be deleted!!"
    exit 1
else
    export AWS_DEFAULT_REGION=$aws_region
    echo $AWS_DEFAULT_REGION

    #Checking if the stack that needs to deleted is present or not
    if aws cloudformation describe-stacks --stack-name $stackName >/dev/null 2>&1; then
        echo -e "Delete Process started\n"

        #triggers deletion of the stack
        aws cloudformation delete-stack --stack-name $stackName

        #waits for the deletion to complete
        aws cloudformation wait stack-delete-complete --stack-name $stackName

        if [ $? -eq 0 ]; then
            echo "$stackName Deleted Successfully"
        else
            echo "Deletion didn't go through!!"
        fi
    else
        echo -e "Stack not present to be deleted!!!\n"
    fi
fi
