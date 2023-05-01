# create resource group
az group create \
  --name "lab_resource_group" \
  --location "polandcentral"

# list resource groups
az group list \
  --output table

# deploy storage account
templateFile="azure_iaac/lab_1/first_ARM_template.json"
today=$(date +"%d-%b-%Y")
DeploymentName="addstorage-"$today

az deployment group create \
  --name $DeploymentName \
  --template-file $templateFile\
  --resource-group "lab_resource_group"\
  --parameters storageAccountType=Standard_LRS\
  --parameters storeName="fowlartlabstorage2"\
  --parameters storageAccountKind="StorageV2"

# delete resource group
az group delete \
  --name "lab_resource_group" \
  --yes \
  --no-wait