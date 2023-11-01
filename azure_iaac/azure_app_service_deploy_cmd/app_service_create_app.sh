export APP_NAME=dzmilcatalog
export RG_NAME=tg_bot_app_service_rg
export SERVICE_PLAN_NAME=tg_bot_app_service_plan
export ACR_NAME=fowlartcontainerregistry
export STORAGE_ACCOUNT_NAME=fowlartstorageaccount

az webapp create --runtime "JAVA:17-java17" -g $RG_NAME \
--public-network-access Enabled \
-p $SERVICE_PLAN_NAME \
-n $APP_NAME

az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings COSMOS_KEY=$COSMOS_KEY
az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings AZURE_STORAGE_CONNECTION_STRING=$AZURE_STORAGE_CONNECTION_STRING
az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings SERVICE_BUS_CONNECTION_STRING=$SERVICE_BUS_CONNECTION_STRING
az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings SERVICE_BUS_SECRET=$SERVICE_BUS_SECRET
az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings EMAIL_PASSWORD=$EMAIL_PASSWORD
az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings AZ_FILE_ACC_KEY=$AZ_FILE_ACC_KEY
az webapp config appsettings set --name $APP_NAME --resource-group $RG_NAME --settings WEBSITES_PORT=443 --slot stage

# create an image
cd /Users/artur/IdeaProjects/fowlart_commerce_telegram
az acr build --image dzmil-catalog:v3  \
    --registry $ACR_NAME \
    --resource-group $RG_NAME \
    --file Dockerfile .

az webapp config container set --name $APP_NAME --resource-group $RG_NAME \
--resource-group $RG_NAME \
--docker-custom-image-name $ACR_NAME.azurecr.io/dzmil-catalog:v3 \
--docker-registry-server-url https://$ACR_NAME.azurecr.io \
--docker-registry-server-user $ACR_NAME \
--docker-registry-server-password $ACR_PASS

az webapp restart --name $APP_NAME --resource-group $RG_NAME

#set up file share
az webapp config storage-account add --name $APP_NAME --resource-group $RG_NAME \
--storage-type AzureFiles \
--share-name botstore \
--account-name $STORAGE_ACCOUNT_NAME \
--mount-path /botstore \
--custom-id botstore \
--access-key $AZ_FILE_ACC_KEY

