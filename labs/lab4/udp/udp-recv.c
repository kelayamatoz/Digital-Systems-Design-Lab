#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include "port.h"
#include <unistd.h>

int main(int argc, char **argv)
{
	struct sockaddr_in myaddr;	            /* our address */
	struct sockaddr_in remaddr;	            /* remote address */
	socklen_t addrlen = sizeof(remaddr);		/* length of addresses */
	int recvlen;			                      /* # bytes received */
	int fd;				                          /* our socket */
	int msgcnt = 0;			                    /* count # of messages we received */
	unsigned char buf[BUFSIZE];	            /* receive buffer */

	/* TODO: create a UDP socket */

	/* TODO: bind the socket to any valid IP address and a specific port */

	/* now loop, receiving data and printing what we received */
	for (;;) {
		printf("waiting on port %d\n", SERVICE_PORT);
    /* TODO: receive a message from fd */

    /* Check the received message and print out the first character */
		if (recvlen > 0) {
			buf[recvlen] = 0;
      printf("received message: (%d bytes)\n", recvlen);
      printf("first char: %c\n", *buf);
		}
		else
			printf("uh oh - something went wrong!\n");

		sprintf(buf, "ack %d", msgcnt++);
		printf("sending response \"%s\"\n", buf);
		if (sendto(fd, buf, strlen(buf), 0, (struct sockaddr *)&remaddr, addrlen) < 0)
			perror("sendto");
	}
}
