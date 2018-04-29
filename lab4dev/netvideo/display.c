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
        for (packetiter; packetiter < NUMPACKFRAME; packetiter ++)
        {
            recvlen = recvfrom(netfd, netbuf, NETBUFSIZE, 0, (struct sockaddr *)&remaddr, &addrlen);
            if (recvlen > 0)
            {
                netbuf[recvlen] = 0;
            }

            char packetind = *netbuf;
            memcpy(yuv_buff + packetind * packetsize, netbuf + 1, packetsize);
        }

        yuyv_to_rgb32(g_out_width, g_out_height, yuv_buff, bgr_buff);
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
