curl -X POST \
  -H "Authorization: Bearer ${IAM_TOKEN}" \
  --data-urlencode "text=astonishing" \
  -d "lang=en-US&folderId=b1g1voqf1u5td0o4lhf1" \
  "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize" >speech.ogg
