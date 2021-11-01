# Set the base image to Ubuntu
FROM ubuntu:latest

# Install required packages
RUN apt-get update && apt-get install -y libpython3.8-minimal libexpat1

# python3.8-minimal and python3.8 seem to be broken in Ubuntu at the moment - so the following hack is used 
RUN apt-get download python3.8-minimal && dpkg --unpack python3.8-minimal*.deb && \
    rm /var/lib/dpkg/info/python3.8-minimal.postinst -f && dpkg --configure python3.8-minimal &&  \
    apt-get install -yf &&  rm -f python3.8-minimal*.deb 
RUN apt-get download python3.8 && dpkg --unpack python3.8*.deb && \
    rm /var/lib/dpkg/info/python3.8.postinst -f && dpkg --configure python3.8 &&  \
    apt-get install -yf &&  rm -f python3.8*.deb 

# this is the normal installation part
RUN DEBIAN_FRONTEND=noninteractive apt-get -y dist-upgrade && \      
    DEBIAN_FRONTEND=noninteractive apt-get -y install curl sudo gnupg2 build-essential npm lsb-release && \
    apt-get -y autoremove

# install node.js version 17 (note that the setup script extends the apt repository information)
RUN  curl -sL https://deb.nodesource.com/setup_17.x | sudo bash - 
RUN  apt-get install -y nodejs gcc g++ make

# Install nodemon and update npm to latest version
RUN npm install -g nodemon && npm install -g npm@8.1.1

# Provides cached layer for node_modules
ADD package.json /tmp/package.json
RUN cd /tmp && npm install && mkdir -p /src 

# Define working directory
VOLUME /src
WORKDIR /src
ADD . /src

# Expose port
EXPOSE  8080

# Run app using nodemon
CMD ["./entrypoint.sh"]
