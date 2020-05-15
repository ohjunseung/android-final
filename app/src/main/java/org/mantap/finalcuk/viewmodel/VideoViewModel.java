package org.mantap.finalcuk.viewmodel;

import android.app.Application;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.mantap.finalcuk.model.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VideoViewModel extends AndroidViewModel {
    private final Application application;
    private final MutableLiveData<List<Video>> videoList = new MutableLiveData<>();

    public VideoViewModel(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void populate() {
        CompletableFuture.runAsync(() -> {
            String[] projection = new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DATE_ADDED,
            };
            String sortOrder = MediaStore.Video.Media.DISPLAY_NAME + " ASC";
            List<Video> tmp = new ArrayList<>();
            try (Cursor cursor = application.getApplicationContext().getContentResolver().query(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, sortOrder
            )) {
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int nameColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int durationColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    int sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        String name = cursor.getString(nameColumn);
                        int duration = cursor.getInt(durationColumn);
                        int size = cursor.getInt(sizeColumn);
                        long date = cursor.getLong(dateColumn) * 1000;

                        Uri contentUri = ContentUris.withAppendedId(
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                        tmp.add(new Video(contentUri, name, duration, size, date));
                    }
                }
                videoList.postValue(tmp);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public LiveData<List<Video>> getVideoList() {
        populate();
        return videoList;
    }
}
