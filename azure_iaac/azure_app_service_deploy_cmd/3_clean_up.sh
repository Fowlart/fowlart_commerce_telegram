export RG_NAME=tg_bot_app_service_rg
export SERVICE_PLAN_NAME=tg_bot_app_service_plan
export ACR_NAME=fowlartcontainerregistry
export APP_NAME=fowlart-tg-bot

az group delete -n $RG_NAME --yes