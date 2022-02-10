    public static Bitmap blurBitmap(Bitmap inBitmap, float scale, int radius) {
        if (radius < 1)
            return (null);

        int width       = Math.round(inBitmap.getWidth() * scale);
        int height      = Math.round(inBitmap.getHeight() * scale);
        inBitmap        = Bitmap.createScaledBitmap(inBitmap, width, height, false);

        Bitmap outBitmap= inBitmap.copy(inBitmap.getConfig(), true);
        int   w         = outBitmap.getWidth(),
              h         = outBitmap.getHeight();

        int[] pix       = new int[w * h];
        outBitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int   wm        = w - 1,
              hm        = h - 1,
              wh        = w * h,
              div       = radius + radius + 1;
        int[][] hue     = new int[wh][4];

        int   x, y, i, p, yp, yi, yw;
        int[] vmin      = new int[Math.max(w, h)];

        int   divsum    = (div + 1) >> 1;
        divsum         *= divsum;

        int[] dv        = new int[256 * divsum];
        for (i=0; i<256 * divsum; i++)
            dv[i]       = (i / divsum);

        yw              = yi = 0;

        int[][] stack   = new int[div][4];
        int[]   sir;
        int     stackpointer, stackstart, rbs,

        r1              = radius + 1;

        int[]   sum     = new int[4],
                insum   = new int[4],
                outsum  = new int[4];

        for (y=0; y<h; y++) {
            for (int ii=0; ii<4; ii++) {
                sum[ii]         = 0;
                insum[ii]       = 0;
                outsum[ii]      = 0;
            }

            for (i=-radius; i<=radius; i++) {
                p               = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir             = stack[i + radius];
                sir[0]          = (p & 0x00ff0000) >> 16;
                sir[1]          = (p & 0x0000ff00) >> 8;
                sir[2]          = (p & 0x000000ff);
                sir[3]          = (p & 0xff000000) >>> 24;
                rbs             = r1 - StrictMath.abs(i);

                for (int ii=0; ii<4; ii++)
                    sum[ii]     += sir[ii] * rbs;

                if (i > 0)
                    for (int ii=0; ii<4; ii++)
                        insum[ii]   += sir[ii];
                else
                    for (int ii=0; ii<4; ii++)
                        outsum[ii]  += sir[ii];
            }
            stackpointer        = radius;

            for (x = 0; x < w; x++) {
                stackstart      = stackpointer - radius + div;
                sir             = stack[stackstart % div];

                for (int ii=0; ii<4; ii++) {
                    hue[yi][ii] = dv[sum[ii]];
                    sum[ii]     -= outsum[ii];
                    outsum[ii]  -= sir[ii];
                }

                if (y == 0)
                    vmin[x]     = Math.min(x + radius + 1, wm);

                p               = pix[yw + vmin[x]];

                sir[3]          = (p & 0xff000000) >>> 24;
                sir[0]          = (p & 0x00ff0000) >> 16;
                sir[1]          = (p & 0x0000ff00) >> 8;
                sir[2]          = (p & 0x000000ff);

                for (int ii=0; ii<4; ii++) {
                    insum[ii]   += sir[ii];
                    sum[ii]     += insum[ii];
                }

                stackpointer    = (stackpointer + 1) % div;
                sir             = stack[(stackpointer) % div];

                for (int ii=0; ii<4; ii++) {
                    outsum[ii]  += sir[ii];
                    insum[ii]   -= sir[ii];
                }

                yi++;
            }
            yw                  += w;
        }

        for (x = 0; x < w; x++) {
            for (int ii=0; ii<4; ii++) {
                insum[ii]       = 0;
                outsum[ii]      = 0;
                sum[ii]         = 0;
            }

            yp                  = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi              = Math.max(0, yp) + x;
                sir             = stack[i + radius];

                rbs             = r1 - StrictMath.abs(i);
                for (int ii=0; ii<4; ii++) {
                    sir[ii]     = hue[yi][ii];
                    sum[ii]     += hue[yi][ii] * rbs;
                }

                if (i > 0)
                    for (int ii=0; ii<4; ii++)
                        insum[ii]      += sir[ii];
                else
                    for (int ii=0; ii<4; ii++)
                        outsum[ii]     += sir[ii];

                if (i < hm)
                    yp          += w;
            }

            yi                  = x;
            stackpointer        = radius;

            for (y = 0; y < h; y++) {
                pix[yi]         = ( dv[sum[3]] << 24 ) | ( dv[sum[0]] << 16 ) | ( dv[sum[1]] << 8 ) | dv[sum[2]];

                stackstart      = stackpointer - radius + div;
                sir             = stack[stackstart % div];
                for (int ii=0; ii<4; ii++) {
                    sum[ii]     -= outsum[ii];
                    outsum[ii]  -= sir[ii];
                }

                if (x == 0)
                    vmin[y]     = Math.min(y + r1, hm) * w;

                p               = x + vmin[y];

                for (int ii=0; ii<4; ii++) {
                    sir[ii]     = hue[p][ii];
                    insum[ii]   += sir[ii];
                    sum[ii]     += insum[ii];
                }

                stackpointer    = (stackpointer + 1) % div;
                sir             = stack[stackpointer];

                for (int ii=0; ii<4; ii++) {
                    outsum[ii]  += sir[ii];
                    insum[ii]   -= sir[ii];
                }

                yi              += w;
            }
        }

        outBitmap.setPixels(pix, 0, w, 0, 0, w, h);

        return (outBitmap);
    }
