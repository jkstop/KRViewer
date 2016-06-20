package ru.jkstop.krviewer.items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.jkstop.krviewer.App;

/**
 * Сохранение фото пользователей во внутреннее хранилище
 */
public class ImageSaver {

    private Context mContext;
    private String mFileName;

    public static final String DIRECTORY_TEMP = "/Temp";
    public static final String PREFIX_WEBP = ".webp";

    public ImageSaver(Context context) {
        mContext = context;
    }

    public ImageSaver setFileName (String fileName){
        mFileName = fileName + PREFIX_WEBP;
        return this;
    }

    public String save (String photo, String dir){
        FileOutputStream fileOutputStream = null;
        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
        Bitmap bitmap;
        try {
            File file = createFile(dir);
            fileOutputStream = new FileOutputStream(file);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, fileOutputStream);
            return file.getAbsolutePath();

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream!=null) fileOutputStream.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public static File getCustomPath(){
        return new File(App.getAppContext().getFilesDir() + DIRECTORY_TEMP);
    }

    private File createFile(String customDir){
        if (customDir == null){
            return new File(mContext.getFilesDir(), mFileName);
        } else {
            File customPath = new File(mContext.getFilesDir() + customDir);
            customPath.mkdirs();
            return new File(customPath, mFileName);
        }
    }

}
