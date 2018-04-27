CC=arm-linux-gnueabihf-gcc
CFLAGS = -g 

all: udp-send udp-recv

udp-send: udp-send.o 
	$(CC) -o udp-send udp-send.o $(LIBS)

udp-recv: udp-recv.o 
	$(CC) -o udp-recv udp-recv.o $(LIBS)

udp-send.o: udp-send.c port.h

udp-recv.o: udp-recv.c port.h

clean:
	rm -f udp-send udp-recv udp-send.o udp-recv.o 
