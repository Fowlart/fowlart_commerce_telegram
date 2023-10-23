export APP_NAME=dzmilcatalog
export RG_NAME=tg_bot_app_service_rg
export SERVICE_PLAN_NAME=tg_bot_app_service_plan
export ACR_NAME=fowlartcontainerregistry
export STORAGE_ACCOUNT_NAME=fowlartstorageaccount

az webapp show \
    --resource-group $RG_NAME \
    --name $APP_NAME \
    --slot stage