# CSYE 6225 - Fall 2019

## Team Information

| Name | NEU ID | Email Address |
| --- | --- | --- |
| Ishita Sequeira| 001403357 | sequeira.i@husky.neu.edu |
| Tejas Shah | 001449694 | shah.te@husky.neu.edu |
| Sumit Anglekar | 001475969 | anglekar.s@husky.neu.edu |

## Technology Stack
1. SpringBoot
2. Spring Security
3. Hibernate
4. Postgres Database
5. Version Control : Git

## Build Instructions
1. Clone the git repository: git@github.com:{SumitAnglekar94/shah-tejas/ishitasequeira}/ccwebapp.git
2. Traverse to the folder ./ccwebapp/webapp/recipe.
3. Download the required maven dependencies by going to File > Maven > Re-import dependencies
4. Run the command to build the module: mvn clean install.

## Deploy Instructions

## Application Endpoints
1. Register a User (localhost:8080/v1/user)
2. Get User records (localhost:8080/v1/user/self)
3. Update User recordds (localhost:8080/v1/user/self)
4. Register a Recipe (localhost:8080/v1/recipe/)
5. Get recipe Information (localhost:8080/v1/recipe/{id})
6. Delete a particular recipe (localhost:8080/v1/recipe/{id})

## Running Tests
1. Run the "run all tests" configuration for JUnit.

## CI/CD
