# GitHub Secrets cần thiết cho Azure deploy

Thêm các secrets sau trong Settings > Secrets and variables > Actions trên GitHub:

## Backend
- AZURE_CREDENTIALS: JSON credentials từ Azure CLI
- AZURE_WEBAPP_NAME_BACKEND: tên Azure Web App backend

Example AZURE_CREDENTIALS:
```json
{
  "clientId": "...",
  "clientSecret": "...",
  "subscriptionId": "...",
  "tenantId": "..."
}
```

## Frontend
- AZURE_STATIC_WEB_APPS_API_TOKEN: token của Azure Static Web Apps
- VITE_API_URL: https://<backend-domain>/api/v1
- VITE_WS_URL: https://<backend-domain>/ws
- VITE_APP_NAME: Horse Racing Tournament

## Cách tạo Azure credentials
```bash
az login
az ad sp create-for-rbac --name "github-actions-sp" --role contributor --scopes /subscriptions/<subscription-id>/resourceGroups/<resource-group-name> --sdk-auth
```
