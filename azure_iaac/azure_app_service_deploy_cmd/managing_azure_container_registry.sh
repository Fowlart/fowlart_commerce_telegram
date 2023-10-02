export RG_NAME=tg_bot_app_service_rg
export SERVICE_PLAN_NAME=tg_bot_app_service_plan
export ACR_NAME=fowlartcontainerregistry
export APP_NAME=fowlart-tg-bot
export STORAGE_ACCOUNT_NAME=fowlartstorageaccount


# List all containers in acr
az acr repository list -n $ACR_NAME --output table

# what image is running
az webapp config container show -n $APP_NAME -g $RG_NAME --query linuxFxVersion

# delete unused images
az acr repository delete -n $ACR_NAME --image fowlart-tg-bot-999 --yes

# deploy new image
az webapp config container set -n $APP_NAME \
-g $RG_NAME --docker-custom-image-name $ACR_NAME.azurecr.io/test_image:v1 \
 --docker-registry-server-url https://$ACR_NAME.azurecr.io \
 --docker-registry-server-user $ACR_NAME \
 --docker-registry-server-password $ACR_PASSWORD

az acr build --image test_image:v1  \
    --registry $ACR_NAME \
    --resource-group $RG_NAME \
    --file Dockerfile .