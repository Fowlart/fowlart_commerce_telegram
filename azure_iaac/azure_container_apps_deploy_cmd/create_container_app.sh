az extension add --name containerapp --upgrade

az provider register --namespace Microsoft.App

az provider register --namespace Microsoft.OperationalInsights

az group create \
    --name $myRG \
    --location $myLocation


# An environment in Azure Container Apps creates a secure boundary around a group of container apps.
# Container Apps deployed to the same environment are deployed in the same virtual network and write logs
# to the same Log Analytics workspace.
az containerapp env create \
    --name $myAppContEnv \
    --resource-group $myRG \
    --location $myLocation

az containerapp env storage set --name $myAppContEnv --resource-group $myRG \
    --storage-name dzmilstore \
    --azure-file-account-name dzmilstore \
    --azure-file-account-key $AZ_FILE_ACC_KEY \
    --azure-file-share-name botstore \
    --access-mode ReadWrite

# create an image
az acr build --image dzmil-commerce:v3  \
    --registry $ACR_NAME \
    --resource-group $RG_NAME \
    --file Dockerfile .

# Create a container app in the environment.
az containerapp create \
    --name dzmil-commerce \
    --resource-group $myRG \
    --environment $myAppContEnv \
    --image $ACR_NAME.azurecr.io/dzmil-commerce:v3 \
    --target-port 443 \
    --ingress 'external' \
    --registry-username $ACR_NAME \
    --registry-password $ACR_PASS \
    --registry-server $ACR_NAME.azurecr.io \
    --env-vars \
    AZURE_STORAGE_CONNECTION_STRING=$AZURE_STORAGE_CONNECTION_STRING \
    EMAIL_PASSWORD=$EMAIL_PASSWORD \
    COSMOS_KEY=$COSMOS_KEY \
    SERVICE_BUS_CONNECTION_STRING=$SERVICE_BUS_CONNECTION_STRING \
    SERVICE_BUS_SECRET=$SERVICE_BUS_SECRET


# WARNING: The command below will delete the resource group and all its resources
az group delete --name $myRG --yes

