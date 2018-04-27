CC=arm-linux-gnueabihf-gcc

BINARY_NAME=video_stream.out

API_OBJS= main.c

LIBS= -lpthread

UTEST_CFLAGS= -O4 -g

$(BINARY_NAME): $(API_OBJS)
	$(CC) $(UTEST_CFLAGS) $(API_OBJS) -o $(BINARY_NAME) $(LIBS)

all : $(BINARY_NAME)

.PHONY  : clean

clean  :
	  rm -f *.o $(BINARY_NAME)

%.o : %.c
	$(CC) $(UTEST_CFLAGS) -c $^ -o $@
