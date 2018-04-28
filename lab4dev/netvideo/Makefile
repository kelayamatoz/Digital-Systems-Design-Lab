CC=arm-linux-gnueabihf-gcc
LIBS= -lpthread
UTEST_CFLAGS= -O4 -g

all : camera display

camera: camera.c
	$(CC) $(UTEST_CFLAGS) camera.c -o camera $(LIBS)

display: display.c
	$(CC) $(UTEST_CFLAGS) display.c -o display$(LIBS)


.PHONY  : clean

clean  :
		rm -f *.o display
	  rm -f *.o camera

%.o : %.c
	$(CC) $(UTEST_CFLAGS) -c $^ -o $@
