# simple-log-monitoring-using-docker

..

## STEP 0.


### Install necessary software
#### install docker
- https://docs.docker.com/engine/installation/linux/centos/#install-using-the-repository
```
sudo yum remove docker \
                  docker-common \
                  container-selinux \
                  docker-selinux \
                  docker-engine
sudo yum install -y yum-utils device-mapper-persistent-data lvm2
sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
sudo yum-config-manager --enable docker-ce-edge
sudo yum makecache fast
sudo yum install docker-ce
sudo systemctl start docker
```

#### install docker-compose
```
sudo yum install epel-release
sudo yum install python-devel
sudo yum install -y python-pip
sudo pip install --upgrade pip
sudo pip install docker-compose
```

#### install sbt
```
> curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
> sudo yum install sbt
```

### Download project file
```
> cd ~
> git clone https://github.com/freepsw/simple-log-monitoring-using-docker.git
>
```


## STEP 1. Run apache flume
- https://github.com/mrwilson/docker-flume 일부 참고

### Build flume image
- java 설치 부분 변경, 수집할 경로 추가
```
> cd 01.flume
> docker build -t flume .
```

### Run flume agent and collect file using spoolDir source
- volume 옵션을 추가하여 외부에서 작성한 conf(properties)파일과 수집할 경로를 매핑하였다.
```
> docker run -e FLUME_AGENT_NAME=agent -e FLUME_CONF_FILE=/var/tmp/spoolDir-kafka.properties \
  -v /home/rts/apps/github/docker-flume/:/var/tmp -v /home/rts/data/input:/opt/flume/input flume
```

## STEP 2. Run apache kafka
- https://github.com/wurstmeister/kafka-docker 참고

### Edit environment setting
-
```
> cd 02.kafka
> vi docker-compose.yml
environment:
  KAFKA_ADVERTISED_HOST_NAME: <kafka broker ip>
  KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
  KAFKA_CREATE_TOPICS: "topic-name:1:1:compact"
```

### Run docker compose
```
> docker-compose up -d
```


### Test command

#### Using docker
- http://wurstmeister.github.io/kafka-docker/ 참고
##### - create topic and run kafka producer using docker
```
# connect to kafka container
> ./start-kafka-shell.sh 10.178.50.105  10.178.50.105:2181

# create topic on container
># $KAFKA_HOME/bin/kafka-topics.sh --create --topic topic --partitions 1 --zookeeper $ZK --replication-factor 1

# run producer on container
># $KAFKA_HOME/bin/kafka-console-producer.sh --topic=topic --broker-list=`broker-list.sh`
```
- `broker-list.sh`를 사용하는 이유는
- container의 kafka의 9092 port를 외부에서 접속하기 위한 port를 동적으로 생성하기 때문에,
- 이를 script로 찾아내기 위함임.
- docker-compose.yml
```
ports:
  - "9092"
```
- 하나의 host에 여러개의 kafka broker를 구동하기 위해서 설정함
- 만약 하나의 kafka container를 구동하려면 "9092:9092"로 하면 외부에서도 9092로 접속할 수 있다.
- 즉 아래와 같이 호출 가능
```
># $KAFKA_HOME/bin/kafka-console-producer.sh --topic=topic1 --broker-list=10.178.50.105:9092
```

##### - run kafka consumer
```
# connect to kafka container
> ./start-kafka-shell.sh 10.178.50.105  10.178.50.105:2181

# run kafka consumer

```

#### Using installed apache kafka command on the host
```
# download kafka
> wget http://apache.mirror.cdnetworks.com/kafka/0.10.2.0/kafka_2.11-0.10.2.0.tgz

> bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic LprData
> bin/kafka-topic.sh --describe --zookeeper localhost:2181 --topic <topic name>
> bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic <topic name> --from-beginning

# consumer group list
> bin/kafka-consumer-groups.sh --bootstrap-server broker1:9092 --list

# view offset of group
> bin/kafka-consumer-groups.sh --bootstrap-server broker1:9092 --describe --group test-consumer-group
```


## STEP 3. Build spark application
```


```

## STEP 3-1. Run apache spark
- https://github.com/gettyimages/docker-spark 참고

### Run docker compose
```
> docker-compose up -d
```



## STEP 4. run elasticsearch & kibana
- https://github.com/deviantony/docker-elk 참고
- 여기서 사용하고 있는 logstash docker image가 2017.06.20 이후로 deprecated 됨
 - https://hub.docker.com/_/logstash/
 - 따라서 공식 docker github인 https://github.com/elastic/logstash-docker로 변경필요

### Run elasticsearch


#### elasticsearch의 config(elasticsearch.yml) 변경
- head plugin server가 elasticsearch에 정상적으로 접속할 수 있도록 설정을 변경 (보안 설정)
```
http.cors.enabled: true
http.cors.allow-origin: "*"
http.cors.allow-headers: Authorization
```

#### logstash는 사용하지 않으므로, docker-compose.yml에서 제거
- 아래 내용 삭제
```
logstash:
  build: logstash/
  volumes:
    - ./logstash/config/logstash.yml:/usr/share/logstash/config/logstash.yml
    - ./logstash/pipeline:/usr/share/logstash/pipeline
  ports:
    - "5000:5000"
  environment:
    LS_JAVA_OPTS: "-Xmx256m -Xms256m"
  networks:
    - elk
  depends_on:
    - elasticsearch
```

### Run docker compose
```
> docker-compose up -d
```

- 아래와 같은 에러가 보이면 docker-py 버전 문제일 수 있으니, 다시 업그레이드 하자.
```
WARNING: Dependency conflict: an older version of the 'docker-py' package may be polluting the namespace. If you're experiencing crashes, run the following command to remedy the issue:
```
```
> sudo pip uninstall docker-py
> sudo pip uninstall docker
> sudo pip install docker
```


#### test elasticsearch
- index 생성 및 조회
```
> curl -XPUT 'localhost:9200/customer?pretty&pretty'
> curl -XGET 'localhost:9200/_cat/indices?v&pretty'
```


### Run elasticsearch plugin - Head
- https://github.com/mobz/elasticsearch-head 참고
- elasticsearch의 상태 및 생성된 index 및 입력정보를 web ui를 통해서 조회가능한 plugin
- elasticsearch와 같은 서버에서 구동한다.
```
> docker run -p 9100:9100 mobz/elasticsearch-head:5
http:<ip>:9100 으로 접속
```
