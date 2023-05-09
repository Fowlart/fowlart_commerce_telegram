# init
clear
echo "delte resource group"
az group delete \
  --name "fowlart_tg_bot_rg" \
  --yes

echo "create resource group"
az group create \
  --name "fowlart_tg_bot_rg" \
  --location "polandcentral"

az group list --output table

mvn clean scala:compile package spring-boot:repackage && jar tf target/app-1.jar

mvn azure-webapp:deploy