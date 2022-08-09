curl -X POST \
-H "Content-Type: application/json" \
-H "Authorization: Bearer ${IAM_TOKEN}" \
-d "{\"folderId\": \"b1g1voqf1u5td0o4lhf1\"}" \
"https://translate.api.cloud.yandex.net/translate/v2/languages"
