Account service
---

Следующий компоненты необходимы для работы

* Tomcat
* Mysql
* Maven

Зайти в MySQL и создать базу test. Один из способов из командной строки:
mysql -uroot -proot
create database test character set utf8

Накатить [скрипт](src/main/sql/schema.sql) для создания таблички балансов

####Сервер
Собрать и задеплоить на томкат [web](web) модуль проекта
В [проперти файле](web/src/main/resources/application.properties) можно задать настройки базы данных и кеша.

####Клиент
Это standalone приложение. Главный класс [Client](client/src/main/java/proservice/Client.java).
Параметры:
- rCount - количество читателей вызывающих метод getAmount(id)
- wCount - количество читателей вызывающих метод addAmount(id,value)
- idList - список или доапазон ключей которые будут использоваться для тестирования
- activeThreads - количество одновременно работающих тредов. Если параметр отсутствует, то количество не ограничено.
В [проперти файле](client/src/main/resources/application.properties) можно задать настройки http и URL до сервера.
