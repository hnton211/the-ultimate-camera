package com.tom.mycamera.bonus;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.InputStream;

public class BitmapUtilis {


    public static Bitmap getBitmapFromAssets(Context context, String fileName, int width, int height)
    {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try{
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            inputStream = assetManager.open(fileName);
            options.inSampleSize = recursiveSample(fileName,width,height);
            options.inJustDecodeBounds = false;
            return  BitmapFactory.decodeStream(inputStream,null,options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmapFromGallery(Context context, Uri uri, int width, int height)
    {
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri,filePathColumn,null,null,null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturePath,options);
        options.inSampleSize = recursiveSample(picturePath,width,height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(picturePath,options);
    }

    public static int recursiveSample(String path, int maxWidth, int maxHeight)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int scale = 1;
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        while (imageWidth / 2 >= maxWidth || imageHeight / 2 >= maxHeight)
        {
            imageWidth /= 2;
            imageHeight /= 2;
            scale *= 2;
        }

        if (scale < 1)
        {
            scale = 1;
        }

        return scale;
    }

    public static int recursiveSample(byte[] data, int maxWidth, int maxHeight)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        int scale = 1;
        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;

        while (imageWidth / 2 >= maxWidth || imageHeight / 2 >= maxHeight)
        {
            imageWidth /= 2;
            imageHeight /= 2;
            scale *= 2;
        }

        if (scale < 1)
        {
            scale = 1;
        }

        return scale;
    }

    public static Bitmap crop(Bitmap bm, int x, int y, int width, int height)
    {
        // recreate the new Bitmap
        Bitmap croppedBitmap = Bitmap.createBitmap(bm, x, y, width, width, null, true);
        return croppedBitmap;
    }

    /**
     * Resizes a bitmap. Original bitmap is recycled after this method is
     * called.
     *
     * @param bm
     *            The bitmap to resize
     * @param width
     *            The new width
     * @param height
     *            Thew new height
     * @return The resized bitmap
     */
    public static Bitmap resize(Bitmap bm, int width, int height)
    {
        int oldWidth = bm.getWidth();
        int oldHeight = bm.getHeight();

        // calculate the scale - in this case = 0.4f
        float scaleWidth = ((float)width) / oldWidth;
        float scaleHeight = ((float)height) / oldHeight;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, oldWidth, oldHeight, matrix, true);
        return resizedBitmap;
    }

    /**
     * Resizes a bitmap to a specific width.
     *
     * @param bm
     *            The bitmap to resize
     * @param width
     *            The new width
     * @return The resized bitmap
     */
    public static Bitmap resizeToWidth(Bitmap bm, int width)
    {
        return resize(bm, width, bm.getHeight());
    }

    /**
     * Resizes a bitmap to a maximum width.
     * @param bm
     *            The bitmap to resize
     * @param width
     *            The new width
     * @return The resized bitmap
     */
    public static Bitmap maxResizeToWidth(Bitmap bm, int width)
    {
        if (bm.getWidth() > width)
        {
            return resize(bm, width, bm.getHeight());
        }

        return bm;
    }

    /**
     * Resizes a bitmap to a specific width, while maintaining ratio.
     *
     * @param bm
     *            The bitmap to resize
     * @param width
     *            The new width
     * @return The resized bitmap
     */
    public static Bitmap resizeToWidthRatio(Bitmap bm, int width)
    {
        float ratio = (float)width / (float)bm.getWidth();
        int height = (int)(bm.getHeight() * ratio);
        return resize(bm, width, height);
    }


    public static Bitmap resizeToHeightRatio(Bitmap bm, int height)
    {
        float ratio = (float)height / (float)bm.getHeight();
        int width = (int)(bm.getWidth() * ratio);
        return resize(bm, width, height);
    }

    /**
     * Resizes a bitmap to a maximum width, while maintaining ratio.
     *
     * @param bm
     *            The bitmap to resize
     * @param width
     *            The new width
     * @return The resized bitmap
     */
    public static Bitmap maxResizeToWidthRatio(Bitmap bm, int width)
    {
        if (bm.getWidth() > width)
        {
            float ratio = (float)width / (float)bm.getWidth();
            int height = (int)(bm.getHeight() * ratio);
            return resize(bm, width, height);
        }

        return bm;
    }

}
