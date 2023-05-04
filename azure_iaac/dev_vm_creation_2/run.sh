# init
clear
echo "delte resource group"
az group delete \
  --name "vm_for_tests" \
  --yes

echo "create resource group"
az group create \
  --name "vm_for_tests" \
  --location "polandcentral"

az group list --output table

#run template
templateFile="template.json"
today=$(date +"%d-%b-%Y")
DeploymentName="VM-LINUX-"$today
param_file="template.parameters.json"

az deployment group create \
  --name $DeploymentName \
  --template-file $templateFile\
  --resource-group "vm_for_tests" \
  --parameters $param_file