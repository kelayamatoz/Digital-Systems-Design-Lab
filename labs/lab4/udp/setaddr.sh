serveraddr=172.24.89.138
clientaddr=172.24.89.140

sed -i "s/char \*server = .*/char \*server = \"${serveraddr}\";/g" port.h
sed -i "s/scp udp-recv root@.*/scp udp-recv root\@${serveraddr}:~\//g" upload.sh
sed -i "s/scp udp-send root@.*/scp udp-send root\@${clientaddr}:~\//g" upload.sh
