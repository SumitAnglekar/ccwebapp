#!/bin/sh
echo "Enter the profile name: "
read profile

if [[ -z "$profile" ]]
then   
    echo "Profile cannot be blank!! Please enter appropriate profile!!";
    exit 1;
fi

export AWS_PROFILE=$profile

stackName=$1

if [ -z "$1" ]
then
    echo "Enter Stack name that needs to be deleted!!"
    exit 1
else
    echo -e "Delete Process started\n"
    aws cloudformation delete-stack --stack-name $stackName

    aws cloudformation wait stack-delete-complete --stack-name $stackName

    if [ $? -eq 0 ]; then
        echo "$stackName Deleted Successfully"
    else
        echo "Deletion didn't go through!!"
    fi
fi