displayaddr=172.24.72.54
cameraaddr=172.24.72.94

sed -i "s/char \*server = .*/char \*server = \"${displayaddr}\";/g" port.h
sed -i "s/scp display root@.*/scp display root\@${displayaddr}:~\//g" upload.sh
sed -i "s/scp camera root@.*/scp camera root\@${cameraaddr}:~\//g" upload.sh
