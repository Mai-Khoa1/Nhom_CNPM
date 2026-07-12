# GitHub Secrets cho deploy frontend lên Azure App Service

Thêm các secrets sau trong GitHub:

- AZURE_CREDENTIALS: JSON credentials từ Azure CLI
- AZURE_WEBAPP_NAME_FRONTEND: tên Azure Web App frontend
- VITE_API_URL: https://<backend-domain>/api/v1
- VITE_WS_URL: https://<backend-domain>/ws
- VITE_APP_NAME: Horse Racing Tournament

## Cách tạo Azure credentials
```bash
az login
az ad sp create-for-rbac --name "github-actions-sp" --role contributor --scopes /subscriptions/<subscription-id>/resourceGroups/<resource-group-name> --sdk-auth
```
