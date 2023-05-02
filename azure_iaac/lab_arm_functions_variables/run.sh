# init
az group create \
  --name "lab_resource_group" \
  --location "polandcentral"

az group delete \
  --name "lab_resource_group" \
  --yes

az configure --defaults group=lab_resource_group

az group list --output table

#run template
templateFile="azure_iaac/lab_arm_functions_variables/template.json"
today=$(date +"%d-%b-%Y")
DeploymentName="addstorage-"$today
param_file="azure_iaac/lab_arm_functions_variables/template_parameters_test.json"

az deployment group create \
  --name $DeploymentName \
  --template-file $templateFile\
  --resource-group "lab_resource_group"\
  --parameters $param_file