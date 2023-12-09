# ChatWords - Vocabulary-Building Telegram Bot
Based on Yandex.Dictionary API.
Translation support: 🇷🇺🇺🇸🇩🇪🇪🇸🇫🇷🇮🇹🇵🇹🇹🇷🇺🇦

## Links
- Bots
    - [Bot (PROD)](https://t.me/chatwordsappbot)
    - [Bot (DEV)](https://t.me/chatwordsappdevbot)
    - [Analytics Bot](https://t.me/chatwordsappproductionbot)
- Resources
    - [Yandex Cloud](https://console.cloud.yandex.ru)
    - [Grafana](Hidden)
    - [Amplitude](Hidden)
    - [Instagram](https://www.instagram.com/chatwords/)
    - [SonarQube](http://localhost:9000)
- Translation
    - [Ads](Hidden)
    - [Ukrainian](Hidden)

## Telegram Bot Description
```
🐈 ChatWords
Словарь и переводчик: 🇷🇺🇺🇸🇩🇪🇪🇸🇫🇷🇮🇹🇵🇹🇹🇷🇺🇦
Реализовано с помощью сервиса «API «Яндекс.Словарь»
✉️ @axelrodvl
```

## Sources
- https://www.wordfrequency.info/intro.asp
- https://www.english-corpora.org/coca/
- https://resources.ncelp.org/concern/resources/0c483j381?locale=en
- https://resources.ncelp.org/concern/resources/02870v87z?locale=en
- http://dict.ruslang.ru/freq.php?act=show&dic=freq_s&title
- http://u-mova.blogspot.com/2013/09/blog-post.html
- https://www.corpusitaliano.it/en/contents/description.html

## Mongo
```
db.user.find({"_id": "36887435"}).pretty()
db.user.update({"_id": "1904595"}, {$unset: {"onboarding":null}})
db.user.find({"_id": "36887435"}).pretty()
db.user.deleteOne({"_id": "36887435"})
```

## nginx
```
sudo tail -f /var/log/nginx/chatwords.log
```

## YandexCloud
```
AQA<HIDDEN>
```
