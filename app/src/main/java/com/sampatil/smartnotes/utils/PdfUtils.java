package com.sampatil.smartnotes.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class PdfUtils {

    public static File createPdfFromImages(Context context, List<Uri> imageUris, String outputFileName) throws Exception {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();

        for (int i = 0; i < imageUris.size(); i++) {
            Uri uri = imageUris.get(i);
            InputStream is = context.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();

            if (bitmap != null) {
                // A4 size roughly in points (595x842)
                int pageWidth = 595;
                int pageHeight = 842;
                
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
                PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                
                Canvas canvas = page.getCanvas();
                
                // Scale bitmap to fit page
                float scale = Math.min((float) pageWidth / bitmap.getWidth(), (float) pageHeight / bitmap.getHeight());
                int scaledWidth = Math.round(bitmap.getWidth() * scale);
                int scaledHeight = Math.round(bitmap.getHeight() * scale);
                
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);
                
                // Center image
                float left = (pageWidth - scaledWidth) / 2f;
                float top = (pageHeight - scaledHeight) / 2f;
                
                canvas.drawBitmap(scaledBitmap, left, top, paint);
                pdfDocument.finishPage(page);
                
                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle();
                }
                bitmap.recycle();
            }
        }

        File outputDir = new File(context.getCacheDir(), "generated_pdfs");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        File outputFile = new File(outputDir, outputFileName);
        FileOutputStream fos = new FileOutputStream(outputFile);
        pdfDocument.writeTo(fos);
        
        pdfDocument.close();
        fos.close();
        
        return outputFile;
    }
}
