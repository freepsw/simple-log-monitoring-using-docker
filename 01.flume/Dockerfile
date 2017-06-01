FROM ubuntu:16.04
ENV DEBIAN_FRONTEND noninteractive
ENV JAVA_HOME       /usr/lib/jvm/java-8-oracle


RUN apt-get update && \
  apt-get dist-upgrade -y

RUN apt-get --purge remove openjdk*

## Install Oracle's JDK
RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections
RUN echo "deb http://ppa.launchpad.net/webupd8team/java/ubuntu xenial main" > /etc/apt/sources.list.d/webupd8team-java-trusty.list
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys EEA14886
RUN apt-get update && \
  apt-get install -y --no-install-recommends oracle-java8-installer && \
  apt-get clean all

RUN mkdir /opt/flume
RUN mkdir /opt/flume/input
RUN wget --no-check-certificate https://archive.apache.org/dist/flume/1.7.0/apache-flume-1.7.0-bin.tar.gz
RUN tar zxvf  apache-flume-1.7.0-bin.tar.gz -C /opt/flume --strip 1

ADD start-flume.sh /opt/flume/bin/start-flume

ENV JAVA_HOME  /usr/lib/jvm/java-8-oracle
ENV PATH /opt/flume/bin:$JAVA_HOME/bin:$PATH

CMD [ "start-flume" ]
