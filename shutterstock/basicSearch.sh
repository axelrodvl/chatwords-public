#curl -X GET "https://api.shutterstock.com/v2/images/search" \
#--header "Authorization: Bearer v2/SET_VALUE" \
#-G \
#--data-urlencode "query=notebook" \
#--data-urlencode "image_type=photo" \
#--data-urlencode "orientation=vertical" \
#--data-urlencode "people_number=0"

curl -X GET "https://api.shutterstock.com/v2/images/search" \
--header "Authorization: Bearer v2/SET_VALUE" \
-G \
--data-urlencode "query=собака" \
--data-urlencode "image_type=photo" \
--data-urlencode "orientation=horizontal" \
--data-urlencode "people_number=0" \
--data-urlencode "page=1" \
--data-urlencode "per_page=1" \
--data-urlencode "language=ru" \
--data-urlencode "fields=data(assets/huge_thumb/url)"