// Some key code snipptes have been borrowed from the designs published at the rocketboard.org:
// https://rocketboards.org/foswiki/Projects/VideoAndImageProcessingWithArria10SoCDevkit

#ifdef __cplusplus
extern "C" {
#endif

#include "port.h"
#include <linux/videodev2.h>

#define TEST_BUFFER_NUM 8

struct testbuffer
{
    unsigned char *start;
    size_t offset;
    unsigned int length;
};

struct testbuffer buffers[TEST_BUFFER_NUM];
int g_out_width = 1280;
int g_out_height = 720;
int g_cap_fmt = V4L2_PIX_FMT_YUYV;
int g_camera_framerate = 30;
char g_v4l_device[100] = "/dev/video0";

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


static int camera(int fd_v4l)
{
    // Create network client
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

    size_t packetsize = NETBUFSIZE - 1;

    for(;;) {
        memset(&buf, 0, sizeof(buf));
        buf.type = V4L2_BUF_TYPE_VIDEO_CAPTURE;
        buf.memory = V4L2_MEMORY_MMAP;
        if (ioctl (fd_v4l, VIDIOC_DQBUF, &buf) < 0) {
            printf("VIDIOC_DQBUF failed.\n");
            break;
        }

        /* TODO: since one frame doesn't fit into the payload of one packet, we need to divide a frame into muptiple packets */
        char packetind = 0;
        for(packetind; packetind < NUMPACKFRAME; packetind ++)
        {
          /* TODO: send the packets here */
        }

        if (ioctl (fd_v4l, VIDIOC_QBUF, &buf) < 0) {
            printf("VIDIOC_QBUF failed\n");
            break;
        }
    }

    if (stop_capturing(fd_v4l) < 0)
    {
        printf("stop_capturing failed\n");
        return -1;
    }

    close(fd_v4l);
    return 0;
}


int main(int argc, char **argv)
{
    int fd_v4l = v4l_capture_setup();
    camera(fd_v4l);
    return 0;
}
