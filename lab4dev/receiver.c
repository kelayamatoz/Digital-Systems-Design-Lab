#include <netdb.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define BUFLEN 2048
#define SERVICE_PORT 21234

int main(int argc, char **argv)
{
    struct sockaddr_in myaddr, remaddr;
    int fd, inet, slen=sizeof(remaddr);
    unsigned char messagebuf[BUFLEN];
    int recvlen;

    // Create a UDP socket
    if ((fd=socket(AF_INET, SOCK_DGRAM, 0)) == -1)
        return 0;

    memset((char *) &myaddr, 0, sizeof(myaddr));
    myaddr.sin_family = AF_INET;
    myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    myaddr.sin_port = htons(0);

    if (bind(fd, (struct sockaddr *)&myaddr, sizeof(myaddr)) < 0)
    {
        perror("bind failed");
        return 0;
    }

    // loop
    for (;;)
    {
       recvlen = recvfrom(fd, messagebuf, BUFLEN, 0, (struct sockaddr *)&remaddr, &slen);
       printf("received %d bytes\n", recvlen);
       if (recvlen > 0)
       {
          messagebuf[recvlen] = 0;
          printf("received message: %s", messagebuf);
       }
    }
}