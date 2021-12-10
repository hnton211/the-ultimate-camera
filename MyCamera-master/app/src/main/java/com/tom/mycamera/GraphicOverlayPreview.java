package com.tom.mycamera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GraphicOverlayPreview extends View {
    private ArrayList<RectF> mList;
    private Paint mPaint;
    private Bitmap bitmap;
    private Rect srcRect;

    private int whichBitmap = 1;

    public static final int NO_MASK = 1;
    public static final int MONEY_HEIST_MASK = 2;
    public static final int BATMAN_MASK = 3;
    public static final int SHREK_FACE = 4;
    public static final int DEADPOOL_MASK = 5;

    public GraphicOverlayPreview(Context context) {
        super(context);
        createPaint();
    }

    public GraphicOverlayPreview(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        createPaint();
    }

    public GraphicOverlayPreview(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        createPaint();
    }

    public GraphicOverlayPreview(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        createPaint();
    }

    public void postRect(List<RectF> list) {
        if (mList != null) mList.clear();
        mList = new ArrayList<>(list);
        invalidate();
    }

    public void createPaint() {
        mPaint = new Paint();
        mPaint.setStrokeWidth(8f);
        mPaint.setColor(Color.GRAY);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap != null && mList != null) {
            for (RectF rect : mList) {
                canvas.drawBitmap(bitmap, srcRect, rect, null);
                //canvas.drawRect(rect, mPaint);
            }
        }
    }

    public void setBitmap(int chosen) {
        switch (chosen) {
            default:
            case NO_MASK:
                bitmap = null;
                break;
            case MONEY_HEIST_MASK:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moneyheistmask);
                break;
            case BATMAN_MASK:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.batmanmask);
                break;
            case SHREK_FACE:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.shrekface);
                break;
            case DEADPOOL_MASK:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.deadpool);
                break;
        }

        whichBitmap = chosen;

        if (bitmap != null) {
            srcRect = new Rect(0, 0, bitmap.getWidth() - 1, bitmap.getHeight() - 1);
        }
    }

    public ArrayList<RectF> getList () {
        return mList;
    }

    public int getWhichBitmap () {
        return whichBitmap;
    }
}
