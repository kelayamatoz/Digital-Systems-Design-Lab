displayaddr=172.24.89.138
cameraaddr=172.24.89.140

sed -i "s/char \*server = .*/char \*server = \"${displayaddr}\";/g" port.h
sed -i "s/scp display root@.*/scp display root\@${displayaddr}:~\//g" upload.sh
sed -i "s/scp camera root@.*/scp camera root\@${cameraaddr}:~\//g" upload.sh
