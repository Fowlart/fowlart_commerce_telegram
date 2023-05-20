# init
clear
echo "delte resource group"
az group delete \
  --name "tg_bot_app_service_rg" \
  --yes

echo "create resource group"
az group create \
  --name "tg_bot_app_service_rg" \
  --location "polandcentral"

az group list --output table

#kill deployment
az webapp deployment source delete --name FowlartTgBot --resource-group tg_bot_app_service_rg