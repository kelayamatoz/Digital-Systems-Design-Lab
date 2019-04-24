#include "port.h"
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>

#define BUFSIZE 61441 /* This would need 30 packets to fill a frame */
#define SERVICE_PORT 21234

int main(void)
{
	struct sockaddr_in myaddr, remaddr;
	int fd, i, slen=sizeof(remaddr);
	char buf[BUFSIZE];	/* message buffer */
	int recvlen;		/* # bytes in acknowledgement message */

  /* TODO: create a UDP socket */

  /* TODO: bind the socket to all local addresses and pick any port number */



  // Set message buffer
  char *message = "Thirsty Thursday.";

  /* TODO: copy the message to the message buffer, buf */

	/* TODO: now define remaddr, the address to whom we want to send messages. For convenience, the host address is expressed as a numeric IP address. We will convert this IP address to a binary format via inet_aton */

	/* TODO: now let's send the messages */

    /* TODO: close fd */

	close(fd);
	return 0;
}
