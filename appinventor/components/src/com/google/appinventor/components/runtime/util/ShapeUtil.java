package com.google.appinventor.components.runtime.util;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by me on 3/2/2018.
 */
public class ShapeUtil {
    private static final String LOG_TAG = "ShapeUtil";

    public Bitmap transformImage(Context context, Bitmap original, Bitmap mask) {
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);
        int size = Math.max(original.getWidth(), original.getHeight());
        Bitmap maskNew = Bitmap.createScaledBitmap(mask, size, size, true);
        Canvas canvas = new Canvas(bitmap);

        // Draw the original bitmap (DST during Porter-Duff transfer)
        canvas.drawBitmap(original, 0, 0, null);


        // DST_IN = Whatever was there, keep the part that overlaps
        // with what I'm drawing now
        Paint maskPaint = new Paint();
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        maskPaint.setStrokeWidth(convertDpToDensity(context, 55));
        maskPaint.setColor(Color.RED);
        canvas.drawBitmap(maskNew, 0, 0, maskPaint);

        return bitmap;
    }

    // ======== Set grayscale
    public Bitmap setImageEffect(Bitmap original, ShapeStyle shapeStyle) {
        Bitmap bitmap = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();
        if (shapeStyle.equals(ShapeStyle.RED)) {
            paint.setColorFilter(new LightingColorFilter(Color.RED, 0));
//            canvas.drawBitmap(original, 0, 0, paint);

        } else if (shapeStyle.equals(ShapeStyle.BROWN)) {
            paint.setColorFilter(new LightingColorFilter(Color.YELLOW, 0));
//            canvas.drawBitmap(original, 0, 0, paint);

        } else if (shapeStyle.equals(ShapeStyle.GREEN)) {
            paint.setColorFilter(new LightingColorFilter(Color.GREEN, 0));
//            canvas.drawBitmap(original, 0,0, paint);

        } else if (shapeStyle.equals(ShapeStyle.BLUE)) {
            paint.setColorFilter(new LightingColorFilter(Color.BLUE, 0));
//            canvas.drawBitmap(original, 0, 0, paint);

        } else if (shapeStyle.equals(ShapeStyle.SEPIA)
                || shapeStyle.equals(ShapeStyle.BINARY)
                || shapeStyle.equals(ShapeStyle.INVERT)
                || shapeStyle.equals(ShapeStyle.BLUE)
                || shapeStyle.equals(ShapeStyle.PINK)
                || shapeStyle.equals(ShapeStyle.BW)
                ) {
            paint.setColorFilter(new ColorMatrixColorFilter(getColorMatrixGray(shapeStyle)));
        }
        canvas.drawBitmap(original, 0, 0, paint);

        return bitmap;
    }

    //http://chiuki.github.io/android-shaders-filters
    private ColorMatrix getColorMatrixGray(ShapeStyle shapeStyle) {
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);

        if (shapeStyle.equals(ShapeStyle.SEPIA)) {
            ColorMatrix colorScale = new ColorMatrix();
            colorScale.setScale(1, 1, 0.8f, 1);

            // Convert to grayscale, then apply brown color
            colorMatrix.postConcat(colorScale);
        } else if (shapeStyle.equals(ShapeStyle.BINARY)) {
            float m = 255f;
            float t = -255 * 128f;
            ColorMatrix threshold = new ColorMatrix(new float[]{
                    m, 0, 0, 1, t,
                    0, m, 0, 1, t,
                    0, 0, m, 1, t,
                    0, 0, 0, 1, 0
            });

            // Convert to grayscale, then scale and clamp
            colorMatrix.postConcat(threshold);
        } else if (shapeStyle.equals(ShapeStyle.INVERT)) {
            colorMatrix = new ColorMatrix(new float[]{
                    -1, 0, 0, 0, 255,
                    0, -1, 0, 0, 255,
                    0, 0, -1, 0, 255,
                    0, 0, 0, 1, 0
            });
        } else if (shapeStyle.equals(ShapeStyle.PINK)) {
            colorMatrix = new ColorMatrix(new float[]{
                    0, 0, 0, 0, 255,
                    0, 0, 0, 0, 0,
                    0.2f, 0, 0, 0, 50,
                    0.2f, 0.2f, 0.2f, 0, -20
            });
        } else if (shapeStyle.equals(ShapeStyle.BLUE)) {
            colorMatrix = new ColorMatrix(new float[]{
                    0, 0, 0, 0, 0,
                    0.3f, 0, 0, 0, 50,
                    0, 0, 0, 0, 255,
                    0.2f, 0.4f, 0.4f, 0, -30
            });
        }


        return colorMatrix;
    }

    protected int convertDpToDensity(Context context, int value) {
        // convert dp to pixels
        double density = context.getResources().getDisplayMetrics().density;
        double pixels = value * density;
        return (int) pixels;
    }

    public Bitmap convertToBitmap(Drawable drawable) {
        Bitmap mutableBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return mutableBitmap;
    }

    // call it this way:         Bitmap scaledBitmap = scaleDown(realImage, MAX_IMAGE_SIZE, true);
//    public Bitmap scaleDown(Drawable realImage, float maxImageSize, boolean filter) {
//       Bitmap bitmap = convertToBitmap(realImage);
//       return scaleDown(bitmap, maxImageSize, filter);
//    }

    public static Bitmap scaleDown(Bitmap bitmap, float targetWidth, boolean filter) {
        Log.d(LOG_TAG, "Starting scaleDown. Target width:" + targetWidth);
        float width = bitmap.getWidth();
//        Log.d(LOG_TAG, "xxxxx ShapeUtil 1:" + width);

        float height = bitmap.getHeight();
//        Log.d(LOG_TAG, "xxxxx ShapeUtil 2:" + height);

        float ratio = (float) width / targetWidth;
//        Log.d(LOG_TAG, "xxxxx ShapeUtil 3. Ratio:" + ratio);

        width = targetWidth;
        height = (int)  (height / ratio);
//        Log.d(LOG_TAG, "xxxxx ShapeUtil 4. New w,h:" + width + "/"+ height);

        bitmap = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, filter);
//        Log.d(LOG_TAG, "xxxxx ShapeUtil 5. Is null?" + (bitmap == null?"yes": "no"));

        return bitmap;
    }
}
