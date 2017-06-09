# Docker ELK stack 사용하기

## STEP 01. install necessary sw
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
sudo usermod -aG docker <username>
```

#### install docker-compose
```
sudo yum install epel-release
sudo yum install python-devel
sudo yum install -y python-pip
sudo pip install --upgrade pip
sudo pip install docker-compose
```

### install docker-py
```
sudo pip uninstall docker-py
sudo pip uninstall docker
sudo pip install docker
```

### install network utils
```
> sudo yum install net-tools
> sudo yum install nc
> sudo yum install curl
> sudo yum install wget
```


## STEP 02. copy doekr-elastic from github & run elk stack
```
> git clone https://github.com/deviantony/docker-elk.git
> cd docker-elk
> docker-compose up -d
```

- logstash config
```
> vi ~/docker-elk/logstash/pipeline/logstash.conf
input {
	tcp {
		port => 5000
	}
}

## elasticsearch에 데이터를 저장한다.
## index 명을 지정하지 않으면, 임의로 생성됨

output {
	elasticsearch {
		hosts => "elasticsearch:9200"
	}
}
```



### STEP 03. Send to elasticsearch using logstash
```
> cd ~/docker-elk
> wget https://github.com/freepsw/demo-spark-analytics/blob/master/00.stage1/tracks_live.csv
> nc localhost 5000 < tracks_live.csv
```
- elasticsearch에 생성된 index명을 확인한다.
```
> curl 'localhost:9200/_cat/indices?v'
health status index               uuid                   pri rep docs.count docs.deleted store.size pri.store.size
yellow open   .kibana             MloKJDfhTu60kDsQHwvL0g   1   1          1            0      3.1kb          3.1kb
yellow open   logstash-2017.06.09 iDmSAximRYCMn2GlLtnMsA   5   1      31814            0      2.5mb          2.5mb
```
- "logstash-2017.06.09"라는 index가 생성됨.

### STEP 04. Visualize collected data using kibana
```
- connect http://<ip>:5601
- index 추가 (logstash-2017.06.09)
```
