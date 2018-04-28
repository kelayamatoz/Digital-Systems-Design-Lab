// Some key code snipptes have been borrowed from the designs published at the rocketboard.org:
// https://rocketboards.org/foswiki/Projects/VideoAndImageProcessingWithArria10SoCDevkit

#ifdef __cplusplus
extern "C" {
#endif

#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <asm/types.h>

#include <linux/videodev2.h>
#include <sys/mman.h>
#include <string.h>
#include <malloc.h>
#include <linux/fb.h>

#include <netdb.h>
#include <string.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#define BUFLEN 2048
#define SERVICE_PORT 21234
#define NETBUFSIZE 61441
#define NUMPACKFRAME 30

#define TEST_BUFFER_NUM 8
#define SAT(c) \
  if (c & (~255)) { if (c < 0) c = 0; else c = 255; }

char *server = "172.24.72.94";

struct testbuffer
{
    unsigned char *start;
    size_t offset;
    unsigned int length;
};

struct testbuffer buffers[TEST_BUFFER_NUM];
int g_in_width = 1280;
int g_in_height = 720;
int g_out_width = 1280;
int g_out_height = 720;
int g_top = 0;
int g_left = 0;
int g_input = 0;
int g_capture_count = 100;
int g_rotate = 0;
int g_cap_fmt = V4L2_PIX_FMT_YUYV;
int g_camera_framerate = 30;
int g_extra_pixel = 0;
int g_capture_mode = 0;
char g_v4l_device[100] = "/dev/video0";
char g_fb_device[100] = "/dev/fb0";
char g_file_name[100] = "Capture.jpg";

/* Global Variables */
static char cmd[512];

static void yuyv_to_rgb32 (int width, int height, char *src, long *dst)
{
    // TODO: Your job to do the color space conversion
    // TODO: How to calculate the adjacent one?
    unsigned char *s;
    unsigned long *d;
    int l, c, alpha = 0x0;
    int r, g, b, cr, cg, cb, y1, y2;

    l = height;
    s = src;
    d = dst;
    while (l--) {
        c = width >> 1;
        while (c--) {
            y1 = *s++;
            cb = ((*s - 128) * 454) >> 8;
            cg = (*s++ - 128) * 88;
            y2 = *s++;
            cr = ((*s - 128) * 359) >> 8;
            cg = (cg + (*s++ - 128) * 183) >> 8;

            r = y1 + cr;
            b = y1 + cb;
            g = y1 - cg;
            SAT(r);
            SAT(g);
            SAT(b);
            *dst++ = ((unsigned int) alpha) << 24 |  (r << 16) | (g << 8) | b;

            r = y2 + cr;
            b = y2 + cb;
            g = y2 - cg;
            SAT(r);
            SAT(g);
            SAT(b);
            *dst++ = ((unsigned int) alpha) << 24 |  (r << 16) | (g << 8) | b;
        }
    }
}

static int start_capturing(int fd_v4l)
{
    unsigned int i;
    struct v4l2_buffer buf;
    enum v4l2_buf_type type;

    for (i = 0; i < TEST_BUFFER_NUM; i++)
    {
        memset(&buf, 0, sizeof (buf));
        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        buf.index = i;
        if (ioctl(fd_v4l, VIDIOC_QUERYBUF, &buf) < 0)
        {
            printf("VIDIOC_QUERYBUF error\n");
            return -1;
        }

        buffers[i].length = buf.length;
        buffers[i].offset = (size_t) buf.m.offset;
        printf("allocating buffer of size %d\n", buffers[i].length);
        buffers[i].start = mmap (NULL, buffers[i].length,
                                 PROT_READ | PROT_WRITE, MAP_SHARED,
                                 fd_v4l, buffers[i].offset);
        memset(buffers[i].start, 0xFF, buffers[i].length);
    }

    for (i = 0; i < TEST_BUFFER_NUM; i++)
    {
        memset(&buf, 0, sizeof (buf));
        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        buf.index = i;
        buf.m.offset = buffers[i].offset;

        if (ioctl (fd_v4l, VIDIOC_QBUF, &buf) < 0) {
            printf("VIDIOC_QBUF error\n");
            return -1;
        }
    }

    type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if (ioctl (fd_v4l, VIDIOC_STREAMON, &type) < 0) {
        printf("VIDIOC_STREAMON error\n");
        return -1;
    }
    return 0;
}

static int stop_capturing(int fd_v4l)
{
    enum v4l2_buf_type type;
    unsigned int i;

    type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    ioctl (fd_v4l, VIDIOC_STREAMOFF, &type);

    for (i = 0; i < TEST_BUFFER_NUM; i++)
    {
        munmap(buffers[i].start, buffers[i].length);
    }
    return 0;
}

static int v4l_capture_setup(void)
{
    struct v4l2_format fmt;
    struct v4l2_control ctrl;
    struct v4l2_streamparm parm;
    struct v4l2_crop crop;
    int fd_v4l = 0;

    if ((fd_v4l = open(g_v4l_device, O_RDWR, 0)) < 0)
    {
        printf("Unable to open %s\n", g_v4l_device);
        return 0;
    }

    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    fmt.fmt.pix.pixelformat = g_cap_fmt;
    fmt.fmt.pix.width = g_out_width;
    fmt.fmt.pix.height = g_out_height;
    if (ioctl(fd_v4l, VIDIOC_S_FMT, &fmt) < 0)
    {
        printf("set format failed\n");
        return 0;
    }


    struct v4l2_requestbuffers req;
    memset(&req, 0, sizeof (req));
    req.count = TEST_BUFFER_NUM;
    req.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    req.memory = V4L2_MEMORY_MMAP;

    if (ioctl(fd_v4l, VIDIOC_REQBUFS, &req) < 0)
    {
        printf("v4l_capture_setup: VIDIOC_REQBUFS failed\n");
        return 0;
    }

    return fd_v4l;
}


static int v4l_stream_test(int fd_v4l)
{
    // Network client part
    struct sockaddr_in myaddr, remaddr;
    int netfd, neti, netslen=sizeof(remaddr);
    char netbuf[NETBUFSIZE]; /* message buffer */

    if ((netfd=socket(AF_INET, SOCK_DGRAM, 0)) == -1)

    /* bind it to all local addresses and pick any port number */
    memset((char *)&myaddr, 0, sizeof(myaddr));
    myaddr.sin_family = AF_INET;
    myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    myaddr.sin_port = htons(0);

    if (bind(netfd, (struct sockaddr *)&myaddr, sizeof(myaddr)) < 0) {
        perror("bind failed");
        return 0;
    }       

    /* now define remaddr, the address to whom we want to send messages */
    /* For convenience, the host address is expressed as a numeric IP address */
    /* that we will convert to a binary format via inet_aton */
    memset((char *) &remaddr, 0, sizeof(remaddr));
    remaddr.sin_family = AF_INET;
    remaddr.sin_port = htons(SERVICE_PORT);
    if (inet_aton(server, &remaddr.sin_addr)==0) 
    {
        fprintf(stderr, "inet_aton() failed\n");
        exit(1);
    }

    // Video stream
    struct v4l2_buffer buf;
    struct v4l2_format fmt;
    struct fb_var_screeninfo vinfo;
    struct fb_fix_screeninfo finfo;
    long screensize, index;
    int fbfd = 0;
    char *fbp;
    unsigned char Bmp, dummy, red, blue, green, alpha;
    long *bgr_buff;
    FILE * fd_y_file = 0;
    int i,hindex,j;
    unsigned long int location = 0, BytesPerLine = 0;
    unsigned long pixel;
    unsigned int t,x,y;
    unsigned long size, bytes_read;

    if ((fbfd = open(g_fb_device, O_RDWR, 0)) < 0)
    {
        printf("Unable to open %s\n", g_v4l_device);
        return 0;
    }

    /* Get fixed screen information */
    if (ioctl(fbfd, FBIOGET_FSCREENINFO, &finfo)) {
        printf("Error reading fixed information.\n");
        exit(2);
    }

    /* Get variable screen information */
    if (ioctl(fbfd, FBIOGET_VSCREENINFO, &vinfo)) {
        printf("Error reading variable information.\n");
        exit(3);
    }

    /* Figure out the size of the screen in bytes */
    screensize = vinfo.xres * vinfo.yres * vinfo.bits_per_pixel / 8;

    /* Map the device to memory */
    fbp = (char *) mmap(0, screensize, PROT_READ | PROT_WRITE, MAP_SHARED,fbfd, 0);
    if ((int)fbp == -1) {
        printf("Error failed to map framebuffer device to memory.\n");
        exit(4);
    }

    fmt.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
    if (ioctl(fd_v4l, VIDIOC_G_FMT, &fmt) < 0)
    {
        printf("get format failed\n");
        return -1;
    }

    if (start_capturing(fd_v4l) < 0)
    {
        printf("start_capturing failed\n");
        return -1;
    }

    bgr_buff = (long *) malloc (sizeof(long) * fmt.fmt.pix.width * fmt.fmt.pix.height * 4);

    for(;;) {
        memset(&buf, 0, sizeof(buf));
        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        if (ioctl (fd_v4l, VIDIOC_DQBUF, &buf) < 0) {
            printf("VIDIOC_DQBUF failed.\n");
            break;
        }

        // Total size: 1280 * 720 * 2 bytes = 1843200 for a frame
        // 61440 for a packet
        // 30 packets / frame
        char packetind = 0;
        for(packetind; packetind < NUMPACKFRAME; packetind ++)
        {
            memset(netbuf, packetind, 1);
            memcpy(netbuf + 1, buffers[buf.index].start + packetind * NETBUFSIZE, NETBUFSIZE);
            if (sendto(netfd, netbuf, NETBUFSIZE, 0, (struct sockaddr *)&remaddr, netslen) == -1)
            {
                perror("sendto");
                exit(1);
            }
        }

        if (ioctl (fd_v4l, VIDIOC_QBUF, &buf) < 0) {
            printf("VIDIOC_QBUF failed\n");
            break;
        }

        printf("sent one frame...\n\n");
    }

    if (stop_capturing(fd_v4l) < 0)
    {
        printf("stop_capturing failed\n");
        return -1;
    }

    free(bgr_buff);
    munmap(fbp, screensize);
    close(fbfd);
    close(fd_v4l);
    return 0;
}

int main(int argc, char **argv)
{
    int fd_v4l, option = 0, img_sel = 0;

    fd_v4l = v4l_capture_setup();
    v4l_stream_test(fd_v4l);
    return 0;
}
