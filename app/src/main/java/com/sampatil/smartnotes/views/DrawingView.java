package com.sampatil.smartnotes.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private Paint penPaint;
    private Paint eraserPaint;
    private Path currentPath;
    private List<DrawPath> paths = new ArrayList<>();
    private boolean isDrawingEnabled = false;
    private boolean isEraserMode = false;
    private int currentPenColor;
    
    private Bitmap canvasBitmap;
    private Canvas drawCanvas;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        currentPenColor = Color.parseColor("#55FADB5F"); // Default yellow, faint

        penPaint = new Paint();
        penPaint.setColor(currentPenColor);
        penPaint.setAntiAlias(true);
        penPaint.setStrokeWidth(30f);
        penPaint.setStyle(Paint.Style.STROKE);
        penPaint.setStrokeJoin(Paint.Join.ROUND);
        penPaint.setStrokeCap(Paint.Cap.ROUND);

        eraserPaint = new Paint();
        eraserPaint.setAntiAlias(true);
        eraserPaint.setStrokeWidth(40f);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    private Paint makeColorPaint(int color) {
        Paint p = new Paint();
        p.setColor(color);
        p.setAntiAlias(true);
        p.setStrokeWidth(30f);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        return p;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            // Redraw all existing paths
            for (DrawPath dp : paths) {
                drawCanvas.drawPath(dp.path, dp.isEraser ? eraserPaint : makeColorPaint(dp.color));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvasBitmap != null) {
            canvas.drawBitmap(canvasBitmap, 0, 0, null);
        }
        // Draw current path being drawn
        if (currentPath != null) {
            penPaint.setColor(currentPenColor);
            canvas.drawPath(currentPath, isEraserMode ? eraserPaint : penPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isDrawingEnabled) return false;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Stop ScrollView from scrolling while drawing
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                currentPath = new Path();
                currentPath.moveTo(x, y);
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (currentPath != null) {
                    currentPath.lineTo(x, y);
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                if (currentPath != null && drawCanvas != null) {
                    if (isEraserMode) {
                        drawCanvas.drawPath(currentPath, eraserPaint);
                    } else {
                        penPaint.setColor(currentPenColor);
                        drawCanvas.drawPath(currentPath, penPaint);
                    }
                    paths.add(new DrawPath(currentPath, isEraserMode, currentPenColor));
                    currentPath = null;
                    invalidate();
                }
                return true;
        }
        return false;
    }

    public void setDrawingEnabled(boolean enabled) {
        isDrawingEnabled = enabled;
    }

    public void setPenMode() {
        isEraserMode = false;
    }

    public void setPenColor(String colorHex) {
        currentPenColor = Color.parseColor(colorHex);
        penPaint.setColor(currentPenColor);
    }

    public void setEraserMode() {
        isEraserMode = true;
    }

    public void clearAll() {
        paths.clear();
        if (canvasBitmap != null) {
            canvasBitmap.eraseColor(Color.TRANSPARENT);
        }
        currentPath = null;
        invalidate();
    }

    public Bitmap captureDrawing() {
        if (canvasBitmap == null) return null;
        return canvasBitmap.copy(canvasBitmap.getConfig(), true);
    }

    public void restoreDrawing(Bitmap bitmap) {
        if (canvasBitmap != null) {
            canvasBitmap.eraseColor(Color.TRANSPARENT);
            paths.clear();
            if (bitmap != null) {
                drawCanvas.drawBitmap(bitmap, 0, 0, null);
            }
            invalidate();
        }
    }

    public boolean hasDrawing() {
        return !paths.isEmpty();
    }

    public void saveToFile(String filePath) {
        if (canvasBitmap == null) return;
        try {
            File file = new File(filePath);
            FileOutputStream fos = new FileOutputStream(file);
            canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadFromFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;
        
        post(() -> {
            Bitmap loaded = BitmapFactory.decodeFile(filePath);
            if (loaded != null && canvasBitmap != null) {
                drawCanvas.drawBitmap(loaded, 0, 0, null);
                invalidate();
                loaded.recycle();
            }
        });
    }

    private static class DrawPath {
        Path path;
        boolean isEraser;
        int color;

        DrawPath(Path path, boolean isEraser, int color) {
            this.path = path;
            this.isEraser = isEraser;
            this.color = color;
        }
    }
}
