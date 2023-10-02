export RG_NAME=tg_bot_app_service_rg
export SERVICE_PLAN_NAME=tg_bot_app_service_plan
export ACR_NAME=fowlartcontainerregistry
export APP_NAME=fowlart-tg-bot
export STORAGE_ACCOUNT_NAME=fowlartstorageaccount

# Common
az group create -l polandcentral -n $RG_NAME
az acr create -n $ACR_NAME --sku basic --admin-enabled true -g $RG_NAME

az appservice plan create -n $SERVICE_PLAN_NAME -g $RG_NAME --sku S1 --output json --is-linux

az webapp create --runtime "JAVA:17-java17" -g $RG_NAME --public-network-access Enabled -p $SERVICE_PLAN_NAME -n $APP_NAME

# Create storage account and blob container, wait for it to be created

az storage account create -n $STORAGE_ACCOUNT_NAME -g $RG_NAME --sku Standard_LRS --kind StorageV2 --access-tier Hot --https-only true --allow-blob-public-access true


sleep 30

# Get connection
CONNECTION_STRING=$(az storage account show-connection-string -n $STORAGE_ACCOUNT_NAME -g $RG_NAME -o tsv)

# Set the connection string as an app setting for the web app
az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings AZURE_STORAGE_CONNECTION_STRING="$CONNECTION_STRING"

# Create a storage container
az storage container create -n itempicturestore --account-name $STORAGE_ACCOUNT_NAME

# Create file share
az storage share create -n botstore --account-name $STORAGE_ACCOUNT_NAME

# sleep 20 seconds to wait for the storage account to be created
sleep 20

# Assign file share to web app, mount path is /botstore
az webapp config storage-account add --resource-group $RG_NAME --name $APP_NAME --custom-id botstore --storage-type AzureFiles --share-name botstore --account-name $STORAGE_ACCOUNT_NAME --access-key $(az storage account keys list -g $RG_NAME -n $STORAGE_ACCOUNT_NAME --query '[0].value' -o tsv) --mount-path /botstore



