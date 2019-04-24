// Some key code snipptes have been borrowed from the designs published at the rocketboard.org:
// https://rocketboards.org/foswiki/Projects/VideoAndImageProcessingWithArria10SoCDevkit

#ifdef __cplusplus
extern "C" {
#endif

#include "port.h"
#include <linux/fb.h>

#define SERVICE_PORT 21234
#define NETBUFSIZE 61441
#define NUMPACKFRAME 30

#define TEST_BUFFER_NUM 8
#define SAT(c) \
  if (c & (~255)) { if (c < 0) c = 0; else c = 255; }

int g_out_width = 1280;
int g_out_height = 720;
char g_fb_device[100] = "/dev/fb0";

static void yuyv_to_rgb32 (int width, int height, char *src, long *dst)
{
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
        // TODO: copy your colorspace conversion code from the last sections here.
        }
    }
}


static int display()
{
    // Create network server
    struct sockaddr_in myaddr;  /* our address */
    struct sockaddr_in remaddr; /* remote address */
    socklen_t addrlen = sizeof(remaddr);        /* length of addresses */
    int recvlen;            /* # bytes received */
    int netfd;             /* our socket */
    unsigned char netbuf[NETBUFSIZE]; /* receive buffer */

    /* create a UDP socket */
    if ((netfd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
        perror("cannot create socket\n");
        return 0;
    }

    /* bind the socket to any valid IP address and a specific port */
    memset((char *)&myaddr, 0, sizeof(myaddr));
    myaddr.sin_family = AF_INET;
    myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    myaddr.sin_port = htons(SERVICE_PORT);

    if (bind(netfd, (struct sockaddr *)&myaddr, sizeof(myaddr)) < 0) {
        perror("bind failed");
        return 0;
    }


    // fb
    struct fb_var_screeninfo vinfo;
    struct fb_fix_screeninfo finfo;
    long screensize, index;
    int fbfd = 0;
    char *fbp;
    long *bgr_buff;
    char *yuv_buff;

    if ((fbfd = open(g_fb_device, O_RDWR, 0)) < 0)
    {
        printf("Unable to open %s\n", g_fb_device);
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


    bgr_buff = (long *) malloc (sizeof(long) * g_out_width * g_out_height * 4);
    yuv_buff = (char *) malloc (sizeof(char) * g_out_width * g_out_height * 2);
    size_t packetsize = NETBUFSIZE - 1;

    for(;;) {
        char packetiter = 0;
        /* TODO: we will need to receive enough packets to assemble a frame.
         * We collect NUMPACKFRAME number of packets,
         * and then use the colleted information to assemble a frame. */
        for (packetiter; packetiter < NUMPACKFRAME; packetiter ++)
        {
            /* TODO: add your implementation for collecting packets here */
        }

        /* TODO: copy the information in the collected network buffer to the frame buffer.
         * You will need to translate the colorspace to a format that the framebuffer can take.
         * You can consult the code in ./video/main.c.
         */
        memcpy(fbp, bgr_buff, (vinfo.xres * vinfo.yres * vinfo.bits_per_pixel)/8);
    }

    free(bgr_buff);
    free(yuv_buff);
    munmap(fbp, screensize);
    close(fbfd);
    return 0;
}

int main(int argc, char **argv)
{
    display();
    return 0;
}
