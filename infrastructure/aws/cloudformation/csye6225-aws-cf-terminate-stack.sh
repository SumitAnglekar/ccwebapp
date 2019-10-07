stackName=$1

if [ -z "$1" ]
then
    echo "Enter Stack name that needs to be deleted!!"
    exit 1
else
    echo -e "Delete Process started\n"
    aws cloudformation --profile dev delete-stack --stack-name $stackName

    aws cloudformation --profile dev wait stack-delete-complete --stack-name $stackName

    if [ $? -eq 0 ]; then
        echo "$stackName Deleted Successfully"
    else
        echo "Deletion didn't go through!!"
    fi
fi