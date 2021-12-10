package com.tom.mycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import java.util.ArrayList;
import java.util.List;

public class GraphicOverlayPicture  {
    private List <RectF> mList = null;
    private Bitmap bitmap;

    public GraphicOverlayPicture (ArrayList<RectF> list, int whichBitmap, Context context) {
        if (list != null) {
            mList = new ArrayList<>(list);
        }

        switch (whichBitmap) {
            case GraphicOverlayPreview.NO_MASK:
                bitmap = null;
                break;
            case GraphicOverlayPreview.MONEY_HEIST_MASK:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.moneyheistmask);
                break;
            case GraphicOverlayPreview.BATMAN_MASK:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.batmanmask);
                break;
            case GraphicOverlayPreview.SHREK_FACE:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.shrekface);
                break;
            case GraphicOverlayPreview.DEADPOOL_MASK:
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.deadpool);
                break;
        }
    }

    public void draw (Canvas canvas) {
        if (bitmap != null && mList != null) {
            Rect src = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);
            for (RectF rect : mList) {
                canvas.drawBitmap(bitmap, src, rect, null);
            }
        }
    }


}
