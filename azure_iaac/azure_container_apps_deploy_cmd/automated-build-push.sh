# automated task, by yaml
az acr run -f build-push-app.yaml https://github.com/Fowlart/fowlart_commerce_telegram  \
--registry $ACR_NAME \
--resource-group $RG_NAME