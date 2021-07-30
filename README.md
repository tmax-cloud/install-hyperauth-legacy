# 버전 정보
keycloak main version : 11.0.2
hyperauth server version : b1.1.1.18
jboss module version : 1.10.1.Final
wildfly core version : 12.0.3.Final

# Install Guide

## Install Postgresql 
- yum install postgresql postgresql-server postgresql-devel postgresql-contrib postgresql-docs
- postgresql-setup initdb
- systemctl enable postgresql.service
- systemctl start postgresql.service
- vi /var/lib/pgsql/data/pg_hba.conf
  - ![image](https://user-images.githubusercontent.com/61040426/127610925-a3c5d8fc-2406-4dee-837d-eb5a4c27e954.png)
- - systemctl restart postgresql.service
- su - postgres
- psql -U postgres
  - create user name keycloak with password 'keycloak';
  - alter role keycloak superuser createdb;
  - create database keycloak owner keycloak;
- systemctl status postgresql.service
  - localhost:5432로 postgresql이 구동중인 것을 확인한다.
- psql -U keycloak -W 로 접속 확인   

## Install Hyperauth
-  bin/standalone.sh 실행
-  bin/add-user-keycloak.sh --user admin 로 admin 유저 생성
-  http://localhost:8080 접속 확인
